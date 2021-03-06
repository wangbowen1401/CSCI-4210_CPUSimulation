

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
		boolean cwEntry = false;
		System.out.println("time 0ms: Simulator started for SRT "+printQueueContents(rq));
		// Making sure n != 0
		if(arrival.size()==0) {
			System.out.println("time 0ms: Simulator ended for SRT "+printQueueContents(rq));
			return;
		}
		Process p = arrival.poll();
		int count = p.getArrivalTime();
		rq.add(p);
		System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) arrived; added to ready queue "+printQueueContents(rq));
		p = rq.poll();
		while((!arrival.isEmpty()||!rq.isEmpty())||p.getNumBurst()!=0){
			// Add all the SRTProcess with the same arrival time
			while(arrival.size()>0&&count==arrival.peek().getArrivalTime()) {
				Process newProcess;
				newProcess = arrival.poll();
				rq.add(newProcess);
				if(newProcess.getState()!="BLOCKED"&&(count<= 999 || full))
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) arrived; added to ready queue "+printQueueContents(rq));
				else if((count<= 999 || full ))
					System.out.println("4time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) completed I/O; added to ready queue "+printQueueContents(rq));
				newProcess.enterQueue(newProcess.getArrivalTime());
			}
			
			// The Process enters CPU
	
			if(p.getState()!="RUNNING") {
				p.enterCPU(count); 
				count+=cw/2;
				// Make sure all the process that arrive during contextswitch gets added
				while(arrival.size()>0&&count>arrival.peek().getArrivalTime()) {
					cwEntry = true;
					addNewProcess();
				}
				if((count<= 999 || full == true))
					if(p.getRemainingTime() != p.getCPUBurstTime()) {
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU with "+p.getRemainingTime()+"ms remaining "+printQueueContents(rq));

					}else {
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.getRemainingTime()+"ms burst "+printQueueContents(rq));
					}
			}
			
			// Check the next process arrival time vs remaining time of current Process
			int running = p.getRemainingTime()+p.getEnterTime();
			int in =Integer.MAX_VALUE;
			if(arrival.size()>0) {
				in =  arrival.peek().getArrivalTime();
			}
			if(!cwEntry)
				count = Math.min(running, in);
			
			// The current process will finish before the new process, just 
			// finish and deal with context switch
			if(count==running) {
				p.complete(count);
				// Still more cpu bursts left
				if(p.getState()!="COMPLETE") {
					if((count<= 999 || full == true)) {
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU burst; "+p.getNumBurst()+" bursts to go "+printQueueContents(rq));
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
				}
				// If the next process is from the arrival queue
				else if(arrival.size()!=0) {
					// Get the process and add it to the ready queue
					p=arrival.poll();
					if(count<p.getArrivalTime())
						count = p.getArrivalTime();
					rq.add(p);
					
					// Print the process arrival statements
					if(p.getState()!="BLOCKED"&& (full == true))
						System.out.println("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) arrived; added to ready queue "+printQueueContents(rq));
					else if((count<= 999 || full == true))
						System.out.println("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess()+"ms) completed I/O; added to ready queue "+printQueueContents(rq));
					p.enterQueue(p.getArrivalTime());
					// Take the statement out
					p=rq.poll();
					// Add any new process with same arrival time
					while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime()) { 
						addNewProcess();
					}
				}
			}
			else if(count==in){ 
				// new process arrives before the current process finish
				double remain = p.getTimeGuess()-(count-p.getEnterTime()); 
				// A preemption is needed
				if(!arrival.isEmpty()&&arrival.peek().getTimeGuess()<remain) {
					Process newProcess = arrival.poll();
					rq.add(newProcess);
					if(newProcess.getState()=="BLOCKED"&&(newProcess.getArrivalTime()<= 999||full))
						System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) completed I/O and will preempt "+p.getProcessID()+" "+printQueueContents(rq));	
					else if((newProcess.getArrivalTime()<= 999||full == true))
						System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) will preempt "+p.getProcessID()+" "+printQueueContents(rq));
					newProcess.enterQueue(count);
					p.enterQueue(count);
					rq.add(p);
					p = rq.poll();
					count+=cw/2;
					while(!arrival.isEmpty()&&arrival.peek().getArrivalTime()<=	count) {
						addNewProcess();
						// Preempting a premption
						while(rq.peek().getTimeGuess()<p.getTimeGuess()) {
							p.enterQueue(count-cw/2);
							rq.add(p);
							p = rq.poll();
						}
					}
						
				}
				// The new process will not cause preemption, so just add it to the queue
				else { 
					while(!arrival.isEmpty()&&arrival.peek().getArrivalTime()==count) {
						addNewProcess();
					}
				}
			}
			// If a process that arrived during contextswitch completes burst before remainder of the process of a new process arrival
			else{
				double remain = p.getTimeGuess()-(count-p.getEnterTime()); 
				cwEntry = false;
				Process newProcess = rq.peek();
				if(newProcess.getTimeGuess()<remain) {
					if(newProcess.getState()=="BLOCKED"&&(full == true || count <= 999)) {
						System.out.println("5time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) completed I/O and will preempt "+p.getProcessID()+" "+printQueueContents(rq));
					}
					else if(full == true || count <= 999)
						System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) will preempt "+p.getProcessID()+" "+printQueueContents(rq));
					rq.poll();
					
					// Deal with the contextswitch in the if condition, or else it will count the entire cw
					p.enterQueue(count);
					count+=cw/2;
					rq.add(p);
					p=newProcess;
					p.enterCPU(count);
					count+=cw/2;
					if((count<= 999 || full == true))
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.remainingTime+"ms burst "+printQueueContents(rq));
				}
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for SRT [Q <empty>]");
	}
	
	private void addNewProcess() {
		Process newProcess = arrival.poll();
		this.rq.add(newProcess);
		if(newProcess.getState()!="BLOCKED"&&(newProcess.getArrivalTime()<=999||full))
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) arrived; added to ready queue "+printQueueContents(rq));
		else if(newProcess.getArrivalTime()<=999||full)
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getTimeGuess()+"ms) completed I/O; added to ready queue "+printQueueContents(rq));
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
		
		NumberFormat formatter = new DecimalFormat("#0.000"); 
		// Prints
		sb.append("-- average CPU burst time: "+ formatter.format(avgCPUBurst)+" ms");
		sb.append("\n");
		sb.append("-- average wait time: "+ formatter.format(avgWaitTime)+" ms");
		sb.append("\n");
		sb.append("-- average turnaround time: "+ formatter.format(avgTurnaroundTime)+" ms");
		sb.append("\n");
		sb.append("-- total number of context switches: "+ numCW);
		sb.append("\n");
		sb.append("-- total number of preemptions: "+ numPreempt);
		return sb.toString();	
	}	
}
