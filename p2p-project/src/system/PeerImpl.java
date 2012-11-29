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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class PeerImpl implements Peer {
	static PeerImpl peer;
	
	static final BlockingDeque<Message> messages = new LinkedBlockingDeque<Message>();
	public ArrayList<Peer> peers = new ArrayList<Peer>();
	
	
	// Compute stuff: 
	public Map<String, Task> waitMap = new ConcurrentHashMap<String , Task>();
	public final BlockingDeque<Task> readyQ = new LinkedBlockingDeque<Task>();
	private final BlockingQueue<Object> result = new LinkedBlockingQueue<Object>();
	public int taskExcecuted = 0;
	public int composeTasksCreated = 0;
	
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
			
			Executor executor = new Executor(peer);
			executor.start();
			
			WorkStealer workStealer = peer.new WorkStealer();
			workStealer.start();
			
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
				System.out.println("Options: exit, terminate, size, hello, mandelbrot, random, readyQ, waitMap");
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
						MandelbrotClient client = new MandelbrotClient(peer, "mandelbrot");
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
			t.creator = peer;
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
	
	public void placeArgument(Peer receiver, String ID, Object returnValue, int returnArgumentNumber){
		Message msg = new SendArgumentMessage(receiver, ID, returnValue, returnArgumentNumber);
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
				if (peer.readyQ.size() < 2){
					Random rnd = new Random();
					Message msg = new GetTaskMessage((Peer)peer);
					Peer temp = peers.get(rnd.nextInt(peers.size())); 
					if (temp!=peer){
						msg.send(peer, temp);
					}
				}
				try {
					sleep(5);
				} catch (InterruptedException e) {
					System.out.println("ERROR: workstealer failed to sleep");
					e.printStackTrace();
				}
			}
		}
	}
}



//--------------------------------------------------------------------------------------------------

