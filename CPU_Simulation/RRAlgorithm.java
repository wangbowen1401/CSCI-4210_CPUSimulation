import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;


public class RRAlgorithm{
    private PriorityQueue<Process> arrival;
    private PriorityQueue<Process> arrivalRecord;
    private ArrayList<Process> done;
    private int cw;
    private double ave_cpu_burst;
    private String begin_or_end;
    private int t_slice;
    private boolean full = false;
    private LinkedList<Process> rq ;
    
    public RRAlgorithm(RandomSequence arrival, int cw, int t_sl, String front_or_end)
    {
        arrivalRecord=arrival.getSequence();
        this.t_slice = t_sl;
        this.begin_or_end = front_or_end;
        this.arrival = arrival.getSequence();
        this.ave_cpu_burst = this.getAvgCPUBurst();
        this.cw=cw;
        done = new ArrayList<Process>();    
    }

    public void simulate() {
        this.rq = new LinkedList<Process>();
        
        
        PriorityQueue<Process> copy = new PriorityQueue<>(arrivalRecord);
        PriorityQueue<Process> print = new PriorityQueue<>(new AlphaComparator());
        
        while(!copy.isEmpty())
        {
            print.add(copy.poll());
        }
        while(!print.isEmpty())
        {
            Process p = print.poll();
            if(p.getNumBurst() ==1)
            {
                System.out.println("Process " + p.getProcessID() + " [NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU burst");

            }
            else
            {
                System.out.println("Process " + p.getProcessID() + " [NEW] (arrival time " + p.getArrivalTime() + " ms) " + p.getNumBurst() + " CPU bursts");
            }
        }
        
        System.out.print("time 0ms: Simulator started for RR " + printQueueContents(this.rq));
        // Making sure n != 0
        if(arrival.size()==0)
        {
            System.out.print("time 0ms: Simulator ended for RR " + printQueueContents(this.rq));
            return;
        }
        Process p = arrival.poll();
        int count = p.getArrivalTime();
        int past = 0;
        this.rq.add(p);
        System.out.print("time "+count+"ms: Process "+p.getProcessID()+" arrived; added to ready queue "+printQueueContents(this.rq));
        p = this.rq.poll();
        while(!arrival.isEmpty()||!this.rq.isEmpty()||p.getNumBurst()!=0)
        {
            // Add all the process with the same arrival time
            //check if any processes have arrived
            while(arrival.size()>0&&count>=arrival.peek().getArrivalTime())
            {
                Process newProcess;
                newProcess = arrival.poll();
                newProcess.enterQueue(newProcess.getArrivalTime());
                 add_to_ready_queue(this.rq, newProcess,this.begin_or_end);//deals with beginning / end of queue issue
                 
                 if(newProcess.getState()!="BLOCKED"&&(count<=999 || this.full == true))
                {
                    System.out.print("time "+count+"ms: Process "+p.getProcessID()+" arrived; added to ready queue "+printQueueContents(this.rq));
                }
                else if((count<=999 || this.full == true))
                {
                    System.out.print("time "+count+"ms: Process "+p.getProcessID()+" completed I/O; added to ready queue "+printQueueContents(this.rq));
                }
            }
            
            // The Process enters CPU
            if(p.getState()!="RUNNING") {
                p.enterCPU(count);
                count+=cw/2;
                while(arrival.size()!=0&&arrival.peek().getArrivalTime()<count)
                {
                    addNewProcess();
                }
                if(count<=999 || this.full == true)
                {
                    if(p.burstTimeGuess==p.burstTimeGuessRecord)
                    {
                        System.out.print("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU for "+p.getRemainingTime()+"ms burst "+printQueueContents(this.rq));
                    }
                    else
                    {
                        System.out.print("time "+count+"ms: Process "+p.getProcessID()+" started using the CPU with "+p.getRemainingTime()+"ms remaining "+printQueueContents(this.rq));    
                    }
                }
                
            }
            
            // Check the time slice versus remaining time of current Process
            int running = p.getRemainingTime()+p.getEnterTime();
            int expire = this.t_slice+count-past;
            int peek = Integer.MAX_VALUE;
            
            if((arrival.size()>0))
            {
                peek = arrival.peek().getArrivalTime();
                
                if(peek<=expire && expire < running)
                {
                    past = peek-count+past;
                }
                
            }
            

            
            int maybe = Math.min(running, expire);
 
            count = Math.min(maybe, peek);

            // The current process will finish before or when the time slice expires
            if(count==running) // processes will only complete in this statement
            {
//                printQueueContents(rq);
                past = 0;
                p.complete(count);
                // Still more cpu bursts left
                if(p.getState()!="COMPLETE")
                {
                    p.resetEnterTime();
                    if(count<=999 || this.full == true)
                    {    
                        if(p.getNumBurst()==1)
                        {    System.out.print("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU burst; "+p.getNumBurst()+" burst to go "+printQueueContents(rq));}
                        else
                        {    System.out.print("time "+count+"ms: Process "+p.getProcessID()+ " completed a CPU burst; "+p.getNumBurst()+" bursts to go "+printQueueContents(rq));}
                    
                        System.out.print("time "+count+"ms: Process "+p.getProcessID()+" switching out of CPU; will block on I/O until time "+p.getArrivalTime()+"ms "+printQueueContents(this.rq));
                    }
//                    System.out.println(count+","+cw/2);
                    
                    arrival.add(p);
                    
                }
                // Completed all the cpu and io bursts, added to arrayList for analysis
                else
                {
                    System.out.print("time "+count+"ms: Process "+p.getProcessID()+" terminated "+ printQueueContents(this.rq));
                    done.add(p);
                }
                // Move onto the next Process in the ready queue because new Process didn't arrive yet.
                count+=cw/2; // Add context switch to move the Process out
//                System.out.println(count+","+cw/2);

                if(rq.size()!=0)
                {
                    p = rq.poll();
                }
                else if(arrival.size()!=0)
                {
                    
                    
                    p=arrival.poll();
                    if(count <= p.getArrivalTime())
                    {
                        count = p.getArrivalTime();

                    }
                    
                    rq.add(p);                
                    // Print the process arrival statements
                    if(p.getState()!="BLOCKED"&& (count<= 999 || full == true))
                    {    System.out.print("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" arrived; added to ready queue "+printQueueContents(rq));}
                    else if((count<= 999 || full == true))
                    {    System.out.print("time "+p.getArrivalTime()+"ms: Process "+p.getProcessID()+" completed I/O; added to ready queue "+printQueueContents(rq));}
                    
                    p.enterQueue(count);
                    p=rq.poll();
                    
                    // Add any new process with same arrival time
                    while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime())
                    {    addNewProcess();}
                    
                }
//                printQueueContents(rq);
                past = 0;
            }
            else if(count == peek && peek != expire)
            {
 
                while(arrival.size()!=0&&arrival.peek().getArrivalTime()==count)
                {    addNewProcess();}
            }
            else if(count == expire)// Process finishes after timeslice
            {

                
//                printQueueContents(rq);
                if(rq.size()==0) //keep process on cpu, no context switch
                {
                    if(count<=999 || this.full == true)
                    {
                        System.out.println("time "+count+"ms: Time slice expired; no preemption because ready queue is empty [Q <empty>]");
                    }

                }
                else //move stuff off and on queue
                {
                    if(count==peek)
                    {
                        while(arrival.size()!=0&&arrival.peek().getArrivalTime()==count)
                            addNewProcess();    
                    }
                    p.enterQueue(count);
                    if (count<=999 || this.full == true)
                    {    
                        System.out.print("time "+count+"ms: Time slice expired; process "+ p.getProcessID() +" preempted with "+p.getRemainingTime()+"ms to go "+printQueueContents(this.rq));
                    }

                    count+=cw/2;
//                    System.out.print("time "+count+"ms: Process "+p.getProcessID()+" arrived;added to ready queue "+printQueueContents(this.rq));
                     add_to_ready_queue(this.rq, p, "END");//Is end because it didn't just arrive.
                    while(arrival.size()!=0&&arrival.peek().getArrivalTime()==p.getArrivalTime())
                    {
                        addNewProcess();
                    }
                    p=rq.poll();
                 }
//                printQueueContents(rq);
                past = 0;
            }
            else
            {
                System.out.println("it should be impossible to print this");
            }
        }
        System.out.println("time "+count+"ms: Simulator ended for RR [Q <empty>]");
    }
    
    private void addNewProcess()
    {
        Process newProcess = arrival.poll();
//        System.out.println(newProcess.getState());
//        System.out.println("here3");
        add_to_ready_queue(this.rq, newProcess,this.begin_or_end);//deals with beginning/ end of queue issue
        if(newProcess.getState()!="BLOCKED"&&newProcess.getArrivalTime()!=-1)
        {
            if(newProcess.getArrivalTime()<=999 || this.full)
            {
                System.out.print("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" arrived; added to ready queue "+printQueueContents(rq));
            }
        }
        else if(newProcess.getArrivalTime()!=-1)
        {
            if(newProcess.getArrivalTime()<=999 || this.full)
            {
            System.out.print("time "+newProcess.getArrivalTime()+"ms: Process "+newProcess.getProcessID()+" completed I/O; added to ready queue "+printQueueContents(rq));
            }
        }
        newProcess.enterQueue(newProcess.getArrivalTime());
    }
    
    private void add_to_ready_queue(LinkedList<Process> queue, Process process, String f_or_b )
    {
        if(f_or_b== "BEGINNING")
        {
            queue.add(0,process);
        }
        else
        {
            queue.add(process);
        }
    }

    
    private String printQueueContents(LinkedList<Process> q) {
        StringBuilder sb = new StringBuilder();
        LinkedList<Process> cp = new LinkedList<Process>(q);
        if(cp.isEmpty()) {
            sb.append("[Q <empty>]\n");
            return sb.toString();
        }
        sb.append("[Q");
        while(!cp.isEmpty()){
            sb.append(" " + cp.poll().getProcessID());            
        }
        sb.append("]\n");
        return sb.toString();
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
        sb.append("Algorithm RR\n");
        
        // Values that need to be calculated
        double avgCPUBurst = this.ave_cpu_burst;
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

