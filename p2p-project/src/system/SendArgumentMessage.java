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
		System.out.println("Argument recieved to ID: " + ID);
		if (ID.equals("0")){
			peer.putResult(returnValue);
			System.out.println("Task completed");
			
			return;
		}
		if (peer.waitMap.containsKey(ID)){
			Task temp = peer.waitMap.remove(ID);
			temp.args[returnArgumentNumner] =  returnValue;
			temp.joinCounter--;
			if (temp.joinCounter <= 0){
				peer.putReadyQ(temp);
				System.out.println("Task moved from waitMap to readyQ");
			}else{
				peer.waitMap.put(ID, temp);
			}
			
		}
	}
}
