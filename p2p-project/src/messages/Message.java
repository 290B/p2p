package messages;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

import system.PeerImpl;

public abstract class Message implements Cloneable, Serializable{
	private static final long serialVersionUID = 1L;
	
	public abstract void action(PeerImpl peer); // Input: this. Responds to message. Can forward message with send() and broadcast() with broadcast.
	
	public void broadcast(PeerImpl sender, boolean toSelf){
			ArrayList<UUID> disconnected = null;
			Object[] temp = sender.keys.toArray();
			for (int i = 0; i < temp.length; i ++){
			//for (UUID temp: sender.keys){
				try {
					if ( !temp[i].equals(sender.peerID) || toSelf){
						sender.peerMap.get(temp[i]).message((Message)this.clone());
					}
				} catch (RemoteException e) {
					System.out.println("ERROR Message: unable to broadcast ");
					if (disconnected == null){
						disconnected = new ArrayList<UUID>();
					}
					disconnected.add((UUID) temp[i]);
					
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
	public boolean send(PeerImpl sender, UUID receiver){
		try {
			sender.peerMap.get(receiver).message((Message)this.clone());
			return true;
		} catch (RemoteException e) {
			System.out.println("Failed to contact peer");
			sender.hasDisconnected(receiver);
			return false;
		} catch (CloneNotSupportedException e) {
			
			System.out.println("ERROR Message: unable to clone message");
			e.printStackTrace();
			return false;
		}
	}
}
