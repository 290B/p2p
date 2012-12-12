package messages;

import system.PeerImpl;

public class HelloMessage extends Message {
	private static final long serialVersionUID = 1L;
	public HelloMessage(){
		
	}
	public void action(PeerImpl peer) {
		System.out.println("Hello!");
	}

}
