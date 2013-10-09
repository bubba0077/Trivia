package net.bubbaland.trivia.client;

import java.awt.*;
import java.awt.event.*;

import java.rmi.RemoteException;

import javax.swing.*;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

/**
 * Panel which displays summary information of the trivia contest.
 */
public class HeaderPanel extends TriviaPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long		serialVersionUID		= 3544918496657028139L;

	/**
	 * Font sizes
	 */	
	private static final float		LABEL_FONT_SIZE			= (float)18.0;
	private static final float		POINT_FONT_SIZE			= (float)28.0;

	/**
	 * Colors
	 */	
	protected static final Color	BACKGROUND_COLOR_NORMAL	= Color.BLACK;
	private static final Color		BACKGROUND_COLOR_SPEED	= Color.RED;
	private static final Color		LABEL_COLOR				= Color.WHITE;
	private static final Color		EARNED_COLOR			= Color.GREEN;
	private static final Color		VALUE_COLOR				= new Color(30, 144, 255);
	private static final Color		ANNOUNCED_COLOR			= Color.ORANGE;
	private static final Color		NEW_ROUND_COLOR			= Color.YELLOW;

	/**
	 * Sizes
	 */
	private static final int		TOP_ROW_HEIGHT			= 24;
	private static final int		MIDDLE_ROW_HEIGHT		= 30;
	private static final int		BOTTOM_ROW_HEIGHT		= 30;

	private static final int		COL0_WIDTH				= 85;
	private static final int		COL1_WIDTH				= 90;
	private static final int		COL2_WIDTH				= 100;
	private static final int		COL3_WIDTH				= 250;
	private static final int		COL4_WIDTH				= 120;
	private static final int		COL5_WIDTH				= 75;

	// Width of the speed/new round button
	private static final int		CENTER_BUTTON_WIDTH		= 100;
	// Height of the speed/new round button
	private static final int		CENTER_BUTTON_HEIGHT	= BOTTOM_ROW_HEIGHT - 4;

	/**
	 * GUI Elements that will need to be updated
	 */
	private final JLabel				roundEarnedLabel, roundValueLabel, totalEarnedLabel;
	private final JLabel				totalValueLabel, announcedLabel, placeLabel;
	private final JLabel				currentHourLabel;
	private final JToggleButton			speedButton;
	private final JButton				newRoundButton;

	/**
	 * Data sources
	 */
	private final TriviaInterface		server;
	private final TriviaClient			client;

	/**
	 * Instantiates a new header panel.
	 * 
	 * @param server the server
	 * @param client the client
	 */
	public HeaderPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );

		this.server = server;
		this.client = client;

		// Set up layout constraints
		GridBagConstraints buttonConstraints = new GridBagConstraints();
		buttonConstraints.anchor = GridBagConstraints.CENTER;
		buttonConstraints.weightx = 1.0;
		buttonConstraints.weighty = 1.0;
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 0;
		buttonConstraints.fill = GridBagConstraints.NONE;

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 1.0;

		/**
		 * Top row
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		JPanel panel = new JPanel( new GridBagLayout() );
		panel.setPreferredSize( new Dimension( COL0_WIDTH, TOP_ROW_HEIGHT ) );
		panel.setBackground( BACKGROUND_COLOR_NORMAL );
		this.add( panel, constraints );

		constraints.gridx = 1;
		constraints.gridy = 0;
		enclosedLabel( "Round", COL1_WIDTH, TOP_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR_NORMAL, constraints,
				LABEL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );

		constraints.gridx = 2;
		constraints.gridy = 0;
		enclosedLabel( "Total", COL2_WIDTH, TOP_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR_NORMAL, constraints,
				LABEL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );

		constraints.gridx = 3;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		enclosedLabel( client.getTrivia().getTeamName(), COL3_WIDTH, TOP_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR_NORMAL,
				constraints, LABEL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.weightx = 0.0;

		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		enclosedLabel( "Last Round ", COL4_WIDTH, TOP_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR_NORMAL,
				constraints, LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );
		constraints.gridwidth = 1;
		

		/**
		 * Middle row
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		enclosedLabel( "Earned", COL0_WIDTH, MIDDLE_ROW_HEIGHT, EARNED_COLOR, BACKGROUND_COLOR_NORMAL, constraints,
				LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 1;
		constraints.gridy = 1;
		this.roundEarnedLabel = enclosedLabel( "", COL1_WIDTH, MIDDLE_ROW_HEIGHT, EARNED_COLOR,
				BACKGROUND_COLOR_NORMAL, constraints, POINT_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 2;
		constraints.gridy = 1;
		this.totalEarnedLabel = enclosedLabel( "", COL2_WIDTH, MIDDLE_ROW_HEIGHT, EARNED_COLOR,
				BACKGROUND_COLOR_NORMAL, constraints, POINT_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 3;
		constraints.gridy = 1;
		this.currentHourLabel = enclosedLabel( "", COL3_WIDTH, MIDDLE_ROW_HEIGHT, LABEL_COLOR, BACKGROUND_COLOR_NORMAL,
				constraints, LABEL_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );

		constraints.gridx = 4;
		constraints.gridy = 1;
		enclosedLabel( "Points ", COL4_WIDTH, MIDDLE_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR_NORMAL, constraints,
				LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 5;
		constraints.gridy = 1;
		this.announcedLabel = enclosedLabel( "", COL5_WIDTH, MIDDLE_ROW_HEIGHT, ANNOUNCED_COLOR,
				BACKGROUND_COLOR_NORMAL, constraints, LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		/**
		 * Bottom row
		 */
		constraints.gridx = 0;
		constraints.gridy = 2;
		enclosedLabel( "Possible", COL0_WIDTH, BOTTOM_ROW_HEIGHT, VALUE_COLOR, BACKGROUND_COLOR_NORMAL, constraints,
				LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 1;
		constraints.gridy = 2;
		this.roundValueLabel = enclosedLabel( "", COL1_WIDTH, BOTTOM_ROW_HEIGHT, VALUE_COLOR, BACKGROUND_COLOR_NORMAL,
				constraints, POINT_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 2;
		constraints.gridy = 2;
		this.totalValueLabel = enclosedLabel( "", COL2_WIDTH, BOTTOM_ROW_HEIGHT, VALUE_COLOR, BACKGROUND_COLOR_NORMAL,
				constraints, POINT_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 3;
		constraints.gridy = 2;

		// Put both the speed button and new round button in the same place, we'll hide the one we don't need
		panel = new JPanel( new GridBagLayout() );
		panel.setPreferredSize( new Dimension( COL3_WIDTH, BOTTOM_ROW_HEIGHT ) );
		panel.setMinimumSize( new Dimension( COL3_WIDTH, BOTTOM_ROW_HEIGHT ) );
		panel.setBackground( BACKGROUND_COLOR_NORMAL );
		this.add( panel, constraints );

			this.speedButton = new JToggleButton( "" );
			this.speedButton.setMargin( new Insets( 0, 0, 0, 0 ) );
			this.speedButton.setPreferredSize( new Dimension( CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT ) );
			this.speedButton.setMinimumSize( new Dimension( CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT ) );
			this.speedButton.setVisible( true );
			panel.add( speedButton, buttonConstraints );
			this.speedButton.addActionListener( this );
	
			this.newRoundButton = new JButton( "New Round" );
			this.newRoundButton.setMargin( new Insets( 0, 0, 0, 0 ) );
			this.newRoundButton.setPreferredSize( new Dimension( CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT ) );
			this.newRoundButton.setMinimumSize( new Dimension( CENTER_BUTTON_WIDTH, CENTER_BUTTON_HEIGHT ) );
			this.newRoundButton.setVisible( false );
			this.newRoundButton.setBackground( NEW_ROUND_COLOR );
			panel.add( newRoundButton, buttonConstraints );
			this.newRoundButton.addActionListener( this );

		constraints.gridx = 4;
		constraints.gridy = 2;
		enclosedLabel( "Place ", COL4_WIDTH, BOTTOM_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR_NORMAL, constraints,
				LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

		constraints.gridx = 5;
		constraints.gridy = 2;
		this.placeLabel = enclosedLabel( "", COL5_WIDTH, BOTTOM_ROW_HEIGHT, ANNOUNCED_COLOR, BACKGROUND_COLOR_NORMAL,
				constraints, LABEL_FONT_SIZE, JLabel.RIGHT, JLabel.CENTER );

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		JComponent source = (JComponent)event.getSource();
		if ( source.equals( this.speedButton ) ) {
			// Speed button changed
			if ( this.speedButton.isSelected() ) {
				// Speed button now pressed, tell server
				int tryNumber = 0;
				boolean success = false;
				while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
					tryNumber++;
					try {
						server.setSpeed();
						success = true;
					} catch ( Exception e ) {
						client.log( "Couldn't make this a speed round (try #" + tryNumber + ")." );
					}

					if ( !success ) {
						client.disconnected();
						return;
					}

					client.log( "Made this a speed round." );
				}

			} else {
				// Speed button now not pressed, tell server
				int tryNumber = 0;
				boolean success = false;
				while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
					tryNumber++;
					try {
						server.unsetSpeed();
						success = true;
					} catch ( RemoteException e ) {
						client.log( "Couldn't make this a normal round (try #" + tryNumber + ")." );
						return;
					}
				}

				if ( !success ) {
					client.disconnected();
					return;
				}

				client.log( "Made this a normal round" );

			}
		} else if ( source.equals( this.newRoundButton ) ) {
			// New round button pressed, tell server
			int tryNumber = 0;
			boolean success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					server.newRound();
					success = true;
				} catch ( Exception e ) {
					client.log( "Couldn't get current round number from server (try #" + tryNumber + ")." );
				}

			}

			if ( !success ) {
				client.log( "Connection failed!" );
				return;
			}

			client.log( "Started new round" );

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		// Get the current Trivia object from the client
		Trivia trivia = client.getTrivia();
		
		// Get the current round
		int currentRound = trivia.getRoundNumber();

		// Update all the labels to match the current data
		this.roundEarnedLabel.setText( "" + trivia.getCurrentRoundEarned() );
		this.totalEarnedLabel.setText( "" + trivia.getEarned() );
		this.roundValueLabel.setText( "" + trivia.getCurrentRoundValue() );
		this.totalValueLabel.setText( "" + trivia.getValue() );
		this.currentHourLabel.setText( "Current Round: " + currentRound );
		
		// Only show announced values once they've been announced
		if ( trivia.isAnnounced( currentRound - 1 ) ) {
			this.announcedLabel.setText( "" + trivia.getAnnouncedPoints( currentRound - 1 ) );
			this.placeLabel.setText( "" + trivia.getAnnouncedPlace( currentRound - 1 ) );
		} else {
			this.announcedLabel.setText( "" );
			this.placeLabel.setText( "" );
		}

		// If the round is over, hide speed round button and show new round button
		if ( trivia.roundOver() ) {
			this.speedButton.setVisible( false );
			this.newRoundButton.setVisible( true );
		} else {
			this.speedButton.setVisible( true );
			this.newRoundButton.setVisible( false );
			if ( trivia.isCurrentSpeed() ) {
				this.speedButton.setText( "Speed" );
				this.speedButton.setSelected( true );
				Component[] children = this.getComponents();
				for ( Component child : children ) {
					child.setBackground( BACKGROUND_COLOR_SPEED );
				}
			} else {
				this.speedButton.setText( "Normal" );
				this.speedButton.setSelected( false );
				Component[] children = this.getComponents();
				for ( Component child : children ) {
					child.setBackground( BACKGROUND_COLOR_NORMAL );
				}
			}
		}

	}

}
