package cpuSimulation;

import java.util.Comparator;

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

public class SRTProcess extends Process {

	public SRTProcess(char id, double[] randomValues, double lambda, double alpha, double contextSwitch) {
		super(id, randomValues, lambda, alpha, contextSwitch);
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
	

}
