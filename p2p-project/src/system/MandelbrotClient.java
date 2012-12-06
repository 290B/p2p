package system;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import tasks.*;

public class MandelbrotClient extends Thread{
	Peer peer;
	
	// Mandelbrot input data
	int N_PIXELS = 1024;
	int ITERATION_LIMIT = 4096;
	double CORNER_X = -0.7510975859375;
	double CORNER_Y = 0.1315680625;
	double EDGE_LENGTH = 0.01611;
	int DEPTH = 2;
	// ---------------------------------
	
	
	MandelbrotClient(Peer peer, String string_task){
		this.peer = peer;
	}
	
	public void run(){
		MandelbrotSetTask temp = new MandelbrotSetTask();
		
		Task split = temp.new Split(CORNER_X, CORNER_Y, EDGE_LENGTH, N_PIXELS, ITERATION_LIMIT, DEPTH);
		
		try {
			long start = System.currentTimeMillis();
			peer.putTask(split);
			int [][] count = (int[][]) peer.getResult();
			long stop = System.currentTimeMillis();
			System.out.println("Time: " +(stop-start) +" milliseconds");
			
			JLabel mandelbrotLabel = displayMandelbrotSetTaskReturnValue( count );
			JFrame frame = new JFrame( "Result Visualizations" );
			frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			Container container = frame.getContentPane();
			container.setLayout( new BorderLayout() );
			container.add( new JScrollPane( mandelbrotLabel ), BorderLayout.WEST );
			frame.pack();
			frame.setVisible( true );
			
			// TODO : wait for task to finnish and take, then print result
		} catch (RemoteException e) {
			System.out.println("ERROR: mandelbrotclient (), could not put task");
			e.printStackTrace();
		}
		
		
	}
	
	private JLabel displayMandelbrotSetTaskReturnValue( int[][] counts )
	{
	    Image image = new BufferedImage( N_PIXELS, N_PIXELS, BufferedImage.TYPE_INT_ARGB );
	    Graphics graphics = image.getGraphics();
	    for ( int i = 0; i < counts.length; i++ )
	    for ( int j = 0; j < counts.length; j++ )
	    {
	        graphics.setColor( getColor( counts[i][j] ) );
	        graphics.fillRect(i, j, 1, 1);
	    }
	    ImageIcon imageIcon = new ImageIcon( image );
	    return new JLabel( imageIcon );
	}

	private Color getColor( int i )
	{
	    if ( i == ITERATION_LIMIT )
	        return Color.BLACK;
	    return Color.getHSBColor((float)i/ITERATION_LIMIT, 1F, 1F);
	}
}
