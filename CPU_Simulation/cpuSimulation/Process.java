package cpuSimulation;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

//Basically wants a queue that orders by remaining time and the order the items
//are inserted


class AlphaComparator implements Comparator<Process>{
	
	@Override
	public int compare(Process a, Process b) {
		return a.getProcessID()<b.getProcessID()?-1:1;
	}
}
class ArrivalComparator implements Comparator<Process>{
	@Override
	public int compare(Process a,Process b) {
		if (a.getArrivalTime()!=b.getArrivalTime())
			return (int)(a.getArrivalTime()-b.getArrivalTime());
		else {
			if(a.getState()=="BLOCKED"&&b.getState()=="BLOCKED") {
				return a.getProcessID()<b.getProcessID()?-1:1;
			}
			else if(a.getState()=="BLOCKED")
				return -1;
		}
		return a.getProcessID()<b.getProcessID()?-1:1;
	}
}

public  class Process{
	final char processID;
	final double alpha;
	
	String state;
    int arrivalTime;
	int enterTime; // When the process enter ready queue or cpu
	int burstTimeGuess;
	int burstTimeGuessRecord;
	int numCPUBurst;
	int numCPUBurstRecord;
	int remainingTime;
	int numPreempt;
	int numContextSwitch;
	int cw;
	
	int [] waitTime;
	int [] turnaroundTime;
	LinkedList<Integer> cpuBurstTime;
	LinkedList<Integer> ioBurstTime;
	
	
	// Need getters and setters for changing variables
	public Process(char id,int arrivalTime,int numCPUBurst,LinkedList<Integer> cpuBurstTime,LinkedList<Integer> ioBurstTime,int cw,double lambda,double alpha){
		burstTimeGuess = (int)(1/lambda);
		burstTimeGuessRecord = burstTimeGuess;
		this.alpha = alpha;
		this.arrivalTime = arrivalTime;
		this.numCPUBurst =numCPUBurst;
		this.cw=cw;
		numCPUBurstRecord = numCPUBurst;
		
		if(numCPUBurst==0) 
			state="COMPLETE";
		else
			state = "NOT ARRIVE";
		// Fill burst times
		this.cpuBurstTime=cpuBurstTime;
		remainingTime = cpuBurstTime.getFirst();
		this.ioBurstTime = ioBurstTime;
		
		
		processID = id;	
		waitTime = new int[numCPUBurstRecord];
		Arrays.fill(waitTime, 0);
		turnaroundTime = new int[numCPUBurstRecord];
		int i=0;
		for(Integer burst:cpuBurstTime) {
			turnaroundTime[i]=(int)burst;
			i++;
		}

		enterTime = -1;
		numPreempt = 0;
		numContextSwitch=0;
	}
	
	/*
	public Process(Process original){
		this.processID=original.getProcessID();
		this.alpha=original.alpha;
		
		this.state=original.state;
	    this.arrivalTime=original.arrivalTime;
		this.enterTime=original.enterTime; // When the process enter ready queue or cpu
		this.burstTimeGuess=original.burstTimeGuess;
		this.burstTimeGuessRecord=original.burstTimeGuessRecord;
		this.numCPUBurst=original.numCPUBurst;
		this.numCPUBurstRecord=original.numCPUBurstRecord;
		this.remainingTime=original.remainingTime;
		this.numPreempt=original.numPreempt;
		this.numContextSwitch=original.numContextSwitch;
		this.cw=original.cw;
		
		this.cpuBurstTime=original.cpuBurstTime;
		this.waitTime= new int[numCPUBurstRecord];
		Arrays.fill(waitTime, 0);
		this.turnaroundTime=new int[numCPUBurstRecord];
		this.ioBurstTime=original.ioBurstTime;
		
		int i=0;
		for(Integer burst:cpuBurstTime) {
			turnaroundTime[i]=(int)burst;
			i++;
		}
		
		
		
	}*/

	
	
	///////////////////////////////// Getters and Setters/////////////////////////////////
	public LinkedList<Integer> getCPUBurst(){
		LinkedList<Integer> copy = new LinkedList<Integer>(cpuBurstTime);
		return copy;
	}
	
	public LinkedList<Integer> getIOBurst(){
		return new LinkedList<Integer>(ioBurstTime);
	}
	
	public char getProcessID() {
		return processID;
	}
	
	public int[] getTurnaroundTime() {
		return Arrays.copyOf(turnaroundTime,turnaroundTime.length);
	}
	
	public int getNumCPUBurstRecord() {
		return numCPUBurstRecord;
	}
	
	public int getTimeGuess() {
		return burstTimeGuess;
	}
	
	public int getArrivalTime() {
		return arrivalTime;
	}
	
	public int getRemainingTime() {
		return remainingTime;
	}
	
	public String getState() {
		return state;
	}
	
	public int getEnterTime(){
		return enterTime;
	}
	
	public int getNumBurst() {
		return numCPUBurst;
	}
	
	public int getCPUBurstTime() {
		return cpuBurstTime.getFirst();
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
	
	//////////////////////////////////////////// Simulation Helpers ///////////////////////////////////////////
	public void enterCPU(int time) {
		this.state= "RUNNING";
		if(enterTime!=-1) {
			waitTime[numCPUBurst-1]+=time-enterTime;
		}
		turnaroundTime[numCPUBurst-1]+=cw/2;
		enterTime = time+cw/2;// Refers to when the process enter the CPU
	}
	
	public void enterQueue(int time) {
		state= "READY";
		// If the process was in the CPU
		if(state == "RUNNING"&&enterTime!=-1) {
			turnaroundTime[numCPUBurst-1]+=cw/2;
			remainingTime -=(time-enterTime); 
			burstTimeGuess-=(time-enterTime);
			numContextSwitch++;
			numPreempt++;
			enterTime = time+cw/2;
		}
		else
			enterTime = time;
	}
	
	// For SRT when CPU burst is complete
	public void complete(int time) {
		numCPUBurst--;
		turnaroundTime[numCPUBurst]+=waitTime[numCPUBurst]+cw/2;
		if(numCPUBurst>0) {
			burstTimeGuess = (int)((1-alpha)*burstTimeGuessRecord+alpha*cpuBurstTime.getFirst());
			cpuBurstTime.poll();
			state="BLOCKED";
			remainingTime = cpuBurstTime.getFirst();
			arrivalTime = ioBurstTime.poll()+time+cw/2;
			burstTimeGuessRecord = burstTimeGuess;
		}
		else {
			state="COMPLETE";
		}
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
	
	
	public String printBursts() {
		StringBuilder sb = new StringBuilder();
		sb.append("Process: "+this.processID+"\n");
		Iterator<Integer> cItr = cpuBurstTime.iterator();
		Iterator<Integer> iItr = ioBurstTime.iterator();
		while(cItr.hasNext()) {
			sb.append("CPU Burst: "+cItr.next());
			if(iItr.hasNext())
				sb.append(" -> I/O Burst: "+iItr.next());
			sb.append("\n");
		}
		return sb.toString();
	}
}
