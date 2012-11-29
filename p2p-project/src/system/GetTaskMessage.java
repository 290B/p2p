package system;

import java.rmi.RemoteException;

public class GetTaskMessage extends Message {
	Peer sender;
	
	private static final long serialVersionUID = 1L;

	public GetTaskMessage(Peer sender){
		this.sender = sender;
	}
	public void action(PeerImpl peer) {
		if (peer.readyQ.size() > 2){
			try {
				sender.giveTask(peer.takeTask());
			} catch (RemoteException e) {
				System.out.println("Failed to give task");
				e.printStackTrace();
			}
		}
	}

}
