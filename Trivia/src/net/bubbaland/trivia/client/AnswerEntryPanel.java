package net.bubbaland.trivia.client;

import javax.swing.JDialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class AnswerEntryPanel.
 */
public class AnswerEntryPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID		= -4127179718225373888L;
	
	/** The Constant LABEL_FONT_SIZE. */
	private static final float	LABEL_FONT_SIZE			= 20.0f;
	
	/** The Constant TEXTBOX_FONT_SIZE. */
	private static final float	TEXTBOX_FONT_SIZE		= 16.0f;
	
	/** The Constant SLIDER_PADDING_BOTTOM. */
	private static final int	SLIDER_PADDING_BOTTOM	= 10;
	
	/** The Constant SLIDER_PADDING_LEFT. */
	private static final int	SLIDER_PADDING_LEFT		= 10;
	
	/** The Constant SLIDER_PADDING_RIGHT. */
	private static final int	SLIDER_PADDING_RIGHT	= 10;
	
	/** The Constant SLIDER_PADDING_TOP. */
	private static final int	SLIDER_PADDING_TOP		= 10;
	
	/**
	 * Instantiates a new answer entry panel.
	 *
	 * @param server the server
	 * @param client the client
	 * @param qNumber the question number
	 * @param user the user's name
	 */
	public AnswerEntryPanel( TriviaInterface server, TriviaClient client, int qNumber, String user ) {

		super( new GridBagLayout() );
		
		Trivia trivia = client.getTrivia();

		String qText = trivia.getQuestionText( qNumber );

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.CENTER;

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		JLabel label = new JLabel( "Question:" );
		label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 0.5;
		JTextArea textArea = new JTextArea( qText );
		textArea.setFont( textArea.getFont().deriveFont( TEXTBOX_FONT_SIZE ) );
		textArea.setEditable( false );
		textArea.setLineWrap( true );
		textArea.setWrapStyleWord( true );
		JScrollPane scrollPane = new JScrollPane( textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize( new Dimension( 0, 200 ) );
		this.add( scrollPane, c );
		c.weightx = 0.0;
		c.weighty = 0.0;

		c.gridx = 0;
		c.gridy = 2;
		label = new JLabel( "Answer: " );
		label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.weighty = 0.5;
		JTextArea answerTextArea = new JTextArea( "", 4, 50 );
		answerTextArea.setLineWrap( true );
		answerTextArea.setWrapStyleWord( true );
		answerTextArea.setFont( answerTextArea.getFont().deriveFont( TEXTBOX_FONT_SIZE ) );
		scrollPane = new JScrollPane( answerTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize( new Dimension( 0, 200 ) );
		this.add( scrollPane, c );
		c.weightx = 0.0;
		c.weighty = 0.0;

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		label = new JLabel( "Confidence", SwingConstants.RIGHT );
		label.setVerticalAlignment( SwingConstants.CENTER );
		label.setFont( label.getFont().deriveFont( LABEL_FONT_SIZE ) );
		this.add( label, c );

		c.gridx = 1;
		c.gridy = 4;
		c.insets = new Insets( SLIDER_PADDING_BOTTOM, SLIDER_PADDING_LEFT, SLIDER_PADDING_RIGHT, SLIDER_PADDING_TOP );
		JSlider confidenceSlider = new JSlider( SwingConstants.HORIZONTAL, 1, 5, 3 );
		confidenceSlider.setMajorTickSpacing( 1 );
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 1 ), new JLabel( "Guess" ) );
		labelTable.put( new Integer( 5 ), new JLabel( "Sure" ) );
		confidenceSlider.setLabelTable( labelTable );
		confidenceSlider.setPaintLabels( true );
		confidenceSlider.setPaintTicks( true );
		this.add( confidenceSlider, c );

		JOptionPane pane = new JOptionPane( this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION );
		JDialog dialog = pane.createDialog( this.getParent(), "Submit Answer for Question " + qNumber );
		dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		dialog.setResizable( true );
		dialog.setVisible( true );

		int option = ( (Integer)pane.getValue() ).intValue();
		if ( option == JOptionPane.OK_OPTION ) {
			String answer = answerTextArea.getText();
			int confidence = confidenceSlider.getValue();

			int tryNumber = 0;
			boolean success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					server.proposeAnswer( qNumber, answer, user, confidence );
					success = true;
				} catch ( RemoteException e ) {
					client.log( "Couldn't set announced round scores on server (try #" + tryNumber + ")." );
				}
			}

			if ( !success ) {
				client.disconnected();
				return;
			}
			
			client.log( "Submitted an answer for Question #" + qNumber );

		}

	}

}