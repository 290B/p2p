package messages;

import java.util.UUID;

import system.Peer;
import system.PeerImpl;

public class EntryMessage extends Message{
	private static final long serialVersionUID = 1L;
	private Peer newPeer;
	private UUID id;
	public EntryMessage(UUID id, Peer peer){
		this.newPeer = peer;
		this.id = id;
	}
	
	public void action(PeerImpl peer) {
			peer.addPeer(id, newPeer);
			System.out.println("New peer added");
	}
}
