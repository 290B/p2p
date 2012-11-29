package tasks;

import java.io.Serializable;
import java.util.ArrayList;

import system.Task;
import system.Task.Shared;

/**
 * This class implements a Traveling Salesman Problem solver as a task
 * which fits into the RMI framework implemented in the API.
 * 
 * The solver simply brute forces and finds all possible routes in both 
 * directions and then evaluates them all to find the most efficient one.
 * 
 * this class contains the less general TspTaks and TspCompose
 * 
 * @author torgel
 *
 */
public class TspTask implements Serializable{
	private static final long serialVersionUID = 227L;		
	private static final double inf = 10000; 

	public SharedTsp sharedTsp;
	public TspReturn currentBestValues = new TspReturn(new ArrayList<Integer>() , inf);
	public Shared sharedLocal;

	/**
	 * Takes a TspTask containing a TspInputArg which can be executed
	 * @author torgel
	 *
	 */
	public class TspExplorer extends Task implements Serializable{
		private static final long serialVersionUID = 227L;		
		public Object [] args;
		public TspExplorer(Object... args){this.args = args;}
		public TspExplorer(){}

	    public ArrayList<Integer> path;
	    public double [][] distances;
	    public double sumPathLength;
	    public ArrayList<Integer> allTowns;
	    public int levelToSplitAt;
	    public double currentShortestPathLength = 1000000;
	    public ArrayList<Integer> currentShortestPath = new ArrayList<Integer>();


		/**
		 * Executes the TspTask which deals with splitting the task up and calculating
		 * shortest paths of sufficiently small sub-trees. uses spawn and spawn_next to 
		 * spawn other smaller tasks and composer tasks.
		 * 
		 * @return returns null, but uses send_argument to distribute TspResults to the 
		 * composer tasks via the space
		 * 
		 */
		public void execute() {

			sharedTsp = (SharedTsp)getShared();

			//System.out.println("shared is " + sharedTsp.getShared());

			//currentBestValues.settSumPathLength((Double) sharedTsp.getShared());

			TspInputArg in = (TspInputArg)args[0];



			path = in.getPath();
		    distances = in.getDistances();
		    sumPathLength = in.getSumPathLength() ;
		    allTowns = in.getAllTowns();
		    levelToSplitAt = in.getLevelToSplitAt() ;    


		  if (path.size() == 1){
				setShared(findInitialShortPath());

			}

		    if (path.size() < levelToSplitAt){ //The tree is still too big to be computed localy, try to split

		    	if (path.size() == distances.length){  //path at maximum length, compute that single path locally
		    	   	TspReturn res = localTsp(in);
					send_argument(res);
		    	}

		    	else { 
			    	//Explore more of the tree, that is add more elements to path and ant split the task up. 
			    	//Also add the traversed Length so far

					int numComposeArguments = 0;

					//for every child not on the travelled path
					for (Integer town : allTowns){
						if (!path.contains(town)){
							ArrayList<Integer> newPath = new ArrayList<Integer>();
							newPath.addAll(path);
							newPath.add(town);	    	

							double newSumPath = sumPathLength+(distances[path.get(path.size()-1)][newPath.get(newPath.size()-1)]);  //distance between the next town to visit and the previous one
							//System.out.println("newPath" +newPath+" with length " + newSumPath);	

							//if (newSumPath < currentBestValues.getSumPathLength()){

							if (newSumPath + distances[0][newPath.get(newPath.size()-1)] <= (Double) sharedTsp.getShared()){ //TODO HERE

								//currentBestValues.settSumPathLength(newSumPath);
								currentBestValues.setPath(newPath);

								spawn(new TspExplorer((Object)new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt)));
								//TODO check if dynamic matters
								numComposeArguments++;
							}
							//spawn(new TspExplorer((Object)new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt)));
							//numComposeArguments++;
						}
					}

					if (numComposeArguments == 0){
						//System.out.println("MISTENKT");
						send_argument(new TspReturn(null,inf));

					}else {
						spawn_next(new TspComposer(), numComposeArguments); 
					}
					numComposeArguments=0;
		    	}



		    }
		    else { // Compute the given task locally
		    	TspReturn res = localTsp(in);
				send_argument(res);
		    }
		}

