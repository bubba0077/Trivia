package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class ScoreByRoundPanel.
 */
public class ScoreByRoundPanel extends TriviaPanel {
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -2047479093232798581L;
	
	/** The Constant HEADER_BACKGROUND_COLOR. */
	final private static Color HEADER_BACKGROUND_COLOR = Color.BLACK;
	
	/** The Constant BACKGROUND_COLOR. */
	final private static Color BACKGROUND_COLOR = Color.DARK_GRAY;
	
	/** The Constant ALT_BACKGROUND_COLOR. */
	final private static Color ALT_BACKGROUND_COLOR = Color.BLACK;
	
	/** The Constant ALT_INTERVAL. */
	final private static int ALT_INTERVAL = 5;
	
	/** The Constant HOUR_COLOR. */
	final private static Color HOUR_COLOR = Color.WHITE;
	
	/** The Constant EARNED_COLOR. */
	final private static Color EARNED_COLOR = Color.GREEN;
	
	/** The Constant VALUE_COLOR. */
	final private static Color VALUE_COLOR = new Color(30, 144, 255);
	
	/** The Constant PERCENT_COLOR. */
	final private static Color PERCENT_COLOR = Color.PINK;
	
	/** The Constant CUMULATIVE_EARNED_COLOR. */
	final private static Color CUMULATIVE_EARNED_COLOR = Color.GREEN.brighter();
	
	/** The Constant CUMULATIVE_VALUE_COLOR. */
	final private static Color CUMULATIVE_VALUE_COLOR = (new Color(30, 144, 255)).brighter();
	
	/** The Constant PERCENT_TOTAL_COLOR. */
	final private static Color PERCENT_TOTAL_COLOR = Color.PINK;
	
	/** The Constant ANNOUNCED_COLOR. */
	final private static Color ANNOUNCED_COLOR = Color.YELLOW;
	
	/** The Constant PLACE_COLOR. */
	final private static Color PLACE_COLOR = Color.ORANGE;
	
	/** The Constant DISCREPENCY_COLOR. */
	final private static Color DISCREPENCY_COLOR = Color.MAGENTA;
	
	/** The Constant HEADER_HEIGHT. */
	final private static int HEADER_HEIGHT = 28;
	
	/** The Constant ROW_HEIGHT. */
	final private static int ROW_HEIGHT = 12;
	
	/** The Constant HEADER_FONT_SIZE. */
	final private static float HEADER_FONT_SIZE = 12.0f;
	
	/** The Constant DATA_FONT_SIZE. */
	final private static float DATA_FONT_SIZE = 18.0f;
	
	/** The Constant HOUR_WIDTH. */
	final private static int HOUR_WIDTH = 35;
	
	/** The Constant EARNED_WIDTH. */
	final private static int EARNED_WIDTH = 65;
	
	/** The Constant VALUE_WIDTH. */
	final private static int VALUE_WIDTH = 65;
	
	/** The Constant PERCENT_WIDTH. */
	final private static int PERCENT_WIDTH = 80;
	
	/** The Constant CUMULATIVE_EARNED_WIDTH. */
	final private static int CUMULATIVE_EARNED_WIDTH = 80;
	
	/** The Constant CUMULATIVE_VALUE_WIDTH. */
	final private static int CUMULATIVE_VALUE_WIDTH = 80;
	
	/** The Constant PERCENT_TOTAL_WIDTH. */
	final private static int PERCENT_TOTAL_WIDTH = 80;
	
	/** The Constant ANNOUNCED_WIDTH. */
	final private static int ANNOUNCED_WIDTH = 85;
	
	/** The Constant PLACE_WIDTH. */
	final private static int PLACE_WIDTH = 40;
	
	/** The Constant DISCREPENCY_WIDTH. */
	final private static int DISCREPENCY_WIDTH = 12;
	
