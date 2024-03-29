package system;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import api.Task;

public class RemoteQueueImpl implements RemoteQueue{
	public UUID user;
	public Map<String, Task> waitMap = new ConcurrentHashMap<String , Task>();
	public Map<String, Task> taskMap = new ConcurrentHashMap<String , Task>();
	
	
	public void putWaitTask(Task t) throws RemoteException {
		if (waitMap.containsKey(t.ID)){
			waitMap.remove(t.ID);
		}
		
		waitMap.put(t.ID, t);
	}
	
	public void removeWaitTask(String id) throws RemoteException {
		waitMap.remove(id);
	}
	
	public void putTask(Task t) throws RemoteException {
		if (taskMap.containsKey(t.ID)){
			taskMap.remove(t.ID);
		}
		taskMap.put(t.ID, t);
	}
	
	public void removeTask(String id) throws RemoteException {
		taskMap.remove(id);
	}
	
	
	public boolean isEmpty(){
		return taskMap.isEmpty();
	}


//	public void backup(Map<String, Task> waitMap, BlockingDeque<Task> clonedReadyQ) throws RemoteException {
//		this.waitMap.putAll(waitMap);
//		while(!clonedReadyQ.isEmpty()){
//			try {
//				Task t = clonedReadyQ.take();
//				taskMap.put(t.ID, t);
//			} catch (InterruptedException e) {
//				System.out.println("ERROR in RemoteQueueImpl, unable to take from cloned queue");
//				e.printStackTrace();
//			}
//		}
//	}
}
