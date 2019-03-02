package CPUSimulation;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Algorithms {
	// Need a queue for corresponding algorithm 
	// 
	
	// Convert randomSequence into corresponding queue 
	// Run simulation. Probably need two different constructors
	public Algorithms(RandomSequence sequence) {
		
	}
	
	public void SRTSimulation(PriorityQueue<Process> events) {
		
	}
	
	public static Comparator<Process> idComparator = new Comparator<Process>(){
		@Override
		public int compare(Process p1, Process p2) {
            return p1.getTimeGuess()<p2.getTimeGuess()||(p1.getTimeGuess()==p2.getTimeGuess()&&p1.getProcessID()<p2.getProcessID());
        }
	};
	
	
	
	
	
}
