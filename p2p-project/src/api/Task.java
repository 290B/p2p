package api;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

import system.PeerImpl;


/**
 * Is an absract class that that tasks has inherit and enables a divide an conquer system
 * to compute distributed recursive tasks
 * 
 * @author torgel
 *
 */

public abstract class Task implements Serializable , Cloneable {
	private static final long serialVersionUID = 1L;
	public String ID;  // format : 0-2-5-3-10-19....    The id is unique and describes the tree
	public String returnID;
	public int joinCounter;
	public int returnArgumentNumber;
	public Object[] args;
	public LinkedList<Task> spawned = new LinkedList<Task>();
	public Task spawn_next;
	public int spawn_nextJoin;
	public Object send_argument = null;
	public UUID creator; // where to send arguments
	public PeerImpl peerImpl;
	
	
	public Task(){}
	
	/**
	 * Tasks must be clonable so they can be distributed and duplicated over the network
	 */
	
	
	public Task clone() {
	try
	{
		return (Task) super.clone();
	}
		catch(Exception e){ return null; }
	}
	
	
	/**
	 * This function should contain the job itself. Will be started by the peer' executors.  
	 * 
	 * @param t the task that will be spawned
	 */
	abstract public void execute();
	
	
	/**
	 * spawn starts a task and immediately puts it on the ready queue for execution
	 * 
	 * @param t the task that will be spawned
	 */
	protected void spawn(Task t){
		spawned.add(t);
	}
	
	
	/**
	 * spawn_next starts a task and puts it in wait queue for input arguments
	 * after getting all the arguments it needs it will execute() the task
	 * 
	 * @param t the task that will be spawned, should be a "compose" task
	 * @param joinCounter is the number of arguments the task will wait for before 
	 * executing
	 */
	
	protected void spawn_next(Task t, int joinCounter){
		spawn_next = t;
		spawn_nextJoin = joinCounter;
	}
	/**
	 * send argument is the method that will move output from the regular task executions
	 * to the waiting tasks which were started with spawn_next
	 * 
	 * @param value an Object of any type which will be sent to the collector tasks that is
	 * linked with the task which the value is coming from
	 */
	
	protected void send_argument(Object value){
		send_argument = value;
	}
	
	
	public void setPeerImpl(PeerImpl peerImpl){ this.peerImpl = peerImpl; }
	
	/**
	 * Can be used from within the executed function if the job uses shared variables. The returned value will always be the newest variable
	 * found by the network 
	 * @return
	 */
	
	protected Shared getShared(){ return peerImpl.getShared();}
	
	/**
	 * Can be used from within the executed function if the job uses shared variables. Will set a new shared value and distribute it
	 * to all other peers if it fulfills Shared.isNewerThan()
	 * @param proposedShared
	 */
	protected void setShared(Shared proposedShared){ peerImpl.setShared(proposedShared); }
	protected void setSharedFromRemote(Shared proposedShared){peerImpl.setSharedFromRemote(proposedShared);}
	
	public interface Shared extends Cloneable{
		

		/** This function will check wheter an object is newer than the one in the space
		 * @param input is the object that will be checked against the version that is stored in the space
		 * @return is true if the space has the newest version, false otherwise
		 */
		public boolean isNewerThan(Shared input);
		/** this is called by the worker to get a fresh version of the shared object from the space
		 * 
		 * @return is the newest object availbe to the space
		 */
		public Object getShared();
		/** clones an object before sending it to the space for the purpose of mutability
		 * 
		 * @return the close
		 * @throws CloneNotSupportedException is called if the object does not implement cloning.
		 */
		public Shared clone()throws CloneNotSupportedException;
	}
	
}
