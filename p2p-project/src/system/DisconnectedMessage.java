package system;

import java.util.UUID;

public class DisconnectedMessage extends Message{
	private static final long serialVersionUID = 1L;
	private UUID disconnectedPeer;
	
	public DisconnectedMessage(UUID id){
		this.disconnectedPeer = id;
	}
	public void action(PeerImpl peer) {
			peer.removePeer(disconnectedPeer);
	}
}
