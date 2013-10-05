package net.bubbaland.trivia;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// TODO: Auto-generated Javadoc
/**
 * The Class TriviaPanel.
 */
public abstract class TriviaPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 3713561221924406596L;

	/**
	 * Instantiates a new trivia panel.
	 */
	public TriviaPanel() {
		super();
	}

	/**
	 * Instantiates a new trivia panel.
	 *
	 * @param isDoubleBuffered the is double buffered
	 */
	public TriviaPanel( boolean isDoubleBuffered ) {
		super( isDoubleBuffered );
	}

	/**
	 * Instantiates a new trivia panel.
	 *
	 * @param layout the layout
	 */
	public TriviaPanel( LayoutManager layout ) {
		super( layout );
	}

	/**
	 * Instantiates a new trivia panel.
	 *
	 * @param layout the layout
	 * @param isDoubleBuffered the is double buffered
	 */
	public TriviaPanel( LayoutManager layout, boolean isDoubleBuffered ) {
		super( layout, isDoubleBuffered );
	}

	/**
	 * Update.
	 */
	public abstract void update();
	
	/**
	 * Enclosed label.
	 *
	 * @param string the string
	 * @param width the width
	 * @param height the height
	 * @param foreground the foreground
	 * @param background the background
	 * @param constraints the constraints
	 * @param fontSize the font size
	 * @param labelHAlignment the label h alignment
	 * @param labelVAlignment the label v alignment
	 * @return the j label
	 */
	public JLabel enclosedLabel( String string, int width, int height, Color foreground, Color background, GridBagConstraints constraints, 
			float fontSize, int labelHAlignment, int labelVAlignment  ) {
		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;
		
		JPanel panel = new JPanel( new GridBagLayout() );
		panel.setBackground( background );
		panel.setPreferredSize( new Dimension( width, height ) );
		panel.setMinimumSize( new Dimension( width, height ) );
		this.add( panel, constraints );
		JLabel label = new JLabel( string, labelHAlignment );
		label.setVerticalAlignment( labelVAlignment );
		label.setFont( label.getFont().deriveFont( fontSize ) );
		label.setForeground( foreground );
		panel.add( label, solo );
		
		return label;
	}
		
	/**
	 * Scrollable text area.
	 *
	 * @param string the string
	 * @param width the width
	 * @param height the height
	 * @param foreground the foreground
	 * @param background the background
	 * @param constraints the constraints
	 * @param fontSize the font size
	 * @param labelHAlignment the label h alignment
	 * @param labelVAlignment the label v alignment
	 * @param horizontalScroll the horizontal scroll
	 * @param verticalScroll the vertical scroll
	 * @return the j text area
	 */
	public JTextArea scrollableTextArea( String string, int width, int height, Color foreground, Color background, GridBagConstraints constraints, 
			float fontSize, float labelHAlignment, float labelVAlignment, int horizontalScroll, int verticalScroll ) {
		
		final JScrollPane pane = new JScrollPane( verticalScroll, horizontalScroll );
		pane.setBackground( background );
		pane.setPreferredSize( new Dimension( width, height ) );
		pane.setMinimumSize( new Dimension( width, height ) );
		pane.setBorder( BorderFactory.createEmptyBorder() );
		this.add( pane, constraints );
		JTextArea textArea = new JTextArea( string );
		textArea.setAlignmentY( labelVAlignment );
		textArea.setAlignmentX( labelHAlignment );
		textArea.setFont( textArea.getFont().deriveFont( fontSize ) );
		textArea.setBackground( background );
		textArea.setForeground( foreground );
		textArea.setLineWrap( true );
		textArea.setWrapStyleWord( true );
		pane.setViewportView( textArea );
		return textArea;
		
	}

}
