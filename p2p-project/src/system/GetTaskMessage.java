package system;

import java.rmi.RemoteException;


// TODO: create a message for returning tasks
public class GetTaskMessage extends Message {
	Peer sender;
	
	private static final long serialVersionUID = 1L;

	public GetTaskMessage(Peer sender){
		this.sender = sender;
	}
	public void action(PeerImpl peer) {
		if (peer.readyQ.size() > 2){
			Task task = peer.takeTask();
			if (task != null){
				try {
					sender.giveTask(task);
				} catch (RemoteException e) {
					peer.putReadyQ(task);
					System.out.println("Failed to give task");
					e.printStackTrace();
				}
			}
		}
	}

}
