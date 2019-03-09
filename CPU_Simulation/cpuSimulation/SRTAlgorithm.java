package cpuSimulation;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import cpuSimulation.ArrivalComparator;


// Basically wants a queue that orders by remaining time and the order the items
//  are inserted
class SRTComparator implements Comparator<SRTProcess>{
	@Override
	public int compare(SRTProcess p1, SRTProcess p2) {
        return (int)Math.ceil(p1.getTimeGuess()-p2.getTimeGuess());
    }
}

public class SRTAlgorithm{
	private PriorityQueue<SRTProcess> arrival;
	private ArrayList<SRTProcess> done;
	
	public SRTAlgorithm(RandomSequence test,double alpha,double cw) {
		arrival = new PriorityQueue<SRTProcess>(new ArrivalComparator());
		double [] values = test.getSequence();
		char id = 'a';
		for(int i=0;i<test.size();i+=4) {
			SRTProcess p = new SRTProcess(id,Arrays.copyOfRange(values, i, i+4),test.getLambda(),alpha,cw);
			id++;
			arrival.add(p);
		}
		for(Process p:arrival)
			System.out.println(p+"\n");
		done = new ArrayList<SRTProcess>();	
	}
	/* Pseudocode
	 * 1. Add a SRTProcess from arrival queue
	 * 2. Check if any other arrival time is the same (No context switch time)
	 * 3. Context switch the SRTProcess into CPU
	 * 4. Set count to the min of arrival time of next SRTProcess or remainingTime + time 
	 * 		of current progress.
	 * 5. Case 1
	 * 		The new SRTProcess has a shorter burst time guess than remainingTime
	 * 				a. Check for context completion at time of SRTProcess arrival
	 * 					If complete
	 * 						1. Send SRTProcess back for I/O burst if numCPUBurst !=0
	 * 					Else 
	 * 						2. Record enter time = time + cw
	 * 						3. Set state to running
	 * 				b. Context switch the two SRTProcess
	 * 	  Case 2
	 * 		The new SRTProcess has a longer burst time guess than remainingTime,
	 * 			insert the SRTProcess into the ready queue. 
	 * 				a. set enter time, change state, 
	 * 		
	 * 		
	 */
	public void simulate() {

		if(arrival.size()==0)
			return;
		PriorityQueue<SRTProcess> pq = new PriorityQueue<SRTProcess>(new SRTComparator());
		SRTProcess p = arrival.poll();
		double count = p.getArrivalTime();
		while(!arrival.isEmpty()||!pq.isEmpty()||p.getNumBurst()!=0) {
			// Add all the SRTProcess with the same arrival time
			SRTProcess newProcess;
			while(arrival.size()>0&&count==arrival.peek().getArrivalTime()) { 
				newProcess = arrival.poll();
				newProcess.enterQueue(count);
				
			}
			
			// The SRTProcess enters CPU
			if(p.getState()!="RUNNING") {
				count+=p.cw/2;
				p.enterCPU(count);
			}
			
			// Check the next SRTProcess arrival time vs remaining time of current SRTProcess
			double running = p.getRemainingTime()+p.getEnterTime();
			double in =Integer.MAX_VALUE;
			if(arrival.size()>0)
				in =  arrival.peek().getArrivalTime();
			count = Math.min(running, in);
			
			// The current SRTProcess will finish before the new SRTProcess, just 
			// finish and deal with context switch
			if(count==running) {
				//System.out.println("Completing CPU SRTProcess");
				count+=p.cw/2; // Add context switch to move the SRTProcess out
				p.complete(count);
				// Still more cpu bursts left
				if(p.getState()!="COMPLETE") {
					p.resetEnterTime();
					arrival.add(p);
				}
				// Completed all the cpu and io bursts, added to arrayList for analysis
				else
					done.add(p);
				// Move onto the next SRTProcess in the ready queue because new SRTProcess didn't arrive yet.
				if(pq.size()!=0&&running!=in) {
					p = pq.poll();
				}
				else if(arrival.size()!=0) {
					p=arrival.poll();
					count = p.getArrivalTime();
				}
			}
			else { // new SRTProcess arrives before the current SRTProcess finish
				double remain = p.getRemainingTime()-(count-p.getEnterTime()); 
				// A preemption is needed
				if(arrival.peek().getTimeGuess()<remain) {
					//System.out.println("Preempting CPU SRTProcess " + "remainTime: "+remain);
					count+=p.cw/2;
					p.enterQueue(count);
					pq.add(p);
					p=arrival.poll();
				}
				// The new SRTProcess will not cause preemption, so just add it to the queue
				else {
					newProcess = arrival.poll();
					//System.out.println("Adding SRTProcess "+ newSRTProcess.getSRTProcessID()+" to Ready Queue ");
					newProcess.enterQueue(count);
					pq.add(newProcess);
				}
			}
		}
		System.out.println("time <"+count+">ms: Simulator ended for <SRT> [Q empty]");
	}
	
	private double getAvgCPUBurst() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries = p.numCPUBurstRecord;
			total += p.getCPUBurstTime()*p.numCPUBurstRecord;;
		}
		return total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries = p.numCPUBurstRecord;
			for(double w:p.waitTime)
				total+=w;
		}
		return total/entries;
	}
	
	private double getAvgTurnaroundTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries = p.numCPUBurstRecord;
			for(double w:p.turnaroundTime)
				total+=w;
		}
		return total/entries;
	}
	
	private int getTotalCW() {
		int total = 0;
		for(Process p : done) {
			total+=p.numContextSwitch;
		}
		return total;
	}
	
	private int getTotalPreempt() {
		int total = 0;
		for(Process p : done) {
			total+=p.numPreempt;
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
