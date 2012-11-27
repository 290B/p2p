package system;
import java.rmi.RemoteException;

import tasks.*;

public class ClientImpl extends Thread{
	String task_string;
	Peer peer;
		
	
	ClientImpl(Peer peer, String string_task){
		task_string = string_task;
		this.peer = peer;
	}
	
	public void run(){
		if (task_string.equals("mandelbrot")){
			mandelbrotClient();
		}
	}
	
	private void mandelbrotClient(){
		int N_PIXELS = 1024;
		int ITERATION_LIMIT = 4096;
		double CORNER_X = -0.7510975859375;
		double CORNER_Y = 0.1315680625;
		double EDGE_LENGTH = 0.01611;
		int DEPTH = 3;
		
		MandelbrotSetTask temp = new MandelbrotSetTask();
		
		Task split = temp.new Split(CORNER_X, CORNER_Y, EDGE_LENGTH, N_PIXELS, ITERATION_LIMIT, DEPTH);
		
		try {
			peer.putTask(split);
			
			
			// TODO : wait for task to finnish and take, then print result
		} catch (RemoteException e) {
			System.out.println("ERROR: mandelbrotclient (), could not put task");
			e.printStackTrace();
		}
	}
}
