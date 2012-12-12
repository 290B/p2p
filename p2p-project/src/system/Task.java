package system;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;


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
	
	public Task clone() {
	try
	{
		return (Task) super.clone();
	}
		catch(Exception e){ return null; }
	}
	
	abstract public void execute();
	
	protected void spawn(Task t){
		spawned.add(t);
	}
	
	protected void spawn_next(Task t, int joinCounter){
		spawn_next = t;
		spawn_nextJoin = joinCounter;
	}
	
	protected void send_argument(Object value){
		send_argument = value;
	}
	
	
	public void setPeerImpl(PeerImpl peerImpl){ this.peerImpl = peerImpl; }
	protected Shared getShared(){ return peerImpl.getShared();}
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
