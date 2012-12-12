package messages;

import api.Task;
import api.Task.Shared;
import system.PeerImpl;

public class SetSharedVarMessage extends Message{
	private static final long serialVersionUID = 1L;
	public Shared shared;
	public SetSharedVarMessage(Shared shared){
		this.shared = shared;
	}

	public void action(PeerImpl peer) {
		peer.setSharedFromRemote(shared);
	}
}
