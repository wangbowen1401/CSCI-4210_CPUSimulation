package cpuSimulation;

/*
 * A RandomSequence class which will generate 4*n random doubles using the exponential distribution algorithm. 
 * The code might be removed to be implemented in other classes, but for now, it will stay in a isolated class
 * for better testing
 * 
 */
class RandomSequence{
	private final double[] sequence;
	private double seed;
 	private final double lambda;
	private final double upper;
	private final int n;
	
	RandomSequence(long seed,double lambda,double upper,int n){
		sequence = new double[4*n];
		this.seed = seed << 16;
		this.seed = this.seed + 13070;
		this.lambda = lambda;
		this.upper = upper;
		this.n = n;
		random();
	}
	
	// Return a copy of the random sequence 
	public double[] getSequence(){
		double [] copy = new double[4*n];
		for(int i =0;i<4*n;i++)
			copy[i]=sequence[i];
		return copy;
	}
	
	//In case the class is not needed anymore
	//public void random(double seed,double lambda,double upper,int n)
	public void random(){
		
		for(int i=0;i<4*n;i++)
		{
			this.seed = ((this.seed*25214903917L)+11)%(Math.pow(2, 48));
			double rand = this.seed/(Math.pow(2,48));
			double randomValue = -1*Math.log(1-rand%1)/lambda;
			if(randomValue<upper)
				sequence[i]=randomValue%1;
			else
				i--;
		}
	}
	
	public int size() {
		return 4*n;
	}
	
	public double getLambda() {
		return lambda;
	}
	
	// String to show all the data
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<4*n;i++) {
			sb.append(sequence[i]+"\n");
		}
		return sb.toString();
	}
	
}