		/**
		 * Performs the local tsp when the job is sufficiently divided
		 * @param inn is the input to the subtask that is to be executed locally
		 * @return a TspReturn object with the best path found and the length of it
		 */
		public TspReturn localTsp(TspInputArg inn){

			sharedTsp = (SharedTsp)getShared();
			if ((Double) sharedTsp.getShared() < currentShortestPathLength){
				currentShortestPathLength = (Double) sharedTsp.getShared();
			}
		    ArrayList<Integer> path = inn.getPath();
		    double [][] distances = inn.getDistances();
		    double sumPathLength = inn.getSumPathLength();
		    ArrayList<Integer> allTowns = inn.getAllTowns();


			if (path.size() < distances.length){

				//for every child on path, that is every town except those visited on the path so far				
				for (Integer town : allTowns){
					if (!path.contains(town)){
						ArrayList<Integer> newPath = new ArrayList<Integer>();
						newPath.addAll(path);
						newPath.add(town);	    	

						double newSumPath = sumPathLength+(distances[path.get(path.size()-1)][newPath.get(newPath.size()-1)]);  //distance between the next town to visit and the previous one
						//System.out.println("newPath" +newPath+" with length " + newSumPath);	
						//TspExplorer localTask = new TspExplorer((Object)new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt));
						if (newSumPath + distances[0][newPath.get(newPath.size()-1)] < currentShortestPathLength){
							localTsp(new TspInputArg(newPath, distances, newSumPath, allTowns ,levelToSplitAt));
						}

						//localTask.execute();


						//return new TspReturn(currentShortestPath, currentShortestPathLength);
					}
				}				
			}
			else if (path.size() == distances.length){
				sumPathLength += (distances[path.get(path.size()-1)][0]); //adding the length back to town -

				if (sumPathLength <= currentShortestPathLength){
					currentShortestPathLength = sumPathLength;
					ArrayList<Integer> tempPath = new ArrayList<Integer>();
					tempPath.addAll(path);
					currentShortestPath = tempPath;
					setShared(new SharedTsp(sumPathLength));
				}
			}			
			return new TspReturn(currentShortestPath, currentShortestPathLength);
		}

