package system;

public class PingRandomMessage extends Message{
	private static final long serialVersionUID = 1L;
	PingRandomMessage(){
		
	}
	public void action(PeerImpl peer) {
		System.out.println("Ping!");
		
	}
	
}
