package system;

import java.rmi.RemoteException;

public class EntryMessage extends Message{
	private static final long serialVersionUID = 1L;
	private Peer newPeer;
	EntryMessage(Peer peer){
		this.newPeer = peer;
	}
	
	public void action(PeerImpl peer) {
		if (!peer.peers.contains(newPeer)){
			peer.peers.add(newPeer);
			System.out.println("A new peer connected");
		}
	}
}