	/** The internal scroll panel. */
	private InternalScrollPanel internalScrollPanel;
		
	
	/**
	 * Instantiates a new score by round panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public ScoreByRoundPanel( TriviaInterface server, TriviaClient client ) {
		
		super(new GridBagLayout());
				
		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;		
				
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;
				
		constraints.gridx = 0;		constraints.gridy = 0;
		enclosedLabel("", HOUR_WIDTH, HEADER_HEIGHT/2, HOUR_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 0;		constraints.gridy = 1;
		enclosedLabel("Hour", HOUR_WIDTH, HEADER_HEIGHT/2, HOUR_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 1;		constraints.gridy = 0;
		enclosedLabel("", EARNED_WIDTH, HEADER_HEIGHT/2, EARNED_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 1;		constraints.gridy = 1;
		enclosedLabel("Earned", EARNED_WIDTH, HEADER_HEIGHT/2, EARNED_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 2;		constraints.gridy = 0;
		enclosedLabel("", VALUE_WIDTH, HEADER_HEIGHT/2, VALUE_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 2;		constraints.gridy = 1;
		enclosedLabel("Possible", VALUE_WIDTH, HEADER_HEIGHT/2, VALUE_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 3;		constraints.gridy = 0;
		enclosedLabel("", PERCENT_WIDTH, HEADER_HEIGHT/2, PERCENT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 3;		constraints.gridy = 1;
		enclosedLabel("Percent", PERCENT_WIDTH, HEADER_HEIGHT/2, PERCENT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 4;		constraints.gridy = 0;
		enclosedLabel("Cumulative", CUMULATIVE_EARNED_WIDTH, HEADER_HEIGHT/2, CUMULATIVE_EARNED_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 4;		constraints.gridy = 1;
		enclosedLabel("Score", CUMULATIVE_EARNED_WIDTH, HEADER_HEIGHT/2, CUMULATIVE_EARNED_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 5;		constraints.gridy = 0;
		enclosedLabel("Cumulative", CUMULATIVE_VALUE_WIDTH, HEADER_HEIGHT/2, CUMULATIVE_VALUE_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 5;		constraints.gridy = 1;
		enclosedLabel("Possible", CUMULATIVE_VALUE_WIDTH, HEADER_HEIGHT/2, CUMULATIVE_VALUE_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 6;		constraints.gridy = 0;
		enclosedLabel("Percent", PERCENT_TOTAL_WIDTH, HEADER_HEIGHT/2, PERCENT_TOTAL_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 6;		constraints.gridy = 1;
		enclosedLabel("Total", PERCENT_TOTAL_WIDTH, HEADER_HEIGHT/2, PERCENT_TOTAL_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 7;		constraints.gridy = 0;
		enclosedLabel("Announced", ANNOUNCED_WIDTH, HEADER_HEIGHT/2, ANNOUNCED_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 7;		constraints.gridy = 1;
		enclosedLabel("Score", ANNOUNCED_WIDTH, HEADER_HEIGHT/2, ANNOUNCED_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.gridx = 8;		constraints.gridy = 0;
		enclosedLabel("", PLACE_WIDTH, HEADER_HEIGHT/2, PLACE_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 8;		constraints.gridy = 1;
		enclosedLabel("Place", PLACE_WIDTH, HEADER_HEIGHT/2, PLACE_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		
		constraints.weightx = 1.0;
		constraints.gridx = 9;		constraints.gridy = 0;
		enclosedLabel("", DISCREPENCY_WIDTH, HEADER_HEIGHT/2, DISCREPENCY_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.gridx = 9;		constraints.gridy = 1;
		enclosedLabel("Discrepency Notes", DISCREPENCY_WIDTH, HEADER_HEIGHT/2, DISCREPENCY_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
		constraints.weightx = 0.00;
		
		
		
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 11;
		this.internalScrollPanel = new InternalScrollPanel(server, client);
		JScrollPane scrollPane = new JScrollPane( this.internalScrollPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setPreferredSize(new Dimension( 200, 200) );
		scrollPane.setBorder( BorderFactory.createEmptyBorder() );
		this.add( scrollPane, constraints );
		
	}
	
	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public void update() {
		this.internalScrollPanel.update();		
	}
	
	/**
	 * The Class InternalScrollPanel.
	 */
	private class InternalScrollPanel extends TriviaPanel implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long	serialVersionUID	= 7121481355244434308L;
		
		/** The place labels. */
		private JLabel[] earnedLabels, valueLabels, percentLabels, cumulativeEarnedLabels, cumulativeValueLabels, percentTotalLabels, announcedScoreLabels, placeLabels;
		
		/** The discrepency text field. */
		private JTextField[] discrepencyTextField;
		
		/** The server. */
		private TriviaInterface			server;
		
		/** The client. */
		private TriviaClient			client;
		
		/** The n rounds. */
		private int nRounds;

		/**
		 * Instantiates a new internal scroll panel.
		 *
		 * @param server the server
		 * @param client the client
		 */
		public InternalScrollPanel(TriviaInterface server, TriviaClient	client) {
			super(new GridBagLayout());
			
			this.server = server;
			this.client = client;
			
			int tryNumber = 0;
			boolean success = false;
			while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
				tryNumber++;
				try {
					this.nRounds = client.getTrivia().getNRounds();
					success = true;
				} catch ( Exception e ) {
					client.log( "Couldn't retrieve number of rounds from server (try #" + tryNumber + ")." );
				}			
			}

			if ( !success ) {
				client.disconnected();
				return;
			}
									
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;			
			constraints.weighty = 0.0;
			
			this.earnedLabels = new JLabel[nRounds];
			this.valueLabels = new JLabel[nRounds];
			this.percentLabels = new JLabel[nRounds];
			this.cumulativeEarnedLabels = new JLabel[nRounds];
			this.cumulativeValueLabels = new JLabel[nRounds];
			this.percentTotalLabels = new JLabel[nRounds];
			this.announcedScoreLabels = new JLabel[nRounds];
			this.placeLabels = new JLabel[nRounds];
			this.discrepencyTextField = new JTextField[nRounds];		
			
