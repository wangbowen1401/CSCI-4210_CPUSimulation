package cpuSimulation;

public class Project1{
	

	public static void main(String[] args) {
		double lambda =0.001;
		double upper = 3000;
		int n = 10;
		long seed = 70;
		double cw = 8;
		double alpha = 0.5;
		RandomSequence test = new RandomSequence(seed,lambda,alpha,upper,n);
		//System.out.println(test);
		//SRTAlgorithm SRT = new SRTAlgorithm(test,cw);
		//SRT.simulate();
		//System.out.println(SRT);
//		FCFSAlgorithm FCFS = new FCFSAlgorithm(test,alpha,cw);
//		FCFS.simulate();
//		System.out.println(FCFS);
		SJFAlgorithm SJF = new SJFAlgorithm(test, alpha, cw);

		//FCFSAlgorithm FCFS = new FCFSAlgorithm(test,alpha,cw);
		//FCFS.simulate();
		//System.out.println(FCFS);

		SJF.simulate();
		System.out.println(SJF);
		
	}

}
	