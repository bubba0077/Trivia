package net.bubbaland.trivia.client;

import java.awt.GridBagLayout;
import java.util.Properties;

/**
 * Super-class for most of the panels in the trivia GUI.
 * 
 * Provides methods for automatically making labels and text areas that fill their space by enclosing them in panels
 * 
 */
public abstract class TriviaMainPanel extends TriviaPanel {

	private static final long		serialVersionUID	= -5381727804575779591L;

	final protected TriviaClient	client;
	protected TriviaFrame			frame;

	/**
	 * Instantiates a new Trivia Panel
	 * 
	 * @param client
	 *            TODO
	 * @param frame
	 *            TODO
	 */
	public TriviaMainPanel(TriviaClient client, TriviaFrame frame) {
		super(new GridBagLayout());
		this.client = client;
		this.frame = frame;
	}

	public void changeFrame(TriviaFrame newFrame) {
		this.frame = newFrame;
	}

	/**
	 * Requires all sub-classes to have a method that updates their contents.
	 */
	public void updateGUI() {
		this.updateGUI(false);
	}

	public abstract void updateGUI(boolean forceUpdate);

	protected abstract void loadProperties(Properties properties);


}
