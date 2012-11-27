package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface Peer extends Remote {
	public void putTask(Task t) throws RemoteException;
	public ArrayList<Peer> getPeers() throws RemoteException;
	public void message(Message msg) throws RemoteException;
}
