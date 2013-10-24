package net.bubbaland.trivia.client;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;

public class TearAwayTab extends JWindow {
	private static final long	serialVersionUID	= -2723420566227526365L;
	private final Timer			mousePoller;

	public TearAwayTab() {
		this.add(new JLabel("New Window"));
		this.pack();
		this.mousePoller = new Timer(0, new ActionListener() {
			private Point	lastPoint	= MouseInfo.getPointerInfo().getLocation();

			@Override
			public void actionPerformed(ActionEvent e) {
				Point point = MouseInfo.getPointerInfo().getLocation();
				if (!point.equals(lastPoint)) {
					center(point);
				}
				lastPoint = point;
			}
		});
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new EasyDropTarget(), true);
		this.setVisible(false);
	}

	private void center(Point location) {
		// System.out.println(location);
		Point center = new Point();
		center.setLocation(location.x - this.getWidth() / 2, location.y - this.getHeight() / 2);
		TearAwayTab.this.setLocation(center);
		for (DnDTabbedPane pane : DnDTabbedPane.getTabbedPanes()) {
			Rectangle bounds = pane.getBounds();
			// System.out.println(bounds);
			if (bounds.contains(location)) {
				this.detach();
				return;
			}
		}
	}

	public void attach(Point location) {
		// System.out.println("attach");
		center(location);
		mousePoller.start();
		this.setVisible(true);
	}

	public void detach() {
		// System.out.println("detatch");
		mousePoller.stop();
		this.setVisible(false);
	}

	private class EasyDropTarget implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			dtde.acceptDrag(dtde.getDropAction());
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
		}

		@Override
		public void drop(DropTargetDropEvent a_event) {
			detach();
			new FloatingPanel(a_event);
			a_event.dropComplete(true);
		}
	}
}