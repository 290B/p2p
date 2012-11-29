package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface Peer extends Remote {
	public void putTask(Task t) throws RemoteException;
	public void giveTask(Task t) throws RemoteException;
	public Object getResult() throws RemoteException;
	public ArrayList<Peer> getPeers() throws RemoteException;
	public void message(Message msg) throws RemoteException;
	
}
