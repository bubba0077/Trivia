package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Super-class for most of the panels in the trivia GUI.
 * 
 * Provides methods for automatically making labels and text areas that fill their space by enclosing them in panels
 * 
 */
public abstract class TriviaPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 3713561221924406596L;

	/**
	 * Instantiates a new Trivia Panel
	 */
	public TriviaPanel() {
		super(new GridBagLayout());
	}

	/**
	 * Adds a space-filling panel with a label to the panel. A reference to the label is returned so the text can be
	 * changed later.
	 * 
	 * @param string
	 *            the string for the label
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param foreground
	 *            the foreground color
	 * @param background
	 *            the background color
	 * @param constraints
	 *            the GridBag constraints
	 * @param fontSize
	 *            the font size
	 * @param labelHAlignment
	 *            the horizontal alignment for the label (JLabel constants)
	 * @param labelVAlignment
	 *            the vertical alignment for the label (JLabel constants)
	 * @return the label inside the panel
	 */
	public JLabel enclosedLabel(String string, int width, int height, Color foreground, Color background,
			GridBagConstraints constraints, float fontSize, int labelHAlignment, int labelVAlignment) {
		final GridBagConstraints solo = new GridBagConstraints();
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0;
		solo.weighty = 1.0;
		solo.gridx = 0;
		solo.gridy = 0;

		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(background);
		panel.setPreferredSize(new Dimension(width, height));
		panel.setMinimumSize(new Dimension(width, height));
		this.add(panel, constraints);
		final JLabel label = new JLabel(string, labelHAlignment);
		label.setVerticalAlignment(labelVAlignment);
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setForeground(foreground);
		panel.add(label, solo);

		return label;
	}

	/**
	 * Adds a word-wrapping text area inside of a scrollable pane to the panel. A reference to the text area is returned
	 * so the text can be read/changed later.
	 * 
	 * @param string
	 *            the initial string for the text area
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param foreground
	 *            the foreground color
	 * @param background
	 *            the background color
	 * @param constraints
	 *            the GridBag constraints
	 * @param fontSize
	 *            the font size
	 * @param horizontalScroll
	 *            the horizontal scroll bar policy (JScrollPane constants)
	 * @param verticalScroll
	 *            the vertical scroll bar policy (JScrollPane constants)
	 * @return the text area inside the scroll pane
	 */
	public JTextArea scrollableTextArea(String string, int width, int height, Color foreground, Color background,
			GridBagConstraints constraints, float fontSize, int horizontalScroll, int verticalScroll) {

		final JScrollPane pane = new JScrollPane(verticalScroll, horizontalScroll);
		pane.setPreferredSize(new Dimension(width, height));
		pane.setMinimumSize(new Dimension(width, height));
		pane.setBorder(BorderFactory.createEmptyBorder());
		this.add(pane, constraints);
		final JTextArea textArea = new JTextArea(string);
		textArea.setFont(textArea.getFont().deriveFont(fontSize));
		textArea.setBackground(background);
		textArea.setForeground(foreground);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		pane.setViewportView(textArea);

		return textArea;

	}

	/**
	 * Requires all sub-classes to have a method that updates their contents.
	 */
	public void update() {
		this.update(false);
	}

	public abstract void update(boolean forceUpdate);

}
