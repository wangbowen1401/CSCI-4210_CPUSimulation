package CPUSimulation;

import java.util.Random;



/*
 * A RandomSequence class which will generate 4*n random doubles using the exponential distribution algorithm. 
 * The code might be removed to be implemented in other classes, but for now, it will stay in a isolated class
 * for better testing
 * 
 */
class RandomSequence{
	private final double[] sequence;
	private final long seed;
	private final double lambda;
	private final double upper;
	private final int n;
	
	
	RandomSequence(long seed,double lambda,double upper,int n){
		sequence = new double[4*n];
		this.seed = seed;
		this.lambda = lambda;
		this.upper = upper;
		this.n = n;
		random();
	}
	
	//In case the class is not needed anymore
	//public void random(double seed,double lambda,double upper,int n)
	public void random() {
		Random randomGenerator = new Random();
		randomGenerator.setSeed(seed);
		for(int i=0;i<4*n;i++) {
			double randomValue = -1*Math.log(1-randomGenerator.nextDouble()%1)/lambda;
			if(randomValue<upper)
				sequence[i]=randomValue;
			else
				i--;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<4*n;i++) {
			sb.append(sequence[i]+"\n");
		}
		return sb.toString();
	}
	
}
