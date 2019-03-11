package cpuSimulation;

class FCFSProcess extends Process {

	public FCFSProcess(char id, double[] randomValues, double lambda, double alpha, double contextSwitch) {
		super(id, randomValues, lambda, alpha, contextSwitch);
	}
	
	public void enterQueue(double time){
		this.state= "RUNNING";
		if(enterTime!=-1)
			waitTime[numCPUBurst-1]+=time-enterTime;
		enterTime = time;// Refers to when the process enter the CP
	}

	@Override
	public void enterCPU(double time) {
		if(state == "RUNNING"&&enterTime!=-1) {
			remainingTime -=(time-enterTime); 
			numContextSwitch++;
			numPreempt++;
		}
		state= "READY";
		enterTime = time;
	}

	@Override
	public void complete(double time) {
		numCPUBurst--;
		turnaroundTime[numCPUBurst]+=waitTime[numCPUBurst];
		if(numCPUBurst>0) {
			state="BLOCKED";
			remainingTime = cpuBurstTime;
			arrivalTime = ioBurstTime+time;
		}
		else 
			state="COMPLETE";
		numContextSwitch++;
	}

}
