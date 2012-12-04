package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

public interface RemoteQueue extends Remote{
	public void putWaitTask(Task t) throws RemoteException;
	public void removeWaitTask(Task t) throws RemoteException;
	//public void backup(Map<String, Task> waitMap, BlockingDeque<Task> readyQ) throws RemoteException;
	
	public void putTask(Task t) throws RemoteException;
	public void removeTask(Task t) throws RemoteException;
}
