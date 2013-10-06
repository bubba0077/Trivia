package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.NumberFormat;

import net.bubbaland.trivia.Trivia;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

// TODO: Auto-generated Javadoc
/**
 * The Class CumulativePointsChartPanel.
 */
public class CumulativePointsChartPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -6171512617495834445L;
	
	/** The Constant BACKGROUND_COLOR. */
	final private static Color BACKGROUND_COLOR = Color.BLACK;
	
	/** The Constant VALUE_COLOR. */
	final private static Color VALUE_COLOR = new Color(30, 144, 255);
	
	/** The Constant EARNED_COLOR. */
	final private static Color EARNED_COLOR = Color.GREEN;
	
	/** The Constant AXIS_FONT_SIZE. */
	final private static float AXIS_FONT_SIZE = 16.0f;
	
	/** The Constant MAX_POINTS. */
	final private static int MAX_POINTS = 750;
	
	/** The client. */
	private TriviaClient client;
	
	/** The n rounds. */
	private int nRounds;
	
	/** The earneds. */
	private int[] values, earneds;
	
	/** The chart panel. */
	private ChartPanel chartPanel;
		
	/**
	 * Instantiates a new cumulative points chart panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public CumulativePointsChartPanel( TriviaClient client ) {
		super( new GridBagLayout() );
		
		this.client = client;
		
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			try {
				this.nRounds = client.getTrivia().getNRounds();
				success = true;
			} catch ( Exception e ) {
				client.log( "Couldn't retrieve number of rounds from server (try #" + tryNumber + ")." );
			}			
		}

		if ( !success ) {
			client.disconnected();
			return;
		}
		
		values = new int[nRounds];
		earneds = new int[nRounds];
				
		chartPanel = null;
		
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		
		Trivia trivia = client.getTrivia();
		
		int[] newValues = new int[nRounds];
		int[] newEarneds = new int[nRounds];
		boolean change = false;
		
		for(int r=0; r<nRounds; r++) {
			newValues[r] = trivia.getValue( r+1 );
			newEarneds[r] = trivia.getEarned( r+1 );
			change = change || (newValues[r] != values[r]) || (newEarneds[r] != earneds[r]);
		}		
		
		if(change) {
			
			DefaultTableXYDataset dataset = new DefaultTableXYDataset();
			XYSeries valueSeries = new XYSeries("Values", true, false);
			XYSeries earnedSeries = new XYSeries("Earned", true, false);
			int cumulativeValue = 0;
			int cumulativeEarned = 0;
			for(int r=0; r<nRounds; r++) {
				cumulativeValue = cumulativeValue + newValues[r];
				cumulativeEarned = cumulativeEarned + newEarneds[r];				
				if(newValues[r] != 0) {					
					valueSeries.add( r+1, cumulativeValue - cumulativeEarned);
					earnedSeries.add( r+1, cumulativeEarned );				
				}
				this.values[r] = newValues[r];
				this.earneds[r] = newEarneds[r];				
			}
			
			dataset.addSeries(earnedSeries);
			dataset.addSeries(valueSeries);

			JFreeChart chart = ChartFactory.createStackedXYAreaChart("Cumulative Score", "Round", "Points",dataset, PlotOrientation.VERTICAL, true, true, false);
			XYItemRenderer renderer = chart.getXYPlot().getRenderer();
			renderer.setSeriesPaint( 0, EARNED_COLOR );
			renderer.setSeriesPaint( 1, VALUE_COLOR );
						
			XYPlot plot = chart.getXYPlot();
			plot.setBackgroundPaint(BACKGROUND_COLOR);			
			
			NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
			xAxis.setRange( 0, nRounds+0.5 );
			xAxis.setAutoRange( false );
			xAxis.setTickUnit(new NumberTickUnit(5));
			xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance() );
			xAxis.setLabelFont( xAxis.getLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			xAxis.setTickLabelFont( xAxis.getTickLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			yAxis.setRange( 0, MAX_POINTS );
			yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance() );
			yAxis.setLabelFont( yAxis.getLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			yAxis.setTickLabelFont( yAxis.getTickLabelFont().deriveFont( AXIS_FONT_SIZE ) );
			
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

}
