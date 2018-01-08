package net.bubbaland.trivia.client.dialog;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigInteger;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.bubbaland.trivia.client.TriviaPanel;

/**
 * Super-class for most of the dialog box panels in the trivia GUI.
 *
 * Creates a new panel using the GridBagLayout manager. Also implements an AncestorListener to allow focus on an element
 * after the dialog is created.
 *
 */
public abstract class TriviaDialogPanel extends TriviaPanel implements AncestorListener, FocusListener, WindowListener {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -4127179718225373888L;

	protected static float		fontSize, textAreaFontSize;
	protected static int		sliderPaddingBottom, sliderPaddingTop, sliderPaddingRight, sliderPaddingLeft;
	protected static Color		warningColor;

	protected TriviaDialog		dialog;

	public TriviaDialogPanel() {
		super(new GridBagLayout());
	}

	/**
	 * Override the default behavior of the text area to click the OK button of the option pane on enter and insert a
	 * line break on shift-enter
	 *
	 * @param component
	 *            The text are whose behavior we want to change
	 */
	public void addEnterOverride(JComponent component) {
		component.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "Text Submit");
		component.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
		component.getActionMap().put("Text Submit", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				TriviaDialogPanel.this.submitText();
			}
		});
	}

	/**
	 * Change focus to the listened-to component when its ancestor is shown.
	 */
	@Override
	public void ancestorAdded(final AncestorEvent event) {
		// Change the focus to the text area when created
		final AncestorListener al = this;
		try {
			Thread.sleep(10);
		} catch (final InterruptedException e) {
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final JComponent component = event.getComponent();
				component.requestFocusInWindow();
				component.removeAncestorListener(al);
			}
		});

	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {
	}

	@Override
	public void focusGained(FocusEvent event) {
		final JComponent source = (JComponent) event.getSource();
		if (source instanceof JTextField) {
			// try {
			// Thread.sleep(10);
			// } catch (InterruptedException e) { }
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					( (JTextField) source ).selectAll();
				}
			});

		}
	}

	@Override
	public void focusLost(FocusEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent event) {
	}

	@Override
	public void windowClosing(WindowEvent event) {
	}

	@Override
	public void windowClosed(WindowEvent event) {
		this.dialog.removeWindowListener(this);
	}

	@Override
	public void windowIconified(WindowEvent event) {
	}

	@Override
	public void windowDeiconified(WindowEvent event) {
	}

	@Override
	public void windowActivated(WindowEvent event) {
	}

	@Override
	public void windowDeactivated(WindowEvent event) {
	}

	/**
	 * Tell the dialog to click the OK button on the option pane.
	 */
	public void submitText() {
		this.dialog.clickOK();
	}

	public static void loadProperties(Properties properties) {
		/**
		 * Warning Color
		 */
		warningColor = new Color(new BigInteger(properties.getProperty("Dialog.Warning.Color"), 16).intValue());

		/**
		 * Slider Paddings (used by AnswerEntryPanel)
		 */
		sliderPaddingBottom = Integer.parseInt(properties.getProperty("Dialog.AnswerEntry.Slider.Padding.Bottom"));
		sliderPaddingTop = Integer.parseInt(properties.getProperty("Dialog.AnswerEntry.Slider.Padding.Top"));
		sliderPaddingLeft = Integer.parseInt(properties.getProperty("Dialog.AnswerEntry.Slider.Padding.Left"));
		sliderPaddingRight = Integer.parseInt(properties.getProperty("Dialog.AnswerEntry.Slider.Padding.Right"));

		/**
		 * Font Sizes
		 */
		fontSize = Float.parseFloat(properties.getProperty("Dialog.FontSize"));
		textAreaFontSize = Float.parseFloat(properties.getProperty("Dialog.TextArea.FontSize"));
	}

}
