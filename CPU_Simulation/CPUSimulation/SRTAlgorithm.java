package CPUSimulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.lang.Math;


// Basically wants a queue that orders by remaining time and the order the items
//  are inserted
class SRTComparator implements Comparator<Process>{
	@Override
	public int compare(Process p1, Process p2) {
        return (int)Math.ceil(p1.getRemainingTime()-p2.getRemainingTime());
    }
}

public class SRTAlgorithm{
	private PriorityQueue<Process> arrival;
	private ArrayList<Process> done;
	
	public SRTAlgorithm(RandomSequence sequence,double alpha,double cw) {
		arrival = new PriorityQueue<Process>(new ArrivalComparator());
		double [] values = sequence.getSequence();
		char id = 'a';
		for(int i=0;i<sequence.size();i+=4) {
			Process p = new Process(id,Arrays.copyOfRange(values, i, i+4),sequence.getLambda(),alpha,cw);
			id++;
			arrival.add(p);
		}
		done = new ArrayList<Process>();	
	}
	/* Pseudocode
	 * 1. Add a process from arrival queue
	 * 2. Check if any other arrival time is the same (No context switch time)
	 * 3. Context switch the process into CPU
	 * 4. Set count to the min of arrival time of next process or remainingTime + time 
	 * 		of current progress.
	 * 5. Case 1
	 * 		The new process has a shorter burst time guess than remainingTime
	 * 				a. Check for context completion at time of process arrival
	 * 					If complete
	 * 						1. Send process back for I/O burst if numCPUBurst !=0
	 * 					Else 
	 * 						2. Record enter time = time + cw
	 * 						3. Set state to running
	 * 				b. Context switch the two process
	 * 	  Case 2
	 * 		The new process has a longer burst time guess than remainingTime,
	 * 			insert the process into the ready queue. 
	 * 				a. set enter time, change state, 
	 * 		
	 * 		
	 */
	public void simulate() {
		if(arrival.size()==0)
			return;
		PriorityQueue<Process> pq = new PriorityQueue<Process>(new SRTComparator());
		Process p = arrival.poll();
		double count = p.getArrivalTime();
		while(!arrival.isEmpty()||!pq.isEmpty()||p.getNumBurst()!=0) {
			System.out.println(count);
			System.out.println(p);
			// Add all the process with the same arrival time
			while(arrival.size()>0&&count>=arrival.peek().getArrivalTime()) 
				pq.add(arrival.poll());
			
			// The process enters CPU
			p.SRTEnterCPU(count);
			
			// Check all three time from CPU, ready queue, and arrival queue
			double running = p.getRemainingTime()+count;
			double in =Integer.MAX_VALUE;
			if(arrival.size()>0)
				in =  arrival.peek().getArrivalTime();
			count = Math.min(running, in);
			// The current process will finish before the new process, just 
			// finish and deal with context switch
			if(count==running) {
				p.SRTComplete(count);
				if(p.getState()!="COMPLETE") {
					p.resetEnterTime();
					arrival.add(p);
				}
				else
					done.add(p);
				// Move onto the next process in the ready queue because new process didn't arrive yet.
				if(pq.size()!=0)
					p = pq.poll();
				else if(arrival.size()!=0) {
					p=arrival.poll();
					count = p.getArrivalTime();
				}
			}
			else { // new process arrives before the current process finish
				p.remainingTime = p.getRemainingTime()-(count-p.getEnterTime());
				// A preemption is needed
				if(arrival.peek().getTimeGuess()<p.remainingTime) {
					p.SRTEnterQueue(count);
					pq.add(p);
					p=arrival.poll();
					p.SRTEnterCPU(count);
				}
				// The new process will not cause preemption, so just add it to the queue
				else
					pq.add(arrival.poll());
			}
		}

	}
	
	
}
