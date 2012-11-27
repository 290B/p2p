package system;

import java.util.LinkedList;


public abstract class Task {
	protected Object[] args;
	public LinkedList<Task> spawn = new LinkedList<Task>();
	public Task spawn_next;
	public int spawn_nextJoin;
	public Object send_argument;
	public Peer paretnHolder; // where to send arguments
	
	
	public Task(){}
	
	abstract public void execute();
	
	protected void spawn(Task t){
		spawn.add(t);
	}
	
	protected void spawn_next(Task t, int joinCounter){
		spawn_next = t;
		spawn_nextJoin = joinCounter;
	}
	
	protected void send_argument(Object value){
		send_argument = value;
	}
	

}