			for(int r=0; r<nRounds; r++) {
				
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;
				
				Color bColor = BACKGROUND_COLOR;
				if((r+1)%ALT_INTERVAL == 0) { bColor = ALT_BACKGROUND_COLOR; }
				
				constraints.gridx = 0;		constraints.gridy = r;
				enclosedLabel((r+1) + "", HOUR_WIDTH, ROW_HEIGHT, HOUR_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 1;		constraints.gridy = r;
				this.earnedLabels[r] = enclosedLabel("", EARNED_WIDTH, ROW_HEIGHT, EARNED_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 2;		constraints.gridy = r;
				this.valueLabels[r] = enclosedLabel("", VALUE_WIDTH, ROW_HEIGHT, VALUE_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 3;		constraints.gridy = r;
				this.percentLabels[r] = enclosedLabel("", PERCENT_WIDTH, ROW_HEIGHT, PERCENT_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 4;		constraints.gridy = r;
				this.cumulativeEarnedLabels[r] = enclosedLabel("", CUMULATIVE_EARNED_WIDTH, ROW_HEIGHT, CUMULATIVE_EARNED_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 5;		constraints.gridy = r;
				this.cumulativeValueLabels[r] = enclosedLabel("", CUMULATIVE_VALUE_WIDTH, ROW_HEIGHT, CUMULATIVE_VALUE_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 6;		constraints.gridy = r;
				this.percentTotalLabels[r] = enclosedLabel("", PERCENT_TOTAL_WIDTH, ROW_HEIGHT, PERCENT_TOTAL_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 7;		constraints.gridy = r;
				this.announcedScoreLabels[r] = enclosedLabel("", ANNOUNCED_WIDTH, ROW_HEIGHT, ANNOUNCED_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.gridx = 8;		constraints.gridy = r;
				this.placeLabels[r] = enclosedLabel("", PLACE_WIDTH, ROW_HEIGHT, PLACE_COLOR, bColor, constraints, DATA_FONT_SIZE, JLabel.CENTER, JLabel.CENTER );
				
				constraints.weightx = 1.0;
				constraints.gridx = 9;		constraints.gridy = r;
				this.discrepencyTextField[r] = new JTextField("", 10);
				this.discrepencyTextField[r].setBackground( bColor.brighter() );
				this.discrepencyTextField[r].setForeground( DISCREPENCY_COLOR );
				this.discrepencyTextField[r].setBorder( BorderFactory.createEmptyBorder() );
				this.discrepencyTextField[r].setFont(this.discrepencyTextField[r].getFont().deriveFont( DATA_FONT_SIZE ));
				this.discrepencyTextField[r].addActionListener(this);
				this.add( this.discrepencyTextField[r], constraints );				
				
			}
			
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.gridx = 0;
			constraints.gridy = nRounds;
			constraints.gridwidth = 11;
			JPanel panel = new JPanel( new GridBagLayout() );
			panel.setBackground( HEADER_BACKGROUND_COLOR );
			panel.setPreferredSize( new Dimension(0,0) );
			this.add(panel, constraints);
			
		}

		/* (non-Javadoc)
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public void update() {
			
			Trivia trivia = client.getTrivia();
					
			int cumulativeEarned = 0;
			int cumulativeValue = 0;
			for(int r=0; r<nRounds; r++) {
				
				int earned = trivia.getEarned(r+1);
				int value = trivia.getValue(r+1);
				int announced = trivia.getAnnouncedPoints( r+1 );
				int place = trivia.getAnnouncedPlace( r+1 );
				String discrepency = trivia.getDiscrepencyText( r+1 );
				boolean isAnnounced = trivia.isAnnounced( r+1 );
				cumulativeEarned += earned;
				cumulativeValue += value;
				
				if(value != 0) {
					String percent = String.format( "%04.1f", (earned * 100.0 / value) ) + "%";
					String percentTotal = String.format( "%04.1f", (cumulativeEarned * 100.0 / cumulativeValue) ) + "%";
					this.earnedLabels[r].setText(earned + "");
					this.valueLabels[r].setText(value + ""); 
					this.percentLabels[r].setText(percent);
					this.cumulativeEarnedLabels[r].setText(cumulativeEarned+"");
					this.cumulativeValueLabels[r].setText(cumulativeValue+"");
					this.percentTotalLabels[r].setText(percentTotal);
					if(isAnnounced) {
						this.announcedScoreLabels[r].setText(announced+"");
						this.placeLabels[r].setText(TriviaClient.ordinalize(place));
					}
				}
				
				this.discrepencyTextField[r].setText( discrepency );
									
			}		

		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public synchronized void actionPerformed(ActionEvent e) {
			JTextField source = (JTextField)e.getSource();
			for(int r=0; r<nRounds; r++) {
				if(source.equals(this.discrepencyTextField[r])) {
					int tryNumber = 0;
					boolean success = false;
					while ( tryNumber < TriviaClient.MAX_RETRIES && success == false ) {
						tryNumber++;
						try {
							server.setDiscrepancyText(r+1, source.getText());
							success = true;
						} catch ( Exception exception ) {
							client.log( "Couldn't set discrepency text on server (try #" + tryNumber + ")." );
						}
					}

					if ( !success ) {
						client.disconnected();
						return;
					}
					
					return;
				}
			}
			
		}
		
	}
	
	

}
