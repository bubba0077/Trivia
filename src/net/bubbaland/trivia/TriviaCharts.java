package net.bubbaland.trivia;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class TriviaCharts {

	/** Font Size */
	final private static float	AXIS_FONT_SIZE		= 16.0f;

	/**
	 * Colors
	 */
	final private static Color	BACKGROUND_COLOR	= Color.BLACK;
	final private static Color	PLACE_COLOR			= Color.ORANGE;
	final private static Color	VALUE_COLOR			= new Color(30, 144, 255);
	final private static Color	EARNED_COLOR		= Color.GREEN;

	/** The upper bound of the vertical axis */
	final private static int	MAX_POINTS			= 750;


	public static JFreeChart PlaceChartFactory(Trivia trivia) {

		final int nRounds = trivia.getNRounds();
		final int nTeams = trivia.getNTeams();

		// Load the round data and determine if there are any changes
		final int[] places = new int[nRounds];
		final boolean[] announced = new boolean[nRounds];
		for (int r = 0; r < nRounds; r++) {
			places[r] = trivia.getAnnouncedPlace(r + 1);
			announced[r] = trivia.isAnnounced(r + 1);
		}

		// Create a new dataset
		final XYSeriesCollection dataset = new XYSeriesCollection();
		// Create a new series
		final XYSeries series = new XYSeries("Place");

		for (int r = 0; r < nRounds; r++) {
			// If the round has been announced, add a new point to the series
			if (announced[r]) {
				series.add(r + 1, places[r]);
			}
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
		rend.setSeriesPaint(0, PLACE_COLOR);
		rend.setSeriesStroke(0, new BasicStroke(3.0f));

		// Set axis properties
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5, nRounds + 0.5);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setInverted(true);
		yAxis.setRange(0.5, nTeams + 0.5);
		yAxis.setAutoRange(false);
		yAxis.setTickUnit(new NumberTickUnit(5));
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

		return chart;
	}


	public static JFreeChart ScoreByRoundChartFactory(Trivia trivia) {

		final int nRounds = trivia.getNRounds();

		// Get the scores for each round and check if they are updated
		final int[] values = new int[nRounds];
		final int[] earneds = new int[nRounds];
		for (int r = 0; r < nRounds; r++) {
			values[r] = trivia.getValue(r + 1);
			earneds[r] = trivia.getEarned(r + 1);
		}

		// Create a new dataset
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		// Create series for the earned and possible points
		final XYSeries valueSeries = new XYSeries("Values", true, false);
		final XYSeries earnedSeries = new XYSeries("Earned", true, false);

		for (int r = 0; r < nRounds; r++) {
			if (values[r] != 0) {
				// If the round has been opened, add the point to the series
				valueSeries.add(r + 1, values[r] - earneds[r]);
				earnedSeries.add(r + 1, earneds[r]);
			}
		}
		// Add the series to the dataset
		dataset.addSeries(earnedSeries);
		dataset.addSeries(valueSeries);

		// Create a new XYBar renderer and format
		final XYBarRenderer renderer = new StackedXYBarRenderer(0.0);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDrawBarOutline(false);
		renderer.setShadowVisible(false);
		renderer.setSeriesPaint(0, EARNED_COLOR);
		renderer.setSeriesPaint(1, VALUE_COLOR);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} Rd {1}: {2}", NumberFormat
				.getIntegerInstance(), NumberFormat.getIntegerInstance()));

		// Create the new bar plot
		final XYPlot plot = new XYPlot(dataset, new NumberAxis("Round"), new NumberAxis("Points"), renderer);

		// Set the background color
		plot.setBackgroundPaint(BACKGROUND_COLOR);

		// Specify the axis parameters
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5, nRounds + 0.5);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setRange(0, MAX_POINTS);
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

		// create a new chart with the plot
		final JFreeChart chart = new JFreeChart(plot);
		chart.setTitle("Points by Round");

		return chart;

	}

	public static JFreeChart CumulativePointsChartFactory(Trivia trivia) {

		final int nRounds = trivia.getNRounds();

		// Read score data and determine if there have been any changes
		final int[] values = new int[nRounds];
		final int[] earneds = new int[nRounds];
		for (int r = 0; r < nRounds; r++) {
			values[r] = trivia.getValue(r + 1);
			earneds[r] = trivia.getEarned(r + 1);
		}

		// Create a new dataset
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		// Create a new series for the earned and possible scores
		final XYSeries valueSeries = new XYSeries("Values", true, false);
		final XYSeries earnedSeries = new XYSeries("Earned", true, false);

		int cumulativeValue = 0;
		int cumulativeEarned = 0;
		for (int r = 0; r < nRounds; r++) {
			// Calculate the cumulative score for each round
			cumulativeValue = cumulativeValue + values[r];
			cumulativeEarned = cumulativeEarned + earneds[r];
			if (values[r] != 0) {
				// If the round has been opened, add the point to the series
				valueSeries.add(r + 1, cumulativeValue - cumulativeEarned);
				earnedSeries.add(r + 1, cumulativeEarned);
			}
			values[r] = values[r];
			earneds[r] = earneds[r];
		}

		// Add the series to the dataset
		dataset.addSeries(earnedSeries);
		dataset.addSeries(valueSeries);

		// Create the Stacked XY plot
		final JFreeChart chart = ChartFactory.createStackedXYAreaChart("Cumulative Score", "Round", "Points", dataset,
				PlotOrientation.VERTICAL, true, true, false);

		// Set the colors of the areas
		final XYItemRenderer renderer = chart.getXYPlot().getRenderer();
		renderer.setSeriesPaint(0, EARNED_COLOR);
		renderer.setSeriesPaint(1, VALUE_COLOR);

		// Set the background color
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(BACKGROUND_COLOR);

		// Set axis properties
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5, nRounds + 0.5);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setLowerBound(0);
		yAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

		return chart;
	}

	public static JFreeChart TeamComparisonChartFactory(Trivia trivia) {

		final int nRounds = trivia.getNRounds();

		int lastAnnounced = 0;
		ArrayList<ScoreEntry[]> scores = new ArrayList<ScoreEntry[]>(0);

		while (trivia.isAnnounced(lastAnnounced + 1)) {
			lastAnnounced++;
			final ScoreEntry roundStandings[] = trivia.getStandings(lastAnnounced);
			Arrays.sort(roundStandings);
			scores.add(roundStandings);
		}

		final int nTeams = scores.get(0).length;
		final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		final JFreeChart chart = ChartFactory.createXYLineChart("Team Comparison", "Round", "Point Differential",
				dataset, PlotOrientation.VERTICAL, false, true, false);
		final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
		for (int t = 0; t < nTeams; t++) {
			final String team = scores.get(0)[t].getTeamName();
			final XYSeries series = new XYSeries(team, true, false);
			for (int r = 0; r < lastAnnounced; r++) {
				final int ourScore = trivia.getAnnouncedPoints(r + 1);
				series.add(r + 1, scores.get(r)[t].getScore() - ourScore);
			}

			renderer.setSeriesShapesVisible(t, true);
			renderer.setSeriesShape(t, makeCircle(2D));
			if (team.equals(trivia.getTeamName())) {
				renderer.setSeriesStroke(t, new BasicStroke(3F));
				renderer.setSeriesPaint(t, Color.WHITE);
			}
			dataset.addSeries(series);
		}

		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} Rd {1}: {2}", NumberFormat
				.getIntegerInstance(), NumberFormat.getIntegerInstance()));
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(BACKGROUND_COLOR);
		final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		xAxis.setRange(0.5D, nRounds + 0.5D);
		xAxis.setAutoRange(false);
		xAxis.setTickUnit(new NumberTickUnit(5D));
		xAxis.setNumberFormatOverride(NumberFormat.getIntegerInstance());
		xAxis.setLabelFont(xAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		xAxis.setTickLabelFont(xAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));
		final DecimalFormat format = new DecimalFormat();
		format.setPositivePrefix("+");
		yAxis.setNumberFormatOverride(format);
		yAxis.setLabelFont(yAxis.getLabelFont().deriveFont(AXIS_FONT_SIZE));
		yAxis.setTickLabelFont(yAxis.getTickLabelFont().deriveFont(AXIS_FONT_SIZE));

		return chart;

	}


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

}
