package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

/**
 * Super-class for most of the panels in the trivia GUI.
 * 
 * Provides methods for automatically making labels and text areas that fill their space by enclosing them in panels
 * 
 */
public abstract class TriviaPanel extends JPanel implements Serializable {

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
	 *            The string for the label
	 * @param width
	 *            The width
	 * @param height
	 *            The height
	 * @param foreground
	 *            The foreground color
	 * @param background
	 *            The background color
	 * @param constraints
	 *            The GridBag constraints
	 * @param fontSize
	 *            The font size
	 * @param labelHAlignment
	 *            The horizontal alignment for the label (JLabel constants)
	 * @param labelVAlignment
	 *            The vertical alignment for the label (JLabel constants)
	 * @return The label inside the panel
	 */
	protected JLabel enclosedLabel(String string, int width, int height, Color foreground, Color background,
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
	 * Adds a space-filling panel with a label to the panel. A reference to the label is returned so the text can be
	 * changed later.
	 * 
	 * @param string
	 *            The string for the label
	 * @param constraints
	 *            The GridBag constraints
	 * @param labelHAlignment
	 *            The horizontal alignment for the label (JLabel constants)
	 * @param labelVAlignment
	 *            The vertical alignment for the label (JLabel constants)
	 * @return The label inside the panel
	 */
	protected JLabel enclosedLabel(String string, GridBagConstraints constraints, int labelHAlignment,
			int labelVAlignment) {
		final GridBagConstraints solo = new GridBagConstraints();
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0;
		solo.weighty = 1.0;
		solo.gridx = 0;
		solo.gridy = 0;

		final JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, constraints);
		final JLabel label = new JLabel(string, labelHAlignment);
		label.setVerticalAlignment(labelVAlignment);
		panel.add(label, solo);

		return label;
	}

	protected static void setLabelProperties(JLabel label, int width, int height, Color foreground, Color background,
			float fontSize) {
		setPanelProperties((JPanel) label.getParent(), width, height, background);
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setForeground(foreground);
	}

