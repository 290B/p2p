package system;

import java.rmi.RemoteException;

public class SendArgumentMessage extends Message{
	private static final long serialVersionUID = 1L; 
	String ID;
	Object returnValue;
	int returnArgumentNumber;
	public SendArgumentMessage(String ID, Object returnValue, int returnArgumentNumber){
		this.ID = ID;
		this.returnValue = returnValue;
		this.returnArgumentNumber = returnArgumentNumber;
	}
	
	public void action(PeerImpl peer) {
		//System.out.println("Argument recieved to ID: " + ID);
		if (ID.equals("0")){
			peer.putResult(returnValue);
			Message msg = new TaskCompletedMessage();
			msg.broadcast(peer, true);
			return;
		}
		if (peer.waitMap.containsKey(ID)){
			Task temp = peer.waitMap.remove(ID);
			temp.args[returnArgumentNumber] =  returnValue;
			temp.joinCounter--;
			if (temp.joinCounter <= 0){
				peer.putReadyQ(temp);
				if (peer.remoteQ != null){
					if (peer.peerMap.containsKey(peer.remoteQueueHost)){
						try {
							peer.remoteQ.removeWaitTask(temp.ID);
						} catch (RemoteException e) {
							peer.hasDisconnected(peer.remoteQueueHost);
							e.printStackTrace();
						}
					}
				}
				//System.out.println("Task moved from waitMap to readyQ. ID: " + temp.ID);
			}else{
				peer.waitMap.put(ID, temp);
			}
		}else{
			System.out.println("FATAL error! Received argument for task that is not in the waitmap");
			System.out.println("The task ID was: " + ID);
			peer.messageQ(this);
			//System.exit(0);
		}
	}
}
