package system;

import java.rmi.RemoteException;
import java.util.UUID;

public class GetPartnerMessage extends Message{
	private UUID sender;
	
	private static final long serialVersionUID = 1L;
	public GetPartnerMessage(UUID sender){
		this.sender = sender;
	}

	public void action(PeerImpl peer) {
		if (peer.second_partner == null){
			try {
				if (peer.peerMap.get(sender).registerPartner(peer.peerID)){
					System.out.println("Got a new partner  " + sender);
					if (peer.first_partner == null){
						peer.first_partner = sender;
					}else{
						peer.second_partner = sender;
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
