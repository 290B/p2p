package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;

import api.Task;

import messages.Message;


public interface Peer extends Remote {
	public void putTask(Task t) throws RemoteException;
	public void giveTask(Task t) throws RemoteException;
	public Object getResult() throws RemoteException;
	public void message(Message msg) throws RemoteException;
	public Map<UUID, Peer> getPeerMap() throws RemoteException;
	public Map<UUID, UUID> getTranslations() throws RemoteException;
	public boolean registerQueue(RemoteQueue rq, UUID remoteQueueHost) throws RemoteException;
	public boolean returnArgument(String taskID, Object returnValue, int returnArgumentNumber) throws RemoteException;
	
}
