package system;

import java.util.UUID;

public class EntryMessage extends Message{
	private static final long serialVersionUID = 1L;
	private Peer newPeer;
	private UUID id;
	EntryMessage(UUID id, Peer peer){
		this.newPeer = peer;
		this.id = id;
	}
	
	public void action(PeerImpl peer) {
			peer.addPeer(id, newPeer);
			System.out.println("New peer added");
	}
}
