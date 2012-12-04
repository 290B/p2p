package system;

import java.util.UUID;

import system.PeerImpl.GetPartner;

public class DisconnectedMessage extends Message{
	private static final long serialVersionUID = 1L;
	private UUID disconnectedPeer;
	
	public DisconnectedMessage(UUID id){
		this.disconnectedPeer = id;
	}
	public void action(PeerImpl peer) {
			peer.removePeer(disconnectedPeer);
			if (disconnectedPeer.equals(peer.first_partner)){
				System.out.println("Fisrt partner disconnected");
				peer.first_partner = null;
				if (peer.second_partner != null){
					peer.first_partner = peer.second_partner;
					peer.second_partner = null;
					System.out.println("Replaced second to first");
				}else{
					GetPartner gp = peer.new GetPartner();
					gp.start();
				}
			}
			if (disconnectedPeer.equals(peer.second_partner)){
				System.out.println("Second partner disconnected");
				peer.second_partner = null;
			}
	}
}
