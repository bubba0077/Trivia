package net.bubbaland.trivia.client;

//imports for GUI
import java.awt.*;

import net.bubbaland.trivia.TriviaInterface;

// TODO: Auto-generated Javadoc
/**
 * The Class RoundPanel.
 */
public class RoundPanel extends TriviaPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 9190017804155701978L;
	
	/** The round header panel. */
	private HeaderPanel			roundHeaderPanel;
	
	/** The round qlist panel. */
	private RoundQlistPanel		roundQlistPanel;

	/**
	 * Instantiates a new round panel.
	 *
	 * @param server the server
	 * @param client the client
	 */
	public RoundPanel( TriviaInterface server, TriviaClient client ) {

		super( new GridBagLayout() );

		roundHeaderPanel = new HeaderPanel( server, client );
		roundQlistPanel = new RoundQlistPanel( server, client );

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;

		c.gridx = 0;
		c.gridy = 0;
		this.add( roundHeaderPanel, c );
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1.0;
		this.add( roundQlistPanel, c );

	}

	/* (non-Javadoc)
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void update() {

		this.roundHeaderPanel.update();
		this.roundQlistPanel.update();

	}

}
