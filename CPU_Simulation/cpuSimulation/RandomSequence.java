package cpuSimulation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/*
 * A RandomSequence class which will generate 4*n random doubles using the exponential distribution algorithm. 
 * The code might be removed to be implemented in other classes, but for now, it will stay in a isolated class
 * for better testing
 * 
 */
class RandomSequence{
	private PriorityQueue<Process>sequence;
	private double seed;
 	private final double lambda;
	private final double upper;
	
	RandomSequence(long seed,double lambda,double alpha,double upper,int n){
		sequence = new PriorityQueue<Process>(new ArrivalComparator());
		this.seed = seed << 16;
		this.seed = this.seed + 13070;
		this.lambda = lambda;
		this.upper = upper;
		char id = 'A';
		for(int i=0;i<n;i++) {
			int arrivalTime = (int)(-1*Math.log(this.random())/lambda);
			int numCPUBurst = (int)(this.random()*100)+1;
			LinkedList<Integer> cpuBurstTime = new LinkedList<Integer>();
			LinkedList<Integer> ioBurstTime = new LinkedList<Integer>();
			for(int j=0;j<numCPUBurst;j++) {
				cpuBurstTime.add((int)(-1*Math.log(this.random())/lambda));
				if(j<numCPUBurst-1)
					ioBurstTime.add((int)(-1*Math.log(this.random())/lambda));
			}
			Process p = new Process(id,arrivalTime,numCPUBurst,cpuBurstTime,ioBurstTime,lambda,alpha);
			sequence.add(p);
			id++;
		}
	}
	
	// Return a copy of the random sequence 
	public PriorityQueue<Process>  getSequence(){
		return new PriorityQueue<Process>(sequence);
	}
	
	public double random(){
		this.seed = ((this.seed*25214903917L)+11)%(Math.pow(2, 48));
		double rand = this.seed/(Math.pow(2,48));
		double randomValue = -1*Math.log(rand%1)/lambda;
		if(randomValue<=upper)
			return rand%1;
		else
			return random();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Process> itr = sequence.iterator();
		while(itr.hasNext()) {
			sb.append(itr.next().printBursts());
			sb.append("\n");
		}
		return sb.toString();
	}
}
