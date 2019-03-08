package cpuSimulation;

public class SRTProcess extends Process {

	public SRTProcess(char id, double[] randomValues, double lambda, double alpha, double contextSwitch) {
		super(id, randomValues, lambda, alpha, contextSwitch);
		
	}
	
	/////////////////////////////////// SRT ////////////////////////////////////////////
	/**  Need to consider context switch **/
	public void SRTEnterCPU(double time) {
		state= "RUNNING";
		if(enterTime!=-1)
			waitTime[numCPUBurst-1]+=time-enterTime;
		enterTime = time;// Refers to when the process enter the CPU
	}
	
	public void SRTEnterQueue(double time) {
		if(state == "RUNNING"&&enterTime!=-1) {
			remainingTime -=(time-enterTime); 
			numPreempt++;
			numContextSwitch++;
		}
		state= "READY";
		enterTime = time;
	}
	
	// For SRT when CPU burst is complete
	public void SRTComplete(double time) {
		numCPUBurst--;
		turnaroundTime[numCPUBurst]+=waitTime[numCPUBurst];
		if(numCPUBurst!=0) {
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
