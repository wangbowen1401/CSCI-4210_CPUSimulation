package CPUSimulation;
import java.util.Arrays;
import java.util.Comparator;

class ArrivalComparator implements Comparator<Process>{
	@Override
	public int compare(Process a,Process b) {
		if (a.arrivalTime<b.arrivalTime)
			return -1;
		else if(a.arrivalTime==b.arrivalTime) {
			if(a.getState()=="BLOCKED"&&b.getState()=="BLOCKED") {
				return a.getProcessID()<b.getProcessID()?-1:1;
			}
			else if(a.getState()=="BLOCKED")
				return -1;
		}
		return a.getProcessID()<b.getProcessID()?-1:1;
	}
}

class Process{
	final char processID;
	final double alpha;
	final double cw;
	
	String state;
	double arrivalTime;
	double enterTime; // When the process enter ready queue or cpu
	double burstTimeGuess;
	int numCPUBurst;
	double cpuBurstTime;
	double remainingTime;
	double ioBurstTime;
	int numPreempt;
	
	double[] waitTime;
	double[] turnaroundTime;
	
	
	// Need getters and setters for changing variables
	Process(char id,double [] randomValues,double lambda,double alpha,double contextSwitch){
		burstTimeGuess = 1/lambda;
		this.alpha = alpha;
		arrivalTime = -1*Math.log(1-randomValues[0])/lambda;
		numCPUBurst = (int)Math.floor(randomValues[1]*100);
		if(numCPUBurst==0) {
			state="COMPLETE";
		}
		else
			state = "NOT ARRIVE";
		cpuBurstTime = -1*Math.log(1-randomValues[2])/lambda;
		remainingTime = cpuBurstTime;
		ioBurstTime = -1*Math.log(1-randomValues[3])/lambda;
		processID = id;	
		waitTime = new double[numCPUBurst];
		turnaroundTime = new double [numCPUBurst];
		Arrays.fill(turnaroundTime, contextSwitch); //Add when we consider context switch
		enterTime = -1;
		numPreempt = 0;
		cw = contextSwitch;
	}
	
	
	
	/////////////////////////////////// SRT ////////////////////////////////////////////
	public void SRTEnterCPU(double time) {
		state= "RUNNING";
		if(enterTime!=-1)
			waitTime[numCPUBurst-1]+=time-enterTime;
		enterTime = time+cw;// Refers to when the process enter the CPU
	}
	
	public void SRTEnterQueue(double time) {
		state= "READY";
		if(enterTime!=-1)
			remainingTime -=(time-enterTime); 
		enterTime = time+cw;
	}
	
	// For SRT when CPU burst is complete
	public void SRTComplete(double time) {
		numCPUBurst--;
		turnaroundTime[numCPUBurst]+=waitTime[numCPUBurst];
		if(numCPUBurst!=0) {
			state="BLOCKED";
			arrivalTime = ioBurstTime+time+cw/2;
			burstTimeGuess = alpha*burstTimeGuess+(1-alpha)*turnaroundTime[numCPUBurst];
		}
		else {
			state="COMPLETE";
		}
		
	}
	
	///////////////////////////////// Getters and Setters/////////////////////////////////
	public char getProcessID() {
		return processID;
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
	
	public void setState() {
		state="BLOCKED";
	}
	// String to show all the data
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Process ID: "+processID+"\n");
		sb.append("Arrival Time: "+arrivalTime+"\n");
		sb.append("# of CPU Bursts: "+numCPUBurst+"\n");
		sb.append("CPU Burst Time: "+cpuBurstTime+"\n");
		sb.append("I/O Burst TIme: "+ioBurstTime+"\n");
		return sb.toString();
	}
}
