package system;

import java.util.LinkedList;

import system.Executor.TaskID;


public abstract class Task {
	public TaskID ID;
	public TaskID returnID;
	public int joinCounter;
	public int returnArgumentNumber;
	protected Object[] args;
	public LinkedList<Task> spawned = new LinkedList<Task>();
	public Task spawn_next;
	public int spawn_nextJoin;
	public Object send_argument;
	public Peer paretnHolder; // where to send arguments
	
	
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
