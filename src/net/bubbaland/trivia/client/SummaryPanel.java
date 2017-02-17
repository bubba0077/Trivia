package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Painter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.messages.SetRoundMessage;
import net.bubbaland.trivia.messages.SetShowHostMessage;
import net.bubbaland.trivia.messages.SetShowNameMessage;
import net.bubbaland.trivia.messages.SetSpeedRoundMessage;

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
public class SummaryPanel extends TriviaMainPanel implements ActionListener, FocusListener {

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
	private final JPanel		emptyPanel, emptyPanel2, speedButtonPanel, showButtonPanel, hostButtonPanel;
	private final JLabel		roundHeaderLabel, totalHeaderLabel, teamNameLabel, earnedRowLabel, valueRowLabel;
	private final JLabel		roundEarnedLabel, roundValueLabel, totalEarnedLabel;
	private final JLabel		totalValueLabel, announcedLabel, placeLabel;
	private final JLabel		announcedBannerLabel, scoreTextLabel, placeTextLabel;
	private final JLabel		currentHourLabel;
	private final JTextField	showNameTextField, showHostTextField;
	private final JToggleButton	speedButton, showNameButton, showHostButton;
	private final JButton		newRoundButton, conflictButton;
	private final UserListPanel	userListPanel;

	private final UIDefaults	textFieldOverrides;


	/**
	 * Instantiates a new header panel.
	 *
	 * @param client
	 *            The local trivia client
	 */
	public SummaryPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

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

		// JTextField overrides for disabled state
		this.textFieldOverrides = new UIDefaults();
		this.textFieldOverrides.put("TextField[Disabled].backgroundPainter", new Painter<JTextField>() {
			@Override
			public void paint(Graphics2D g, JTextField field, int width, int height) {
				// Don't paint a background when disabled
			}
		});
		this.textFieldOverrides.put("TextField[Disabled].borderPainter", new Painter<JTextField>() {
			@Override
			public void paint(Graphics2D g, JTextField field, int width, int height) {
				// Don't paint a border when disabled
			}
		});


