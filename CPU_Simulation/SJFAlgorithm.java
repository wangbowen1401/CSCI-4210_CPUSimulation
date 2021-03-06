
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;



// Basically wants a queue that orders by remaining time and the order the items
//  are inserted
class SJFComparator implements Comparator<Process>{
	@Override
	public int compare(Process p1, Process p2) {
		if(p1.getTimeGuess()!=p2.getTimeGuess())
			return (int)Math.ceil(p1.getTimeGuess()-p2.getTimeGuess());
		else
			return p1.getProcessID()<p2.getProcessID()?-1:1;
    }
}


public class SJFAlgorithm {
	Process r;
	PriorityQueue<Process> Q;
	private PriorityQueue<Process> arrivalQueue;
	private ArrayList<Process> completedProcesses;
	private double cw;
	private double avgCPUBurst;
	private boolean neverStarted = false;
	
	public SJFAlgorithm(RandomSequence test,double cw) {
		arrivalQueue = test.getSequence();
		this.cw=cw;
		completedProcesses = new ArrayList<Process>();
		avgCPUBurst = this.getAvgCPUBurst();
	}
	
	public void simulate() {
		PriorityQueue<Process> copy = new PriorityQueue<>(arrivalQueue);
		PriorityQueue<Process> print = new PriorityQueue<>(new AlphaComparator());
		
		
		while(!copy.isEmpty()) {
			print.add(copy.poll());
		}
		//Iterator<Process> it = print.iterator();
		while(!print.isEmpty()) {
			Process p = print.poll();
			if(p.getNumBurst() > 1)
				System.out.println("Process " + p.getProcessID() + " [NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU bursts");
			else
				System.out.println("Process " + p.getProcessID() + " [NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU burst");
		}
		
		Q = new PriorityQueue<Process>(new SJFComparator());
		System.out.print("time 0ms: Simulator started for SJF ");
		printQueueContents(Q);


		if(arrivalQueue.isEmpty()) {
			System.out.println("time "+0+"ms: Simulator ended for SJF [Q <empty>]");
			neverStarted = true;
			return;
		}
			
		
		
		Process currentProcess = arrivalQueue.peek();
		
		//System.out.println("Process " + currentProcess.getProcessID() + "[NEW] (arrival time " + currentProcess.getArrivalTime() + " ms) " + currentProcess.getNumBurst() + " CPU bursts");
		Q.add(arrivalQueue.poll());
		
		
		
		int count = currentProcess.getArrivalTime();
		System.out.print("time " + count + "ms: " + "Process " + currentProcess.getProcessID() + " (tau " + currentProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
		printQueueContents(Q);
		currentProcess = Q.poll();
		
		
		while((!arrivalQueue.isEmpty()|!Q.isEmpty())||currentProcess.getNumBurst()!=0) {
			//printQueueContents(Q);
			Process newProcess;
			while(arrivalQueue.size()>0&&count==arrivalQueue.peek().getArrivalTime()) { 
				newProcess = arrivalQueue.poll();
				if(!Q.contains(newProcess))
				Q.add(newProcess);
				
				if(newProcess.getState() == "BLOCKED") {
					if(newProcess.getArrivalTime() <= 999) {
						System.out.print("time " + newProcess.getArrivalTime() + "ms:" + " Process " + newProcess.getProcessID() + " (tau " + newProcess.getTimeGuess() + "ms) completed I/O; added to ready queue " );
						printQueueContents(Q);
					}
					
				}else {
					if(r.getArrivalTime() <= 999) {
						System.out.print("time " + newProcess.getArrivalTime() + "ms: Process " + newProcess.getProcessID() + " (tau " + newProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
						printQueueContents(Q);
					}
					
				}
				
				newProcess.enterQueue(count);
			}
			
			if(currentProcess.getState()!="RUNNING") {
				currentProcess.enterCPU(count);

				count+=cw/2;
				if(arrivalQueue.isEmpty() == false && arrivalQueue.peek().getArrivalTime() < count) {
					r = arrivalQueue.poll();
					Q.add(r);
					
					if(r.getState() == "BLOCKED") {
						if(r.getArrivalTime() <= 999) {
							System.out.print("time " + r.getArrivalTime() + "ms:" + " Process " + r.getProcessID() + " (tau " + r.getTimeGuess() + "ms) completed I/O; added to ready queue " );
							printQueueContents(Q);
						}
						
					}else {
						if(r.getArrivalTime() <= 999) {
							System.out.print("time " + r.getArrivalTime() + "ms: Process " + r.getProcessID() + " (tau " + r.getTimeGuess() + "ms) arrived; added to ready queue ");
							printQueueContents(Q);
						}
						
					}
					
					r.enterQueue(r.getArrivalTime());
					
					
				}

				if(count <= 999) {
					System.out.print("time " + count +"ms: Process " + currentProcess.getProcessID()+ " started using the CPU for " + currentProcess.getCPUBurstTime() + "ms burst ");
					printQueueContents(Q);
				}

//				System.out.print("time " + count +"ms: Process " + currentProcess.getProcessID()+ " started using the CPU for " + currentProcess.getCPUBurstTime() + "ms burst ");
//				printQueueContents(Q);

			}
			
			int running = currentProcess.getRemainingTime()+currentProcess.getEnterTime();
			int in =Integer.MAX_VALUE;
			if(arrivalQueue.size()>0)
				in =  arrivalQueue.peek().getArrivalTime();
			//printQueueContents(arrivalQueue);
			count = Math.min(running, in);

			if(count==running) {
//				while( arrivalQueue.size() > 0 && arrivalQueue.peek().getArrivalTime() < count ) {
//					Process p = arrivalQueue.peek();
//					Q.add(arrivalQueue.poll());
//					if(p.getState() == "BLOCKED") {
//						if(count <= 999) {
//							System.out.print("time " + count + "ms:" + " Process " + p.getProcessID() + " (tau " + p.getTimeGuess() + "ms) completed I/O; added to ready queue " );
//							printQueueContents(Q);
//						}
//					}else {
//						if(count <= 999) {
//							System.out.print("time " + count + "ms: Process " + p.getProcessID() + " (tau " + p.getTimeGuess() + "ms) arrived; added to ready queue ");
//							printQueueContents(Q);
//						}
//					}				
//					p.enterQueue(p.getArrivalTime());
//				}
				
				
				currentProcess.complete(count);
				if(currentProcess.getState()!="COMPLETE") {
//					System.out.print("time "+ count + "ms: Process " + currentProcess.getProcessID() + " completed a CPU burst; " + currentProcess.getNumBurst() + " bursts to go " );
//					printQueueContents(Q);
//					System.out.print("time " + count + "ms: Recalculated tau = " + currentProcess.getTimeGuess() + "ms for Process " + currentProcess.getProcessID() + " ");
//					printQueueContents(Q);
//					System.out.print("time "+ count+ "ms: Process " + currentProcess.getProcessID() + " switching out of CPU; will block on I/O until time "+ currentProcess.getArrivalTime() + " ");
				//	printQueueContents(Q);
					if(count <= 999) {
						if(currentProcess.getNumBurst() > 1)
							System.out.print("time "+ count + "ms: Process " + currentProcess.getProcessID() + " completed a CPU burst; " + currentProcess.getNumBurst() + " bursts to go " );
						else
							System.out.print("time "+ count + "ms: Process " + currentProcess.getProcessID() + " completed a CPU burst; " + currentProcess.getNumBurst() + " burst to go " );

						printQueueContents(Q);
						System.out.print("time " + count + "ms: Recalculated tau = " + currentProcess.getTimeGuess() + "ms for process " + currentProcess.getProcessID() + " ");
						printQueueContents(Q);
						System.out.print("time "+ count + "ms: Process " + currentProcess.getProcessID() + " switching out of CPU; will block on I/O until time "+ currentProcess.getArrivalTime() + "ms ");
						printQueueContents(Q);	
					}

					
					
					currentProcess.resetEnterTime();
					
					arrivalQueue.add(currentProcess);
					//System.out.println(currentProcess.getArrivalTime());
					
				}
				else {
					completedProcesses.add(currentProcess);
					
					System.out.print("time " + count + "ms: Process " + currentProcess.getProcessID() + " terminated ");
					printQueueContents(Q);

				}
				count+=cw/2;
				
				if(arrivalQueue.isEmpty() == false && arrivalQueue.peek().getArrivalTime() <= count) {
					r = arrivalQueue.poll();
					Q.add(r);
					
					if(r.getState() == "BLOCKED") {
						if(r.getArrivalTime() <= 999) {
							System.out.print("time " + r.getArrivalTime() + "ms:" + " Process " + r.getProcessID() + " (tau " + r.getTimeGuess() + "ms) completed I/O; added to ready queue " );
							printQueueContents(Q);
						}
						
					}else {
						if(r.getArrivalTime() <= 999) {
							System.out.print("time " + r.getArrivalTime() + "ms: Process " + r.getProcessID() + " (tau " + r.getTimeGuess() + "ms) arrived; added to ready queue ");
							printQueueContents(Q);
						}
						
					}
					r.enterQueue(r.getArrivalTime());
					
				}
				//System.out.println(count);
				if(!Q.isEmpty()) {
					currentProcess = Q.poll();
				}
				else if(!arrivalQueue.isEmpty()) {
					currentProcess=arrivalQueue.peek();
					if(count<currentProcess.getArrivalTime())
						count = currentProcess.getArrivalTime();
					Q.add(arrivalQueue.poll());
					
					
					
					if(currentProcess.getState() == "BLOCKED") {
						if(count <= 999) {
							System.out.print("time " + currentProcess.getArrivalTime() + "ms:" + " Process " + currentProcess.getProcessID() + " (tau " + currentProcess.getTimeGuess() + "ms) completed I/O; added to ready queue " );
							printQueueContents(Q);
						}
						
					}else {
						if(count <= 999) {
							System.out.print("time " + currentProcess.getArrivalTime() + "ms: Process " + currentProcess.getProcessID() + " (tau " + currentProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
							printQueueContents(Q);
						}
						
					}
					
					currentProcess = Q.poll();
				}
			}
			else {
				newProcess = arrivalQueue.poll();
				Q.add(newProcess);

				if(newProcess.getState() == "BLOCKED") {
					
					if(count <= 999) {
						System.out.print("time " + count + "ms:" + " Process " + newProcess.getProcessID() + " (tau " + newProcess.getTimeGuess() + "ms) completed I/O; added to ready queue " );
						printQueueContents(Q);
					}
				}else {
					if(count <= 999) {
						System.out.print("time " + count + "ms: Process " + newProcess.getProcessID() + " (tau " + newProcess.getTimeGuess() + "ms) arrived; added to ready queue ");
						printQueueContents(Q);
					}
				}
				newProcess.enterQueue(count);
			}
		}
		System.out.println("time "+count+"ms: Simulator ended for SJF [Q <empty>]");
//		System.out.println("Arrival: ");
//		printQueueContents(arrivalQueue);
//		System.out.println("Ready: ");
//		printQueueContents(Q);
//		System.out.println("Completed: ");
////		printQueueContents(completedProcesses);
//		for(Process p : completedProcesses) {
//			System.out.println(p.getProcessID());
//		}
	}
	
	
	private void printQueueContents(PriorityQueue<Process> q) {
		PriorityQueue<Process> cp = new PriorityQueue<Process>(q);
		if(cp.isEmpty()) {
			System.out.println("[Q <empty>]");
			return;
		}
		System.out.print("[Q");
		while(!cp.isEmpty()){
			System.out.print(" " + cp.poll().getProcessID());			
		}
		System.out.println("]");
	}
	
	
	private double getAvgCPUBurst() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : arrivalQueue) {
			entries += currentProcess.getNumCPUBurstRecord();
			for(Integer time : currentProcess.getCPUBurst()) {
				total+=time;
			}
		}
		return total/entries;
	}
	
	private double getAvgWaitTime() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : completedProcesses) {
			entries += currentProcess.numCPUBurstRecord;
			for(double w:currentProcess.waitTime)
				total+=w;
		}
		return total/entries;
	}
	
	private double getAvgTurnaroundTime() {
		double total = 0;
		int entries=0;
		for(Process currentProcess : completedProcesses) {
			entries += currentProcess.numCPUBurstRecord;
			for(double w:currentProcess.turnaroundTime)
				total+=w;
		}
		return total/entries;
	}
	
	private int getTotalCW() {
		int total = 0;
		for(Process currentProcess : completedProcesses) {
			total+=currentProcess.numContextSwitch;
		}
		return total;
	}
	
	private int getTotalPreempt() {
		int total = 0;
		for(Process currentProcess : completedProcesses) {
			total+=currentProcess.numPreempt;
		}
		return total;
	}
	
	
	public String toString(){
		

		StringBuilder sb = new StringBuilder();
		
		
		// Actual content
		sb.append("Algorithm SJF\n");
		if(neverStarted) {
			sb.append("-- average CPU burst time: "+ String.format("%.3f",0.0));
			sb.append("\n");
			sb.append("-- average wait time: "+ String.format("%.3f",0.0));
			sb.append("\n");
			sb.append("-- average turnaround time: "+ String.format("%.3f",0.0));
			sb.append("\n");
			sb.append("-- total number of context switches: "+ 0.0);
			sb.append("\n");
			sb.append("-- total number of preemptions: "+ 0.0);
			return sb.toString();
		}
		
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

