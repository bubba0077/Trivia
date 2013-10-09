package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import net.bubbaland.trivia.TriviaInterface;

/**
 * Panel to select and display any round
 *
 */
public class HistoryPanel extends TriviaPanel implements ItemListener {
	
	/** The Constant serialVersionUID. */
	final private static long	serialVersionUID	= -5094201314926851039L;
	
	/** Font size for the round selector */
	final private static float ROUND_FONT_SIZE = 16.0f;
	
	/**
	 * Colors
	 */
	final private static Color TOPLINE_BACKGROUND_COLOR = Color.BLACK;
	final private static Color ROUND_COLOR = Color.WHITE;
	final private static Color SELECTOR_BACKGROUND_COLOR = Color.WHITE;
	
	/**
	 * Sizes 
	 */
	final private static int SELECTOR_ROW_HEIGHT = 24;
	final private static int SELECTOR_HEIGHT = 20;
	final private static int SELECTOR_WIDTH = 40;
	
	/**
	 * GUI Elements that will need to be updated
	 */	
	private final JComboBox<String> roundSelector;	
	private final RoundQlistPanel	roundQListPanel;
	
	// The number of rounds
	private final int nRounds;
		
	/**
	 * Data sources
	 */
	TriviaInterface server;
	TriviaClient client;		
	
	/**
	 * Instantiates a new history panel.
	 *
	 * @param server the server
	 * @param client the client application
	 */
	public HistoryPanel(TriviaInterface server, TriviaClient client) {
		
		super(new GridBagLayout());
		
		this.server = server;
		this.client = client;		
		this.nRounds = client.getTrivia().getNRounds();
		
		// Set up layout constraints		
		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;	
				
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.05;
		constraints.weighty = 0.0;
		
		/**
		 * Create the top row with the selector combo box
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setPreferredSize( new Dimension(0, SELECTOR_ROW_HEIGHT) );
		topPanel.setMinimumSize( new Dimension(0, SELECTOR_ROW_HEIGHT) );
		topPanel.setBackground( TOPLINE_BACKGROUND_COLOR );
		this.add( topPanel, constraints );
		JLabel label = new JLabel("Round: ", JLabel.RIGHT);
		label.setVerticalAlignment( JLabel.CENTER );
		label.setFont( label.getFont().deriveFont(ROUND_FONT_SIZE) );
		label.setForeground( ROUND_COLOR );
		topPanel.add(label, solo);
		
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.weightx = 0.0;
		topPanel = new JPanel(new GridBagLayout());
		topPanel.setPreferredSize( new Dimension(SELECTOR_WIDTH, SELECTOR_ROW_HEIGHT) );
		topPanel.setMinimumSize( new Dimension(SELECTOR_WIDTH, SELECTOR_ROW_HEIGHT) );
		topPanel.setBackground( TOPLINE_BACKGROUND_COLOR );
		String[] rNumbers = new String[nRounds];
		for(int r=0; r<nRounds; r++) { rNumbers[r] = (r+1)+""; }
		this.add( topPanel, constraints );
		this.roundSelector = new JComboBox<String>( rNumbers );
		this.roundSelector.addItemListener( this );
		this.roundSelector.setBackground( SELECTOR_BACKGROUND_COLOR );
		this.roundSelector.setPreferredSize(  new Dimension(SELECTOR_WIDTH, SELECTOR_HEIGHT) );
		this.roundSelector.setMinimumSize(  new Dimension(SELECTOR_WIDTH, SELECTOR_HEIGHT) );
		topPanel.add( this.roundSelector );
		
		/**
		 * Add a question list panel to show the selected round data
		 */		
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		this.roundQListPanel = new RoundQlistPanel(server, client, false, 1);
		this.add( roundQListPanel, constraints );				
	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {
		this.roundQListPanel.update();

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@SuppressWarnings( "unchecked" )
	@Override
	public synchronized void itemStateChanged(ItemEvent e) {
		JComboBox<String> source = (JComboBox<String>)e.getSource();
		int rNumber = Integer.parseInt( (String)source.getSelectedItem() );
		this.roundQListPanel.setRound( rNumber );
	}

}
