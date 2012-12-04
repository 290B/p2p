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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import system.Task.Shared;

public class PeerImpl implements Peer {
	static PeerImpl peer;
	
	static final BlockingDeque<Message> messages = new LinkedBlockingDeque<Message>();
	//public ArrayList<Peer> peers = new ArrayList<Peer>();
	private Shared shared = null;
	UUID peerID;
	public ArrayList<UUID> keys = new ArrayList<UUID>();
	public Map<UUID, Peer> peerMap = new ConcurrentHashMap<UUID , Peer>();
	
	
	public RemoteQueue remoteQ;
	public Map<UUID, RemoteQueueImpl> hostedQueues = new ConcurrentHashMap<UUID , RemoteQueueImpl>();
	
	// Compute stuff: 
	public Map<String, Task> waitMap = new ConcurrentHashMap<String , Task>();
	public final BlockingDeque<Task> readyQ = new LinkedBlockingDeque<Task>();
	private final BlockingQueue<Object> result = new LinkedBlockingQueue<Object>();
	public int taskExcecuted = 0;
	public int composeTasksCreated = 0;
	
	public static void main(String[] args) {
		
		try {
			if (System.getSecurityManager() == null ) { 
				System.setSecurityManager(new java.rmi.RMISecurityManager() ); 
			}
			
			// Read user input:
			String remotePeer;
			int remotePort;
			int localPort;
			
			
	        System.out.println("Type in hostname of remote peer: ");
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        String input = br.readLine();
	        
	        if (!input.equals("")){
	        	remotePeer = input;	
	        }else{
	        	remotePeer = "localhost";
	        }
	        System.out.println("Type in remote port: ");
	        input = br.readLine();
	        if (!input.equals("")){
	        	remotePort = Integer.parseInt(input);	
	        }else{
	        	remotePort = 1099;
	        }
	        System.out.println("Type in local port: ");
	        input = br.readLine();
	        if (!input.equals("")){

		        localPort = Integer.parseInt(input);	
	        }else{
	        	localPort = 1099;
	        }
			
			
			// ------------------------------------ Starting RMI server. Portnumber is arg[0] -----------------------
			peer = new PeerImpl();
			peer.peerID = UUID.randomUUID();
			Peer stub = (Peer)UnicastRemoteObject.exportObject((Peer)peer, 0);
			Registry registry = LocateRegistry.createRegistry(localPort);
			registry.rebind("Peer", stub);
			peer.addPeer(peer.peerID, (Peer)peer);
			
			//-------------------------------------- Connecting to initPeer. Address is args[1], port is args[2] --------------------------------
			if (!remotePeer.equals("none")){
				Registry remoteRegistry = LocateRegistry.getRegistry(remotePeer, remotePort);
	    		Peer initPeer  = (Peer) remoteRegistry.lookup("Peer");
	    		peer.updatePeerMap(initPeer.getPeerMap());
	    		Message msg = new EntryMessage(peer.peerID, peer);
	    		msg.broadcast(peer, false);
			}else{
			
			}
			
			GetRemoteQueue getRemoteQueue = peer.new GetRemoteQueue();
			getRemoteQueue.start();
			
			
			MessageProxy messageProxy = peer.new MessageProxy();
			messageProxy.start();

			Executor executor = new Executor(peer);
			executor.start();
			
			WorkStealer workStealer = peer.new WorkStealer();
			workStealer.start();
			
			UI ui = peer.new UI();
			ui.run();
		} catch (RemoteException e) {
			System.out.println("ERROR: Peer.main()\n");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("ERROR: Peer.main()\n");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------Message system-----------------------------------
 	public void message(Message msg) throws RemoteException{
		messages.add(msg);
	}
	
 		
	public static int log2(int n){
	    if(n <= 0) throw new IllegalArgumentException();
	    return 31 - Integer.numberOfLeadingZeros(n);
	}
	
	public void hasDisconnected(UUID disconnectedPeer){
		Message msg = new DisconnectedMessage(disconnectedPeer);
		msg.broadcast(this, true);
	}
	
	
	// ------------------------------ Console User Interface---------------------------------
	
	public class UI extends Thread{
		public UI(){}
		public void run(){
			while(true){
				
				System.out.println("Options: id, exit, terminate, size, hello, mandelbrot, random, readyQ, waitMap");
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			        String input = br.readLine();
					if (input.equals("exit")){
						System.exit(0);
					}
					if (input.equals("id")){
						System.out.println("This peer has ID: " + peerID);
					}
					if (input.equals("size")){
						System.out.println("Number of nodes is: " +  peerMap.size());
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
						MandelbrotClient client = new MandelbrotClient(peer, "mandelbrot");
						client.start();
					}
					
					if (input.equals("tsp")){
						TspCli client = new TspCli(peer);
						client.start();
					}
					
					if (input.equals("fib")){
						FibClient client = new FibClient(peer);
						client.start();
					}
					
					if (input.equals("readyQ")){
							System.out.println("The size of readyQ is : " +  readyQ.size());
					}
					if (input.equals("waitMap")){
						System.out.println("The size of waitMap is : " +  waitMap.size());
					}
					if (input.equals("executed")){
						System.out.println("The number of tasks executed is : " +  taskExcecuted);
					}
					if (input.equals("compose")){
						System.out.println("The number of compose tasks created is : " +  composeTasksCreated);
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
			t.returnID = "0";
			t.ID = "0";
			t.creator = peerID;
			readyQ.putFirst(t);
			System.out.println("New task registered");
			
		} catch (InterruptedException e) {
			System.out.println("Failed to put first task to readyQ");
			e.printStackTrace();
		}
	}
	
	public void giveTask(Task t){
		try {
			readyQ.put(t);
		} catch (InterruptedException e) {
			System.out.println("Failed to put task");
			e.printStackTrace();
		}
	}
	
	
	public Task takeTask(){
		try {
			return readyQ.takeFirst();
		} catch (InterruptedException e) {
			System.out.println("Failed to take task from readyQ");
			e.printStackTrace();
		}
		return null;
	};
	
	public void putWaitMap(Task task){
		waitMap.put(task.ID, task);
	}
	
	public synchronized void addPeer(UUID id, Peer peer){
		if (!peerMap.containsKey(id)){
			keys.add(id);
			peerMap.put(id, peer);
		}
	}
	
	public UUID randomPeer(){
		Random rnd = new Random();
		return keys.get(rnd.nextInt(keys.size()));
	}
	
	public synchronized void removePeer(UUID id){
		if (peerMap.containsKey(id)){
			keys.remove(id);
			peerMap.remove(id);
		}
	}
	
	public synchronized void clearPeerMap(){
		keys.clear();
		peerMap.clear();
	}
	
	public Map<UUID, Peer> getPeerMap() throws RemoteException{
		return peerMap;
	}
	
	public synchronized void updatePeerMap(Map<UUID, Peer> peerMap){
		for (Map.Entry<UUID, Peer> entry : peerMap.entrySet()){
			keys.add(entry.getKey());
		}
		this.peerMap.putAll(peerMap);
	}
	
	public void placeArgument(UUID receiver, String ID, Object returnValue, int returnArgumentNumber){
		Message msg = new SendArgumentMessage(ID, returnValue, returnArgumentNumber);
		msg.send(peer, receiver);
	}
	
	public void putReadyQ(Task t){
		try {
			readyQ.put(t);
		} catch (InterruptedException e) {
			System.out.println("ERROR: putRreadyQ");
			e.printStackTrace();
		}
	}
	
	public void putResult(Object resultObject){
		try {
			result.put(resultObject);
		} catch (InterruptedException e) {
			System.out.println("ERROR: putResult");
			e.printStackTrace();
		}
	}
	
	public Object getResult(){
		try {
			return result.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		
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
	
	public class WorkStealer extends Thread{
		public WorkStealer(){}
		
		public void run(){
			while (true){
				if (peer.readyQ.size() < 4){
					Message msg = new GetTaskMessage((Peer)peer);
					UUID temp = peer.randomPeer();
					if (temp!=peer.peerID){
						msg.send(peer, temp);
					}
				}
				try {
					sleep(10);
				} catch (InterruptedException e) {
					System.out.println("ERROR: workstealer failed to sleep");
					e.printStackTrace();
				}
			}
		}
	}

	public Shared getShared(){
		if (shared != null){
			try {
				return shared.clone(); 
			} catch (CloneNotSupportedException e) {
	
				e.printStackTrace();
				System.exit(0);
			}
		}
		return null;
	}
	
	public void setShared(Shared proposedShared){
		try {
			if (proposedShared.isNewerThan(shared)){
				shared = proposedShared.clone();
				SetSharedVarMessage msg = new SetSharedVarMessage(shared);
				msg.broadcast(peer, false); //TODO is peer self?
			}

		} catch (CloneNotSupportedException e) {
			System.out.println("proposedShared not clonable");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void setSharedFromRemote(Shared proposedShared){
		try {
			if (proposedShared.isNewerThan(shared)){
				shared = proposedShared.clone();
			}

		} catch (CloneNotSupportedException e) {
			System.out.println("proposedShared not clonable");
			e.printStackTrace();
			System.exit(0);
		}
	}

	public class GetRemoteQueue extends Thread{
		public GetRemoteQueue(){
			remoteQ = null;
		}
		public void run(){
			while(remoteQ == null){
				Message msg = new GetRemoteQueueMessage(peerID);
				msg.broadcast(peer, false);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					System.out.print("Error sleeping in GetRemoteQueue");
					e.printStackTrace();
				}
			}
		}
	}

	synchronized public boolean registerQueue(RemoteQueue rq) throws RemoteException {
		if (remoteQ == null){
			remoteQ = rq;
			System.out.println("Recieved reference to a remote Queue!");
			return true;
		}
		return false;
	}
}



//--------------------------------------------------------------------------------------------------

