package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

/**
 * Abstract class providing common methods for all trivia panels.
 *
 * @author Walter Kolczynski
 *
 */
public class TriviaPanel extends JPanel {

	private static final long serialVersionUID = 7023089773420890665L;

	public TriviaPanel() {
		super();
	}

	public TriviaPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public TriviaPanel(LayoutManager layout) {
		super(layout);
	}

	public TriviaPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	/**
	 * Adds a word-wrapping text pane inside of a scrollable pane to the panel that can process hyperlink clicks. A
	 * reference to the text pane is returned so the text can be read/changed later.
	 *
	 * @param client
	 *            TODO
	 * @param string
	 *            The initial string for the text pane
	 * @param constraints
	 *            The GridBag constraints
	 * @param horizontalScroll
	 *            The horizontal scroll bar policy (JScrollPane constants)
	 * @param verticalScroll
	 *            The vertical scroll bar policy (JScrollPane constants)
	 *
	 * @return The text pane inside the scroll pane
	 */
	public QuestionPane hyperlinkedTextPane(TriviaClient client, String string, GridBagConstraints constraints,
			int horizontalScroll, int verticalScroll) {

		final InternalScrollPane pane = new InternalScrollPane(verticalScroll, horizontalScroll);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setEnabled(false);
		this.add(pane, constraints);
		final QuestionPane textPane = new QuestionPane(client, new DefaultStyledDocument());
		textPane.setContentType("text/html");
		textPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException exception) {}
				} else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					TriviaPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
					TriviaPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}

			}
		});
		textPane.setText(string);
		textPane.setBorder(BorderFactory.createEmptyBorder());
		final DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		pane.setViewportView(textPane);

		return textPane;
	}

	/**
	 * Adds a word-wrapping text pane inside of a scrollable pane to the panel that can process hyperlink clicks. A
	 * reference to the text pane is returned so the text can be read/changed later.
	 *
	 * @param client
	 *            TODO
	 * @param string
	 *            The initial string for the text pane
	 * @param constraints
	 *            The GridBag constraints
	 * @param horizontalScroll
	 *            The horizontal scroll bar policy (JScrollPane constants)
	 * @param verticalScroll
	 *            The vertical scroll bar policy (JScrollPane constants)
	 *
	 * @return The text pane inside the scroll pane
	 */
	public QuestionPane hyperlinkedTextPane(int rNumber, String string, GridBagConstraints constraints,
			int horizontalScroll, int verticalScroll) {

		final InternalScrollPane pane = new InternalScrollPane(verticalScroll, horizontalScroll);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setEnabled(false);
		this.add(pane, constraints);
		final QuestionPane textPane = new QuestionPane(rNumber, new DefaultStyledDocument());
		textPane.setContentType("text/html");
		textPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException exception) {}
				} else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					TriviaPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
					TriviaPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}

			}
		});
		textPane.setText(string);
		textPane.setBorder(BorderFactory.createEmptyBorder());
		final DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		pane.setViewportView(textPane);

		return textPane;
	}

	/**
	 * Adds a word-wrapping text pane inside of a scrollable pane to the panel that can process hyperlink clicks. A
	 * reference to the text pane is returned so the text can be read/changed later.
	 *
	 * @param client
	 *            TODO
	 * @param string
	 *            The initial string for the text pane
	 * @param constraints
	 *            The GridBag constraints
	 * @param horizontalScroll
	 *            The horizontal scroll bar policy (JScrollPane constants)
	 * @param verticalScroll
	 *            The vertical scroll bar policy (JScrollPane constants)
	 *
	 * @return The text pane inside the scroll pane
	 */
	public JTextPane scrollableTextPane(String string, GridBagConstraints constraints, int horizontalScroll,
			int verticalScroll) {

		final InternalScrollPane pane = new InternalScrollPane(verticalScroll, horizontalScroll);
		pane.setBorder(BorderFactory.createEmptyBorder());
		pane.setEnabled(false);
		this.add(pane, constraints);
		final JTextPane textPane = new JTextPane(new DefaultStyledDocument());
		textPane.setText(string);
		textPane.setBorder(BorderFactory.createEmptyBorder());
		final DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		pane.setViewportView(textPane);

		return textPane;
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
	public JTextPane scrollableTextPane(String string, int width, int height, Color foreground, Color background,
			GridBagConstraints constraints, float fontSize, int horizontalScroll, int verticalScroll) {

		final InternalScrollPane pane = new InternalScrollPane(verticalScroll, horizontalScroll);
		pane.setPreferredSize(new Dimension(width, height));
		pane.setBorder(BorderFactory.createEmptyBorder());
		this.add(pane, constraints);
		final JTextPane textPane = new JTextPane(new DefaultStyledDocument());
		textPane.setBorder(BorderFactory.createEmptyBorder());
		final DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		pane.setViewportView(textPane);

		TriviaPanel.setTextPaneProperties(textPane, width, height, foreground, background, fontSize);

		return textPane;
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

		final InternalScrollPane pane = new InternalScrollPane(verticalScroll, horizontalScroll);
		pane.setBorder(BorderFactory.createEmptyBorder());
		this.add(pane, constraints);
		final JTextArea textArea = new JTextArea(string);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder());
		final DefaultCaret caret = (DefaultCaret) textArea.getCaret();
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

		final InternalScrollPane pane = new InternalScrollPane(verticalScroll, horizontalScroll);
		pane.setPreferredSize(new Dimension(width, height));
		pane.setBorder(BorderFactory.createEmptyBorder());
		this.add(pane, constraints);
		final JTextArea textArea = new JTextArea(string);
		textArea.setFont(textArea.getFont().deriveFont(fontSize));
		textArea.setBackground(background);
		textArea.setForeground(foreground);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder());
		final DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		pane.setViewportView(textArea);

		return textArea;
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
	public JLabel enclosedLabel(String string, GridBagConstraints constraints, int labelHAlignment,
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
	protected JTextField enclosedTextField(String string, GridBagConstraints constraints, int labelHAlignment) {
		final GridBagConstraints solo = new GridBagConstraints();
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0;
		solo.weighty = 1.0;
		solo.gridx = 0;
		solo.gridy = 0;

		final JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, constraints);
		final JTextField field = new JTextField(string, labelHAlignment);
		// field.setVerticalAlignment(labelVAlignment);
		panel.add(field, solo);

		return field;
	}

	/**
	 * Update a button's properties.
	 *
	 * @param button
	 *            The button to update
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @param foreground
	 *            The new foreground color
	 * @param fontSize
	 *            The new font size
	 */
	protected static void setButtonProperties(Component button, int width, int height, Color foreground,
			float fontSize) {
		if (!( button instanceof JButton || button instanceof JToggleButton )) return;
		button.setForeground(foreground);
		button.setFont(button.getFont().deriveFont(fontSize));
		button.setPreferredSize(new Dimension(width, height));
		button.setMinimumSize(new Dimension(width, height));
	}

	/**
	 * Update a combo box's properties.
	 *
	 * @param comboBox
	 *            The combo box to update
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @param foreground
	 *            The new foreground color
	 * @param background
	 *            The new background color
	 * @param panelBackground
	 *            The new panel background color
	 * @param fontSize
	 *            The new font size
	 */
	protected static void setComboBoxProperties(JComboBox<String> comboBox, int width, int height, Color foreground,
			Color background, Color panelBackground, float fontSize) {
		setPanelProperties((JPanel) comboBox.getParent(), width, height, panelBackground);
		comboBox.setBackground(background);
		comboBox.setPreferredSize(new Dimension(width, height));
		comboBox.setMinimumSize(new Dimension(width, height));
		comboBox.setBackground(background);
		comboBox.setForeground(foreground);
	}

	/**
	 * Update a label's properties.
	 *
	 * @param label
	 *            The label to update
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @param foreground
	 *            The new foreground color
	 * @param background
	 *            The new background color
	 * @param fontSize
	 *            The new font size
	 */
	protected static void setLabelProperties(JLabel label, int width, int height, Color foreground, Color background,
			float fontSize) {
		setPanelProperties((JPanel) label.getParent(), width, height, background);
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setForeground(foreground);
	}

	protected static void setTextFieldProperties(JTextField field, int width, int height, Color foreground,
			Color background, float fontSize) {
		setPanelProperties((JPanel) field.getParent(), width, height, background);
		field.setFont(field.getFont().deriveFont(fontSize));
		field.setForeground(foreground);
		field.setBackground(background);
	}

	/**
	 * Update a panel's properties.
	 *
	 * @param panel
	 *            The panel to update
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @param background
	 *            The new background color
	 */
	protected static void setPanelProperties(JPanel panel, int width, int height, Color background) {
		panel.setBackground(background);
		panel.setPreferredSize(new Dimension(width, height));
		panel.setMinimumSize(new Dimension(width, height));
	}

	/**
	 * Update a text area's properties.
	 *
	 * @param textArea
	 *            The text area to update
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @param foreground
	 *            The new foreground color
	 * @param background
	 *            The new background color
	 * @param fontSize
	 *            The new font size
	 */
	protected static void setTextAreaProperties(JTextArea textArea, int width, int height, Color foreground,
			Color background, float fontSize) {
		( (JScrollPane) textArea.getParent().getParent() ).setPreferredSize(new Dimension(width, height));

		textArea.setFont(textArea.getFont().deriveFont(fontSize));
		textArea.setForeground(foreground);
		textArea.setBackground(background);
	}

	/**
	 * Update a text pane's properties.
	 *
	 * @param textPane
	 *            The text pane to update
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @param foreground
	 *            The new foreground color
	 * @param background
	 *            The new background color
	 * @param fontSize
	 *            The new font size
	 */
	protected static void setTextPaneProperties(JTextPane textPane, int width, int height, Color foreground,
			Color background, float fontSize) {
		// Scroll pane is two levels up
		textPane.getParent().getParent().setPreferredSize(new Dimension(width, height));
		textPane.getParent().getParent().setMinimumSize(new Dimension(width, height));
		textPane.setFont(textPane.getFont().deriveFont(fontSize));
		textPane.setForeground(foreground);
		textPane.setBackground(background);
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			final UIDefaults defaults = new UIDefaults();
			defaults.put("TextPane[Enabled].backgroundPainter", background);
			textPane.putClientProperty("Nimbus.Overrides", defaults);
			textPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		}
	}

	/**
	 * A custom text pane that automatically replaces references to visual trivia with hyperlinks to the web page with
	 * the visual trivia.
	 *
	 * @author Walter Kolczynski
	 *
	 */
	public class QuestionPane extends JTextPane {
		private static final long	serialVersionUID	= -4043733624909281303L;

		private final String		visualPattern		= "([Vv]isual )([Tt]rivia )?(#)?([0-9]+)";
		private final String		audioPattern		= "([Aa]udio)( [Tt]rivia)?";
		private String				simpleString;
		final private TriviaClient	client;
		private int					rNumber;
		final private boolean		live;

		public QuestionPane(TriviaClient client) {
			super();
			this.client = client;
			this.rNumber = 0;
			this.live = true;
		}

		public QuestionPane(int rNumber) {
			super();
			this.client = null;
			this.rNumber = rNumber;
			this.live = false;
		}

		public QuestionPane(TriviaClient client, StyledDocument doc) {
			super(doc);
			this.client = client;
			this.rNumber = 0;
			this.live = true;
		}

		public QuestionPane(int rNumber, StyledDocument doc) {
			super(doc);
			this.client = null;
			this.rNumber = rNumber;
			this.live = false;
		}

		public void setRNumber(int rNumber) {
			this.rNumber = rNumber;
		}

		private int getRNumber() {
			if (this.live) {
				this.rNumber = this.client.getCurrentRoundNumber();
			}
			return this.rNumber;
		}

		@Override
		public void setText(String question) {
			final String RR = String.format("%02d", this.getRNumber());
			String hQuestion = question.replaceFirst(this.visualPattern,
					"<a href=\"" + TriviaGUI.VISUAL_URL + "$4\">Visual Trivia #$4</a>");
			hQuestion = hQuestion.replaceFirst("(?<=" + TriviaGUI.VISUAL_URL + ")([0-9])(?![0-9])", "0$1"); // pad
																											// single-digit
																											// with zero
			hQuestion = hQuestion.replaceFirst(this.audioPattern,
					"<a href=\"" + TriviaGUI.AUDIO_URL + "/Hour_" + RR + "\">Audio Trivia</a>");
			if (!hQuestion.equals(this.simpleString)) {
				super.setText(hQuestion);
				this.simpleString = hQuestion;
			}
		}

		public boolean textEquals(String string) {
			final String RR = String.format("%02d", this.getRNumber());
			string = string.replaceFirst(this.visualPattern,
					"<a href=\"" + TriviaGUI.VISUAL_URL + "$4\">Visual Trivia #$4</a>");
			string.replaceFirst(this.audioPattern,
					"<a href=\"" + TriviaGUI.AUDIO_URL + "/Hour_" + RR + "\">Audio Trivia</a>");
			return this.simpleString.equals(string);
		}

	}

	/**
	 * A scroll pane designed to be used inside of another scroll pane. When the mouse wheel is scrolled and the scroll
	 * pane is already at that edge, the scroll is passed up to the parent container.
	 *
	 * @author Walter Kolczynski
	 *
	 */
	private class InternalScrollPane extends JScrollPane {

		private static final long serialVersionUID = -8318646989146037930L;

		public InternalScrollPane(int vsbPolicy, int hsbPolicy) {
			super(vsbPolicy, hsbPolicy);
		}

		@Override
		protected void processMouseWheelEvent(MouseWheelEvent e) {
			final boolean scrollUp = e.getWheelRotation() < 0;
			// If the mouse wheel scrolls up and we are already at top, tell parent to scroll up
			if (scrollUp && this.verticalScrollBar.getValue() == this.verticalScrollBar.getMinimum()) {
				if (this.getParent() != null) {
					this.getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, this.getParent()));
				}
				return;
			}
			// If the mouse wheel scrolls down and we are already at bottom, tell parent to scroll down
			if (!scrollUp
					&& this.verticalScrollBar.getValue() == this.verticalScrollBar.getMaximum() - this.getHeight()) {
				if (this.getParent() != null) {
					this.getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, this.getParent()));
				}
				return;
			}
			super.processMouseWheelEvent(e);
		}
	}

}