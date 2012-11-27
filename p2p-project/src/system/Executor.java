package system;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Executor extends Thread{
	PeerImpl peer;
	String taskID;
	
	public Executor(PeerImpl peer){
		this.peer = peer;
	}
	public void run(){
		while(true){
			Task t = peer.takeTask();
			System.out.print("Executing task... ");
			t.execute();
			System.out.println("Done");
			
			if (t.spawn_next != null){
				String next_id = t.ID + "-0";
				t.spawn_next.ID = next_id;
				t.spawn_next.joinCounter = t.spawned.size();
				t.spawn_next.args = new Object[t.spawned.size()];
				peer.putWaitMap(t.spawn_next);
				int spawned_counter = 0;
				for (Task temp : t.spawned){
					temp.ID = t.ID + "-" + String.valueOf(spawned_counter+1);
					temp.returnID = next_id;
					temp.returnArgumentNumber = spawned_counter;
					spawned_counter++;
					peer.putTask(temp);
				}
			}
			if (t.send_argument != null){
				peer.placeArgument(t.creator , t.returnID, t.send_argument, t.returnArgumentNumber);
			}
		}
	}
}
