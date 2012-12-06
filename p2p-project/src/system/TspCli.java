package system;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;  
import system.Task.Shared;
import tasks.TspTask.*;
import tasks.TspTask;



public class TspCli extends Thread{
	Peer peer;
	
	private static final int N_PIXELS = 512;

	private static final int whichLevelToSplitAt = 4;

	private static double[][] towns =
//		{
//		{ 1, 1 },
//		{ 8, 1 },
//		{ 8, 8 },
//		{ 1, 8 },
//		{ 2, 2 },
//		{ 7, 2 },
//		{ 7, 7 },
//		{ 2, 7 },
//		{ 3, 3 },
//		{ 6, 3 },
//		{ 6, 6 },
//		{ 3, 6 },
//		{ 4, 4 },
//		{ 5, 4 },
//		{ 5, 5 },
//		{ 4, 5 }
//		};
	
	{
	{0,13},
	{0,26},
	{0,27},
	{0,39},
	{2,0},
	{5,13},
	{5,19},
	{5,25},
	{5,31},
	{5,37},
	{5,43},
	{5,8},
	{8,0},
	{9,10},
	{10,10},
	{11,10},
	{12,10},
	{12,5},
	{15,13},
	{15,19},
	{15,25},
	{15,31},
	{15,37},
	{15,43},
	{15,8},
	{18,11},
	{18,13},
	{18,15},
	//{18,17},
	//{18,19},
	};
    TspCli(Peer peer){
    	this.peer = peer;
    }
	//LOL
	
	public void run() {
    	
        try {
    		int tour[] = new int [towns.length];
  
        	tour = runJob (towns,whichLevelToSplitAt);

            JLabel euclideanTspLabel = displayEuclideanTspTaskReturnValue( towns, tour );
            JFrame frame = new JFrame( "Result Visualizations" );
            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
            Container container = frame.getContentPane();
            container.setLayout( new BorderLayout() );
            container.add( new JScrollPane( euclideanTspLabel ), BorderLayout.EAST );
            frame.pack();
            frame.setVisible( true );

        } catch (Exception e) {
            System.err.println("ComputeTSP exception:");
            e.printStackTrace();
        }
        
        
        
    }    
            
    private int[] runJob(double [][] towns, int levelToSplitAt){
    	
       	try{   
    		TspTask tspTask = new TspTask();
    		
    		double[][] distances = calcAllDistances(towns);
    		

    		
    		ArrayList<Integer> path = new ArrayList<Integer>();
    		//Start in town 0
    		path.add(0);
    		

    		

    		//generate list of all town indexes
    		ArrayList<Integer> allTowns = new ArrayList<Integer>();
    		for (int i = 0; i < distances.length;i++){
    			allTowns.add(i);
    		}


    		TspTask.TspInputArg in = tspTask.new TspInputArg(path, distances, 0, allTowns ,levelToSplitAt);
            
    		TspTask.TspExplorer tsp = tspTask.new TspExplorer((Object) in);
    		long start = System.currentTimeMillis();
            peer.putTask(tsp);
           	
			TspReturn results = (TspReturn)peer.getResult();
			long stop = System.currentTimeMillis();
			System.out.println("Time: " +(stop-start) +" milliseconds");
           	ArrayList<Integer> ret = results.getPath();    
           	System.out.println("Length of the shortest path is: "+results.getSumPathLength());
           	
           	return toIntArray(ret);
    	
    	}catch (Exception e){
    		System.err.println("TspClient exception:");
    		e.printStackTrace();
    		return null;
    		}
    }
    
