package system;

import system.Task.Shared;

public class SetSharedVarMessage extends Message{
	private static final long serialVersionUID = 1L;
	public Shared shared;
	SetSharedVarMessage(Shared shared){
		this.shared = shared;
	}

	public void action(PeerImpl peer) {
		System.out.println("Got remote shared object");
		peer.setSharedFromRemote(shared);
	}
}
