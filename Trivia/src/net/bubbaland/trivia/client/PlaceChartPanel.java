package net.bubbaland.trivia.client;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;

import net.bubbaland.trivia.Trivia;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A panel with a chart showing announced place by round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class PlaceChartPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	final private static long	serialVersionUID	= -4937894497657719097L;

	/** Font Size */
	final private static float	AXIS_FONT_SIZE		= 16.0f;

	/**
	 * Colors
	 */
	final private static Color	BACKGROUND_COLOR	= Color.BLACK;
	final private static Color	LINE_COLOR			= Color.ORANGE;

	/** Data */
	private final int			nRounds, nTeams;
	private final int[]			places;

	/** The chart panel */
	private ChartPanel			chartPanel;

	/** The local client */
	final private TriviaClient	client;

	/**
	 * Make circle.
	 * 
	 * @param radius
	 *            the radius
	 * @return the shape
	 */
	public static Shape makeCircle(double radius) {
		return new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius);
	}

	/**
	 * Instantiates a new place chart
	 * 
	 * @param client
	 *            The local trivia client
	 * 
	 */
	public PlaceChartPanel(TriviaClient client) {

		super();

		this.client = client;
		this.nRounds = client.getTrivia().getNRounds();
		this.nTeams = client.getTrivia().getNTeams();

		this.places = new int[this.nRounds];

		this.chartPanel = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {

		// Get the current Trivia data object
		final Trivia trivia = this.client.getTrivia();

		// Load the round data and determine if there are any changes
		final int[] newPlaces = new int[this.nRounds];
		final boolean[] announced = new boolean[this.nRounds];
		boolean change = false;
		for (int r = 0; r < this.nRounds; r++) {
			newPlaces[r] = trivia.getAnnouncedPlace(r + 1);
			announced[r] = trivia.isAnnounced(r + 1);
			change = change || ( newPlaces[r] != this.places[r] );
		}

		// If there are changes, remake the plot
		if (change) {
			// Create a new dataset
			final XYSeriesCollection dataset = new XYSeriesCollection();
			// Create a new series
			final XYSeries series = new XYSeries("Place");

			for (int r = 0; r < this.nRounds; r++) {
				// If the round has been announced, add a new point to the series
				if (announced[r]) {
					series.add(r + 1, newPlaces[r]);
				}
				this.places[r] = newPlaces[r];
			}
			// Add the series to the dataset
			dataset.addSeries(series);

			// Create the XY plot
			final JFreeChart chart = ChartFactory.createXYLineChart("Place by Round", "Round", "Place", dataset,
					PlotOrientation.VERTICAL, false, true, false);

			// Set the background color
			final XYPlot plot = chart.getXYPlot();
			plot.setBackgroundPaint(BACKGROUND_COLOR);

			// Set the line color and thickness, and turn on data points
			final XYLineAndShapeRenderer rend = (XYLineAndShapeRenderer) plot.getRenderer();
			rend.setSeriesShapesVisible(0, true);
			rend.setSeriesShape(0, makeCircle(4));
			rend.setSeriesPaint(0, LINE_COLOR);
			rend.setSeriesStroke(0, new BasicStroke(3.0f));

			// Set axis properties
			final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
			final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
			xAxis.setRange(0.5, this.nRounds + 0.5);
			xAxis.setAutoRange(false);
			xAxis.setTickUnit(new NumberTickUnit(5));
			xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
			xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
			xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
			yAxis.setInverted(true);
			yAxis.setRange(0.5, this.nTeams + 0.5);
			yAxis.setAutoRange(false);
			yAxis.setTickUnit(new NumberTickUnit(5));
			yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
			yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
			yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

			// If there is an old chart panel, remove it
			if (this.chartPanel != null) {
				this.remove(this.chartPanel);
			}

			// Create a new chart panel
			this.chartPanel = new ChartPanel(chart);

			// Add the new chart to the panel
			final GridBagConstraints solo = new GridBagConstraints();
			solo.fill = GridBagConstraints.BOTH;
			solo.anchor = GridBagConstraints.CENTER;
			solo.weightx = 1.0;
			solo.weighty = 1.0;
			solo.gridx = 0;
			solo.gridy = 0;
			this.add(this.chartPanel, solo);

		}

	}


}
