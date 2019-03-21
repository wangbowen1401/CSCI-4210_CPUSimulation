package cpuSimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

class SRTComparator implements Comparator<Process>{
	public int compare(Process p, Process p2) {
		if(p.getTimeGuess()!=p2.getTimeGuess())
			return (int)Math.ceil(p.getTimeGuess()-p2.getTimeGuess());
		else
			return p.getProcessID()<p2.getProcessID()?-1:1;
    }
}

public class SRTAlgorithm{
	private PriorityQueue<Process> arrival;
	private double avgCPUBurst;
	private ArrayList<Process> done;
	private PriorityQueue<Process> rq;
	private boolean full = false;
	private int cw;
	
	public SRTAlgorithm(RandomSequence arrival,int cw) {
		this.arrival = arrival.getSequence();
		this.cw=cw;
		done = new ArrayList<Process>();
		arrival.printSequenceContent();
		rq = new PriorityQueue<Process>(new SRTComparator());
		avgCPUBurst = this.getAvgCPUBurst();
	}

	public void simulate() {
		System.out.println("time 0ms: Simulator started for SRT "+printQueueContents(rq));
		// Making sure n != 0
		if(arrival.size()==0) {
			System.out.println("time 0ms: Simulator ended for SRT "+printQueueContents(rq));
			return;
		}
		boolean cwEntry = false;
		Process p = arrival.poll();
		int count = p.getArrivalTime();
		rq.add(p);
		System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) arrived;added to ready queue "+printQueueContents(rq));
		p = rq.poll();
		while((!arrival.isEmpty()||!rq.isEmpty())||p.getNumBurst()!=0){
			// Add all the SRTProcess with the same arrival time
			while(arrival.size()>0&&count==arrival.peek().getArrivalTime()) {
				Process newProcess;
				newProcess = arrival.poll();
				rq.add(newProcess);
				if(newProcess.getState()!="BLOCKED"&&(count<= 999 || full == true))
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) arrived;added to ready queue "+printQueueContents(rq));
				else if((count<= 999 || full == true))
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
				newProcess.enterQueue(newProcess.getArrivalTime());
			}
			
