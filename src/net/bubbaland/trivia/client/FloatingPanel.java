package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * 
 * @author Walter Kolczynski
 * 
 */
public class FloatingPanel extends JFrame implements ChangeListener {

	private static final long							serialVersionUID		= -3639363131235278472L;
	private static final ArrayList<FloatingPanel>		floatingPanelList		= new ArrayList<FloatingPanel>(0);
	private static final ArrayList<WindowFocusListener>	floatingPanelListeners	= new ArrayList<WindowFocusListener>(0);
	private final DnDTabbedPane							book;

	public FloatingPanel(TriviaClient client, DropTargetDropEvent a_event) {
		super("Trivia Satellite Panel");
		this.setName("Trivia Satellite Panel");
		JPanel panel = new JPanel(new GridBagLayout());
		registerFloatingPanel(this);

		this.book = new DnDTabbedPane(client);
		this.book.convertTab(this.book.getTabTransferData(a_event), this.book.getTargetTabIndex(a_event.getLocation()));

		this.book.addChangeListener(this);

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		panel.add(this.book, constraints);
		this.add(panel);
		this.pack();
		this.setVisible(true);
	}

	public DnDTabbedPane getTabbedPane() {
		return this.book;
	}

	public static ArrayList<FloatingPanel> getFloatingPanels() {
		return FloatingPanel.floatingPanelList;
	}

	public static void registerFloatingPanel(FloatingPanel newPanel) {
		FloatingPanel.floatingPanelList.add(newPanel);
		for (WindowFocusListener listener : FloatingPanel.floatingPanelListeners) {
			newPanel.addWindowFocusListener(listener);
		}
	}

	public static ArrayList<WindowFocusListener> getFloatingPanelListeners() {
		return FloatingPanel.floatingPanelListeners;
	}

	public static void registerFloatingPanelListener(WindowFocusListener listener) {
		FloatingPanel.floatingPanelListeners.add(listener);
		for (FloatingPanel panel : FloatingPanel.floatingPanelList) {
			panel.addWindowFocusListener(listener);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(this.book)) {
			if (this.book.getTabCount() == 0) {
				// If there are no tabs left, hide the frame
				this.setVisible(false);
				// Wait 100 ms to see if the tab is added back, then close if there are still no tabs
				Timer timer = new Timer(100, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!FloatingPanel.this.isVisible()) {
							FloatingPanel.this.dispose();
						}
					}
				});
				timer.setRepeats(false);
				timer.start();
			} else {
				this.setVisible(true);
			}
		}

	}

}
