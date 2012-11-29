package system;

import tasks.FibTask;
import tasks.FibTask.Fib;

public class FibClient extends Thread {
	PeerImpl peer;
	public FibClient(PeerImpl peer){
		this.peer = peer;
	}
	
	public void run(){
		FibTask fibTask = new FibTask();
		FibTask.Fib fib = fibTask.new Fib(20);

		
		int tries = 1;
    	int doesntCount = 0;
		int total = 0;	    	
		
		int result = 0;
		
		for (int i = 0; i < tries; i++){
			long start = System.currentTimeMillis();

    		peer.putTask(fib);
    		result = (Integer) peer.getResult();
    		
			long stop = System.currentTimeMillis();
			System.out.println("mandel, " + (i+1) +" try: " +(stop-start) +" milliseconds");
			if (i >= doesntCount){
				total += (stop-start);		
			}
		}
		System.out.println("Average time: " + total/(tries-doesntCount));

		System.out.println("The result is: " + result);
	}
}