			// The Process enters CPU
			if(p.getState()!="RUNNING") {
				p.enterCPU(count); 
				count+=cw/2;
				// Make sure all the process that arrive during contextswitch gets added
				while(arrival.size()>0&&count>arrival.peek().getArrivalTime()) {
					cwEntry=true;
					addNewProcess();
				}
				if((count<= 999 || full == true))
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.getRemainingTime()+"ms burst "+printQueueContents(rq));
			}
			
			// Check the next process arrival time vs remaining time of current Process
			int running = p.getRemainingTime()+p.getEnterTime();
			int in =Integer.MAX_VALUE;
			int queue = Integer.MAX_VALUE;
			if(arrival.size()>0) {
				in =  arrival.peek().getArrivalTime();
				System.out.println("time "+count+"ms: in = "+in);
			}
			else if(rq.size()>0&&cwEntry) {
				queue = count+rq.peek().getCPUBurstTime();
				cwEntry=false;
			}
			
			if(Math.min(running, in)<queue)
				count = Math.min(running, in);
			else
				count=queue;
			
			// The current process will finish before the new process, just 
			// finish and deal with context switch
			if(count==running) {
				p.complete(count);
				// Still more cpu bursts left
				if(p.getState()!="COMPLETE") {
					if((count<= 999 || full == true)) {
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU Burst; "+p.getNumBurst()+" bursts to go "+printQueueContents(rq));
						System.out.println("time "+count+"ms: Recalculated tau = "+p.getTimeGuess()+"ms for process "+p.getProcessID()+" "+printQueueContents(rq));
					}
					p.resetEnterTime();
					arrival.add(p);
					if((count<= 999 || full == true))
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" switching out of CPU; will block on I/O until time "+p.getArrivalTime()+"ms "+printQueueContents(rq));
				}
				// Completed all the cpu and io bursts, added to arrayList for analysis
				else {
					done.add(p);
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" terminated "+printQueueContents(rq));
				}
				count+=cw/2; // Add context switch to move the process out, does not need to consider preemption
				// In case a newProcess arrives during this context switch period
				while(!arrival.isEmpty()&&arrival.peek().getArrivalTime()<=count) {
					addNewProcess();
				}
				if(rq.size()!=0) {
					p = rq.poll();
					/*while(arrival.size()>0&&count>=arrival.peek().getArrivalTime()) {
						cwEntry=true;
						addNewProcess();
					}
					p.enterCPU(count);
					if(count!=-1)
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.remainingTime+"ms burst "+printQueueContents(rq));*/
				}
				// If the next process is from the arrival queue
				else if(arrival.size()!=0) {
					// Get the process and add it to the ready queue
					p=arrival.poll();
					count = p.getArrivalTime();
					rq.add(p);
					
					// Print the process arrival statements
					if(p.getState()!="BLOCKED"&& (count<= 999 || full == true))
						System.out.println("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) arrived;added to ready queue "+printQueueContents(rq));
					else if((count<= 999 || full == true))
						System.out.println("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
					p.enterQueue(count);
					// Take the statement out
					p=rq.poll();
					// Add any new process with same arrival time
					while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime()) 
						addNewProcess();
				}
			}
			else if(count==in){ 
				// new process arrives before the current process finish
				double remain = p.getRemainingTime()-(count-p.getEnterTime()); 
				// A preemption is needed
				if(!arrival.isEmpty()&&arrival.peek().getTimeGuess()<remain) {
					Process newProcess = arrival.poll();
					if(p.getState()!="BLOCKED"&&newProcess.getArrivalTime()!=-1)
						System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) will preempt "+p.getProcessID()+" "+printQueueContents(rq));
					else if(newProcess.getArrivalTime()!=-1)
						System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) completed I/O and will preempt "+p.getProcessID()+" "+printQueueContents(rq));	
					while(!arrival.isEmpty()&&newProcess.getArrivalTime()==count)
						addNewProcess();
					p.enterQueue(count);
					rq.add(p);
					p = newProcess;
					count+=cw/2;
				}
				// The new SRTProcess will not cause preemption, so just add it to the queue
				else { 
					while(!arrival.isEmpty()&&arrival.peek().getArrivalTime()==count)
						addNewProcess();
				}
			}
			// If a process that arrived during contextswitch completes burst before remainder of the process of a new process arrival
			else {
				Process newProcess = rq.peek();
				if((count<= 999 || full == true))
					System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) will preempt "+p.getProcessID()+" "+printQueueContents(rq));
				rq.poll();
				
				// Deal with the contextswitch in the if condition, or else it will count the entire cw
				count+=cw/2;
				p.enterQueue(count);
				rq.add(p);
				p=newProcess;
				p.enterCPU(count);
				if((count<= 999 || full == true))
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.remainingTime+"ms burst "+printQueueContents(rq));
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for SRT "+printQueueContents(rq));
	}
	
	private void addNewProcess() {
		Process newProcess = arrival.poll();
		this.rq.add(newProcess);
		if(newProcess.getState()!="BLOCKED"&&newProcess.getArrivalTime()!=-1)
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) arrived;added to ready queue "+printQueueContents(rq));
		else if(newProcess.getArrivalTime()!=-1)
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
		newProcess.enterQueue(newProcess.getArrivalTime());
	}
	
	
	private String printQueueContents(PriorityQueue<Process> q) {
		StringBuilder sb = new StringBuilder();
		PriorityQueue<Process> cp = new PriorityQueue<Process>(q);
		if(cp.isEmpty()) {
			sb.append("[Q <empty>]");
			return sb.toString();
		}
		sb.append("[Q");
		while(!cp.isEmpty()){
			sb.append(" " + cp.poll().getProcessID());			
		}
		sb.append("]");
		return sb.toString();
	}
	
	private double getAvgCPUBurst() {
		long total = 0;
		int entries=0;
		for(Process p : arrival) {
			entries += p.getNumCPUBurstRecord();
			for(Integer time:p.getCPUBurst()){
				total+=time;
			}
		}
		if(entries==0)
			return 0;
		return (double)total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries += p.getNumCPUBurstRecord();
			for(int w:p.waitTime) {
				total+=w;
			}
		}
		if(entries==0)
			return 0;
		return total/entries;
	}
	
	private double getAvgTurnaroundTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries += p.getNumCPUBurstRecord();
			for(double w:p.getTurnaroundTime())
				total+=w;
		}
		if(entries==0)
			return 0;
		return total/entries;
	}
	
	private int getTotalCW() {
		int total = 0;
		for(Process p : done) {
			total+=p.getNumContextSwitch();
		}
		return total;
	}
	
	private int getTotalPreempt() {
		int total = 0;
		for(Process p : done) {
			total+=p.getNumPreempt();
		}
		return total;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		// Actual content
		sb.append("Algorithm SRT\n");
		
		// Values that need to be calculated
		double avgCPUBurst = this.avgCPUBurst;
		double avgWaitTime = this.getAvgWaitTime();
		double avgTurnaroundTime = this.getAvgTurnaroundTime();
		int numCW = this.getTotalCW();
		int numPreempt = this.getTotalPreempt();
		
		// Prints
		sb.append("-- average CPU burst time: "+ String.format("%.3f",avgCPUBurst));
		sb.append("\n");
		sb.append("-- average wait time: "+ String.format("%.3f",avgWaitTime));
		sb.append("\n");
		sb.append("-- average turnaround time: "+ String.format("%.3f",avgTurnaroundTime));
		sb.append("\n");
		sb.append("-- total number of context switches: "+ numCW);
		sb.append("\n");
		sb.append("-- total number of preemptions: "+ numPreempt);
		return sb.toString();	
	}	
}
