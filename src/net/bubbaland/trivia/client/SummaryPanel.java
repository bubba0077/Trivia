package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;
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
public class SummaryPanel extends TriviaMainPanel implements ActionListener {

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
	private final JPanel		emptyPanel, emptyPanel2, buttonPanel;
	private final JLabel		roundHeaderLabel, totalHeaderLabel, teamNameLabel, earnedRowLabel, valueRowLabel;
	private final JLabel		roundEarnedLabel, roundValueLabel, totalEarnedLabel;
	private final JLabel		totalValueLabel, announcedLabel, placeLabel;
	private final JLabel		announcedBannerLabel, scoreTextLabel, placeTextLabel;
	private final JLabel		currentHourLabel, showNameLabel, showHostLabel;
	private final JTextField	showNameTextField, showHostTextField;
	private final JToggleButton	speedButton;
	private final JButton		newRoundButton, conflictButton;
	private final UserListPanel	userListPanel;
	private final JPopupMenu	showNameMenu, showHostMenu;

	/**
	 * Instantiates a new header panel.
	 *
	 * @param client
	 *            The local trivia client
	 */
	public SummaryPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

		/**
		 * Build context menu
		 */
		this.showNameMenu = new JPopupMenu();
		this.showNameTextField = new JTextField();
		this.showNameTextField.setPreferredSize(new Dimension(100, 25));
		this.showNameTextField.setActionCommand("Set Show Name");
		this.showNameTextField.addActionListener(this);
		this.showNameMenu.add(this.showNameTextField);
		this.add(this.showNameMenu);

