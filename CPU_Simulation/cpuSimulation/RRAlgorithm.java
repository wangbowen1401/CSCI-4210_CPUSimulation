 package cpuSimulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;


public class RRAlgorithm{
	private PriorityQueue<Process> arrival;
	private PriorityQueue<Process> arrivalRecord;
	private ArrayList<Process> done;
	private double cw;
	private String front_or_end;
	private int t_slice;
	
	public RRAlgorithm(RandomSequence arrival, double cw, int t_slice, String front_or_end) {
		arrivalRecord=arrival.getSequence();
		this.t_slice = t_slice;
		this.front_or_end = front_or_end;
		this.arrival = arrival.getSequence();
		this.cw=cw;
		done = new ArrayList<Process>();	
	}

	public void simulate() {
		System.out.println("time 0ms: Simulator started for RR "+printQueueContents(rq));
		// Making sure n != 0
		if(arrival.size()==0) 
		{
			System.out.println("time <0>ms: Simulator ended for RR "+printQueueContents(rq));
			return;
		}
		Queue<Process> rq = new LinkedList<Process>();
 		Process p = arrival.poll();
		int count = p.getArrivalTime();
//		System.out.println("time "+count+"ms: Process "+p.getProcessID()+" (tau "+p.getTimeGuess());
//		is this line necessary?

		while(!arrival.isEmpty()||!rq.isEmpty()||p.getNumBurst()!=0)
		{
			// Add all the process with the same arrival time
			printQueueContents(rq);
			//check if any processes have arrived
			while(arrival.size()>0&&count>=arrival.peek().getArrivalTime()) 
			{
				Process newProcess;
				newProcess = arrival.poll();
				newProcess.enterQueue(newProcess.getArrivalTime());
				add_to_ready_queue(rq, newProcess)//deals with beginning/ end of queue issue
			}
			
			// The Process enters CPU
			if(p.getState()!="RUNNING") {
				count+=cw/2;
				p.enterCPU(count);
			}
			
			// Check the next process arrival time vs remaining time of current Process
			int running = p.getRemainingTime()+p.getEnterTime();
			int in = this.t_slice+count;
			count = Math.min(running, in);
			
			// The current process will finish before or when the time slice expires 
			if(count==running) // processes will only complete in this statement
			{
				count+=cw/2; // Add context switch to move the Process out
				p.complete(count);
				// Still more cpu bursts left
				if(p.getState()!="COMPLETE") 
				{
					p.resetEnterTime();
					arrival.add(p);
				}
				// Completed all the cpu and io bursts, added to arrayList for analysis
				else 
				{
					done.add(p);
				}
				// Move onto the next Process in the ready queue because new Process didn't arrive yet.
				if(rq.size()!=0) 
				{
					p = rq.poll();
				}
				else if(arrival.size()!=0) 
				{
					p=arrival.poll();
					count = p.getArrivalTime();
				}
			}
			else // Process finishes after timeslice
			{ 
				
				if(rq.size()==0) //keep process on cpu, no context switch
				{

				}
				else //move stuff off queue
				{
					count+=cw/2;
					p.enterQueue(count);
					add_to_ready_queue(rq, newProcess);
					p=arrival.poll();
 				}
				
			}
		}
		System.out.println("time <"+count+">ms: Simulator ended for <SRT> [Q empty]");
	}
	
	private void add_to_ready_queue(Queue queue, Process process)
	{
		if(this.begin_or_end == "BEGINNING")
		{
			queue.add(0,process);
		}
		else
		{
			queue.add(process);
		}
	}

	
	private void printQueueContents(PriorityQueue<Process> q){
		Iterator<Process> itr = q.iterator();
		System.out.println("Queue Size: " + q.size());
		System.out.print("Queue Contents: ");
		while(itr.hasNext()) {
			Process p = itr.next();
			System.out.print(p.getProcessID());
			if(itr.hasNext())
				System.out.print(",");	
		}
		System.out.println();	
	}
	
	private double getAvgCPUBurst() {
		double total = 0;
		int entries=0;
		for(Process p : arrivalRecord) {
			entries += p.getNumCPUBurstRecord();
			for(Integer time:p.getCPUBurst()) {
				total += time;
			}
		}
		if(entries==0)
			return 0;
		return total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries += p.getNumCPUBurstRecord();
			for(double w:p.waitTime)
				total+=w;
		}
		if(entries==0)
			return 0;
		return total/entries;
	}
	
	private double getAvgTurnaroundTime() {
		double total = 0;
		int entries=0;
		for(Process p : done) {
			entries += p.getNumCPUBurstRecord();
			for(double w:p.getTurnaroundTime())
				total+=w;
		}
		return total/entries;
	}
	
	private int getTotalCW() {
		int total = 0;
		for(Process p : done) {
			total+=p.getNumContextSwitch();
		}
		return total;
	}
	
	private int getTotalPreempt() {
		int total = 0;
		for(Process p : done) {
			total+=p.getNumPreempt();
		}
		return total;
	}
	
	@Override
	public String toString(){

		StringBuilder sb = new StringBuilder();
		
		// Actual content
		sb.append("Algorithm SRT\n");
		
		// Values that need to be calculated
		double avgCPUBurst = this.getAvgCPUBurst();
		double avgWaitTime = this.getAvgWaitTime();
		double avgTurnaroundTime = this.getAvgTurnaroundTime();
		int numCW = this.getTotalCW();
		int numPreempt = this.getTotalPreempt();
		
		// Prints
		sb.append("-- average CPU burst time: "+ String.format("%.3f",avgCPUBurst));
		sb.append("\n");
		sb.append("-- average wait time: "+ String.format("%.3f",avgWaitTime));
		sb.append("\n");
		sb.append("-- average turnaround time: "+ String.format("%.3f",avgTurnaroundTime));
		sb.append("\n");
		sb.append("-- total number of context switches: "+ numCW);
		sb.append("\n");
		sb.append("-- total number of preemptions: "+ numPreempt);
		return sb.toString();	
	}	
}
