package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;


public interface Peer extends Remote {
	public void putTask(Task t) throws RemoteException;
	public void giveTask(Task t) throws RemoteException;
	public Object getResult() throws RemoteException;
	public void message(Message msg) throws RemoteException;
	public Map<UUID, Peer> getPeerMap() throws RemoteException;
	public boolean registerQueue(RemoteQueue rq, UUID remoteQueueHost) throws RemoteException;
}
