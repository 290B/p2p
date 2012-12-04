package system;

import java.rmi.RemoteException;

import system.PeerImpl.GetRemoteQueue;

public class SendArgumentMessage extends Message{
	private static final long serialVersionUID = 1L; 
	String ID;
	Object returnValue;
	int returnArgumentNumner;
	public SendArgumentMessage(String ID, Object returnValue, int returnArgumentNumner){
		this.ID = ID;
		this.returnValue = returnValue;
		this.returnArgumentNumner = returnArgumentNumner;
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
			temp.args[returnArgumentNumner] =  returnValue;
			temp.joinCounter--;
			if (temp.joinCounter <= 0){
				peer.putReadyQ(temp);
				try {
					if (peer.remoteQ != null){
						peer.remoteQ.removeWaitTask(temp.ID);
					}
				} catch (RemoteException e) {
					GetRemoteQueue getRemoteQueue = peer.new GetRemoteQueue();
					getRemoteQueue.start();
					e.printStackTrace();
				}
				System.out.println("Task moved from waitMap to readyQ. ID: " + temp.ID);
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
