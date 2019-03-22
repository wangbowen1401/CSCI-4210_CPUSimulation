package cpuSimulation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/*
 * A RandomSequence class which will generate 4*n random doubles using the exponential distribution algorithm. 
 * The code might be removed to be implemented in other classes, but for now, it will stay in a isolated class
 * for better testing
 * 
 */
class RandomSequence{
	private PriorityQueue<Process>sequence;
	private long seed;
 	private final double lambda;
	private final double upper;
	private boolean print = false;
	
	RandomSequence(long seed,int cw,double lambda,double alpha,double upper,int n){
		sequence = new PriorityQueue<Process>(new ArrivalComparator());
		this.seed= seed << 16;
		this.seed = this.seed + 13070;
		this.lambda = lambda;
		this.upper = upper;
		char id = 'A';
		for(int i=0;i<n;i++) {
			int arrivalTime = (int)Math.floor(-1*Math.log(this.random(true))/lambda);
			if(arrivalTime == 56) 
				print=true;
			int numCPUBurst = (int)(this.random(false)*100)+1;
			print = false;
			LinkedList<Integer> cpuBurstTime = new LinkedList<Integer>();
			LinkedList<Integer> ioBurstTime = new LinkedList<Integer>();
			for(int j=0;j<numCPUBurst;j++) {
				int a=(int)Math.ceil(-1*Math.log(this.random(true))/lambda);
				cpuBurstTime.add(a);
				if(j<numCPUBurst-1) {
					int b = (int)Math.ceil(-1*Math.log(this.random(true))/lambda);
					ioBurstTime.add(b);
				}
			}
			Process p = new Process(id,arrivalTime,numCPUBurst,cpuBurstTime,ioBurstTime,cw,lambda,alpha);
			sequence.add(p);
			id++;
		}
	}
	
	public void printSequenceContent() {
		Queue<Process> copy = new PriorityQueue<>(sequence);
		Queue<Process> print = new PriorityQueue<>(new AlphaComparator());
		while(!copy.isEmpty()) {
			print.add(copy.poll());
		}
		while(!print.isEmpty()) {
			Process p = print.poll();
			System.out.println("Process " + p.getProcessID() + "[NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU bursts");
		}
		
	}
	
	
	// Return a copy of the random sequence 
	public PriorityQueue<Process>  getSequence(){
		return new PriorityQueue<Process>(sequence);
	}
	
	public double random(boolean exponential){
		long mod = (long) Math.pow(2,48);
		this.seed = (long)((this.seed*25214903917L)+11)%(mod);
		double rand = (this.seed+0.00)/(mod);
		if(rand<=0)
			rand=1+rand;
		double randomValue = Math.ceil(-1*Math.log(rand)/lambda);
		if(randomValue<=upper) {
			return rand;
		}
		else if(exponential){
			return this.random(exponential);
		}
		return rand;
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
