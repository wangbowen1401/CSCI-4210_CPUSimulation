package cpuSimulation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Project1{
	

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		//System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("out.txt"))));
		PrintWriter writer  = new PrintWriter("simout.txt","UTF-8");
		
		double lambda =0.001;
		double upper = 3000;
		int n = 10;
		long seed = 70;
		double cw = 8;
		double alpha = 0.5;
		RandomSequence test = new RandomSequence(seed,lambda,alpha,upper,n);
		//SRTAlgorithm SRT = new SRTAlgorithm(test,cw);
		//SRT.simulate();

		//System.out.println(SRT);
		//FCFSAlgorithm FCFS = new FCFSAlgorithm(test,alpha,cw);
		//FCFS.simulate();
		//System.out.println(FCFS);
		SJFAlgorithm SJF = new SJFAlgorithm(test, alpha, cw);

		//FCFSAlgorithm FCFS = new FCFSAlgorithm(test,alpha,cw);
		//FCFS.simulate();
		//System.out.println(FCFS);

		SJF.simulate();
		System.out.println(SJF);
		writer.println(SJF);
		//	writer.println(SRT);
		//writer.println(FCFS);
		//writer.println(RR);
		
	}

}
	