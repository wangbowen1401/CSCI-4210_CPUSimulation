package CPUSimulation;

import java.util.Comparator;
import java.util.PriorityQueue;

//Basically wants a queue that orders by remaining time and the order the items
//are inserted
class SRTComparator implements Comparator<Process>{
@Override
public int compare(Process p1, Process p2) {
    return (int)Math.ceil(p1.getRemainingTime()-p2.getRemainingTime());
}
}
public class Project1{
	

	public static void main(String[] args) {
		double lambda =0.1;
		double upper = 3000;
		int n =26;
		long seed = 5;
		double alpha = 0.5;
		RandomSequence test = new RandomSequence(seed,lambda,upper,n);
		Algorithm algo = new Algorithm(test);
		
	}

}
