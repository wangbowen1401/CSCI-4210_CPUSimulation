package cpuSimulation;
import java.util.ArrayList;
import java.util.Comparator;
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
	
	public SJFAlgorithm(RandomSequence test,double alpha,double cw) {
		arrivalQueue = test.getSequence();
		this.cw=cw;
		completedProcesses = new ArrayList<Process>();	
	}
	
	
	
	
	public void simulate() {
		
		PriorityQueue<Process> print = new PriorityQueue<>(arrivalQueue);
		
		for(Process p : print) {
			System.out.println("Process " + p.getProcessID() + "[NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU bursts");
		}

		if(arrivalQueue.isEmpty()) {
			return;
			
		}
			
		Q = new PriorityQueue<Process>(new SJFComparator());
		Process currentProcess = arrivalQueue.poll();
		double count = currentProcess.getArrivalTime();
		while(!arrivalQueue.isEmpty()||!Q.isEmpty()||currentProcess.getNumBurst()!=0) {
			printQueueContents(Q);
			Process newProcess;
			while(arrivalQueue.size()>0&&count==arrivalQueue.peek().getArrivalTime()) { 
				newProcess = arrivalQueue.poll();
				newProcess.enterQueue(count);				
			}
			
			if(currentProcess.getState()!="RUNNING") {
				count+=cw/2;
				currentProcess.enterCPU(count);
			}
			
			double running = currentProcess.getRemainingTime()+currentProcess.getEnterTime();
			double in =Integer.MAX_VALUE;
			if(arrivalQueue.size()>0)
				in =  arrivalQueue.peek().getArrivalTime();
			count = Math.min(running, in);

			if(count==running) {
				count+=cw/2;
				currentProcess.complete(count);
				if(currentProcess.getState()!="COMPLETE") {
					currentProcess.resetEnterTime();
					arrivalQueue.add(currentProcess);
				}
				else
					completedProcesses.add(currentProcess);
				if(!Q.isEmpty()&&running!=in) {
					currentProcess = Q.poll();
				}
				else if(!arrivalQueue.isEmpty()) {
					currentProcess=arrivalQueue.poll();
					count = currentProcess.getArrivalTime();
				}
			}
			else {
				newProcess = arrivalQueue.poll();
				newProcess.enterQueue(count);
				Q.add(newProcess);
				
			}
		}
		System.out.println("time <"+count+">ms: Simulator ended for <SJF> [Q empty]");
	}
	
	
	private void printQueueContents(PriorityQueue<Process> q) {
		PriorityQueue<Process> cp = new PriorityQueue<Process>(q);
		//System.out.println("Queue Size: " + cp.size());
		if(cp.isEmpty()) {
			System.out.println("[Q <empty>]");
			return;
		}
		System.out.print("[Q ");
		for(int i = 0; i < cp.size(); i++) {
			System.out.print(" " + cp.poll().getProcessID());			
		}
		System.out.println("]");
	
	}
	
	
	private double getAvgCPUBurst() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : completedProcesses) {
			entries = currentProcess.numCPUBurstRecord;
			total += currentProcess.getCPUBurstTime()*currentProcess.numCPUBurstRecord;;
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

