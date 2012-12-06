package system;

import java.util.Map;
import java.util.UUID;

public class TaskCompletedMessage extends Message{
	
	private static final long serialVersionUID = 1L;

	public void action(PeerImpl peer) {
		int wms = peer.waitMap.size();
		int rqs = peer.readyQ.size();
		peer.readyQ.clear();
		peer.waitMap.clear();
		for(Map.Entry<UUID, RemoteQueueImpl> temp : peer.hostedQueues.entrySet()){
			temp.getValue().taskMap.clear();
			temp.getValue().waitMap.clear();
		} 
		peer.shared = null;
		System.out.println("Task completed! duplicates deleted: " + rqs + "tasks, " + wms + " wait tasks");
		
	}
}
