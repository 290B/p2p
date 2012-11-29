package system;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Message implements Cloneable, Serializable{
	private static final long serialVersionUID = 1L;
	
	public abstract void action(PeerImpl peer); // Input: this. Responds to message. Can forward message with send() and broadcast() with broadcast.
	
	public void broadcast(PeerImpl sender, boolean toSelf){
		if (sender.peers == null){
			return;
			
		}else{
			ArrayList<Peer> disconnected = null;
			for (Peer temp: sender.peers){
				try {
					if (sender != temp || toSelf)
					temp.message((Message)this.clone());
				} catch (RemoteException e) {
					System.out.println("ERROR Message: unable to broadcast ");
					if (disconnected == null){
						disconnected = new ArrayList<Peer>();
					}
					disconnected.add(temp);
					
					//e.printStackTrace();
				} catch (CloneNotSupportedException e) {
					System.out.println("ERROR Message: unable to clone message");
					e.printStackTrace();
				}
			}
			if (disconnected != null){
				for (Peer dc: disconnected){
					if (sender.peers.contains(dc)){
						sender.peers.remove(dc);
					}
					sender.hasDisconnected(dc);
				}
			}
		
		}
	};
	public void send(PeerImpl sender, Peer receiver){
		try {
			receiver.message((Message)this.clone());
		} catch (RemoteException e) {
			System.out.println("ERROR Message: unable to send message");
			if (sender.peers.contains(receiver)){
				sender.peers.remove(receiver);
				sender.hasDisconnected(receiver);
			}
			

			//e.printStackTrace();
		} catch (CloneNotSupportedException e) {
			System.out.println("ERROR Message: unable to clone message");
			e.printStackTrace();
		}
	}
}
