package system;

import java.rmi.Remote;
import java.rmi.RemoteException;

import api.Task;

public interface RemoteQueue extends Remote{
	public void putWaitTask(Task t) throws RemoteException;
	public void removeWaitTask(String id) throws RemoteException;
	//public void backup(Map<String, Task> waitMap, BlockingDeque<Task> readyQ) throws RemoteException;
	
	public void putTask(Task t) throws RemoteException;
	public void removeTask(String id) throws RemoteException;
}
