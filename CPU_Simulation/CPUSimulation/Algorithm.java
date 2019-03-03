package CPUSimulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;


// Basically wants a queue that orders by remaining time and the order the items
//  are inserted
class SRTComparator implements Comparator<Process>{
	@Override
	public int compare(Process p1, Process p2) {
        return (int)Math.ceil(p1.getRemainingTime()-p2.getRemainingTime());
    }
}

public class Algorithm{
	private PriorityQueue<Process> arrival;
	private ArrayList<Process> done;
	// Need a queue for corresponding algorithm 
	// 
	
	// Convert randomSequence into corresponding queue 
	// Run simulation. Probably need two different constructors
	public Algorithm(RandomSequence sequence) {
		arrival = new PriorityQueue<Process>();
		double [] values = sequence.getSequence();
		char id = 'a';
		for(int i=0;i<sequence.size();i+=3) {
			Process p = new Process(id,Arrays.copyOfRange(values, i, i+4),sequence.getLambda(),0.5,1.00);
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
	
	
	public void SRTSimulation() {
		PriorityQueue<Process> pq = new PriorityQueue<Process>(new SRTComparator());
		Process p = arrival.poll();
		double count = p.getArrivalTime();
		while(!arrival.isEmpty()) {
			
			// Add all the process with the same arrival time
			while(count>=arrival.peek().getArrivalTime()) {
				pq.add(arrival.poll());
			}
			
			// The process enters CPU
			p.SRTEnterCPU(count);
			
			// If there is nothing else in the ready queue
			if(pq.isEmpty()) {
				double complete = p.getRemainingTime()+count;
				double in =  arrival.peek().getArrivalTime();
				count = Math.min(complete, in);
				// The current process will finish before the new process, just 
				// finish and deal with context switch
				if(count==complete) {
					p.SRTComplete(count);
					if(p.getState()!="COMPLETE") {
						arrival.add(p);
					}
					else
						done.add(p);
				}
				else {
					p.SRTEnterQueue(count);
				}
				
			}
			
		}
		
		
	}
	
	
}
