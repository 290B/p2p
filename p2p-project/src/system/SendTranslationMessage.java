package system;

import java.util.UUID;

public class SendTranslationMessage extends Message{
	private static final long serialVersionUID = 1L;
	UUID oldPeer;
	UUID newPeer;
	
	public SendTranslationMessage(UUID oldPeer, UUID newPeer){
		this.oldPeer = oldPeer;
		this.newPeer = newPeer;
	}
	public void action(PeerImpl peer) {
		peer.translations.put(oldPeer, newPeer);
		System.out.println("Recieved a translation from " + oldPeer + " to " + newPeer);
	}
}
