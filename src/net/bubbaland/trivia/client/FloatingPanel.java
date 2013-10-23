package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FloatingPanel extends JFrame implements ChangeListener {

	private static final long	serialVersionUID	= -3639363131235278472L;

	private final DnDTabbedPane	book;

	public FloatingPanel(DropTargetDropEvent a_event) {
		super();
		JPanel panel = new JPanel(new GridBagLayout());

		this.book = new DnDTabbedPane();
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

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(this.book)) {
			if (this.book.getTabCount() == 0) {
				this.dispose();
			}
		}

	}

}
