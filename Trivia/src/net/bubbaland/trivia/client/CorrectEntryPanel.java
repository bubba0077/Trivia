package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.*;

import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class CorrectEntryPanel.
 */
public class CorrectEntryPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -8974614016214193902L;
	
	/** The Constant FONT_SIZE. */
	private static final float	FONT_SIZE			= 20.0f;

	/**
	 * Instantiates a new correct entry panel.
	 *
	 * @param server the server
	 * @param client the client
	 * @param caller the caller
	 * @param queueIndex the queue index
	 * @param statusComboBox the status combo box
	 */
	public CorrectEntryPanel( TriviaInterface server, TriviaClient client, String caller, int queueIndex, JComboBox<String> statusComboBox ) {
		super( new GridBagLayout() );
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;

		c.gridx = 0;
		c.gridy = 1;
		JLabel label = new JLabel( "Operator: " );
		label.setFont( label.getFont().deriveFont( FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 1;
		c.gridy = 1;
		JTextField operatorTextField = new JTextField( "", 15 );
		operatorTextField.setFont( operatorTextField.getFont().deriveFont( FONT_SIZE ) );
		this.add( operatorTextField, c );

		JOptionPane pane = new JOptionPane( this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
		JDialog dialog = pane.createDialog( this.getParent(), "Mark question correct" );
		dialog.setVisible( true );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );

		int option = ( (Integer)pane.getValue() ).intValue();
		if ( option == JOptionPane.OK_OPTION ) {
			int tryNumber = 0;
			boolean success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					server.markCorrect( queueIndex, caller, operatorTextField.getText() );
					success = true;
				} catch ( RemoteException e ) {
					client.log( "Couldn't change answer status on server (try #" + tryNumber + ")." );
				}
			}

			if ( !success ) {
				client.disconnected();
				return;
			}
			
			client.log( "Marked answer #" + (queueIndex+1) + "correct" );			
			
		} else {
			statusComboBox.setSelectedItem( client.getTrivia().getAnswerQueueStatus( queueIndex ) );
		}
	}
}
