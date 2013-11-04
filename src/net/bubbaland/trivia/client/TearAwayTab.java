package net.bubbaland.trivia.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
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
import java.awt.image.BufferedImage;

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
	private static final long		serialVersionUID	= -2723420566227526365L;

	// A timer to poll the mouse location and move the window around
	private final Timer				mousePoller;

	private final GhostGlassPane	glassPane;

	// The root client
	private final TriviaClient		client;

	public TearAwayTab(TriviaClient client) {
		this.client = client;
		this.glassPane = new GhostGlassPane();
		this.add(this.glassPane);
		// Create a timer to poll the mouse location and update the window location
		this.mousePoller = new Timer(50, new ActionListener() {
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
		// Make frame transparent
		this.setBackground(new Color(0, 255, 0, 0));
		this.setOpacity(0.7f);
		// Don't display this until needed
		this.setVisible(false);
	}

	/**
	 * Display this window and attach it to the mouse pointer.
	 * 
	 * @param location
	 *            The location to start at
	 */
	public void attach(DnDTabbedPane tabbedPane, int tabIndex) {
		if (this.isVisible()) {
			return;
		}
		// Get image of tab
		final Rectangle rect = tabbedPane.getBoundsAt(tabIndex);
		BufferedImage tabImage = new BufferedImage(tabbedPane.getWidth(), tabbedPane.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final Graphics g = tabImage.getGraphics();
		tabbedPane.paint(g);
		tabImage = tabImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
		// Get image of panel
		Component panel = tabbedPane.getComponentAt(tabIndex);
		BufferedImage panelImage = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics panelGraphics = panelImage.getGraphics();
		panel.paint(panelGraphics);
		int combinedHeight = tabImage.getHeight() + panelImage.getHeight();
		// Combine images into single image
		BufferedImage combinedImage = new BufferedImage(panelImage.getWidth(), combinedHeight,
				BufferedImage.TYPE_INT_ARGB);
		combinedImage.createGraphics().drawImage(tabImage, 0, 0, null);
		combinedImage.createGraphics().drawImage(panelImage, 0, tabImage.getHeight(), null);
		// Set image of pane
		this.glassPane.setImage(combinedImage);
		// Set size & location and start polling mouse
		this.setSize(panel.getSize());
		this.mousePoller.start();
		this.setVisible(true);
	}

	/**
	 * Stop displaying this window.
	 */
	public void detach() {
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
		Point offsetLocation = location;
		offsetLocation.setLocation(location.x - 10, location.y - 10);
		TearAwayTab.this.setLocation(offsetLocation);
		for (final DnDTabbedPane pane : DnDTabbedPane.getTabbedPanes()) {
			final Rectangle bounds = pane.getRootPane().getBounds();
			bounds.setLocation(pane.getRootPane().getLocationOnScreen());
			if (bounds.contains(location)) {
				this.setVisible(false);
				return;
			}
		}
		this.setVisible(true);
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