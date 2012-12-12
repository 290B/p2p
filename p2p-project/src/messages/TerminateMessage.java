package messages;

import system.PeerImpl;

public class TerminateMessage extends Message {
	private static final long serialVersionUID = 1L;

	public TerminateMessage(){
		
	}
	public void action(PeerImpl peer) {
		System.exit(0);
	}

}
