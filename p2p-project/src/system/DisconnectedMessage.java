package system;
public class DisconnectedMessage extends Message{
	private static final long serialVersionUID = 1L;
	private Peer disconnectedPeer;
	
	public DisconnectedMessage(Peer peer){
		this.disconnectedPeer = peer;
	}
	public void action(PeerImpl peer) {
		if (peer.peers.contains(disconnectedPeer)){
			peer.peers.remove(disconnectedPeer);
		}
	}
}
