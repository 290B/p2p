package system;

public class StringMessage extends Message{
	private static final long serialVersionUID = 1L;
	private String string;
	StringMessage(String string){
		this.string = string;
	}
	
	public void action(PeerImpl peer) {
		System.out.println(string);
		return;
	}
}
