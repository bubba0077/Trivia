package net.bubbaland.trivia.client;

import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Super-class for most of the dialog box panels in the trivia GUI.
 * 
 * Creates a new panel using the GridBagLayout manager. Also implements an AncestorListener to allow focus on an element after the dialog is created.
 * 
 */
public class TriviaDialogPanel extends JPanel implements AncestorListener {

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -4127179718225373888L;

	/**
	 * 
	 */
	public TriviaDialogPanel() {
		super( new GridBagLayout() );
	}

	/**
	 * Change focus to the listened-to component when its ancestor is shown.
	 */
	@Override
	public void ancestorAdded(final AncestorEvent event) {
		// Change the focus to the text area when created
		final AncestorListener al = this;
		try {
			Thread.sleep(100);
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
	public void ancestorMoved(AncestorEvent event) {	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {	}

}
