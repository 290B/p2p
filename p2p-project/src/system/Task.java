package system;

import java.util.LinkedList;


public abstract class Task {
	public String ID;  // format : 0-2-5-3-10-19....    The id is unique and describes the tree
	public String returnID;
	public int joinCounter;
	public int returnArgumentNumber;
	protected Object[] args;
	public LinkedList<Task> spawned = new LinkedList<Task>();
	public Task spawn_next;
	public int spawn_nextJoin;
	public Object send_argument = null;
	public Peer creator; // where to send arguments
	
	
	public Task(){}
	
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
}
