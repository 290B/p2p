package messages;

import java.util.Map;
import java.util.UUID;

import api.Task;

import system.PeerImpl;
import system.RemoteQueueImpl;
import system.PeerImpl.GetRemoteQueue;

public class DisconnectedMessage extends Message{
	private static final long serialVersionUID = 1L;
	private UUID disconnectedPeer;
	
	public DisconnectedMessage(UUID id){
		this.disconnectedPeer = id;
	}
	public void action(PeerImpl peer) {
			peer.removePeer(disconnectedPeer);
			if (peer.hostedQueues.containsKey(disconnectedPeer)){
				RemoteQueueImpl rq = peer.hostedQueues.remove(disconnectedPeer);
				int count = 0;
				for (Map.Entry<String, Task> temp : rq.taskMap.entrySet()){
					peer.putReadyQ(temp.getValue());
					count++;
				}
				System.out.println("Tasks copied from backup to readyQ:  " + count);
				count = 0;
				for (Map.Entry<String, Task> temp : rq.waitMap.entrySet()){
					
					peer.putWaitMap(temp.getValue());
					count++;
				}
				System.out.println("Wait tasks copied from backup to waitMap: " + count);
				System.out.println("Sending translation message");
				Message msg = new SendTranslationMessage(disconnectedPeer, peer.peerID);
				msg.broadcast(peer, true); 
			}
			if (peer.remoteQ != null){
				if (peer.remoteQueueHost.equals(disconnectedPeer)){
					GetRemoteQueue getRemoteQueue = peer.new GetRemoteQueue();
					getRemoteQueue.start();
				}
			}
	}
}
