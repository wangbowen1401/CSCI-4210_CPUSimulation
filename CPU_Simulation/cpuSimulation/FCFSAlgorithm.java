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
		if(arrival.size()==0)
			return;
		Queue<Process> rq = new LinkedList<Process>();
		Process p = arrival.poll();
		int count = p.getArrivalTime();
		while(!arrival.isEmpty()||!rq.isEmpty()||p.getNumBurst()!=0) {
			// Add all the Process with the same arrival time
			printQueueContents(rq);
			Process newProcess;
			while(arrival.size()>0&&count==arrival.peek().getArrivalTime()) { 
				newProcess = arrival.poll();
				newProcess.enterQueue(count);
				rq.add(newProcess);
			}
			
			// The Process enters CPU
			if(p.getState()!="RUNNING") {
				count+=cw/2;
				p.enterCPU(count);
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
					p.resetEnterTime();
					arrival.add(p);
				}
				// Completed all the cpu and io bursts, added to arrayList for analysis
				else
					done.add(p);
				// Move onto the next Process in the ready queue because new Process didn't arrive yet.
				if(rq.size()!=0&&running!=in) {
					p = rq.poll();
				}
				else if(arrival.size()!=0) {
					p=arrival.poll();
					count = p.getArrivalTime();
				}
			}
			else { // new Process arrives before the current Process finish
				newProcess = arrival.poll();
				//System.out.println("Adding Process "+ newProcess.getProcessID()+" to Ready Queue ");
				newProcess.enterQueue(count);
				rq.add(newProcess);	
			}
		}
		System.out.println("time <"+count+">ms: Simulator ended for <FCFS> [Q empty]");		
	}
	
	private void printQueueContents(Queue<Process> q){
		Iterator<Process> itr = q.iterator();
		System.out.println("Queue Size: " + q.size());
		System.out.print("Queue Contents: ");
		while(itr.hasNext()) {
			Process p = itr.next();
			System.out.print(p.getProcessID());
			if(itr.hasNext())
				System.out.print(",");	
		}
		System.out.println();	
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
