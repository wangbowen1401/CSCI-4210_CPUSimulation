package CPUSimulation;

public class Event {
	double arrivalTime;
	String state;
	double remainingTime;
	double burstTimeGuess;
	int numCPUBursts;
	double cpuBurstTime;
	double ioBurstTime;
	String name;
	
	Event(){
		state = "Ready";
	}
}
