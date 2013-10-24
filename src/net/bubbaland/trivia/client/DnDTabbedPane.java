package net.bubbaland.trivia.client;

/**
 * Modified DnDTabbedPane.java http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
 * originally written by Terai Atsuhiro. so that tabs can be transfered from one pane to another. eed3si9n.
 * 
 * Further modifications by Walter Kolczynski to implement creating a new frame and tabbed pane when dragging a tab off
 * of all existing tabbed panes.
 */

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public class DnDTabbedPane extends JTabbedPane {
	public static final long						serialVersionUID	= 1L;
	private static final int						LINEWIDTH			= 3;
	private static final String						NAME				= "TabTransferData";
	private final DataFlavor						FLAVOR				= new DataFlavor(
																				DataFlavor.javaJVMLocalObjectMimeType,
																				NAME);
	private static GhostGlassPane					s_glassPane			= new GhostGlassPane();
	private final static TearAwayTab				tearTab				= new TearAwayTab();

	private boolean									m_isDrawRect		= false;
	private final Rectangle2D						m_lineRect			= new Rectangle2D.Double();

	private final Color								m_lineColor			= new Color(0, 100, 255);
	private TabAcceptor								m_acceptor			= null;

	private static final ArrayList<DnDTabbedPane>	tabbedPaneList		= new ArrayList<DnDTabbedPane>(0);
	private static final ArrayList<ChangeListener>	tabbedPaneListeners	= new ArrayList<ChangeListener>(0);

	public DnDTabbedPane() {
		super();
		registerTabbedPane(this);
		final DragSourceListener dsl = new DragSourceListener() {
			public void dragEnter(DragSourceDragEvent e) {
				// System.out.println("dragEnter");
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			public void dragExit(DragSourceEvent e) {
				// System.out.println(a++ + "dragExit");
				tearTab.attach(e.getLocation());
				// e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				m_lineRect.setRect(0, 0, 0, 0);
				m_isDrawRect = false;
				s_glassPane.setPoint(new Point(-1000, -1000));
				s_glassPane.repaint();
			}

			public void dragOver(DragSourceDragEvent e) {

				TabTransferData data = getTabTransferData(e);
				// System.out.println(a++ + e.getTargetActions() + " " + DnDConstants.ACTION_NONE);

				if (data == null || e.getTargetActions() != DnDConstants.ACTION_MOVE) {
					// System.out.println("action none");
					tearTab.attach(e.getLocation());
				}

				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			public void dragDropEnd(DragSourceDropEvent e) {
				m_isDrawRect = false;
				m_lineRect.setRect(0, 0, 0, 0);

				if (hasGhost()) {
					s_glassPane.setVisible(false);
					s_glassPane.setImage(null);
				}
			}

			public void dropActionChanged(DragSourceDragEvent e) {
			}
		};

		final DragGestureListener dgl = new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e) {
				Point tabPt = e.getDragOrigin();
				int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
				if (dragTabIndex < 0) {
					return;
				}

				initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);
				try {
					e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(DnDTabbedPane.this, dragTabIndex), dsl);
				} catch (InvalidDnDOperationException idoe) {
					idoe.printStackTrace();
				}
			}
		};

		new DropTarget(this, DnDConstants.ACTION_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dgl);
		m_acceptor = new TabAcceptor() {
			public boolean isDropAcceptable(DnDTabbedPane a_component, int a_index) {
				return true;
			}
		};
	}

	public TabAcceptor getAcceptor() {
		return m_acceptor;
	}

	public void setAcceptor(TabAcceptor a_value) {
		m_acceptor = a_value;
	}

	private TabTransferData getTabTransferData(DragSourceDragEvent a_event) {
		try {
			return (TabTransferData) a_event.getDragSourceContext().getTransferable().getTransferData(FLAVOR);
		} catch (Exception e) {
		}

		return null;
	}

	TabTransferData getTabTransferData(DropTargetDropEvent a_event) {
		try {
			TabTransferData data = (TabTransferData) a_event.getTransferable().getTransferData(FLAVOR);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private TabTransferData getTabTransferData(DropTargetDragEvent a_event) {
		try {
			TabTransferData data = (TabTransferData) a_event.getTransferable().getTransferData(FLAVOR);
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	class TabTransferable implements Transferable {
		private TabTransferData	m_data	= null;

		public TabTransferable(DnDTabbedPane a_tabbedPane, int a_tabIndex) {
			m_data = new TabTransferData(DnDTabbedPane.this, a_tabIndex);
		}

		public Object getTransferData(DataFlavor flavor) {
			return m_data;
			// return DnDTabbedPane.this;
		}

		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] f = new DataFlavor[1];
			f[0] = FLAVOR;
			return f;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.getHumanPresentableName().equals(NAME);
		}
	}

	class TabTransferData {
		private DnDTabbedPane	m_tabbedPane	= null;
		private int				m_tabIndex		= -1;

		public TabTransferData() {
		}

		public TabTransferData(DnDTabbedPane a_tabbedPane, int a_tabIndex) {
			m_tabbedPane = a_tabbedPane;
			m_tabIndex = a_tabIndex;
		}

		public DnDTabbedPane getTabbedPane() {
			return m_tabbedPane;
		}

		public void setTabbedPane(DnDTabbedPane pane) {
			m_tabbedPane = pane;
		}

		public int getTabIndex() {
			return m_tabIndex;
		}

		public void setTabIndex(int index) {
			m_tabIndex = index;
		}
	}

	private Point buildGhostLocation(Point a_location) {
		Point retval = new Point(a_location);

		switch (getTabPlacement()) {
			case JTabbedPane.TOP: {
				retval.y = 1;
				retval.x -= s_glassPane.getGhostWidth() / 2;
			}
				break;

			case JTabbedPane.BOTTOM: {
				retval.y = getHeight() - 1 - s_glassPane.getGhostHeight();
				retval.x -= s_glassPane.getGhostWidth() / 2;
			}
				break;

			case JTabbedPane.LEFT: {
				retval.x = 1;
				retval.y -= s_glassPane.getGhostHeight() / 2;
			}
				break;

			case JTabbedPane.RIGHT: {
				retval.x = getWidth() - 1 - s_glassPane.getGhostWidth();
				retval.y -= s_glassPane.getGhostHeight() / 2;
			}
				break;
		} // switch

		retval = SwingUtilities.convertPoint(DnDTabbedPane.this, retval, s_glassPane);
		return retval;
	}

	public static ArrayList<DnDTabbedPane> getTabbedPanes() {
		return DnDTabbedPane.tabbedPaneList;
	}

	public static void registerTabbedPane(DnDTabbedPane newPane) {
		DnDTabbedPane.tabbedPaneList.add(newPane);
		for (ChangeListener listener : DnDTabbedPane.tabbedPaneListeners) {
			newPane.addChangeListener(listener);
		}
	}

	public static ArrayList<ChangeListener> getTabbedPaneListeners() {
		return DnDTabbedPane.tabbedPaneListeners;
	}

	public static void registerTabbedPaneListener(ChangeListener listener) {
		DnDTabbedPane.tabbedPaneListeners.add(listener);
		for (DnDTabbedPane pane : DnDTabbedPane.tabbedPaneList) {
			pane.addChangeListener(listener);
		}
	}

	class CDropTargetListener implements DropTargetListener {
		public void dragEnter(DropTargetDragEvent e) {
			// System.out.println("DropTarget.dragEnter: " + DnDTabbedPane.this);

			if (isDragAcceptable(e)) {
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			} // if
		}

		public void dragExit(DropTargetEvent e) {
			// System.out.println("DropTarget.dragExit: " + DnDTabbedPane.this);
			m_isDrawRect = false;
		}

		public void dropActionChanged(DropTargetDragEvent e) {
		}

		public void dragOver(final DropTargetDragEvent e) {
			TabTransferData data = getTabTransferData(e);

			if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM) {
				initTargetLeftRightLine(getTargetTabIndex(e.getLocation()), data);
			} else {
				initTargetTopBottomLine(getTargetTabIndex(e.getLocation()), data);
			} // if-else

			repaint();
			if (hasGhost()) {
				s_glassPane.setPoint(buildGhostLocation(e.getLocation()));
				s_glassPane.repaint();
			}
		}

		public void drop(DropTargetDropEvent a_event) {
			// System.out.println("DropTarget.drop: " + DnDTabbedPane.this);

			if (isDropAcceptable(a_event)) {
				convertTab(getTabTransferData(a_event), getTargetTabIndex(a_event.getLocation()));
				a_event.dropComplete(true);
			} else {
				a_event.dropComplete(false);
			} // if-else

			m_isDrawRect = false;
			repaint();
		}

		public boolean isDragAcceptable(DropTargetDragEvent e) {
			// System.out.println("DropTarget.isDragAcceptable: " + DnDTabbedPane.this);

			Transferable t = e.getTransferable();
			if (t == null) {
				return false;
			} // if

			DataFlavor[] flavor = e.getCurrentDataFlavors();
			if (!t.isDataFlavorSupported(flavor[0])) {
				return false;
			} // if

			TabTransferData data = getTabTransferData(e);

			if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) {
				return true;
			} // if

			if (DnDTabbedPane.this != data.getTabbedPane()) {
				if (m_acceptor != null) {
					return m_acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
				} // if
			} // if

			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e) {
			// System.out.println("DropTarget.isDropAcceptable: " + DnDTabbedPane.this);

			Transferable t = e.getTransferable();
			if (t == null) {
				return false;
			} // if

			DataFlavor[] flavor = e.getCurrentDataFlavors();
			if (!t.isDataFlavorSupported(flavor[0])) {
				return false;
			} // if

			TabTransferData data = getTabTransferData(e);

			if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) {
				return true;
			} // if

			if (DnDTabbedPane.this != data.getTabbedPane()) {
				if (m_acceptor != null) {
					return m_acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
				} // if
			} // if
			return false;
		}
	}

	private boolean	m_hasGhost	= true;

	public void setPaintGhost(boolean flag) {
		m_hasGhost = flag;
	}

	public boolean hasGhost() {
		return m_hasGhost;
	}

	public void addTab(String tabName, TriviaPanel panel) {
		super.addTab(tabName, panel);
		if (panel instanceof ChangeListener) {
			this.addChangeListener((ChangeListener) panel);
		}
		for (Component child : panel.getComponents()) {
			if (child instanceof ChangeListener) {
				System.out.println("listener added");
				this.addChangeListener((ChangeListener) child);
			}
		}
	}

	/**
	 * returns potential index for drop.
	 * 
	 * @param a_point
	 *            point given in the drop site component's coordinate
	 * @return returns potential index for drop.
	 */
	int getTargetTabIndex(Point a_point) {
		boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;

		// if the pane is empty, the target index is always zero.
		if (getTabCount() == 0) {
			return 0;
		} // if

		for (int i = 0; i < getTabCount(); i++) {
			Rectangle r = getBoundsAt(i);
			if (isTopOrBottom) {
				r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
			} else {
				r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
			} // if-else

			if (r.contains(a_point)) {
				return i;
			} // if
		} // for

		Rectangle r = getBoundsAt(getTabCount() - 1);
		if (isTopOrBottom) {
			int x = r.x + r.width / 2;
			r.setRect(x, r.y, getWidth() - x, r.height);
		} else {
			int y = r.y + r.height / 2;
			r.setRect(r.x, y, r.width, getHeight() - y);
		} // if-else

		return r.contains(a_point) ? getTabCount() : -1;
	}

	void convertTab(TabTransferData a_data, int a_targetIndex) {

		DnDTabbedPane source = a_data.getTabbedPane();
		// System.out.println("this=source? " + ( this == source ));
		int sourceIndex = a_data.getTabIndex();
		if (sourceIndex < 0) {
			return;
		} // if
			// Save the tab's component, title, and TabComponent.
		Component cmp = source.getComponentAt(sourceIndex);
		String str = source.getTitleAt(sourceIndex);
		Component tcmp = source.getTabComponentAt(sourceIndex);

		if (this != source) {
			source.remove(sourceIndex);

			if (a_targetIndex == getTabCount()) {
				addTab(str, cmp);
				setTabComponentAt(getTabCount() - 1, tcmp);
			} else {
				if (a_targetIndex < 0) {
					a_targetIndex = 0;
				} // if

				insertTab(str, null, cmp, null, a_targetIndex);
				setTabComponentAt(a_targetIndex, tcmp);
			} // if

			setSelectedComponent(cmp);
			return;
		} // if
		if (a_targetIndex < 0 || sourceIndex == a_targetIndex) {
			return;
		} // if
		if (a_targetIndex == getTabCount()) {
			source.remove(sourceIndex);
			addTab(str, cmp);
			setTabComponentAt(getTabCount() - 1, tcmp);
			setSelectedIndex(getTabCount() - 1);
		} else if (sourceIndex > a_targetIndex) {
			source.remove(sourceIndex);
			insertTab(str, null, cmp, null, a_targetIndex);
			setTabComponentAt(a_targetIndex, tcmp);
			setSelectedIndex(a_targetIndex);
		} else {
			source.remove(sourceIndex);
			insertTab(str, null, cmp, null, a_targetIndex - 1);
			setTabComponentAt(a_targetIndex - 1, tcmp);
			setSelectedIndex(a_targetIndex - 1);
		}

	}

	private void initTargetLeftRightLine(int next, TabTransferData a_data) {
		if (next < 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		} // if

		if (( a_data.getTabbedPane() == this ) && ( a_data.getTabIndex() == next || next - a_data.getTabIndex() == 1 )) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
		} else if (getTabCount() == 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		} else if (next == 0) {
			Rectangle rect = getBoundsAt(0);
			m_lineRect.setRect(-LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			m_isDrawRect = true;
		} else if (next == getTabCount()) {
			Rectangle rect = getBoundsAt(getTabCount() - 1);
			m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			m_isDrawRect = true;
		} else {
			Rectangle rect = getBoundsAt(next - 1);
			m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			m_isDrawRect = true;
		}
	}

	private void initTargetTopBottomLine(int next, TabTransferData a_data) {
		if (next < 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		} // if

		if (( a_data.getTabbedPane() == this ) && ( a_data.getTabIndex() == next || next - a_data.getTabIndex() == 1 )) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
		} else if (getTabCount() == 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		} else if (next == getTabCount()) {
			Rectangle rect = getBoundsAt(getTabCount() - 1);
			m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
			m_isDrawRect = true;
		} else if (next == 0) {
			Rectangle rect = getBoundsAt(0);
			m_lineRect.setRect(rect.x, -LINEWIDTH / 2, rect.width, LINEWIDTH);
			m_isDrawRect = true;
		} else {
			Rectangle rect = getBoundsAt(next - 1);
			m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
			m_isDrawRect = true;
		}
	}

	private void initGlassPane(Component c, Point tabPt, int a_tabIndex) {
		// Point p = (Point) pt.clone();
		getRootPane().setGlassPane(s_glassPane);
		if (hasGhost()) {
			Rectangle rect = getBoundsAt(a_tabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			c.paint(g);
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			s_glassPane.setImage(image);
		} // if

		s_glassPane.setPoint(buildGhostLocation(tabPt));
		s_glassPane.setVisible(true);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (m_isDrawRect) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(m_lineColor);
			g2.fill(m_lineRect);
		} // if
	}

	public interface TabAcceptor {
		boolean isDropAcceptable(DnDTabbedPane a_component, int a_index);
	}
}

class GhostGlassPane extends JPanel {
	public static final long		serialVersionUID	= 1L;
	private final AlphaComposite	m_composite;

	private Point					m_location			= new Point(0, 0);

	private BufferedImage			m_draggingGhost		= null;

	public GhostGlassPane() {
		setOpaque(false);
		m_composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	}

	public void setImage(BufferedImage draggingGhost) {
		m_draggingGhost = draggingGhost;
	}

	public void setPoint(Point a_location) {
		m_location.x = a_location.x;
		m_location.y = a_location.y;
	}

	public int getGhostWidth() {
		if (m_draggingGhost == null) {
			return 0;
		} // if

		return m_draggingGhost.getWidth(this);
	}

	public int getGhostHeight() {
		if (m_draggingGhost == null) {
			return 0;
		} // if

		return m_draggingGhost.getHeight(this);
	}

	public void paintComponent(Graphics g) {
		if (m_draggingGhost == null) {
			return;
		} // if

		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(m_composite);

		g2.drawImage(m_draggingGhost, (int) m_location.getX(), (int) m_location.getY(), null);
	}

}
