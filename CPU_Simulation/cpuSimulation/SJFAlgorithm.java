package cpuSimulation;
import java.util.ArrayList;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;



// Basically wants a queue that orders by remaining time and the order the items
//  are inserted
class SJFComparator implements Comparator<Process>{
	@Override
	public int compare(Process p1, Process p2) {
        return (int)Math.ceil(p1.getTimeGuess()-p2.getTimeGuess());
    }
}


public class SJFAlgorithm {
	PriorityQueue<Process> Q;
	private PriorityQueue<Process> arrivalQueue;
	private ArrayList<Process> completedProcesses;
	private double cw;
	private double avgCPUBurst;
	
	public SJFAlgorithm(RandomSequence test,double cw) {
		arrivalQueue = test.getSequence();
		this.cw=cw;
		completedProcesses = new ArrayList<Process>();
		avgCPUBurst = this.getAvgCPUBurst();
	}
	
	
	
	
	public void simulate() {
		PriorityQueue<Process> copy = new PriorityQueue<>(arrivalQueue);
		PriorityQueue<Process> print = new PriorityQueue<>(new AlphaComparator());
		
		
		while(!copy.isEmpty()) {
			print.add(copy.poll());
		}
		Iterator<Process> it = print.iterator();
		while(it.hasNext()) {
			Process p = it.next();
			System.out.println("Process " + p.getProcessID() + "[NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU bursts");
		}
		
		Q = new PriorityQueue<Process>(new SJFComparator());
		System.out.print("time 0ms: Simulator started for SJF ");
		printQueueContents(Q);


		if(arrivalQueue.isEmpty()) {
			System.out.println("time "+0+"ms: Simulator ended for <SJF> [Q empty]");

			return;
		}
			
		
		
		Process currentProcess = arrivalQueue.peek();
		
		//System.out.println("Process " + currentProcess.getProcessID() + "[NEW] (arrival time " + currentProcess.getArrivalTime() + " ms) " + currentProcess.getNumBurst() + " CPU bursts");
		Q.add(arrivalQueue.poll());
		
		
		
		int count = currentProcess.getArrivalTime();
		System.out.print("time " + count + "ms: " + "Process " + currentProcess.getProcessID() + "(tau " + currentProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
		//Q.add(arrivalQueue.poll());
		printQueueContents(Q);
		
		
		
		while((!arrivalQueue.isEmpty()|!Q.isEmpty())||currentProcess.getNumBurst()!=0) {
			//printQueueContents(Q);
			Process newProcess;
			while(arrivalQueue.size()>0&&count==arrivalQueue.peek().getArrivalTime()) { 
				newProcess = arrivalQueue.poll();
				newProcess.enterQueue(count);
				System.out.print("time " + count + "ms: " + "Process " + newProcess.getProcessID() + "(tau " + newProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
				printQueueContents(Q);

			}
			
			if(currentProcess.getState()!="RUNNING") {
				Q.poll();
				currentProcess.enterCPU(count);
				count+=cw/2;
				System.out.print("time " + count +"ms: Process " + currentProcess.getProcessID()+ " started using the CPU for " + currentProcess.getCPUBurstTime() + "ms burst ");
				printQueueContents(Q);
			}
			
			int running = currentProcess.getRemainingTime()+currentProcess.getEnterTime();
			int in =Integer.MAX_VALUE;
			if(arrivalQueue.size()>0)
				in =  arrivalQueue.peek().getArrivalTime();
			//printQueueContents(arrivalQueue);
			count = Math.min(running, in);

			if(count==running) {
				count+=cw/2;
				while( arrivalQueue.size() > 0 && arrivalQueue.peek().getArrivalTime() < count ) {
					Process p = arrivalQueue.peek();
					Q.add(arrivalQueue.poll());
					if(p.getState() == "BLOCKED") {
						System.out.print("time " + count + "ms:" + " Process " + p.getProcessID() + " (tau " + p.getTimeGuess() + "ms) completed I/O; added to ready queue " );
						printQueueContents(Q);
					}else {
						System.out.print("time " + count + "ms: Process " + p.getProcessID() + " (tau " + p.getTimeGuess() + "ms) arrived; added to ready queue ");
						printQueueContents(Q);
					}				
					p.enterQueue(p.getArrivalTime());
				}
				
				
				currentProcess.complete(count);
				if(currentProcess.getState()!="COMPLETE") {
					System.out.print("time "+ (int)(count-(cw/2)) + "ms: Process " + currentProcess.getProcessID() + " completed a CPU burst; " + currentProcess.getNumBurst() + " bursts to go " );
					printQueueContents(Q);
					System.out.print("time " + (int)(count - (cw/2)) + "ms: Recalculated tau = " + currentProcess.getTimeGuess() + "ms for Process " + currentProcess.getProcessID() + " ");
					printQueueContents(Q);
					System.out.print("time "+ (int)(count-(cw/2)) + "ms: Process " + currentProcess.getProcessID() + " switching out of CPU; will block on I/O until time "+ currentProcess.getArrivalTime() + " ");
					printQueueContents(Q);

					
					
					currentProcess.resetEnterTime();
					
					arrivalQueue.add(currentProcess);
					//System.out.println(currentProcess.getArrivalTime());
					
				}
				else {
					completedProcesses.add(currentProcess);
					System.out.print("time " + count + "ms: Process " + currentProcess.getProcessID() + " terminated ");
					printQueueContents(Q);

				}
				if(!Q.isEmpty()&&running!=in) {
					currentProcess = Q.poll();
				}
				else if(!arrivalQueue.isEmpty()) {
					currentProcess=arrivalQueue.peek();
					count = currentProcess.getArrivalTime();
					Q.add(arrivalQueue.poll());
					if(currentProcess.getState() == "BLOCKED") {
						System.out.print("time " + count + "ms:" + " Process " + currentProcess.getProcessID() + " (tau " + currentProcess.getTimeGuess() + "ms) completed I/O; added to ready queue " );
						printQueueContents(Q);
					}else {
						System.out.print("time " + count + "ms: Process " + currentProcess.getProcessID() + " (tau " + currentProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
						printQueueContents(Q);
					}
					
					currentProcess = Q.poll();
				}
			}
			else {
				newProcess = arrivalQueue.poll();
				newProcess.enterQueue(count);
				Q.add(newProcess);

				if(newProcess.getState() == "BLOCKED") {
					System.out.print("time " + count + "ms:" + "Process " + newProcess.getProcessID() + "(tau " + newProcess.getTimeGuess() + "ms) completed I/O; added to ready queue " );
					printQueueContents(Q);
				}else {
					System.out.print("time " + count + "ms: Process " + newProcess.getProcessID() + "(tau " + newProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
					printQueueContents(Q);
				}
				
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for SJF [Q empty]");
	}
	
	
	private void printQueueContents(PriorityQueue<Process> q) {
		PriorityQueue<Process> cp = new PriorityQueue<Process>(q);
		//System.out.println("Queue Size: " + cp.size());
		if(cp.isEmpty()) {
			System.out.println("[Q <empty>]");
			return;
		}
		System.out.print("[Q ");
		while(!cp.isEmpty()){
			System.out.print(" " + cp.poll().getProcessID());			
		}
		System.out.println("]");
	
	}
	
	
	private double getAvgCPUBurst() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : arrivalQueue) {
			entries += currentProcess.getNumCPUBurstRecord();
			for(Integer time : currentProcess.getCPUBurst()) {
				total+=time;
			}
		}
		return total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : completedProcesses) {
			entries = currentProcess.numCPUBurstRecord;
			for(double w:currentProcess.waitTime)
				total+=w;
		}
		return total/entries;
	}
	
	private double getAvgTurnaroundTime() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : completedProcesses) {
			entries = currentProcess.numCPUBurstRecord;
			for(double w:currentProcess.turnaroundTime)
				total+=w;
		}
		return total/entries;
	}
	
	private int getTotalCW() {
		int total = 0;
		for(Process currentProcess : completedProcesses) {
			total+=currentProcess.numContextSwitch;
		}
		return total;
	}
	
	private int getTotalPreempt() {
		int total = 0;
		for(Process currentProcess : completedProcesses) {
			total+=currentProcess.numPreempt;
		}
		return total;
	}
	
	
	public String toString(){

		StringBuilder sb = new StringBuilder();
		
		// Actual content
		sb.append("Algorithm SJF\n");
		
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

