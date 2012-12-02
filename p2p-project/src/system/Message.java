package system;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public abstract class Message implements Cloneable, Serializable{
	private static final long serialVersionUID = 1L;
	
	public abstract void action(PeerImpl peer); // Input: this. Responds to message. Can forward message with send() and broadcast() with broadcast.
	
	public void broadcast(PeerImpl sender, boolean toSelf){
			ArrayList<UUID> disconnected = null;
			for (UUID temp: sender.keys){
				try {
					if (sender.peerID != temp || toSelf)
					sender.peerMap.get(temp).message((Message)this.clone());
				} catch (RemoteException e) {
					System.out.println("ERROR Message: unable to broadcast ");
					if (disconnected == null){
						disconnected = new ArrayList<UUID>();
					}
					disconnected.add(temp);
					
					//e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					System.out.println("ERROR Message: unable to clone message");
					e.printStackTrace();
				}
			}
			if (disconnected != null){
				for (UUID dc: disconnected){
						sender.removePeer(dc);
						sender.hasDisconnected(dc);
				}
			}
	};
	public void send(PeerImpl sender, UUID receiver){
		try {
			sender.peerMap.get(receiver).message((Message)this.clone());
		} catch (RemoteException e) {
			System.out.println("Failed to contact peer");
			sender.removePeer(receiver);
			sender.hasDisconnected(receiver);
		} catch (CloneNotSupportedException e) {
			System.out.println("ERROR Message: unable to clone message");
			e.printStackTrace();
		}
	}
}
