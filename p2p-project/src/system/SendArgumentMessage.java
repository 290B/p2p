package system;

public class SendArgumentMessage extends Message{
	private static final long serialVersionUID = 1L;
	Peer peer; 
	String ID;
	Object returnValue;
	int returnArgumentNumner;
	public SendArgumentMessage(Peer peer, String ID, Object returnValue, int returnArgumentNumner){
		this.peer = peer;
		this.ID = ID;
		this.returnValue = returnValue;
		this.returnArgumentNumner = returnArgumentNumner;
	}
	
	public void action(PeerImpl peer) {
		if (peer.waitMap.containsKey(ID)){
			Task temp = peer.waitMap.remove(ID);
			temp.args[returnArgumentNumner] =  returnValue;
			temp.joinCounter--;
			if (temp.joinCounter <= 0){
				peer.putReadyQ(temp);
			}else{
				peer.waitMap.put(ID, temp);
			}
			
		}
	}
}
