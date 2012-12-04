package system;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class GetRemoteQueueMessage extends Message {
	private static final long serialVersionUID = 1L;
	private UUID sender;
	
	GetRemoteQueueMessage(UUID sender){
		this.sender = sender;
	}
	public void action(PeerImpl peer){
		if (peer.hostedQueues.size() < 2){
			try {
				RemoteQueueImpl rq = new RemoteQueueImpl();
				RemoteQueue stub = (RemoteQueue) UnicastRemoteObject.exportObject((RemoteQueue)rq, 0);
				if(peer.peerMap.get(sender).registerQueue(stub)){
					peer.hostedQueues.put(sender, rq);
				};
			} catch (RemoteException e) {
				System.out.println("Error while registering remote queue");
				e.printStackTrace();
			}
		}
	}
}
