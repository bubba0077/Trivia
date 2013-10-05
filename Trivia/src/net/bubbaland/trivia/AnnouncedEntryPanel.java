package net.bubbaland.trivia;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.*;

import net.bubbaland.trivia.server.TriviaInterface;

/**
 * Creates a dialog box that prompts for announced score and place 
 */
public class AnnouncedEntryPanel extends TriviaPanel {

	private static final long	serialVersionUID	= -363168881130169668L;
	
	// Font size for the dialog box
	private static final float	FONT_SIZE			= 20.0f;
	
	/**
	 * Creates a dialog box and prompts for response
	 *
	 * @param server The trivia server
	 * @param client The root trivia client
	 * @param rNumber The round number of the announced scores
	 */
	public AnnouncedEntryPanel( TriviaInterface server, TriviaClient client, int rNumber ) {

		// Call parent constructor and use GridBagLayout
		super( new GridBagLayout() );
		
		// Initialize variables that will be retrieved from server
		int oldPlace = 1;
		int lastRoundPts = 0;
		
		// Obtain data from server
		int tryNumber = 0;
		boolean success = false;
		while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
			tryNumber++;
			// Try to get data from the server
			try {
				// Retrieve the place of the previous round
				if ( rNumber > 1 ) {
					oldPlace = server.getAnnouncedPlace( rNumber - 1 );
				}
				// Retrieve the cumulative score of the round
				lastRoundPts = server.getCumulativeEarned( rNumber );
				success = true;
			} catch ( RemoteException e ) {
				// Retry if the connection is broken
				client.log( "Couldn't retrieve announced scores from previous round (try #" + tryNumber + ")." );
			}
		}

		if ( !success ) {
			// Couldn't retrieve data from server, create a disconnected dialog box
			client.disconnected();
			return;
		}

		// Set up base layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		// Add announced score label
		constraints.gridx = 0;
		constraints.gridy = 0;
		JLabel label = new JLabel( "Announced Score: " );
		label.setFont( label.getFont().deriveFont( FONT_SIZE ) );
		this.add( label, constraints );

		// Add spinner for announced score, using calculated score as first guess
		constraints.gridx = 1;
		constraints.gridy = 0;
		JSpinner pointSpinner = new JSpinner( new SpinnerNumberModel( lastRoundPts, 0, 100000, 5 ) );
		pointSpinner.setFont( pointSpinner.getFont().deriveFont( FONT_SIZE ) );
		this.add( pointSpinner, constraints );

		// Add announced place label
		constraints.gridx = 0;
		constraints.gridy = 1;
		label = new JLabel( "Announced Place: " );
		label.setFont( label.getFont().deriveFont( FONT_SIZE ) );
		this.add( label, constraints );

		// Add spinner for announced place, using last round's place as first guess
		constraints.gridx = 1;
		constraints.gridy = 1;
		JSpinner placeSpinner = new JSpinner( new SpinnerNumberModel( oldPlace, 1, 200, 1 ) );
		placeSpinner.setFont( placeSpinner.getFont().deriveFont( FONT_SIZE ) );
		this.add( placeSpinner, constraints );

		// Create dialog box with OK and CANCEL buttons
		JOptionPane pane = new JOptionPane( this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
		JDialog dialog = pane.createDialog( this.getParent(), "Announced Scores for Round " + rNumber );
		dialog.setVisible( true );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		// Retrieve the button pressed
		int option = ( (Integer)pane.getValue() ).intValue();
		
		if ( option == JOptionPane.OK_OPTION ) {
			// OK pressed, update the announced values on server
			
			// Get input values from spinners
			int announcedPts = (int)pointSpinner.getValue();
			int announcedPlace = (int)placeSpinner.getValue();
			
			if(announcedPts != lastRoundPts) {
				// Entered announced points doesn't match calculated value, create mismatch prompt
				if(!announcedScoreMismatch(announcedPts, lastRoundPts)) { 
					new AnnouncedEntryPanel( server, client, rNumber);
					return;
				}
			}

			// Try to communicate with server
			tryNumber = 0;
			success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					// Set the announced values
					server.setAnnounced( rNumber, announcedPts, announcedPlace );
					success = true;
				} catch ( RemoteException e ) {
					// Retry if the connection is broken
					client.log( "Couldn't set announced round scores on server (try #" + tryNumber + ")." );
				}
			}

			if ( !success ) {
				// Couldn't retrieve data from server, create a disconnected dialog box
				client.disconnected();
				return;
			}
			
			// Change the status message to indicate success
			client.log( "Sent announced score for Round " + rNumber + " to server");

		}

	}
	
	/**
	 * Creates a dialog informing of a mismatch between the announced score and the computed score.
	 *
	 * @param announcedPts The announced score
	 * @param lastRoundPts The score calculated based on server data
	 * @return True if mismatched data is accepted
	 */
	private boolean announcedScoreMismatch(int announcedPts, int lastRoundPts) {
		
		// Call parent constructor and use GridBagLayout
		JPanel mismatchInfoPanel = new JPanel(new GridBagLayout());
		
		// Set up base layout constraints
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		
		constraints.gridx = 0;
		constraints.gridy = 0;		
		constraints.gridwidth = 2;
		JLabel label = new JLabel("Announced score does not match calculated score!", JLabel.CENTER);
		mismatchInfoPanel.add( label, constraints );
		constraints.gridwidth = 1;
		
		constraints.gridx = 0;
		constraints.gridy = 1;		
		constraints.gridwidth = 2;
		label = new JLabel("Check for typos, then call the point dispute line: (320) 308-4748.", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont( FONT_SIZE ));
		mismatchInfoPanel.add( label, constraints );
		constraints.gridwidth = 1;
		
		constraints.gridx = 0;
		constraints.gridy = 2;		
		label = new JLabel("Annnounced score: ", JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont( FONT_SIZE ));
		mismatchInfoPanel.add( label, constraints );
		
		constraints.gridx = 1;
		constraints.gridy = 2;		
		label = new JLabel(announcedPts+"", JLabel.LEFT);
		label.setFont(label.getFont().deriveFont( FONT_SIZE ));
		mismatchInfoPanel.add( label, constraints );
		
		constraints.gridx = 0;
		constraints.gridy = 3;		
		label = new JLabel("Computed score: ", JLabel.RIGHT);
		label.setFont(label.getFont().deriveFont( FONT_SIZE ));
		mismatchInfoPanel.add( label, constraints );
		
		constraints.gridx = 1;
		constraints.gridy = 3;		
		label = new JLabel(lastRoundPts+"", JLabel.LEFT);
		label.setFont(label.getFont().deriveFont( FONT_SIZE ));
		mismatchInfoPanel.add( label, constraints );
		
		Object[] options = { "OK", "RETRY" };							
		JOptionPane pane = new JOptionPane( mismatchInfoPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[1] );
		JDialog dialog = pane.createDialog( this.getParent(), "Score Discrepency" );
		dialog.setVisible( true );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		
		int option = ( (Integer)pane.getValue() ).intValue();
		if ( option == JOptionPane.OK_OPTION ) {
			return true;
		} else {
			return false;
		}
		
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public void update() {
				
	}

}
