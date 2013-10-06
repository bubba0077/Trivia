package net.bubbaland.trivia.client;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import net.bubbaland.trivia.Trivia;

// TODO: Auto-generated Javadoc
/**
 * The Class PlaceChartPanel.
 */
public class PlaceChartPanel extends TriviaPanel {
	
	/** The Constant serialVersionUID. */
	final private static long	serialVersionUID	= -4937894497657719097L;
	
	/** The Constant BACKGROUND_COLOR. */
	final private static Color BACKGROUND_COLOR = Color.BLACK;	
	
	/** The Constant LINE_COLOR. */
	final private static Color LINE_COLOR = Color.ORANGE;	
	
	/** The Constant AXIS_FONT_SIZE. */
	final private static float AXIS_FONT_SIZE = 16.0f;
		
	/** The client. */
	private TriviaClient client;
	
	/** The n teams. */
	private int nRounds, nTeams;
	
	/** The places. */
	private int[] places;
	
	/** The chart panel. */
	private ChartPanel chartPanel;

	/**
	 * Instantiates a new place chart panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public PlaceChartPanel(TriviaClient client) {
		
		super(new GridBagLayout());
		
		this.client = client;
		
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				this.nRounds = client.getTrivia().getNRounds();
				this.nTeams = client.getTrivia().getNTeams();
				success = true;
			} catch ( Exception e ) {
				client.log( "Couldn't retrieve number of rounds from server (try #" + tryNumber + ")." );
			}
		}

		if ( !success ) {
			client.disconnected();
			return;
		}
		
		places = new int[nRounds];
		
		chartPanel = null;
			
	}
	
	/**
	 * Creates the chart.
	 *
	 * @param dataset the dataset
	 * @return the j free chart
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		
		final JFreeChart chart = ChartFactory.createXYLineChart("Place by Round", "Round", "Place", dataset, PlotOrientation.VERTICAL, false, false, false);		
		return chart;
		
	}
	
	

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		
		Trivia trivia = client.getTrivia();
				
		int[] newPlaces = new int[nRounds];
		boolean[] announced = new boolean[nRounds];
		boolean change = false;
		
		for(int r=0; r<nRounds; r++) {
			newPlaces[r] = trivia.getAnnouncedPlace( r+1 );
			announced[r] = trivia.isAnnounced( r+1 );
			change = change || (newPlaces[r] != places[r]);
		}
		
		if(change) {
			
			XYSeriesCollection dataset = new XYSeriesCollection();
			XYSeries series = new XYSeries("Place");
			for(int r=0; r<nRounds; r++) {
				if(announced[r]) {	series.add( r+1, newPlaces[r] ); }
				this.places[r] = newPlaces[r];
			}
			dataset.addSeries( series );
			
			JFreeChart chart = this.createChart(dataset);
			XYPlot plot = chart.getXYPlot();
			plot.setBackgroundPaint(BACKGROUND_COLOR);
			
			NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
					
			xAxis.setRange( 0.5, nRounds + 0.5 );
			xAxis.setAutoRange( false );
			xAxis.setTickUnit(new NumberTickUnit(5));
			xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance() );
			xAxis.setLabelFont( xAxis.getLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			xAxis.setTickLabelFont( xAxis.getTickLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			yAxis.setInverted( true );
			yAxis.setRange( 0.5, nTeams + 0.5);
			yAxis.setAutoRange( false );
			yAxis.setTickUnit(new NumberTickUnit(5));
			yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance() );
			yAxis.setLabelFont( yAxis.getLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			yAxis.setTickLabelFont( yAxis.getTickLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			
			XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer)plot.getRenderer();
			rend.setSeriesShapesVisible(0, true);
			rend.setSeriesShape( 0, makeCircle(4) );
			rend.setSeriesPaint( 0, LINE_COLOR );
			rend.setSeriesStroke( 0, new BasicStroke(3.0f) );
						
			if(this.chartPanel != null) {
				this.remove(this.chartPanel);		
			}
			this.chartPanel = new ChartPanel(chart);
			
			GridBagConstraints solo = new GridBagConstraints();		
			solo.fill = GridBagConstraints.BOTH;
			solo.anchor = GridBagConstraints.CENTER;
			solo.weightx = 1.0; solo.weighty = 1.0;
			solo.gridx = 0; solo.gridy = 0;
			
			this.add(chartPanel, solo);
			
		}
		
	}
	
	/**
	 * Make circle.
	 *
	 * @param radius the radius
	 * @return the shape
	 */
	public static Shape makeCircle(double radius) {
		return new Ellipse2D.Double(-radius, -radius, 2*radius, 2*radius);
	}
	

}