		/**
		 * Top row
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.emptyPanel = new JPanel();
		this.add(this.emptyPanel, constraints);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.roundHeaderLabel = this.enclosedLabel("Round", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		// Space panel below

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.totalHeaderLabel = this.enclosedLabel("Total", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.weightx = 1.0;
		this.teamNameLabel = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		constraints.weightx = 0.0;

		constraints.gridx = 7;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		this.announcedBannerLabel =
				this.enclosedLabel("Last Round ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

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

		this.newRoundButton = new JButton("New Round");
		this.newRoundButton.setMargin(new Insets(0, 0, 0, 0));
		this.newRoundButton.setVisible(false);
		this.newRoundButton.setActionCommand("New Round");
		this.newRoundButton.addActionListener(this);
		this.currentHourLabel.getParent().add(this.newRoundButton, buttonConstraints);

		constraints.gridx = 5;
		constraints.gridy = 1;
		this.showButtonPanel = new JPanel(new GridBagLayout());
		this.add(this.showButtonPanel, constraints);
		this.showNameButton = new JToggleButton("Show");
		this.showNameButton.setMargin(new Insets(0, 0, 0, 0));
		this.showNameButton.setActionCommand("Enter Show Name");
		this.showNameButton.addActionListener(this);
		this.showButtonPanel.add(this.showNameButton, buttonConstraints);

		constraints.gridx = 6;
		constraints.gridy = 1;
		constraints.weightx = 0.5;
		this.showNameTextField = this.enclosedTextField("", constraints, SwingConstants.LEFT);
		this.showNameTextField.setActionCommand("Set Show Name");
		this.showNameTextField.addActionListener(this);
		this.showNameTextField.setEnabled(false);
		this.showNameTextField.addFocusListener(this);
		this.showNameTextField.putClientProperty("Nimbus.Overrides", this.textFieldOverrides);

		constraints.weightx = 0.0;

		constraints.gridx = 7;
		constraints.gridy = 1;
		this.scoreTextLabel = this.enclosedLabel("Points ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 8;
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
		this.speedButtonPanel = new JPanel(new GridBagLayout());
		this.add(this.speedButtonPanel, constraints);

		this.speedButton = new JToggleButton("");
		this.speedButton.setMargin(new Insets(0, 0, 0, 0));
		this.speedButton.setVisible(true);
		this.speedButtonPanel.add(this.speedButton, buttonConstraints);
		this.speedButton.setActionCommand("Change Speed");
		this.speedButton.addActionListener(this);

		constraints.gridx = 5;
		constraints.gridy = 2;
		this.hostButtonPanel = new JPanel(new GridBagLayout());
		this.add(this.hostButtonPanel, constraints);
		this.showHostButton = new JToggleButton("Host");
		this.showHostButton.setMargin(new Insets(0, 0, 0, 0));
		this.showHostButton.setActionCommand("Enter Host Name");
		this.showHostButton.addActionListener(this);
		this.hostButtonPanel.add(this.showHostButton, buttonConstraints);

		constraints.gridx = 6;
		constraints.gridy = 2;
		this.showHostTextField = this.enclosedTextField("", constraints, SwingConstants.LEFT);
		this.showHostTextField.setEnabled(false);
		this.showHostTextField.setActionCommand("Set Show Host");
		this.showHostTextField.addActionListener(this);
		this.showHostTextField.addFocusListener(this);
		this.showHostTextField.putClientProperty("Nimbus.Overrides", this.textFieldOverrides);

		constraints.gridx = 7;
		constraints.gridy = 2;
		this.placeTextLabel = this.enclosedLabel("Place ", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 8;
		constraints.gridy = 2;
		this.placeLabel = this.enclosedLabel("", constraints, SwingConstants.RIGHT, SwingConstants.CENTER);

		constraints.gridx = 9;
		constraints.gridy = 0;
		constraints.gridheight = 3;
		this.userListPanel = new UserListPanel(client, parent);
		this.add(this.userListPanel, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		this.emptyPanel2 = new JPanel(new GridBagLayout());
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
						SummaryPanel.this.client.sendMessage(
								new SetSpeedRoundMessage(SummaryPanel.this.client.getTrivia().getCurrentRoundNumber(),
										SummaryPanel.this.speedButton.isSelected()));
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
						SummaryPanel.this.client.sendMessage(
								new SetRoundMessage(SummaryPanel.this.client.getTrivia().getCurrentRoundNumber() + 1));
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
			case "Enter Show Name":
				this.showNameTextField.setEnabled(this.showNameButton.isSelected());
				if (this.showNameButton.isSelected()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SummaryPanel.this.showNameTextField.requestFocus();
						}
					});
				}
				break;
			case "Set Show Name":
				this.showNameTextField.setEnabled(false);
				this.showNameButton.setSelected(false);
				final String showName = this.showNameTextField.getText();
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						SummaryPanel.this.client.sendMessage(new SetShowNameMessage(
								SummaryPanel.this.client.getTrivia().getCurrentRoundNumber(), showName));
						return null;
					}

					@Override
					public void done() {

					}
				} ).execute();
				this.client.log("Changed show name to " + showName);
				break;
			case "Enter Host Name":
				this.showHostTextField.setEnabled(this.showHostButton.isSelected());
				if (this.showHostButton.isEnabled()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							SummaryPanel.this.showHostTextField.requestFocus();
						}
					});
				}
				break;
			case "Set Show Host":
				this.showHostTextField.setEnabled(false);
				this.showHostButton.setSelected(false);
				final String showHost = this.showHostTextField.getText();
				( new SwingWorker<Void, Void>() {
					@Override
					public Void doInBackground() {
						SummaryPanel.this.client.sendMessage(new SetShowHostMessage(
								SummaryPanel.this.client.getTrivia().getCurrentRoundNumber(), showHost));
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
		final Color labelColor =
				new Color(new BigInteger(properties.getProperty("Summary.Label.Color"), 16).intValue());
		final Color earnedColor = new Color(new BigInteger(properties.getProperty("Earned.Color"), 16).intValue());
		final Color valueColor = new Color(new BigInteger(properties.getProperty("Value.Color"), 16).intValue());
		final Color announcedColor =
				new Color(new BigInteger(properties.getProperty("Announced.Color"), 16).intValue());
		speedColor = new Color(new BigInteger(properties.getProperty("Summary.Speed.Color"), 16).intValue());
		final Color newRoundColor =
				new Color(new BigInteger(properties.getProperty("Summary.NewRound.Color"), 16).intValue());
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
		final int col7width = Integer.parseInt(properties.getProperty("Summary.Col7.Width"));
		final int col8width = Integer.parseInt(properties.getProperty("Summary.Col8.Width"));

		/**
		 * Font sizes
		 */
		final float labelFontSize = Float.parseFloat(properties.getProperty("Summary.Label.FontSize"));
		final float scoreFontSize = Float.parseFloat(properties.getProperty("Summary.Score.FontSize"));

		/**
		 * Button sizes
		 */
		final int speedButtonWidth = Integer.parseInt(properties.getProperty("Summary.SpeedButton.Width"));
		final int speedButtonHeight = Integer.parseInt(properties.getProperty("Summary.SpeedButton.Height"));
		final int conflictButtonWidth = Integer.parseInt(properties.getProperty("Summary.ConflictButton.Width"));
		final int conflictButtonHeight = Integer.parseInt(properties.getProperty("Summary.ConflictButton.Height"));
		final int showButtonWidth = Integer.parseInt(properties.getProperty("Summary.ShowButton.Width"));
		final int showButtonHeight = Integer.parseInt(properties.getProperty("Summary.ShowButton.Height"));

		setPanelProperties(this.emptyPanel, col0width, topRowHeight, backgroundColor);
		setLabelProperties(this.roundHeaderLabel, col1width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.totalHeaderLabel, col3width, topRowHeight, labelColor, backgroundColor, labelFontSize);
		setLabelProperties(this.teamNameLabel, col4width + col5width + col6width, topRowHeight, labelColor,
				backgroundColor, labelFontSize);
		setLabelProperties(this.announcedBannerLabel, col7width + col8width, topRowHeight, announcedColor,
				backgroundColor, labelFontSize);

