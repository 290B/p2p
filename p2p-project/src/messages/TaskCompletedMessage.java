package messages;

import java.util.Map;
import java.util.UUID;

import system.PeerImpl;
import system.RemoteQueueImpl;

public class TaskCompletedMessage extends Message{
	
	private static final long serialVersionUID = 1L;

	public void action(PeerImpl peer) {
		peer.readyQ.clear();
		peer.waitMap.clear();
		for(Map.Entry<UUID, RemoteQueueImpl> temp : peer.hostedQueues.entrySet()){
			temp.getValue().taskMap.clear();
			temp.getValue().waitMap.clear();
		} 
		peer.shared = null;
		System.out.println("Job completed!");
		
	}
}
