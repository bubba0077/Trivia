package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class HistoryPanel.
 *
 * @author Walter Kolczynski
 */
public class HistoryPanel extends TriviaPanel implements ItemListener {
	
	/** The Constant serialVersionUID. */
	final private static long	serialVersionUID	= -5094201314926851039L;
	
	/** The Constant SELECTOR_ROW_HEIGHT. */
	final private static int SELECTOR_ROW_HEIGHT = 24;
	
	/** The Constant SELECTOR_HEIGHT. */
	final private static int SELECTOR_HEIGHT = 20;
	
	/** The Constant SELECTOR_WIDTH. */
	final private static int SELECTOR_WIDTH = 40;
	
	/** The Constant ROUND_FONT_SIZE. */
	final private static float ROUND_FONT_SIZE = 16.0f;
	
	/** The Constant TOPLINE_BACKGROUND_COLOR. */
	final private static Color TOPLINE_BACKGROUND_COLOR = Color.BLACK;
	
	/** The Constant ROUND_COLOR. */
	final private static Color ROUND_COLOR = Color.WHITE;
	
	/** The Constant SELECTOR_BACKGROUND_COLOR. */
	final private static Color SELECTOR_BACKGROUND_COLOR = Color.WHITE;
		
	/** The server. */
	TriviaInterface server;
	
	/** The client. */
	TriviaClient client;
	
	/** The round selector. */
	JComboBox<String> roundSelector;
	
	/** The n rounds. */
	int nRounds;

	/** The round q list panel. */
	private RoundQlistPanel	roundQListPanel;
	
	/**
	 * Instantiates a new history panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public HistoryPanel(TriviaInterface server, TriviaClient client) {
		
		super(new GridBagLayout());
		
		this.server = server;
		this.client = client;
		
		this.nRounds = client.getTrivia().getNRounds();
		
		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;		
				
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 0.05;
		c.weighty = 0.0;		
		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setPreferredSize( new Dimension(0, SELECTOR_ROW_HEIGHT) );
		topPanel.setMinimumSize( new Dimension(0, SELECTOR_ROW_HEIGHT) );
		topPanel.setBackground( TOPLINE_BACKGROUND_COLOR );
		this.add( topPanel, c );
		JLabel label = new JLabel("Round: ", JLabel.RIGHT);
		label.setVerticalAlignment( JLabel.CENTER );
		label.setFont( label.getFont().deriveFont(ROUND_FONT_SIZE) );
		label.setForeground( ROUND_COLOR );
		topPanel.add(label, solo);
		
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.0;
		topPanel = new JPanel(new GridBagLayout());
		topPanel.setPreferredSize( new Dimension(SELECTOR_WIDTH, SELECTOR_ROW_HEIGHT) );
		topPanel.setMinimumSize( new Dimension(SELECTOR_WIDTH, SELECTOR_ROW_HEIGHT) );
		topPanel.setBackground( TOPLINE_BACKGROUND_COLOR );
		String[] rNumbers = new String[nRounds];
		for(int r=0; r<nRounds; r++) { rNumbers[r] = (r+1)+""; }
		this.add( topPanel, c );
		this.roundSelector = new JComboBox<String>( rNumbers );
		this.roundSelector.addItemListener( this );
		this.roundSelector.setBackground( SELECTOR_BACKGROUND_COLOR );
		this.roundSelector.setPreferredSize(  new Dimension(SELECTOR_WIDTH, SELECTOR_HEIGHT) );
		this.roundSelector.setMinimumSize(  new Dimension(SELECTOR_WIDTH, SELECTOR_HEIGHT) );
		topPanel.add( this.roundSelector );
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weightx = 1.0;
		c.weighty = 1.0;
		this.roundQListPanel = new RoundQlistPanel(server, client, false, 1);
		this.add( roundQListPanel, c );
				
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
