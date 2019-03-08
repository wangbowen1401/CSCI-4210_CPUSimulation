package cpuSimulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.lang.Math;

public class RRAlgorithm
{
    
	public RRAlgorithm(RandomSequence sequence,double alpha,double cw)
	{
        arrival = new PriorityQueue<Process>(new ArrivalComparator());
		double [] values = sequence.getSequence();
		char id = 'a';
		for(int i=0;i<sequence.size();i+=4) 
		{
			Process p = new Process(id,Arrays.copyOfRange(values, i, i+4),sequence.getLambda(),alpha,cw);
			id++;
			arrival.add(p);
		}
		done = new ArrayList<Process>();	
	}
	
	/* Pseudocode
	    
        while some process is not dead
            for each process at time, if arrival time = time, add to queue (front or back)
            take front of queue, run for time slice or until end of CPU burst, ie :
            		while (!(time_inc != time_slice) || time_inc < burst_time)
            		for FCFS, make time_slice = -1, time_inc is just how much time has passed since the process got on the cpu and burst_time is the time it will take or the burst to finish.
            when process comes off cpu, check to see if I/O, dead, or back in queue, and reset time_inc
            if back in queue, check if only process in queue then no context switch, else context switch and get next process on cpu

            remember to keep track of entering and exiting of I/O, preeemptions, processes stopping and starting on CPU, arrivials, and process death
            
            reminder, time_inc is for keeping track of how long a process is on the cpu which is != to the (global) time used for arrivals and other global activities.

	 */

	
	public void simulate() {
	    
	}
}