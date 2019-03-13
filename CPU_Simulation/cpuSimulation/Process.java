package cpuSimulation;
import java.util.Arrays;

public abstract class Process{
	final char processID;
	final double alpha;
	final double cw;
	
	String state;
    double arrivalTime;
	double enterTime; // When the process enter ready queue or cpu
	double burstTimeGuess;
	int numCPUBurst;
	int numCPUBurstRecord;
	double cpuBurstTime;
	double remainingTime;
	double ioBurstTime;
	int numPreempt;
	int numContextSwitch;
	
	double[] waitTime;
	double[] turnaroundTime;
	
	
	// Need getters and setters for changing variables
	protected Process(char id,double [] randomValues,double lambda,double alpha,double contextSwitch){
		burstTimeGuess = 1/lambda;
		this.alpha = alpha;
		arrivalTime = -1*Math.log(randomValues[0])/lambda;
		numCPUBurst = (int)(randomValues[1]*100);
		numCPUBurstRecord = numCPUBurst;
		if(numCPUBurst==0) {
			state="COMPLETE";
		}
		else
			state = "NOT ARRIVE";
		cpuBurstTime = -1*Math.log(randomValues[2])/lambda;
		remainingTime = cpuBurstTime;
		ioBurstTime = -1*Math.log(randomValues[3])/lambda;
		processID = id;	
		waitTime = new double[numCPUBurstRecord];
		turnaroundTime = new double [numCPUBurstRecord];
		//Arrays.fill(turnaroundTime, contextSwitch); //Add when we consider context switch
		Arrays.fill(waitTime, 0);
		Arrays.fill(turnaroundTime, cpuBurstTime);
		enterTime = -1;
		numPreempt = 0;
		numContextSwitch=0;
		cw = contextSwitch;
	}

	
	
	///////////////////////////////// Getters and Setters/////////////////////////////////
	public char getProcessID() {
		return processID;
	}
	
	public double[] getTurnaroundTime() {
		return Arrays.copyOf(turnaroundTime,turnaroundTime.length);
	}
	
	public int getNumCPUBurstRecord() {
		return numCPUBurstRecord;
	}
	
	public double getTimeGuess() {
		return burstTimeGuess;
	}
	
	public double getArrivalTime() {
		return arrivalTime;
	}
	
	public double getRemainingTime() {
		return remainingTime;
	}
	
	public String getState() {
		return state;
	}
	
	public double getEnterTime(){
		return enterTime;
	}
	
	public int getNumBurst() {
		return numCPUBurst;
	}
	
	public double getCPUBurstTime() {
		return cpuBurstTime;
	}
	
	public void resetEnterTime() {
		enterTime=-1;
	}
	
	public int getNumPreempt() {
		return numPreempt;
	}
	
	public int getNumContextSwitch() {
		return numContextSwitch;
	}
	
	public void enterCPU(double time) {
		this.state= "RUNNING";
		if(enterTime!=-1)
			waitTime[numCPUBurst-1]+=time-enterTime;
		enterTime = time;// Refers to when the process enter the CPU
	}
	
	public void enterQueue(double time) {
		if(state == "RUNNING"&&enterTime!=-1) {
			remainingTime -=(time-enterTime); 
			numContextSwitch++;
			numPreempt++;
		}
		state= "READY";
		enterTime = time;
	}
	
	// For SRT when CPU burst is complete
	public void complete(double time) {
		numCPUBurst--;
		turnaroundTime[numCPUBurst]+=waitTime[numCPUBurst];
		if(numCPUBurst>0) {
			state="BLOCKED";
			remainingTime = cpuBurstTime;
			arrivalTime = ioBurstTime+time;
			burstTimeGuess = (1-alpha)*burstTimeGuess+alpha*cpuBurstTime;
		}
		else 
			state="COMPLETE";
		numContextSwitch++;
	}
	
	// String to show all the data
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Process ID: "+processID+"\n");
		sb.append("Arrival Time: "+arrivalTime+"\n");
		sb.append("# of CPU Bursts: "+numCPUBurst+"\n");
		sb.append("CPU Burst Time: "+cpuBurstTime+"\n");
		sb.append("I/O Burst Time: "+ioBurstTime+"\n");
		sb.append("Time Guess: " + burstTimeGuess+"\n");
		sb.append("Number of Preemptions: " + numPreempt + "\n");
		sb.append("Number of CW:" + numContextSwitch + "\n");
		sb.append("Waiting Time: ");
		for(int i =numCPUBurstRecord-1;i>=0;i--) {
			sb.append(waitTime[i]+" ");
		}
		sb.append("\nTurnaround Time: ");
		for(int i =numCPUBurstRecord-1;i>=0;i--) {
			sb.append(turnaroundTime[i]+" ");
		}
		
		return sb.toString();
	}
}
