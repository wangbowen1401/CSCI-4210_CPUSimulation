package CPUSimulation;
import java.lang.Math;

class Process {
	double arrivalTime;
	String state;
	double remainingTime;
	double waitTime;
	double burstTimeGuess;
	int numCPUBurst;
	int numIOBurst;
	double cpuBurstTime;
	double ioBurstTime;
	char processID;
	
	
	Process(char id,double [] randomValues,double lambda){
		burstTimeGuess = 1/lambda;
		state = "READY";
		arrivalTime = randomValues[0];
		numCPUBurst = (int)Math.floor(randomValues[1]*100);
		numIOBurst = numCPUBurst-1;
		cpuBurstTime = randomValues[2];
		remainingTime = cpuBurstTime;
		ioBurstTime = randomValues[3];
		processID = id;	
	}
	
	
}