	protected static void setTextPaneProperties(JTextPane textPane, int width, int height, Color foreground,
			Color background, float fontSize) {
		textPane.setPreferredSize(new Dimension(width, height));
		textPane.setFont(textPane.getFont().deriveFont(fontSize));
		textPane.setForeground(foreground);
		textPane.setBackground(background);
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			UIDefaults defaults = new UIDefaults();
			defaults.put("TextPane[Enabled].backgroundPainter", background);
			textPane.putClientProperty("Nimbus.Overrides", defaults);
			textPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		}
	}

	protected static void setTextAreaProperties(JTextArea textArea, int width, int height, Color foreground,
			Color background, float fontSize) {
		( (JScrollPane) textArea.getParent().getParent() ).setPreferredSize(new Dimension(width, height));

		textArea.setFont(textArea.getFont().deriveFont(fontSize));
		textArea.setForeground(foreground);
		textArea.setBackground(background);
	}

	protected static void setPanelProperties(JPanel panel, int width, int height, Color background) {
		panel.setBackground(background);
		panel.setPreferredSize(new Dimension(width, height));
		panel.setMinimumSize(new Dimension(width, height));
	}


	protected static void setButtonProperties(Component button, int width, int height, float fontSize, Color foreground) {
		if (!( button instanceof JButton || button instanceof JToggleButton )) {
			return;
		}
		button.setForeground(foreground);
		button.setFont(button.getFont().deriveFont(fontSize));
		button.setPreferredSize(new Dimension(width, height));
		button.setMinimumSize(new Dimension(width, height));
	}

	/**
	 * Adds a word-wrapping text area inside of a scrollable pane to the panel. A reference to the text area is returned
	 * so the text can be read/changed later.
	 * 
	 * @param string
	 *            The initial string for the text area
	 * @param constraints
	 *            The GridBag constraints
	 * @param horizontalScroll
	 *            The horizontal scroll bar policy (JScrollPane constants)
	 * @param verticalScroll
	 *            The vertical scroll bar policy (JScrollPane constants)
	 * @return The text area inside the scroll pane
	 */
	public JTextArea scrollableTextArea(String string, GridBagConstraints constraints, int horizontalScroll,
			int verticalScroll) {

		final JScrollPane pane = new JScrollPane(verticalScroll, horizontalScroll) {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void processMouseWheelEvent(MouseWheelEvent e) {
				boolean scrollUp = e.getWheelRotation() < 0;
				if (scrollUp && this.verticalScrollBar.getValue() == this.verticalScrollBar.getMinimum()) {
					if (getParent() != null) {
						getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, getParent()));
					}
					return;
				}
				if (!scrollUp
						&& this.verticalScrollBar.getValue() == this.verticalScrollBar.getMaximum() - this.getHeight()) {
					if (getParent() != null)
						getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, getParent()));
					return;
				}
				super.processMouseWheelEvent(e);
			}
		};
		pane.setBorder(BorderFactory.createEmptyBorder());
		this.add(pane, constraints);
		final JTextArea textArea = new JTextArea(string) {
			private static final long	serialVersionUID	= 1L;

			// public JToolTip createToolTip() {
			// JMultiLineToolTip tip = new JMultiLineToolTip();
			// tip.setComponent(this);
			// // tip.setFixedWidth(300);
			// tip.setColumns(50);
			// return tip;
			// }
		};
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder());
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		pane.setViewportView(textArea);

		return textArea;
	}

	/**
	 * Adds a word-wrapping text area inside of a scrollable pane to the panel. A reference to the text area is returned
	 * so the text can be read/changed later.
	 * 
	 * @param string
	 *            The initial string for the text area
	 * @param width
	 *            The width
	 * @param height
	 *            The height
	 * @param foreground
	 *            The foreground color
	 * @param background
	 *            The background color
	 * @param constraints
	 *            The GridBag constraints
	 * @param fontSize
	 *            The font size
	 * @param horizontalScroll
	 *            The horizontal scroll bar policy (JScrollPane constants)
	 * @param verticalScroll
	 *            The vertical scroll bar policy (JScrollPane constants)
	 * @return The text area inside the scroll pane
	 */
	public JTextArea scrollableTextArea(String string, int width, int height, Color foreground, Color background,
			GridBagConstraints constraints, float fontSize, int horizontalScroll, int verticalScroll) {

		final JScrollPane pane = new JScrollPane(verticalScroll, horizontalScroll) {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void processMouseWheelEvent(MouseWheelEvent e) {
				boolean scrollUp = e.getWheelRotation() < 0;
				if (scrollUp && this.verticalScrollBar.getValue() == this.verticalScrollBar.getMinimum()) {
					if (getParent() != null) {
						getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, getParent()));
					}
					return;
				}
				if (!scrollUp
						&& this.verticalScrollBar.getValue() == this.verticalScrollBar.getMaximum() - this.getHeight()) {
					if (getParent() != null)
						getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, getParent()));
					return;
				}
				super.processMouseWheelEvent(e);
			}
		};
		pane.setPreferredSize(new Dimension(width, height));
		// pane.setMinimumSize(new Dimension(width, height));
		pane.setBorder(BorderFactory.createEmptyBorder());
		this.add(pane, constraints);
		final JTextArea textArea = new JTextArea(string) {
			private static final long	serialVersionUID	= 1L;

			// public JToolTip createToolTip() {
			// JMultiLineToolTip tip = new JMultiLineToolTip();
			// tip.setComponent(this);
			// // tip.setFixedWidth(300);
			// tip.setColumns(50);
			// return tip;
			// }
		};
		textArea.setFont(textArea.getFont().deriveFont(fontSize));
		textArea.setBackground(background);
		textArea.setForeground(foreground);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder());
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
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
