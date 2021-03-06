package net.bubbaland.trivia.client;

import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.SwingUtilities;

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
	final protected TriviaGUI		gui;

	/**
	 * Instantiates a new Trivia Panel
	 *
	 * @param triviaGUI
	 *            TODO
	 * @param frame
	 *            TODO
	 */
	public TriviaMainPanel(TriviaClient triviaGUI, TriviaFrame frame) {
		super(new GridBagLayout());
		this.client = triviaGUI;
		this.gui = frame.getGUI();
		this.frame = frame;
	}

	public void changeFrame(TriviaFrame newFrame) {
		this.frame = newFrame;
	}

	/**
	 * Requires all sub-classes to have a method that updates their contents.
	 */
	protected void updateGUI() {
		this.updateGUIonEDT(false);
	}

	protected abstract void updateGUI(boolean forceUpdate);

	public final void updateGUIonEDT(boolean forceUpdate) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TriviaMainPanel.this.updateGUI(forceUpdate);
			}
		});
	}

	public final void updateGUIonEDT() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TriviaMainPanel.this.updateGUI();
			}
		});
	}

	protected abstract void loadProperties(Properties properties);


}
