package messages;

import java.rmi.RemoteException;
import java.util.UUID;

import system.PeerImpl;
import system.Task;


// TODO: create a message for returning tasks
public class GetTaskMessage extends Message {
	UUID sender;
	
	private static final long serialVersionUID = 1L;

	public GetTaskMessage(UUID sender){
		this.sender = sender;
	}
	public void action(PeerImpl peer) {
		if (peer.peerMap.containsKey(sender)){
			if (peer.readyQ.size() > 2){
				Task task = peer.takeTask();
				if (task != null){
					try { 
						peer.peerMap.get(sender).giveTask(task);
					} catch (RemoteException e) {
						peer.putReadyQ(task);
						System.out.println("Failed to give task");
						e.printStackTrace();
					}
				}
			}
		}
	}

}
