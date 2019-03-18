package cpuSimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

class SRTComparator implements Comparator<Process>{
	public int compare(Process p1, Process p2) {
		if(p1.getTimeGuess()!=p2.getTimeGuess())
			return (int)Math.ceil(p1.getTimeGuess()-p2.getTimeGuess());
		else
			return p1.getProcessID()<p2.getProcessID()?-1:1;
    }
}

public class SRTAlgorithm{
	private PriorityQueue<Process> arrival;
	private PriorityQueue<Process> arrivalRecord;
	private ArrayList<Process> done;
	private PriorityQueue<Process> rq;
	private double cw;
	
	public SRTAlgorithm(RandomSequence arrival,double cw) {
		arrivalRecord=arrival.getSequence();
		this.arrival = arrival.getSequence();
		this.cw=cw;
		done = new ArrayList<Process>();
		arrival.printSequenceContent();
		rq = new PriorityQueue<Process>(new SRTComparator());
	}

	public void simulate() {
		System.out.println("time 0ms: Simulator started for SRT "+printQueueContents(rq));
		// Making sure n != 0
		if(arrival.size()==0) {
			System.out.println("time <0>ms: Simulator ended for SRT "+printQueueContents(rq));
			return;
		}
	
		Process p = arrival.poll();
		int count = p.getArrivalTime();
		rq.add(p);
		System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
		p = rq.poll();
		while((!arrival.isEmpty()||!rq.isEmpty())||p.getNumBurst()!=0){
			// Add all the SRTProcess with the same arrival time
			while(arrival.size()>0&&count>=arrival.peek().getArrivalTime()) {
				Process newProcess;
				newProcess = arrival.poll();
				rq.add(newProcess);
				if(newProcess.getState()!="BLOCKED")
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
				else
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
				newProcess.enterQueue(newProcess.getArrivalTime());
			}
			
			// The Process enters CPU
			if(p.getState()!="RUNNING") {
				count+=cw/2;
				p.enterCPU(count);
				System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.remainingTime+"ms burst "+printQueueContents(rq));
			}
			
			// Check the next process arrival time vs remaining time of current Process
			int running = p.getRemainingTime()+p.getEnterTime();
			int in =Integer.MAX_VALUE;
			if(arrival.size()>0)
				in =  arrival.peek().getArrivalTime();
			count = Math.min(running, in);
			
			// The current process will finish before the new process, just 
			// finish and deal with context switch
			if(count==running) {
				count+=cw/2; // Add context switch to move the Process out
				p.complete(count);
				// In case a newProcess arrives during this context switch period
				while(!arrival.isEmpty()&&arrival.peek().getArrivalTime()<count) {
					addNewProcess();
				}
				// Still more cpu bursts left
				if(p.getState()!="COMPLETE") {
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU Burst; "+p.getNumBurst()+" bursts to go "+printQueueContents(rq));
					System.out.println("time "+count+"ms: Recalculated tau = "+p.getTimeGuess()+"ms for process "+p.getProcessID()+" "+printQueueContents(rq));
					p.resetEnterTime();
					arrival.add(p);
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" switching out of CPU; will block on I/O until time "+p.getArrivalTime()+"ms "+printQueueContents(rq));
				}
				// Completed all the cpu and io bursts, added to arrayList for analysis
				else {
					done.add(p);
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" terminated.");
				}
				// Move onto the next SRTProcess in the ready queue because new SRTProcess didn't arrive yet.
				if(rq.size()!=0) {
					p = rq.poll();
				}
				// If the next process is from the arrival queue
				else if(arrival.size()!=0) {
					// Get the process and add it to the ready queue
					p=arrival.poll();
					count = p.getArrivalTime();
					rq.add(p);
					if(p.getState()!="BLOCKED")
						System.out.println("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
					else
						System.out.println("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
					p.enterQueue(count);
					while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime()) {
						addNewProcess();
					}
					p=rq.poll();
				}
			}
			else { // new SRTProcess arrives before the current SRTProcess finish
				double remain = p.getRemainingTime()-(count-p.getEnterTime()); 
				// A preemption is needed
				if(arrival.peek().getTimeGuess()<remain) {
					count+=cw/2;
					while(arrival.peek().getArrivalTime()<count)
						addNewProcess();
					p.enterQueue(count);
					rq.add(p);
					System.out.println("time "+count+"ms: Process "+arrival.peek().getProcessID()+" (tau "+arrival.peek().getCPUBurstTime()+"ms) completed I/O and will preempt "+p.getProcessID()+" "+printQueueContents(rq));
					p=arrival.poll();
				}
				// The new SRTProcess will not cause preemption, so just add it to the queue
				else {
					Process newProcess = arrival.poll();
					rq.add(newProcess);
					if(newProcess.getState()!="BLOCKED")
						System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) arrived;added to ready queue "+newProcess.getState()+printQueueContents(rq));
					else
						System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
					newProcess.enterQueue(count);
				}
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for <SRT> "+printQueueContents(rq));
	}
	
	private void addNewProcess() {
		Process newProcess = arrival.poll();
		this.rq.add(newProcess);
		if(newProcess.getState()!="BLOCKED")
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
		else
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
		newProcess.enterQueue(newProcess.getArrivalTime());
	}
	
	
	private String printQueueContents(PriorityQueue<Process> q){
		Iterator<Process> itr = q.iterator();
		StringBuilder sb = new StringBuilder();
		sb.append("[Q");
		if(q.isEmpty()) {
			sb.append(" <empty>]");
			return sb.toString();
		}
		while(itr.hasNext()) {
			Process p = itr.next();
			sb.append(" "+p.getProcessID());
		}
		sb.append("]");
		return sb.toString();
	}
	
	private double getAvgCPUBurst() {
		double total = 0;
		int entries=0;
		for(Process p : arrivalRecord) {
			entries += p.getNumCPUBurstRecord();
			for(Integer time:p.getCPUBurst()) {
				total += time;
			}
		}
		if(entries==0)
			return 0;
		return total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries += p.getNumCPUBurstRecord();
			for(double w:p.waitTime)
				total+=w;
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
		double avgCPUBurst = this.getAvgCPUBurst();
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
