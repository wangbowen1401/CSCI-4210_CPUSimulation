
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
		long seed = Long.parseLong(args[0]);
		double lambda =Double.parseDouble(args[1]);
		double upper = Double.parseDouble(args[2]);
		
		int n = Integer.parseInt(args[3]);
		int cw = Integer.parseInt(args[4]);
		double alpha = Double.parseDouble(args[5]);
		String rrAdd;
		
		int ts = Integer.parseInt(args[6]);
		if(args.length == 8) {
			 rrAdd = args[7];
		}else {
			 rrAdd = "END";
		}
		
		
		RandomSequence seq = new RandomSequence(seed,cw,lambda,alpha,upper,n);
		RandomSequence seq2 = new RandomSequence(seed,cw,lambda,alpha,upper,n);
		RandomSequence seq3 = new RandomSequence(seed,cw,lambda,alpha,upper,n);
		RandomSequence seq4 = new RandomSequence(seed,cw,lambda,alpha,upper,n);
		
		SJFAlgorithm SJF = new SJFAlgorithm(seq3,cw);
		SJF.simulate();
		System.out.println();

		
		SRTAlgorithm SRT = new SRTAlgorithm(seq,cw);
		SRT.simulate();
		System.out.println();


		FCFSAlgorithm FCFS = new FCFSAlgorithm(seq2,cw);
		FCFS.simulate();
		System.out.println();
//		
		RRAlgorithm RR = new RRAlgorithm(seq4,cw, ts, rrAdd);
		RR.simulate();

		
		writer.println(SJF);
		writer.println(SRT);
		writer.println(FCFS);
		writer.println(RR);
		writer.close();

		
	}

}
	