    private static JLabel displayEuclideanTspTaskReturnValue( double[][] cities, int[] tour )
    {
        System.out.print( "Tour: ");
        for ( int city: tour )
        {
        	System.out.print( city + " ");
        }
        System.out.println("");

        // display the graph graphically, as it were
        // get minX, maxX, minY, maxY, assuming they 0.0 <= mins
        double minX = cities[0][0], maxX = cities[0][0];
        double minY = cities[0][1], maxY = cities[0][1];
        for ( int i = 0; i < cities.length; i++ )
        {
            if ( cities[i][0] < minX ) minX = cities[i][0];
            if ( cities[i][0] > maxX ) maxX = cities[i][0];
            if ( cities[i][1] < minY ) minY = cities[i][1];
            if ( cities[i][1] > maxY ) maxY = cities[i][1];
        }
    	
        // scale points to fit in unit square
        double side = Math.max( maxX - minX, maxY - minY );
        double[][] scaledCities = new double[cities.length][2];
        for ( int i = 0; i < cities.length; i++ )
        {
            scaledCities[i][0] = ( cities[i][0] - minX ) / side;
            scaledCities[i][1] = ( cities[i][1] - minY ) / side;
        }

        Image image = new BufferedImage( N_PIXELS, N_PIXELS, BufferedImage.TYPE_INT_ARGB );
        Graphics graphics = image.getGraphics();

        int margin = 10;
        int field = N_PIXELS - 2*margin;
        // draw edges
        graphics.setColor( Color.BLUE );
        int x1, y1, x2, y2;
        int city1 = tour[0], city2;
        x1 = margin + (int) ( scaledCities[city1][0]*field );
        y1 = margin + (int) ( scaledCities[city1][1]*field );
        for ( int i = 1; i < cities.length; i++ )
        {
            city2 = tour[i];
            x2 = margin + (int) ( scaledCities[city2][0]*field );
            y2 = margin + (int) ( scaledCities[city2][1]*field );
            graphics.drawLine( x1, y1, x2, y2 );
            x1 = x2;
            y1 = y2;
        }
        city2 = tour[0];
        x2 = margin + (int) ( scaledCities[city2][0]*field );
        y2 = margin + (int) ( scaledCities[city2][1]*field );
        graphics.drawLine( x1, y1, x2, y2 );

        // draw vertices
        int VERTEX_DIAMETER = 6;
        graphics.setColor( Color.RED );
        for ( int i = 0; i < cities.length; i++ )
        {
            int x = margin + (int) ( scaledCities[i][0]*field );
            int y = margin + (int) ( scaledCities[i][1]*field );
            graphics.fillOval( x - VERTEX_DIAMETER/2,
                               y - VERTEX_DIAMETER/2,
                              VERTEX_DIAMETER, VERTEX_DIAMETER);
        }
        ImageIcon imageIcon = new ImageIcon( image );
        return new JLabel( imageIcon );
    }
    /**
     * Calculates the distance between two towns from their position 
     * as x and y coordinates on the map.
     * 
     * @param town1X
     * @param town1Y
     * @param town2X
     * @param town2Y
     * @return the length between two towns
     */
    public static double calcOneDistance(double town1X, double town1Y, double town2X, double town2Y){
    	return Math.sqrt((town1X-town2X)*(town1X-town2X) + (town1Y-town2Y)*(town1Y-town2Y));
    }
    
    /**   
	 * This methods simply calculates the distance between each and every
	 * pair of towns on the map. 
	 *
	 * @param towns , the coordinates of all towns on the map
	 * @return a list of distances between every pair of towns
	 */
    public static double[][] calcAllDistances(double[][] towns){
    	int inf = 1000000;
    	double[][] distances = new double[towns.length][towns.length];
    	for (int i = 0; i< towns.length;i++){
    		for (int j = 0; j<towns.length;j++){
    			if (i != j){
    				distances[i][j] = calcOneDistance(towns[i][0],towns[i][1],towns[j][0],towns[j][1]);
    			}
    			else distances[i][j] = inf;
    		}
    	}
    	return distances;
    }
    
    public static int[] toIntArray(ArrayList<Integer> list){
    	  int[] ret = new int[list.size()];
    	  for(int i = 0;i < ret.length;i++)
    	    ret[i] = list.get(i);
    	  return ret;
    	}
    
}