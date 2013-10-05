package net.bubbaland.trivia.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;

import javax.swing.*;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class QuestionEntryWindow.
 */
public class QuestionEntryWindow extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 8250659442772286086L;
	
	/** The Constant LABEL_FONT_SIZE. */
	private static final float	LABEL_FONT_SIZE		= 20.0f;
	
	/** The Constant TEXTAREA_FONT_SIZE. */
	private static final float	TEXTAREA_FONT_SIZE	= 16.0f;
	
	/**
	 * Instantiates a new question entry window.
	 *
	 * @param server the server
	 * @param client the client
	 * @param nQuestions the n questions
	 * @param qNumberStart the q number start
	 */
	public QuestionEntryWindow( TriviaInterface server, TriviaClient client, int nQuestions, int qNumberStart ) {
		this( server, client, nQuestions, qNumberStart, 10, "" );
	}

	/**
	 * Instantiates a new question entry window.
	 *
	 * @param server the server
	 * @param client the client
	 * @param nQuestions the n questions
	 * @param qNumberStart the q number start
	 * @param qValueStart the q value start
	 * @param qTextStart the q text start
	 */
	public QuestionEntryWindow(
			TriviaInterface server,
			TriviaClient client, 
			int nQuestions,
			int qNumberStart,
			int qValueStart,
			String qTextStart ) {

		super( new GridBagLayout() );

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;

		c.gridx = 0;
		c.gridy = 0;
		JLabel label = new JLabel( "Question Number: " );
		label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 1;
		c.gridy = 0;
		JSpinner qNumberSpinner = new JSpinner( new SpinnerNumberModel( qNumberStart, 1, nQuestions, 1 ) );
		qNumberSpinner.setFont( qNumberSpinner.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( qNumberSpinner, c );

		c.gridx = 0;
		c.gridy = 1;
		label = new JLabel( "Question Value: " );
		label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 1;
		c.gridy = 1;
		JSpinner qValueSpinner = new JSpinner( new SpinnerNumberModel( qValueStart, 10, 1000, 5 ) );
		qValueSpinner.setFont( qValueSpinner.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( qValueSpinner, c );

		c.gridx = 0;
		c.gridy = 2;
		label = new JLabel( "Question: " );
		label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		JTextArea qTextArea = new JTextArea( "", 4, 50 );
		qTextArea.setLineWrap( true );
		qTextArea.setWrapStyleWord( true );
		qTextArea.setFont( qTextArea.getFont().deriveFont( TEXTAREA_FONT_SIZE ) );
		JScrollPane scrollPane = new JScrollPane( qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize( new Dimension( 0, 200 ) );
		this.add( scrollPane, c );
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;

		JOptionPane pane = new JOptionPane( this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
		JDialog dialog = pane.createDialog( this.getParent(), "Enter New Question" );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		dialog.setResizable( true );
		dialog.setVisible( true );

		int option = ( (Integer)pane.getValue() ).intValue();
		if ( option == JOptionPane.OK_OPTION ) {
			int qNumber = (int)qNumberSpinner.getValue();
			int qValue = (int)qValueSpinner.getValue();
			String qText = qTextArea.getText();
			
			if ( qText == "" ) {

			}
			
			Trivia trivia = client.getTrivia();
			int currentRound = trivia.getRoundNumber();
			if ( trivia.beenOpen( currentRound, qNumber ) ) {
				int existingQValue = trivia.getValue( currentRound, qNumber );
				String existingQText = trivia.getQuestionText( currentRound, qNumber );

				JPanel panel = new JPanel( new GridBagLayout() );

				c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.anchor = GridBagConstraints.CENTER;

				c.gridx = 0;
				c.gridy = 0;
				label = new JLabel( "Question alread open, overwrite?" );
				label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
				panel.add( label, c );

				c.gridx = 0;
				c.gridy = 1;
				label = new JLabel( "Existing question:" );
				label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
				panel.add( label, c );

				c.gridx = 0;
				c.gridy = 2;
				c.weightx = 0.5;
				c.weighty = 0.5;
				JTextArea textArea = new JTextArea( existingQText );
				textArea.setLineWrap( true );
				textArea.setWrapStyleWord( true );
				textArea.setEditable( false );
				scrollPane = new JScrollPane( qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
				scrollPane.setPreferredSize( new Dimension( 100, 100 ) );
				panel.add( scrollPane, c );
				c.weightx = 0.0;
				c.weighty = 0.0;

				c.gridx = 0;
				c.gridy = 3;
				label = new JLabel( "Value: " + existingQValue );
				label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
				panel.add( label, c );

				c.gridx = 0;
				c.gridy = 4;
				label = new JLabel( "Entered question:" );
				label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
				panel.add( label, c );

				c.gridx = 0;
				c.gridy = 5;
				c.weightx = 0.5;
				c.weighty = 0.5;
				textArea = new JTextArea( existingQText );
				textArea.setLineWrap( true );
				textArea.setWrapStyleWord( true );
				textArea.setEditable( false );
				scrollPane = new JScrollPane( qTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
				scrollPane.setPreferredSize( new Dimension( 100, 100 ) );
				panel.add( scrollPane, c );
				c.weightx = 0.0;
				c.weighty = 0.0;

				c.gridx = 0;
				c.gridy = 6;
				label = new JLabel( "Value: " + qValue );
				label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
				panel.add( label, c );

				pane = new JOptionPane( panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
				dialog = pane.createDialog( this.getParent(), "Confirm Question Overwrite " + qNumber );
				dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
				dialog.setResizable( true );
				dialog.setVisible( true );

				int confirm = ( (Integer)pane.getValue() ).intValue();
				if ( confirm != JOptionPane.OK_OPTION ) {
					new QuestionEntryWindow( server, client, nQuestions, qNumber, qValue, qText );
				}
			}
			
			int tryNumber = 0;
			boolean success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					server.open( qNumber, qValue, qText );
					success = true;
				} catch ( RemoteException e ) {
					client.log( "Couldn't open question on server (try #" + tryNumber + ")." );
				}
			}

			if ( !success ) {
				client.disconnected();
				return;
			}
			
			client.log("Question #" + qNumber + " submitted.");

		}

	}

}