		setLabelProperties(this.earnedRowLabel, col0width, middleRowHeight, earnedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.roundEarnedLabel, col1width, middleRowHeight, earnedColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.totalEarnedLabel, col3width, middleRowHeight, earnedColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.currentHourLabel, col4width, middleRowHeight, labelColor, backgroundColor,
				labelFontSize);
		setPanelProperties(this.showButtonPanel, col5width, middleRowHeight, backgroundColor);
		setButtonProperties(this.showNameButton, showButtonWidth, showButtonHeight, backgroundColor, labelFontSize);
		setTextFieldProperties(this.showNameTextField, col6width, middleRowHeight, labelColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.scoreTextLabel, col7width, middleRowHeight, announcedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.announcedLabel, col8width, middleRowHeight, announcedColor, backgroundColor,
				labelFontSize);

		setLabelProperties(this.valueRowLabel, col0width, bottomRowHeight, valueColor, backgroundColor, labelFontSize);
		setLabelProperties(this.roundValueLabel, col1width, bottomRowHeight, valueColor, backgroundColor,
				scoreFontSize);
		setLabelProperties(this.totalValueLabel, col3width, bottomRowHeight, valueColor, backgroundColor,
				scoreFontSize);

		setPanelProperties(this.speedButtonPanel, col4width, speedButtonHeight, backgroundColor);
		setButtonProperties(this.speedButton, speedButtonWidth, speedButtonHeight, null, labelFontSize);
		setButtonProperties(this.newRoundButton, speedButtonWidth, speedButtonHeight, null, labelFontSize);

		setPanelProperties(this.hostButtonPanel, col5width, bottomRowHeight, backgroundColor);
		setButtonProperties(this.showHostButton, showButtonWidth, showButtonHeight, backgroundColor, labelFontSize);
		setTextFieldProperties(this.showHostTextField, col6width, middleRowHeight, labelColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.placeTextLabel, col7width, bottomRowHeight, announcedColor, backgroundColor,
				labelFontSize);
		setLabelProperties(this.placeLabel, col8width, bottomRowHeight, announcedColor, backgroundColor, labelFontSize);

		this.textFieldOverrides.put("TextField[Disabled].textForeground", labelColor);

		this.newRoundButton.setBackground(newRoundColor);
		setButtonProperties(this.conflictButton, conflictButtonWidth, conflictButtonHeight, null, labelFontSize);

		this.userListPanel.setBackground(backgroundColor);
		this.emptyPanel2.setBackground(backgroundColor);
		this.emptyPanel2.setPreferredSize(new Dimension(col2width, 0));
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
		this.teamNameLabel
				.setText(this.client.getTrivia().getTeamName() + " (#" + this.client.getTrivia().getTeamNumber() + ")");
		if (!this.showNameTextField.isEnabled()) {
			this.showNameTextField.setText(this.client.getTrivia().getRound(currentRound).getShowName());
		}
		if (!this.showHostTextField.isEnabled()) {
			this.showHostTextField.setText(this.client.getTrivia().getRound(currentRound).getShowHost());
		}
		this.roundEarnedLabel.setText("" + trivia.getCurrentRound().getEarned());
		this.totalEarnedLabel.setText("" + trivia.getEarned());
		this.roundValueLabel.setText("" + trivia.getCurrentRound().getValue());
		this.totalValueLabel.setText("" + trivia.getValue());
		this.currentHourLabel.setText("Round " + currentRound);

		// Only show announced values once they've been announced
		if (currentRound > 1 && trivia.getRound(currentRound - 1).isAnnounced()) {
			final int announcedPoints = trivia.getRound(currentRound - 1).getAnnouncedPoints();
			this.announcedLabel.setText("" + announcedPoints);
			this.placeLabel.setText("" + TriviaGUI.ordinalize(trivia.getRound(currentRound - 1).getPlace()));
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
		if (trivia.getCurrentRound().isSpeed()) {
			this.speedButton.setText("Speed");
			this.speedButton.setSelected(true);
			this.speedButton.setForeground(speedColor);
		} else {
			this.speedButton.setText("Normal");
			this.speedButton.setSelected(false);
			this.speedButton.setForeground(Color.BLACK);
		}

		this.newRoundButton.setVisible(
				trivia.getCurrentRound().roundOver() && trivia.getCurrentRoundNumber() < trivia.getNRounds());
		if (trivia.getCurrentRound().roundOver() && trivia.getCurrentRoundNumber() < trivia.getNRounds()) {
			this.newRoundButton.setVisible(true);
			this.newRoundButton.setText("Start Rd " + ( trivia.getCurrentRoundNumber() + 1 ));
			this.currentHourLabel.setText("");

		} else {
			this.newRoundButton.setVisible(false);
		}
		this.userListPanel.updateGUI(force);
	}

	@Override
	public void focusGained(FocusEvent event) {}

	@Override
	public void focusLost(FocusEvent event) {
		final JTextField field = (JTextField) event.getComponent();
		if (field.isEnabled()) {
			field.setEnabled(false);
			field.postActionEvent();
		}
	}

}
