package messages;

import system.PeerImpl;
import system.Task;
import system.Task.Shared;

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
