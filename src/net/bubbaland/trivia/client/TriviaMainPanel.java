package net.bubbaland.trivia.client;

import java.awt.GridBagLayout;

/**
 * Super-class for most of the panels in the trivia GUI.
 * 
 * Provides methods for automatically making labels and text areas that fill their space by enclosing them in panels
 * 
 */
public abstract class TriviaMainPanel extends TriviaPanel {

	private static final long	serialVersionUID	= -5381727804575779591L;

	/**
	 * Instantiates a new Trivia Panel
	 */
	public TriviaMainPanel() {
		super(new GridBagLayout());
	}

	/**
	 * Requires all sub-classes to have a method that updates their contents.
	 */
	public void update() {
		this.update(false);
	}

	public abstract void update(boolean forceUpdate);

	protected abstract void loadProperties();


}