		public Shared findInitialShortPath (){

			ArrayList<Integer> newPath = new ArrayList<Integer>();
			newPath.add(0);
			double totalDistance = 0;
			double distanceToClosestTown = inf;
			Integer closestTown = 0;
			double tempDistance;

			for(int i = 0 ; i < distances.length-1;i++){ // TODO, should the -1 be there?
				for (Integer town : allTowns){
					if (!newPath.contains(town)){
						tempDistance = distances[town][newPath.get(newPath.size()-1)];  										
						if (tempDistance < distanceToClosestTown){
							distanceToClosestTown = tempDistance;
							closestTown = town;						
						}
					}
				}
				newPath.add(closestTown);
				totalDistance += distanceToClosestTown;
				distanceToClosestTown = inf;
			}

			totalDistance += distances[newPath.get(newPath.size()-1)][newPath.get(0)]; // add length back to root town
			//System.out.println("distances " + totalDistance);
			newPath.add(0);
			//System.out.println(newPath);

			System.out.println("initial distance: "+totalDistance);

			//currentBestValues.settSumPathLength(totalDistance);
			//currentBestValues.setPath(newPath);



			return new SharedTsp(totalDistance);
		}
	}



	/**
	 * The Tsp Compose task takes an array of objects which is actually
	 * TspReturn objects and finds the shortest path amongst the input paths.
	 * It then sends this result to the next composer task by using 
	 * send_argument. This continues until the root composer task receives
	 * all it's input. 
	 * 
	 * @author torgel
	 *
	 */
	public class TspComposer extends Task implements Serializable{
		private static final long serialVersionUID = 227L;		
		public TspComposer(Object ... args){this.args = args;}
		public TspComposer(){}
		/**
		 * Executes the compose task and sends arguments to other compose tasks
		 * 
		 * @return returns null, but uses send_arguments to distribute TspResult objects
		 * to the other composer tasks
		 */
		public void execute(){			
			TspReturn inputVal;
			ArrayList<Integer> currentShortestPath = new ArrayList<Integer>();
			double currentShortestPathLength = inf;

			for (int i = 0; i < args.length ; i++){
				inputVal = (TspReturn)args[i];
				if (inputVal.getSumPathLength() < currentShortestPathLength){
					currentShortestPathLength = inputVal.getSumPathLength();
					currentShortestPath = inputVal.getPath();
				}
			}

			//System.out.println("short path  " + currentShortestPath);
			//System.out.println(" short path len " + currentShortestPathLength);

			TspReturn ret = new TspReturn(currentShortestPath,currentShortestPathLength);
			send_argument(ret);
		}
	}
	
	/**This class is used for sharing the best pathlength found between workers and space in the DAC system
	 * 
	 * @author torgel
	 *
	 */
	public class SharedTsp implements Shared, Serializable{
	    static final long serialVersionUID = 227L; // Was missing 
	    private double tspShared;

	    /** constructs the shared object, that is the path length
	     * 
	     * @param input is the inital value of the shared object, that is the initial best path length or cost
	     */
		public SharedTsp(double input){
			tspShared = input;
		}

		public SharedTsp(){

		}
		/**Returns the shared object from the space, that is the newst one
		 * 
		 */
		public Object getShared(){
			return (double)tspShared;
		}
		/**Detects if the path found is shorter than the shortest path found.
		 * 
		 */
		public boolean isNewerThan(Shared input) {
			if ( (Double) input.getShared() <= this.tspShared){
				return false;
			}else{
				return true;
			}
		}

		public Shared clone() throws CloneNotSupportedException{
			return (Shared) super.clone();
		}

	}

	
	/**
	 * The return class that the workers and space use to communicate
	 * results to each other
	 * 
	 * @param path ArrayList of integers that indicates the sequence of towns
	 * that were visited for this result
	 * @param sumPathLength is the euclidean length of the path, the distance travelled
	 */
	public class TspReturn implements Serializable{

	    static final long serialVersionUID = 227L; // Was missing 
		private double sumPathLength;
		private ArrayList<Integer> path;


	    public TspReturn( ArrayList<Integer> path, double sumPathLength) 
	    {
	    	this.sumPathLength = sumPathLength;
	    	this.path = path;
	    }

	    public  ArrayList<Integer> getPath() { return path; }
	    
	    public double getSumPathLength(){ return sumPathLength;}
	    
	    public void setPath (ArrayList<Integer> inPath){
	    	this.path = inPath;
	    }
	    
	    public void settSumPathLength(double inSum){
	    	this.sumPathLength = inSum;
	    }
	}
	
	
	/**
	 * This is the class which is used by workers, clients and space to communicate
	 * input parameters to Tsp tasks. Mostly used to get a clean interface with the
	 * generics used in the DAC framework.
	 * 
	 * @param path is the sequence of cities that have been traversed so far
	 * @param distances is the array of distances between pairs of towns
	 * @param sumPathLength is the euclidean distance traveled by the path
	 * @param allTowns list of all the towns (indexes) , 1...N
	 * @param levelToSplitAt is an identifier that tells the task executor the
	 * level at which to stop splitting the tasks into more tasks. This int counts
	 * "from the top", that is a level of 0 will spawn only ONE task. a level of 10
	 * will spawn a -huge- amount of tasks.
	 */
	public class TspInputArg implements Serializable{

	    static final long serialVersionUID = 227L; // Was missing 
	    double[][] distances; 
		private ArrayList<Integer> path;
		private double sumPathLength;
		private ArrayList<Integer> allTowns;
		private int levelToSplitAt;


	    public TspInputArg( ArrayList<Integer> path, double [][] distances, double sumPathLength, ArrayList<Integer> allTowns, int levelToSplitAt)
	    {
	    	this.distances = distances;
	    	this.path = path;
	    	this.sumPathLength = sumPathLength;
	    	this.allTowns = allTowns;
	    	this.levelToSplitAt = levelToSplitAt;
	    	
	    }

	    public ArrayList<Integer> getPath() { return path; }
	    
	    public double [][] getDistances(){ return distances;}
	    
	    public double getSumPathLength() {return sumPathLength;}
	    
	    public ArrayList<Integer> getAllTowns(){ return allTowns; }
	    
	    public int getLevelToSplitAt() {return levelToSplitAt; }

	    

	}
}
