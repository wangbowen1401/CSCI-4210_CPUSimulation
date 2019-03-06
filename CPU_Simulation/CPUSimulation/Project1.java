package CPUSimulation;


public class Project1{
	

	public static void main(String[] args) {
		double lambda =0.1;
		double upper = 3000;
		int n = 1;
		long seed = 5;
		double cw = 0.05;
		double alpha = 0.5;
		RandomSequence test = new RandomSequence(seed,lambda,upper,n);
		SRTAlgorithm SRT = new SRTAlgorithm(test,alpha,cw);
		SRT.simulate();
	}

}
