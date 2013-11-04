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

/**
 * A class that allows tabs to be dragged off of a frame.
 * 
 * This class provides a drop location when a tab is dragged off a frame by having a window follow the cursor around
 * when not inside of a @TriviaFrame. When a tab is dropped into this, it passes the drop data along to a new
 * TriviaFrame.
 * 
 * @author Walter Kolczynski
 * 
 */
public class TearAwayTab extends JWindow {
	private static final long	serialVersionUID	= -2723420566227526365L;

	// A timer to poll the mouse location and move the window around
	private final Timer			mousePoller;
	// The root client
	private final TriviaClient	client;

	public TearAwayTab(TriviaClient client) {
		this.client = client;
		this.add(new JLabel("New Window"));
		this.pack();
		// Create a timer to poll the mouse location and update the window location
		this.mousePoller = new Timer(0, new ActionListener() {
			private Point	lastPoint	= MouseInfo.getPointerInfo().getLocation();

			@Override
			public void actionPerformed(ActionEvent e) {
				final Point point = MouseInfo.getPointerInfo().getLocation();
				if (!point.equals(this.lastPoint)) {
					TearAwayTab.this.center(point);
				}
				this.lastPoint = point;
			}
		});
		// Make this a valid drop target
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new EasyDropTarget(), true);
		// Don't display this until needed
		this.setVisible(false);
	}

	/**
	 * Display this window and attach it to the mouse pointer.
	 * 
	 * @param location
	 *            The location to start at
	 */
	public void attach(Point location) {
		// System.out.println("attach");
		this.center(location);
		this.mousePoller.start();
		this.setVisible(true);
	}

	/**
	 * Stop displaying this window.
	 */
	public void detach() {
		// System.out.println("detatch");
		this.mousePoller.stop();
		this.setVisible(false);
	}

	/**
	 * Move the window.
	 * 
	 * @param location
	 *            The new window location
	 */
	private void center(Point location) {
		final Point center = new Point();
		center.setLocation(location.x - this.getWidth() / 2, location.y - this.getHeight() / 2);
		TearAwayTab.this.setLocation(center);
		for (final DnDTabbedPane pane : DnDTabbedPane.getTabbedPanes()) {
			final Rectangle bounds = pane.getBounds();
			if (bounds.contains(location)) {
				this.detach();
				return;
			}
		}
	}

	/**
	 * A drop target to handle creation of a new frame when a tab is dropped.
	 * 
	 * @author Walter Kolczynski
	 * 
	 */
	private class EasyDropTarget implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			dtde.acceptDrag(dtde.getDropAction());
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		public void drop(DropTargetDropEvent a_event) {
			TearAwayTab.this.detach();
			new TriviaFrame(TearAwayTab.this.client, a_event, TearAwayTab.this.getLocation());
			a_event.dropComplete(true);
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}
	}
}