		this.showHostMenu = new JPopupMenu();
		this.showHostTextField = new JTextField();
		this.showHostTextField.setPreferredSize(new Dimension(100, 25));
		this.showHostTextField.setActionCommand("Set Show Host");
		this.showHostTextField.addActionListener(this);
		this.showHostMenu.add(this.showHostTextField);
		this.add(this.showHostMenu);

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
		this.roundHeaderLabel = this.enclosedLabel("Round", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.totalHeaderLabel = this.enclosedLabel("Total", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.weightx = 1.0;
		this.teamNameLabel = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 6;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		this.announcedBannerLabel = this.enclosedLabel("Last Round ", constraints, SwingConstants.RIGHT,
				SwingConstants.CENTER);

		this.conflictButton = new JButton("Conflict!");
		this.conflictButton.setMargin(new Insets(0, 0, 0, 0));
		this.conflictButton.setVisible(false);
		this.announcedBannerLabel.getParent().add(this.conflictButton, buttonConstraints);
		this.conflictButton.setActionCommand("Conflict");
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

		constraints.gridx = 3;
		constraints.gridy = 1;
		this.totalEarnedLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		this.currentHourLabel = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 5;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		this.showNameLabel = this.enclosedLabel("Show: ", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		this.showNameLabel.addMouseListener(new PopupListener(this.showNameMenu));
		constraints.weightx = 0.0;

		constraints.gridx = 6;
		constraints.gridy = 1;
		this.scoreTextLabel = this.enclosedLabel("Points ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 7;
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

		constraints.gridx = 3;
		constraints.gridy = 2;
		this.totalValueLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 2;
		// Put both the speed button and new round button in the same place, we'll hide the one we don't need
		this.buttonPanel = new JPanel(new GridBagLayout());
		this.add(this.buttonPanel, constraints);

		this.speedButton = new JToggleButton("");
		this.speedButton.setMargin(new Insets(0, 0, 0, 0));
		this.speedButton.setVisible(true);
		this.buttonPanel.add(this.speedButton, buttonConstraints);
		this.speedButton.setActionCommand("Change Speed");
		this.speedButton.addActionListener(this);

		this.newRoundButton = new JButton("New Round");
		this.newRoundButton.setMargin(new Insets(0, 0, 0, 0));
		this.newRoundButton.setVisible(false);
		this.buttonPanel.add(this.newRoundButton, buttonConstraints);
		this.newRoundButton.setActionCommand("New Round");
		this.newRoundButton.addActionListener(this);

		constraints.gridx = 5;
		constraints.gridy = 2;
		this.showHostLabel = this.enclosedLabel("Host: ", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
		this.showHostLabel.addMouseListener(new PopupListener(this.showHostMenu));

		constraints.gridx = 6;
		constraints.gridy = 2;
		this.placeTextLabel = this.enclosedLabel("Place ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 7;
		constraints.gridy = 2;
		this.placeLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 8;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.userListPanel = new UserListPanel(client, parent);
		this.add(this.userListPanel, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.emptyPanel2 = new JPanel(new GridBagLayout());
		this.emptyPanel2.setPreferredSize(new Dimension(10, 0));
		this.add(this.emptyPanel2, constraints);

		this.loadProperties(TriviaGUI.PROPERTIES);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
			case "Change Speed":
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						SummaryPanel.this.client
								.sendMessage(ClientMessageFactory.setSpeed(SummaryPanel.this.speedButton.isSelected()));
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				if (this.speedButton.isSelected()) {
					this.client.log("Made this a speed round.");
				} else {
					this.client.log("Made this a normal round");
				}
				break;
			case "New Round":
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						SummaryPanel.this.client.sendMessage(ClientMessageFactory.advanceRound());
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				this.client.log("Started new round");
				break;
			case "Conflict":
				new ConflictDialog(this.client);
				break;
			case "Set Show Name":
				final String showName = this.showNameTextField.getText();
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						SummaryPanel.this.client.sendMessage(ClientMessageFactory
								.setShowName(SummaryPanel.this.client.getTrivia().getCurrentRoundNumber(), showName));
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				this.client.log("Changed show name to " + showName);
				break;
			case "Set Show Host":
				final String showHost = this.showHostTextField.getText();
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						SummaryPanel.this.client.sendMessage(ClientMessageFactory
								.setShowHost(SummaryPanel.this.client.getTrivia().getCurrentRoundNumber(), showHost));
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				this.client.log("Changed host name to " + showHost);
				break;
		}
	}

	@Override
	public void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		backgroundColor = new Color(new BigInteger(properties.getProperty("Summary.BackgroundColor"), 16).intValue());
		final Color labelColor = new Color(
				new BigInteger(properties.getProperty("Summary.Label.Color"), 16).intValue());
		final Color earnedColor = new Color(new BigInteger(properties.getProperty("Earned.Color"), 16).intValue());
		final Color valueColor = new Color(new BigInteger(properties.getProperty("Value.Color"), 16).intValue());
		final Color announcedColor = new Color(
				new BigInteger(properties.getProperty("Announced.Color"), 16).intValue());
		speedColor = new Color(new BigInteger(properties.getProperty("Summary.Speed.Color"), 16).intValue());
		final Color newRoundColor = new Color(
				new BigInteger(properties.getProperty("Summary.NewRound.Color"), 16).intValue());
		conflictColor = new Color(new BigInteger(properties.getProperty("Summary.Conflict.Color"), 16).intValue());

		/**
		 * Sizes
		 */
		final int topRowHeight = Integer.parseInt(properties.getProperty("Summary.TopRow.Height"));
		final int middleRowHeight = Integer.parseInt(properties.getProperty("Summary.MiddleRow.Height"));
		final int bottomRowHeight = Integer.parseInt(properties.getProperty("Summary.BottomRow.Height"));

		final int col0width = Integer.parseInt(properties.getProperty("Summary.Col0.Width"));
		final int col1width = Integer.parseInt(properties.getProperty("Summary.Col1.Width"));
		final int col2width = Integer.parseInt(properties.getProperty("Summary.Col2.Width"));
		final int col3width = Integer.parseInt(properties.getProperty("Summary.Col3.Width"));
		final int col4width = Integer.parseInt(properties.getProperty("Summary.Col4.Width"));
		final int col5width = Integer.parseInt(properties.getProperty("Summary.Col5.Width"));
		final int col6width = Integer.parseInt(properties.getProperty("Summary.Col6.Width"));

		/**
		 * Font sizes
		 */
		final float labelFontSize = Float.parseFloat(properties.getProperty("Summary.Label.FontSize"));
		final float scoreFontSize = Float.parseFloat(properties.getProperty("Summary.Score.FontSize"));

		/**
		 * Button sizes
		 */
		final int centerButtonWidth = Integer.parseInt(properties.getProperty("Summary.CenterButton.Width"));
		final int centerButtonHeight = Integer.parseInt(properties.getProperty("Summary.CenterButton.Height"));
		final int conflictButtonWidth = Integer.parseInt(properties.getProperty("Summary.ConflictButton.Width"));
		final int conflictButtonHeight = Integer.parseInt(properties.getProperty("Summary.ConflictButton.Height"));

		setPanelProperties(this.emptyPanel, col0width, topRowHeight, backgroundColor);
		setLabelProperties(this.roundHeaderLabel, col1width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.totalHeaderLabel, col2width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.teamNameLabel, col3width + col4width, topRowHeight, labelColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.announcedBannerLabel, col5width + col6width, topRowHeight, announcedColor,
				backgroundColor, labelFontSize);

		setLabelProperties(this.earnedRowLabel, col0width, middleRowHeight, earnedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.roundEarnedLabel, col1width, middleRowHeight, earnedColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.totalEarnedLabel, col2width, middleRowHeight, earnedColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.currentHourLabel, col3width, middleRowHeight, labelColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.showNameLabel, col4width, middleRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.scoreTextLabel, col5width, middleRowHeight, announcedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.announcedLabel, col6width, middleRowHeight, announcedColor, backgroundColor,
				labelFontSize);

		setLabelProperties(this.valueRowLabel, col0width, bottomRowHeight, valueColor, backgroundColor, labelFontSize);
		setLabelProperties(this.roundValueLabel, col1width, bottomRowHeight, valueColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.totalValueLabel, col2width, bottomRowHeight, valueColor, backgroundColor,
				scoreFontSize);

		setPanelProperties(this.buttonPanel, col3width, bottomRowHeight, backgroundColor);
		setButtonProperties(this.speedButton, centerButtonWidth, centerButtonHeight, null, labelFontSize);
		setButtonProperties(this.newRoundButton, centerButtonWidth, centerButtonHeight, null, labelFontSize);
		this.newRoundButton.setBackground(newRoundColor);
		setButtonProperties(this.conflictButton, conflictButtonWidth, conflictButtonHeight, null, labelFontSize);

		setLabelProperties(this.showHostLabel, col4width, bottomRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.placeTextLabel, col5width, bottomRowHeight, announcedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.placeLabel, col6width, bottomRowHeight, announcedColor, backgroundColor, labelFontSize);

		this.userListPanel.setBackground(backgroundColor);
		this.emptyPanel2.setBackground(backgroundColor);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		// Get the current Trivia object from the client
		final Trivia trivia = this.client.getTrivia();

		// Get the current round
		final int currentRound = trivia.getCurrentRoundNumber();

		// Update all the labels to match the current data
		this.teamNameLabel.setText(
				this.client.getTrivia().getTeamName() + " (# " + this.client.getTrivia().getTeamNumber() + ")");
		this.showNameLabel.setText("Show: " + this.client.getTrivia().getShowName(currentRound));
		this.showNameTextField.setText(this.client.getTrivia().getShowName(currentRound));
		this.showHostLabel.setText("Host: " + this.client.getTrivia().getShowHost(currentRound));
		this.showHostTextField.setText(this.client.getTrivia().getShowHost(currentRound));
		this.roundEarnedLabel.setText("" + trivia.getCurrentRoundEarned());
		this.totalEarnedLabel.setText("" + trivia.getEarned());
		this.roundValueLabel.setText("" + trivia.getCurrentRoundValue());
		this.totalValueLabel.setText("" + trivia.getValue());
		this.currentHourLabel.setText("Current Round: " + currentRound);

		// Only show announced values once they've been announced
		if (trivia.isAnnounced(currentRound - 1)) {
			final int announcedPoints = trivia.getAnnouncedPoints(currentRound - 1);
			this.announcedLabel.setText("" + announcedPoints);
			this.placeLabel.setText("" + TriviaGUI.ordinalize(trivia.getAnnouncedPlace(currentRound - 1)));
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
			this.announcedBannerLabel.getParent().setBackground(backgroundColor);
			this.scoreTextLabel.getParent().setBackground(backgroundColor);
			this.placeTextLabel.getParent().setBackground(backgroundColor);
			this.announcedLabel.getParent().setBackground(backgroundColor);
			this.placeLabel.getParent().setBackground(backgroundColor);

			this.announcedBannerLabel.setVisible(true);
			this.conflictButton.setVisible(false);
		}

		// If the round is over, hide speed round button and show new round button
		if (trivia.roundOver() && trivia.getCurrentRoundNumber() < trivia.getNRounds()) {
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
		this.userListPanel.updateGUI(force);
	}

	private class PopupListener extends MouseAdapter {

		private final JPopupMenu menu;

		public PopupListener(JPopupMenu menu) {
			this.menu = menu;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.checkForPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.checkForPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.checkForPopup(e);
		}

		private void checkForPopup(MouseEvent event) {
			final JComponent source = (JComponent) event.getSource();
			if (event.isPopupTrigger()) {
				this.menu.show(source, event.getX(), event.getY());
			}
		}

	}

}
