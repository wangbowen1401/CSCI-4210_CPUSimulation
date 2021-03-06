	import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class FCFSAlgorithm {
	private PriorityQueue<Process> arrival;
	private Queue<Process> rq;
	private ArrayList<Process> done;
	private final double avgCPUBurst;
	private int cw;
	private boolean full = false;
	
	public FCFSAlgorithm(RandomSequence test,int cw) {
		arrival = test.getSequence();
		this.cw = cw;
		done = new ArrayList<Process>();
		this.rq = new LinkedList<Process>();
		test.printSequenceContent();
		avgCPUBurst = this.getAvgCPUBurst();
	}
	
	public void simulate(){
		System.out.println("time 0ms: Simulator started for FCFS [Q <empty>]");
		if(arrival.size()==0) {
			System.out.println("time 0ms: Simulator ended for FCFS [Q <empty>]");
			return;
		}
		Process p = arrival.poll();
		int count = p.getArrivalTime();
		rq.add(p);
		System.out.println("time "+count+"ms: Process "+p.getProcessID()+" arrived; added to ready queue "+printQueueContents(rq));
		p = rq.poll();
		while((!arrival.isEmpty()||!rq.isEmpty())||p.getNumBurst()!=0){
			// Add all the Process with the same arrival time
			while(arrival.size()>0&&count==arrival.peek().getArrivalTime()) { 
				Process newProcess = arrival.poll();
				rq.add(newProcess);
				if(newProcess.getState()!="BLOCKED"&&(count<=999 || full))
					System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" arrived; added to ready queue "+printQueueContents(rq));
				else if((count<=999 || full))
					System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" completed I/O; added to ready queue "+printQueueContents(rq));
				newProcess.enterQueue(newProcess.getArrivalTime());
			}
			
			// The Process enters CPU
			if(p.getState()!="RUNNING") {
				p.enterCPU(count);
				count+=cw/2;
				while(arrival.size()!=0&&count>arrival.peek().getArrivalTime())
					addNewProcess();
				if((count<=999 || full == true))
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.getRemainingTime()+"ms burst "+printQueueContents(rq));
			}
			
			int running = p.getRemainingTime()+p.getEnterTime();
			int in =Integer.MAX_VALUE;
			if(arrival.size()>0)
				in =  arrival.peek().getArrivalTime();
			count = Math.min(running, in);
			if(count==running) {
				p.complete(count);
				// Still more cpu bursts left
				if(p.getState()!="COMPLETE") {
					if((count<=999 || this.full == true)) {
						if(p.getNumBurst()!=1){
							System.out.println("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU burst; "+p.getNumBurst()+" bursts to go "+printQueueContents(rq));
						}
						else{
							System.out.println("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU burst; "+p.getNumBurst()+" burst to go "+printQueueContents(rq));
						}
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" switching out of CPU; will block on I/O until time "+p.getArrivalTime()+"ms "+printQueueContents(rq));
					}
					
					p.resetEnterTime();
					arrival.add(p);
						
				}
				// Completed all the cpu and io bursts, added to arrayList for analysis
				else {
					done.add(p);
					System.out.println("time "+count+"ms: Process "+p.getProcessID()+" terminated "+printQueueContents(rq));
				}
				count+=cw/2;
				while(!arrival.isEmpty()&&arrival.peek().getArrivalTime()<=count)
					addNewProcess();
				// Move onto the next Process in the ready queue because new Process didn't arrive yet.
				if(rq.size()!=0) {
					p = rq.poll();
				}
				else if(arrival.size()!=0){
					p=arrival.poll();
					if(count <= p.getArrivalTime()){
                        count = p.getArrivalTime();

                    }
					rq.add(p);
					if(p.getState()!="BLOCKED"&&(count<=999 || this.full == true))
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" arrived; added to ready queue "+printQueueContents(rq));
					else if((count<=999 || this.full == true))
						System.out.println("time "+count+"ms: Process "+p.getProcessID()+" completed I/O; added to ready queue "+printQueueContents(rq));
					p.enterQueue(p.getArrivalTime());
					while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime()) {
						addNewProcess();
					}
					p=rq.poll();
				}
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for FCFS "+printQueueContents(rq));		
	}
	
	private void addNewProcess() {
		Process newProcess = arrival.poll();
		this.rq.add(newProcess);
		if(newProcess.getState()!="BLOCKED"&&(newProcess.getArrivalTime()<=999||full))
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" arrived; added to ready queue "+printQueueContents(rq));
		else if(newProcess.getArrivalTime()<=999||full)
			System.out.println("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" completed I/O; added to ready queue "+printQueueContents(rq));
		newProcess.enterQueue(newProcess.getArrivalTime());
	}
	
	private String printQueueContents(Queue<Process> q) {
		StringBuilder sb = new StringBuilder();
		Queue<Process> cp = new LinkedList<Process>(q);
		if(cp.isEmpty()) {
			sb.append("[Q <empty>]");
			return sb.toString();
		}
		sb.append("[Q");
		while(!cp.isEmpty()){
			sb.append(" " + cp.poll().getProcessID());			
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	private double getAvgCPUBurst() {
		long total = 0;
		int entries=0;
		for(Process p : arrival) {
			entries += p.getNumCPUBurstRecord();
			for(Integer time:p.getCPUBurst()){
				total+=time;
			}
		}
		if(entries==0)
			return 0;
		return (double)total/entries;
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
			for(int i:p.getTurnaroundTime())
				total+=i;
		}
		if(entries==0)
			return 0;
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
		sb.append("Algorithm FCFS\n");
		
		// Values that need to be calculated
		double avgCPUBurst = this.avgCPUBurst;
		double avgWaitTime = this.getAvgWaitTime();
		double avgTurnaroundTime = this.getAvgTurnaroundTime();
		int numCW = this.getTotalCW();
		int numPreempt = this.getTotalPreempt();
		
		NumberFormat formatter = new DecimalFormat("#0.000"); 
		// Prints
		sb.append("-- average CPU burst time: "+ formatter.format(avgCPUBurst)+" ms");
		sb.append("\n");
		sb.append("-- average wait time: "+ formatter.format(avgWaitTime)+" ms");
		sb.append("\n");
		sb.append("-- average turnaround time: "+ formatter.format(avgTurnaroundTime)+" ms");
		sb.append("\n");
		sb.append("-- total number of context switches: "+ numCW);
		sb.append("\n");
		sb.append("-- total number of preemptions: "+ numPreempt);
		return sb.toString();	
	}	

}

