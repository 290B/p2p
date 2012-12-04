package system;

import tasks.FibTask;
import tasks.FibTask.Fib;

public class FibClient extends Thread {
	PeerImpl peer;
	public FibClient(PeerImpl peer){
		this.peer = peer;
	}
	
	public void run(){
		

		
		int tries = 20;
    	int doesntCount = 0;
		int total = 0;	    	
		
		int result = 0;
		
		for (int i = 0; i < tries; i++){
			long start = System.currentTimeMillis();
			FibTask fibTask = new FibTask();
			FibTask.Fib fib = fibTask.new Fib(20);
    		peer.putTask(fib);
    		result = (Integer) peer.getResult();
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
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
