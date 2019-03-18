package cpuSimulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class FCFSAlgorithm {
	private PriorityQueue<Process> arrival;
	private ArrayList<Process> done;
	private double cw;
	
	public FCFSAlgorithm(RandomSequence test,double alpha,double cw) {
		arrival = test.getSequence();
		this.cw = cw;
		done = new ArrayList<Process>();
		test.printSequenceContent();
	}
	
	public void simulate(){
		System.out.println("time 0ms: Simulator started for FCFS [Q <empty>]");
		if(arrival.size()==0) {
			System.out.println("time <0>ms: Simulator ended for FCFS [Q <empty>]");
			return;
		}
		Queue<Process> rq = new LinkedList<Process>();
		Process p = arrival.poll();
		int count = p.getArrivalTime();
		rq.add(p);
		System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
		p = rq.poll();
		while((!arrival.isEmpty()||!rq.isEmpty())||p.getNumBurst()!=0){
			// Add all the Process with the same arrival time
			while(arrival.size()>0&&count==arrival.peek().getArrivalTime()) { 
				Process newProcess = arrival.poll();
				newProcess.enterQueue(count);
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
			
			int running = p.getRemainingTime()+p.getEnterTime();
			int in =Integer.MAX_VALUE;
			if(arrival.size()>0)
				in =  arrival.peek().getArrivalTime();
			count = Math.min(running, in);
			
			if(count==running) {
				p.complete(count);
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
				// Move onto the next Process in the ready queue because new Process didn't arrive yet.
				if(rq.size()!=0&&running!=in) {
					p = rq.poll();
				}
				else if(arrival.size()!=0) {
					p=arrival.poll();
					count = p.getArrivalTime();
					rq.add(p);
					if(p.getState()!="BLOCKED")
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
					else
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
					p.enterQueue(count);
					while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime()) {
						Process newProcess=arrival.poll();
						rq.add(newProcess);
						if(newProcess.getState()!="BLOCKED")
							System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) arrived;added to ready queue "+printQueueContents(rq));
						else
							System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
					}
					p=rq.poll();
				}
			}
			else { // new Process arrives before the current Process finish
				Process newProcess = arrival.poll();
				rq.add(newProcess);
				if(newProcess.getState()!="BLOCKED")
					System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) arrived;added to ready queue "+newProcess.getState()+printQueueContents(rq));
				else
					System.out.println("time "+count+"ms: Process "+newProcess.getProcessID()+" (tau "+newProcess.getCPUBurstTime()+"ms) completed I/O;added to ready queue "+printQueueContents(rq));
				newProcess.enterQueue(count);
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for FCFS "+printQueueContents(rq));		
	}
	
	private String printQueueContents(Queue<Process> q){
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
		for(Process p : done) {
			entries += p.getNumCPUBurstRecord();
			total += p.getCPUBurstTime()*p.getNumCPUBurstRecord();
		}
		return total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries = p.getNumCPUBurstRecord();
			for(double w:p.waitTime)
				total+=w;
		}
		return total/entries;
	}
	
	private double getAvgTurnaroundTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries = p.getNumCPUBurstRecord();
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
		sb.append("Algorithm FCFS\n");
		
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
		sb.append("-- total number of preemptions: "+numPreempt);
		return sb.toString();	
	}	

}
