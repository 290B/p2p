package system;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import system.Executor.TaskID;

public class PeerImpl implements Peer {
	static PeerImpl peer;
	
	static final BlockingDeque<Message> messages = new LinkedBlockingDeque<Message>();
	public ArrayList<Peer> peers = new ArrayList<Peer>();
	
	
	// Compute stuff: 
	private static final Map<TaskID, Task> waitMap = new ConcurrentHashMap<TaskID , Task>();
	private static final BlockingDeque<Task> readyQ = new LinkedBlockingDeque<Task>();
	
	public static void main(String[] args) {
		if (args.length == 0){
			System.out.println("Missing initPeer argument");
			return;
		}
		
		try {
			if (System.getSecurityManager() == null ) { 
				System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
			}
			
			// ------------------------------------ Starting RMI server. Portnumber is arg[0] -----------------------
			peer = new PeerImpl();
			Peer stub = (Peer)UnicastRemoteObject.exportObject((Peer)peer, 0);
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("Peer", stub);
			peer.peers.add((Peer)peer);
			
			//-------------------------------------- Connecting to initPeer. Address is args[1], port is args[2] --------------------------------
			if (args.length > 2){
				Registry remoteRegistry = LocateRegistry.getRegistry(args[1], Integer.parseInt(args[2]));
	    		Peer initPeer  = (Peer) remoteRegistry.lookup("Peer");
	    		peer.peers = new ArrayList<Peer>(initPeer.getPeers());
	    		peer.peers.add((Peer)peer);
	    		Message msg = new EntryMessage(peer);
	    		msg.broadcast(peer, false);
			}else{
			
			}
			
			MessageProxy messageProxy = peer.new MessageProxy();
			messageProxy.start();
			
			UI ui = peer.new UI();
			ui.start();
		} catch (RemoteException e) {
			System.out.println("ERROR: Peer.main()\n");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("ERROR: Peer.main()\n");
			e.printStackTrace();
		}
	}
	
	public ArrayList<Peer>  getPeers ()throws RemoteException{
		return peers;
	} 

	// ----------------------------------------Message system-----------------------------------
 	public void message(Message msg) throws RemoteException{
		messages.add(msg);
	}
	
 		
	public static int log2(int n){
	    if(n <= 0) throw new IllegalArgumentException();
	    return 31 - Integer.numberOfLeadingZeros(n);
	}
	
	public void hasDisconnected(Peer disconnectedPeer){
		Message msg = new DisconnectedMessage(disconnectedPeer);
		msg.broadcast(this, true);
	}
	
	
	// ------------------------------ Console User Interface---------------------------------
	
	public class UI extends Thread{
		public UI(){}
		public void run(){
			while(true){
				System.out.println("Options: exit, terminate, size, hello, mandelbrot");
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			        String input = br.readLine();
					if (input.equals("exit")){
						System.exit(0);
					}
					if (input.equals("size")){
						System.out.println("Number of nodes is: " +  peers.size());
					}
					if (input.equals("terminate")){
						Message msg = new TerminateMessage();
						msg.broadcast(peer, false);
						System.exit(0);
					}
					if (input.equals("hello")){
						Message msg = new HelloMessage();
						msg.broadcast(peer, false);
					}
					if (input.equals("mandelbrot")){
						ClientImpl client = new ClientImpl(peer, "mandelbrot");
						client.start();
					}
					
				} catch (IOException e) {
					System.out.println("ERROR: UI.run()");
					e.printStackTrace();
				}
			}
		}
	}
	// ---------------------------------------- Task computation stuff ----------------------------
	public void putTask(Task t){
		// Notify all that a new task has arrived. Spesify an ID
		// PUT TASK TO QUEUE
		// Start a handler that steals/gives tasks
		try {
			readyQ.putFirst(t);
			System.out.println("New task registered");
		} catch (InterruptedException e) {
			System.out.println("Failed to put first task to readyQ");
			e.printStackTrace();
		}
	}
	
	public Task takeTask(){
		return readyQ.getFirst();
	};
	
	private int taskID = 1;
	
	public int getNextTaskID(){
		return taskID++;
	}
	
	
	public void putWaitMap(Task task){
		waitMap.put(task.ID, task);
	}
	
	
	
	
	
	public class MessageProxy extends Thread{
		public MessageProxy(){}
		
		public void run(){
			while(true){
				Message msg;
				try {
					msg = messages.take();
					msg.action(peer);
				} catch (InterruptedException e) {
					System.out.println("ERROR: MessageProxy()");
					e.printStackTrace();
				}
				
			}
		}
	}
}



//--------------------------------------------------------------------------------------------------

