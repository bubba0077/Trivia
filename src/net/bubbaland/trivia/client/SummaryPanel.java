package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.bubbaland.trivia.Trivia;

/**
 * A panel which displays summary information of the trivia contest.
 * 
 * The <code>HeaderPanel</code> class is a panel that contains summary information about the current state of the trivia
 * contest and of the current round. It also provides buttons to make the current round a speed round (or not) and
 * advance to a new round.
 * 
 * @author Walter Kolczynski
 * 
 */
public class SummaryPanel extends TriviaPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 3544918496657028139L;

	/**
	 * Colors
	 */
	private static Color		backgroundColor;
	private static Color		speedColor;
	private static Color		conflictColor;

	/**
	 * GUI Elements that will need to be updated
	 */
	private final JPanel		emptyPanel, buttonPanel;
	private final JLabel		roundHeaderLabel, totalHeaderLabel, teamNameLabel, earnedRowLabel, valueRowLabel;
	private final JLabel		roundEarnedLabel, roundValueLabel, totalEarnedLabel;
	private final JLabel		totalValueLabel, announcedLabel, placeLabel;
	private final JLabel		announcedBannerLabel, scoreTextLabel, placeTextLabel;
	private final JLabel		currentHourLabel;
	private final JToggleButton	speedButton;
	private final JButton		newRoundButton, conflictButton;
	private final UserListPanel	userListPanel;

	/**
	 * Data sources
	 */
	private final TriviaClient	client;

	/**
	 * Instantiates a new header panel.
	 * 
	 * @param server
	 *            The remote trivia server
	 * @param client
	 *            The local trivia client
	 */
	public SummaryPanel(TriviaClient client) {

		super();

		this.client = client;

		// Set up layout constraints
		final GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.anchor = GridBagConstraints.CENTER;
		buttonConstraints.weightx = 1.0;
		buttonConstraints.weighty = 1.0;
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 0;
		buttonConstraints.fill = GridBagConstraints.NONE;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 1.0;

		/**
		 * Top row
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.emptyPanel = new JPanel(new GridBagLayout());
		this.add(this.emptyPanel, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.roundHeaderLabel = this.enclosedLabel("Round", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.totalHeaderLabel = this.enclosedLabel("Total", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		this.teamNameLabel = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		this.announcedBannerLabel = this.enclosedLabel("Last Round ", constraints, SwingConstants.RIGHT,
				SwingConstants.CENTER);

		this.conflictButton = new JButton("Conflict!");
		this.conflictButton.setMargin(new Insets(0, 0, 0, 0));
		this.conflictButton.setVisible(false);
		this.announcedBannerLabel.getParent().add(this.conflictButton, buttonConstraints);
		this.conflictButton.addActionListener(this);

		constraints.gridwidth = 1;


		/**
		 * Middle row
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.earnedRowLabel = this.enclosedLabel("Earned", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 1;
		this.roundEarnedLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 1;
		this.totalEarnedLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 1;
		this.currentHourLabel = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 1;
		this.scoreTextLabel = this.enclosedLabel("Points ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 5;
		constraints.gridy = 1;
		this.announcedLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		/**
		 * Bottom row
		 */
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.valueRowLabel = this.enclosedLabel("Possible", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 1;
		constraints.gridy = 2;
		this.roundValueLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 2;
		constraints.gridy = 2;
		this.totalValueLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 2;

		// Put both the speed button and new round button in the same place, we'll hide the one we don't need
		this.buttonPanel = new JPanel(new GridBagLayout());
		this.add(this.buttonPanel, constraints);

		this.speedButton = new JToggleButton("");
		this.speedButton.setMargin(new Insets(0, 0, 0, 0));
		this.speedButton.setVisible(true);
		this.buttonPanel.add(this.speedButton, buttonConstraints);
		this.speedButton.addActionListener(this);

		this.newRoundButton = new JButton("New Round");
		this.newRoundButton.setMargin(new Insets(0, 0, 0, 0));
		this.newRoundButton.setVisible(false);
		this.buttonPanel.add(this.newRoundButton, buttonConstraints);
		this.newRoundButton.addActionListener(this);

		constraints.gridx = 4;
		constraints.gridy = 2;
		this.placeTextLabel = this.enclosedLabel("Place ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 5;
		constraints.gridy = 2;
		this.placeLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 6;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.userListPanel = new UserListPanel(client);
		this.add(this.userListPanel, constraints);

		this.loadProperties();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		final JComponent source = (JComponent) event.getSource();
		if (source.equals(this.speedButton)) {
			// Speed button changed
			if (this.speedButton.isSelected()) {
				// Speed button now pressed, tell server
				int tryNumber = 0;
				boolean success = false;
				while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
						&& success == false) {
					tryNumber++;
					try {
						this.client.getServer().setSpeed(this.client.getUser());
						success = true;
					} catch (final Exception e) {
						this.client.log("Couldn't make this a speed round (try #" + tryNumber + ").");
					}

					if (!success) {
						this.client.disconnected();
						return;
					}

					this.client.log("Made this a speed round.");
				}

			} else {
				// Speed button now not pressed, tell server
				int tryNumber = 0;
				boolean success = false;
				while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries"))
						&& success == false) {
					tryNumber++;
					try {
						this.client.getServer().unsetSpeed(this.client.getUser());
						success = true;
					} catch (final RemoteException e) {
						this.client.log("Couldn't make this a normal round (try #" + tryNumber + ").");
						return;
					}
				}

				if (!success) {
					this.client.disconnected();
					return;
				}

				this.client.log("Made this a normal round");

			}
		} else if (source.equals(this.newRoundButton)) {
			// New round button pressed, tell server
			int tryNumber = 0;
			boolean success = false;
			while (tryNumber < Integer.parseInt(TriviaClient.PROPERTIES.getProperty("MaxRetries")) && success == false) {
				tryNumber++;
				try {
					this.client.getServer().newRound(this.client.getUser());
					success = true;
				} catch (final Exception e) {
					this.client.log("Couldn't get current round number from server (try #" + tryNumber + ").");
				}

			}

			if (!success) {
				this.client.log("Connection failed!");
				return;
			}

			this.client.log("Started new round");

		} else if (source.equals(this.conflictButton)) {
			new ConflictDialog(this.client);
		}

	}

	public void loadProperties() {
		/**
		 * Colors
		 */
		backgroundColor = new Color(
				Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.BackgroundColor"), 16));
		final Color labelColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Label.Color"),
				16));
		final Color earnedColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Earned.Color"), 16));
		final Color valueColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Value.Color"), 16));
		final Color announcedColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Announced.Color"),
				16));
		speedColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Speed.Color"), 16));
		final Color newRoundColor = new Color(Integer.parseInt(
				TriviaClient.PROPERTIES.getProperty("Summary.NewRound.Color"), 16));
		conflictColor = new Color(Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Conflict.Color"), 16));

		/**
		 * Sizes
		 */
		final int topRowHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.TopRow.Height"));
		final int middleRowHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.MiddleRow.Height"));
		final int bottomRowHeight = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.BottomRow.Height"));

		final int col0width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Col0.Width"));
		final int col1width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Col1.Width"));
		final int col2width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Col2.Width"));
		final int col3width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Col3.Width"));
		final int col4width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Col4.Width"));
		final int col5width = Integer.parseInt(TriviaClient.PROPERTIES.getProperty("Summary.Col5.Width"));

		/**
		 * Font sizes
		 */
		final float labelFontSize = Float.parseFloat(TriviaClient.PROPERTIES.getProperty("Summary.Label.FontSize"));
		final float scoreFontSize = Float.parseFloat(TriviaClient.PROPERTIES.getProperty("Summary.Score.FontSize"));

		/**
		 * Button sizes
		 */
		final int centerButtonWidth = Integer.parseInt(TriviaClient.PROPERTIES
				.getProperty("Summary.CenterButton.Width"));
		final int centerButtonHeight = Integer.parseInt(TriviaClient.PROPERTIES
				.getProperty("Summary.CenterButton.Height"));
		final int conflictButtonWidth = Integer.parseInt(TriviaClient.PROPERTIES
				.getProperty("Summary.ConflictButton.Width"));
		final int conflictButtonHeight = Integer.parseInt(TriviaClient.PROPERTIES
				.getProperty("Summary.ConflictButton.Height"));

		setPanelProperties(this.emptyPanel, col0width, topRowHeight, backgroundColor);
		setLabelProperties(this.roundHeaderLabel, col1width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.totalHeaderLabel, col2width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.teamNameLabel, col3width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.announcedBannerLabel, col4width + col5width, topRowHeight, announcedColor,
				backgroundColor, labelFontSize);

		setLabelProperties(this.earnedRowLabel, col0width, middleRowHeight, earnedColor, backgroundColor, labelFontSize);
		setLabelProperties(this.roundEarnedLabel, col1width, middleRowHeight, earnedColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.totalEarnedLabel, col2width, middleRowHeight, earnedColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.currentHourLabel, col3width, middleRowHeight, labelColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.scoreTextLabel, col4width, middleRowHeight, announcedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.announcedLabel, col5width, middleRowHeight, announcedColor, backgroundColor,
				labelFontSize);

		setLabelProperties(this.valueRowLabel, col0width, bottomRowHeight, valueColor, backgroundColor, labelFontSize);
		setLabelProperties(this.roundValueLabel, col1width, bottomRowHeight, valueColor, backgroundColor, scoreFontSize);
		setLabelProperties(this.totalValueLabel, col2width, bottomRowHeight, valueColor, backgroundColor, scoreFontSize);
		setPanelProperties(this.buttonPanel, col3width, bottomRowHeight, backgroundColor);

		setLabelProperties(this.placeTextLabel, col4width, bottomRowHeight, announcedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.placeLabel, col5width, bottomRowHeight, announcedColor, backgroundColor, labelFontSize);

		setButtonProperties(this.speedButton, centerButtonWidth, centerButtonHeight, labelFontSize, null);
		setButtonProperties(this.newRoundButton, centerButtonWidth, centerButtonHeight, labelFontSize, newRoundColor);
		setButtonProperties(this.conflictButton, conflictButtonWidth, conflictButtonHeight, labelFontSize, null);

		this.userListPanel.setBackground(backgroundColor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update(boolean force) {
		// Get the current Trivia object from the client
		final Trivia trivia = this.client.getTrivia();

		// Get the current round
		final int currentRound = trivia.getCurrentRoundNumber();

		// Update all the labels to match the current data
		this.teamNameLabel.setText(client.getTrivia().getTeamName());
		this.roundEarnedLabel.setText("" + trivia.getCurrentRoundEarned());
		this.totalEarnedLabel.setText("" + trivia.getEarned());
		this.roundValueLabel.setText("" + trivia.getCurrentRoundValue());
		this.totalValueLabel.setText("" + trivia.getValue());
		this.currentHourLabel.setText("Current Round: " + currentRound);

		// Only show announced values once they've been announced
		if (trivia.isAnnounced(currentRound - 1)) {
			final int announcedPoints = trivia.getAnnouncedPoints(currentRound - 1);
			this.announcedLabel.setText("" + announcedPoints);
			this.placeLabel.setText("" + TriviaClient.ordinalize(trivia.getAnnouncedPlace(currentRound - 1)));
			if (announcedPoints != trivia.getCumulativeEarned(currentRound - 1)) {
				this.announcedBannerLabel.getParent().setBackground(conflictColor);
				this.scoreTextLabel.getParent().setBackground(conflictColor);
				this.placeTextLabel.getParent().setBackground(conflictColor);
				this.announcedLabel.getParent().setBackground(conflictColor);
				this.placeLabel.getParent().setBackground(conflictColor);

				this.announcedBannerLabel.setVisible(false);
				this.conflictButton.setVisible(true);
			} else {
				this.announcedBannerLabel.getParent().setBackground(backgroundColor);
				this.scoreTextLabel.getParent().setBackground(backgroundColor);
				this.placeTextLabel.getParent().setBackground(backgroundColor);
				this.announcedLabel.getParent().setBackground(backgroundColor);
				this.placeLabel.getParent().setBackground(backgroundColor);

				this.announcedBannerLabel.setVisible(true);
				this.conflictButton.setVisible(false);
			}

		} else {
			this.announcedLabel.setText("");
			this.placeLabel.setText("");
		}

		// If the round is over, hide speed round button and show new round button
		if (trivia.roundOver()) {
			this.speedButton.setVisible(false);
			this.newRoundButton.setVisible(true);
		} else {
			this.speedButton.setVisible(true);
			this.newRoundButton.setVisible(false);
			if (trivia.isCurrentSpeed()) {
				this.speedButton.setText("Speed");
				this.speedButton.setSelected(true);
				this.speedButton.setForeground(speedColor);
			} else {
				this.speedButton.setText("Normal");
				this.speedButton.setSelected(false);
				this.speedButton.setForeground(Color.BLACK);
			}
		}
		this.userListPanel.update(force);

	}

}
