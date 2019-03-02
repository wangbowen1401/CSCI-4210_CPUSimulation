package CPUSimulation;

import java.util.Comparator;
import java.util.PriorityQueue;

class SRTComparator implements Comparator<Process>{
	@Override
	public int compare(Process p1, Process p2) {
        if(p1.getTimeGuess()==p2.getTimeGuess())
        	return p1.getProcessID()-p2.getProcessID();
        else
        	return (int)(p1.getTimeGuess()-p2.getTimeGuess());
    }
}

public class Algorithms {
	// Need a queue for corresponding algorithm 
	// 
	
	// Convert randomSequence into corresponding queue 
	// Run simulation. Probably need two different constructors
	public Algorithms(RandomSequence sequence) {
		
	}
	
	public void SRTSimulation(PriorityQueue<Process> events,double alpha) {
		PriorityQueue<Process> pq = new PriorityQueue<Process>(new SRTComparator());
		/* 1.Add all process in
		 * 2.Pop first process
		 * 3.Increment time, decrement CPU bursts counter, change burstTimeGuess with alpha
		 * 4.Record necessary data
		 * 5.Run until the pq is empty
		 * 6.Output results
		 */
		
		/* Comparator testing code
		 *  Process a1 = new Process('a',5.5);
			Process b1 = new Process('b',5.5);
			Process c1 = new Process('c',4.5);
			Process d1 = new Process('d',3.5);
			Process e1 = new Process('e',4.5);
			PriorityQueue<Process> a = new PriorityQueue<Process>(new SRTComparator());
			a.add(a1);
			a.add(b1);
			a.add(c1);
			a.add(d1);
			a.add(e1);
			while (!a.isEmpty()) {
	           Process p = a.poll();
	           System.out.println(p.getProcessID());
		    }
		 */
	}
	
	
}
