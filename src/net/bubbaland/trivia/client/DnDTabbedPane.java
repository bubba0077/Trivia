package net.bubbaland.trivia.client;

/**
 * Modified DnDTabbedPane.java http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
 * originally written by Terai Atsuhiro. so that tabs can be transfered from one pane to another. eed3si9n.
 * 
 * Further modifications by Walter Kolczynski to implement creating a new frame and tabbed pane when dragging a tab off
 * of all existing tabbed panes.
 */

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.Painter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DnDTabbedPane extends JTabbedPane implements MouseListener, ActionListener, ChangeListener {
	public static final long						serialVersionUID	= 1L;
	private static final int						LINEWIDTH			= 3;
	private static final String						NAME				= "TabTransferData";
	private final DataFlavor						FLAVOR				= new DataFlavor(
																				DataFlavor.javaJVMLocalObjectMimeType,
																				NAME);
	private static GhostGlassPane					s_glassPane			= new GhostGlassPane();
	private final TearAwayTab						tearTab;

	private boolean									m_isDrawRect		= false;
	private final Rectangle2D						m_lineRect			= new Rectangle2D.Double();

	private final Color								m_lineColor			= new Color(0, 100, 255);
	private TabAcceptor								m_acceptor			= null;

	private static final ArrayList<DnDTabbedPane>	tabbedPaneList		= new ArrayList<DnDTabbedPane>(0);
	private static final ArrayList<ChangeListener>	tabbedPaneListeners	= new ArrayList<ChangeListener>(0);
	private final TriviaClient						client;
	private final JPopupMenu						closeMenu;

	private final TriviaFrame						parent;
	private final JPanel							blankPanel;

	private static final JLabel						addTabLabel			= new JLabel(
																				new ImageIcon(DnDTabbedPane.class
																						.getResource("images/plus.png")));


	private boolean									m_hasGhost			= true;

	public DnDTabbedPane(TriviaFrame parent, TriviaClient client) {
		super();
		this.parent = parent;
		this.client = client;
		this.tearTab = new TearAwayTab(client);
		this.blankPanel = new JPanel();
		registerTabbedPane(this);
		final DragSourceListener dsl = new DragSourceListener() {
			@Override
			public void dragDropEnd(DragSourceDropEvent e) {
				DnDTabbedPane.this.m_isDrawRect = false;
				DnDTabbedPane.this.m_lineRect.setRect(0, 0, 0, 0);

				if (DnDTabbedPane.this.hasGhost()) {
					s_glassPane.setVisible(false);
					s_glassPane.setImage(null);
				}
			}

			@Override
			public void dragEnter(DragSourceDragEvent e) {
				// System.out.println("dragEnter");
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dragExit(DragSourceEvent e) {
				DnDTabbedPane.this.tearTab.attach(e.getLocation());
				DnDTabbedPane.this.m_lineRect.setRect(0, 0, 0, 0);
				DnDTabbedPane.this.m_isDrawRect = false;
				s_glassPane.setPoint(new Point(-1000, -1000));
				s_glassPane.repaint();
			}

			@Override
			public void dragOver(DragSourceDragEvent e) {

				final TabTransferData data = DnDTabbedPane.this.getTabTransferData(e);

				if (data == null || e.getTargetActions() != DnDConstants.ACTION_MOVE) {
					DnDTabbedPane.this.tearTab.attach(e.getLocation());
				}

				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dropActionChanged(DragSourceDragEvent e) {
			}
		};

		final DragGestureListener dgl = new DragGestureListener() {
			@Override
			public void dragGestureRecognized(DragGestureEvent e) {
				final Point tabPt = e.getDragOrigin();
				final int dragTabIndex = DnDTabbedPane.this.indexAtLocation(tabPt.x, tabPt.y);
				if (dragTabIndex < 0) return;

				DnDTabbedPane.this.initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);
				try {
					e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(DnDTabbedPane.this, dragTabIndex), dsl);
				} catch (final InvalidDnDOperationException idoe) {
					idoe.printStackTrace();
				}
			}
		};

		new DropTarget(this, DnDConstants.ACTION_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, dgl);
		this.m_acceptor = new TabAcceptor() {
			@Override
			public boolean isDropAcceptable(DnDTabbedPane a_component, int a_index) {
				return true;
			}
		};

		/**
		 * Build close menu
		 */
		this.closeMenu = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Close Tab");
		menuItem.setActionCommand("Close Tab");
		menuItem.addActionListener(this);
		this.closeMenu.add(menuItem);

		menuItem = new JMenuItem("Close Other Tabs");
		menuItem.setActionCommand("Close Other Tabs");
		menuItem.addActionListener(this);
		this.closeMenu.add(menuItem);

		this.closeMenu.setVisible(false);

		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			final UIDefaults defaults = new UIDefaults();
			Painter<?> painter = (Painter<?>) UIManager.get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter");
			defaults.put("TabbedPane:TabbedPaneTab[Disabled].backgroundPainter", painter);
			this.putClientProperty("Nimbus.Overrides", defaults);
			this.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		}

		this.addMouseListener(new PopupListener(this.closeMenu));
		this.addChangeListener(this);
		this.addMouseListener(this);

		makeNewTabTab();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final int tabIndex = Integer.parseInt(this.closeMenu.getName());
		final String command = event.getActionCommand();
		switch (command) {
			case "Close Tab":
				this.removeTabAt(tabIndex);
				break;
			case "Close Other Tabs":
				final String thisTab = this.getTitleAt(tabIndex);
				for (final String tabName : this.getTabNames()) {
					if (!tabName.equals(thisTab)) {
						final int index = this.indexOfTab(tabName);
						this.removeTabAt(index);
					}
				}
				break;
			default:
				break;
		}
	}

	private void makeNewTabTab() {
		this.addTab("+", this.blankPanel);
		final int nTabs = this.getTabCount();
		this.setTabComponentAt(nTabs - 1, addTabLabel);
		this.setEnabledAt(nTabs - 1, false);
	}

	public void addTab(String tabName, TriviaMainPanel panel) {
		super.addTab(tabName, panel);
		if (panel instanceof ChangeListener) {
			this.addChangeListener((ChangeListener) panel);
		}
		for (final Component child : panel.getComponents()) {
			if (child instanceof ChangeListener) {
				this.addChangeListener((ChangeListener) child);
			}
		}
	}

	public TabAcceptor getAcceptor() {
		return this.m_acceptor;
	}

	public String[] getTabNames() {
		final int nTabs = this.getTabCount();
		final String[] names = new String[nTabs - 1];
		for (int t = 0; t < nTabs - 1; t++) {
			names[t] = this.getTitleAt(t);
		}
		return names;
	}

	public boolean hasGhost() {
		return this.m_hasGhost;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		final int addButtonIndex = this.indexOfComponent(this.blankPanel);
		if (addButtonIndex > -1 && this.getBoundsAt(addButtonIndex).contains(event.getPoint())) {
			new AddTabDialog(this.parent, this.client, this);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.m_isDrawRect) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(this.m_lineColor);
			g2.fill(this.m_lineRect);
		} // if
	}

	public void setAcceptor(TabAcceptor a_value) {
		this.m_acceptor = a_value;
	}

	public void setPaintGhost(boolean flag) {
		this.m_hasGhost = flag;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// Make sure the add button stays at the end
		final int nTabs = this.getTabCount();
		final int addButtonIndex = this.indexOfComponent(this.blankPanel);
		if (addButtonIndex > -1 && addButtonIndex != ( nTabs - 1 )) {
			this.remove(addButtonIndex);
			this.makeNewTabTab();
		}
	}

	void convertTab(TabTransferData a_data, int a_targetIndex) {

		final DnDTabbedPane source = a_data.getTabbedPane();
		final int sourceIndex = a_data.getTabIndex();
		if (sourceIndex < 0) return;
		// Save the tab's component, title, and TabComponent.
		final Component cmp = source.getComponentAt(sourceIndex);
		final String str = source.getTitleAt(sourceIndex);
		final Component tcmp = source.getTabComponentAt(sourceIndex);

		if (this != source) {
			source.remove(sourceIndex);

			if (a_targetIndex == this.getTabCount()) {
				this.addTab(str, cmp);
				this.setTabComponentAt(this.getTabCount() - 1, tcmp);
			} else {
				if (a_targetIndex < 0) {
					a_targetIndex = 0;
				} // if

				this.insertTab(str, null, cmp, null, a_targetIndex);
				this.setTabComponentAt(a_targetIndex, tcmp);
			} // if

			this.setSelectedComponent(cmp);
			return;
		} // if
		if (a_targetIndex < 0 || sourceIndex == a_targetIndex) return;
		if (a_targetIndex == this.getTabCount()) {
			source.remove(sourceIndex);
			this.addTab(str, cmp);
			this.setTabComponentAt(this.getTabCount() - 1, tcmp);
			this.setSelectedIndex(this.getTabCount() - 1);
		} else if (sourceIndex > a_targetIndex) {
			source.remove(sourceIndex);
			this.insertTab(str, null, cmp, null, a_targetIndex);
			this.setTabComponentAt(a_targetIndex, tcmp);
			this.setSelectedIndex(a_targetIndex);
		} else {
			source.remove(sourceIndex);
			this.insertTab(str, null, cmp, null, a_targetIndex - 1);
			this.setTabComponentAt(a_targetIndex - 1, tcmp);
			this.setSelectedIndex(a_targetIndex - 1);
		}

	}

	TabTransferData getTabTransferData(DropTargetDropEvent a_event) {
		try {
			final TabTransferData data = (TabTransferData) a_event.getTransferable().getTransferData(this.FLAVOR);
			return data;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * returns potential index for drop.
	 * 
	 * @param a_point
	 *            point given in the drop site component's coordinate
	 * @return returns potential index for drop.
	 */
	int getTargetTabIndex(Point a_point) {
		final boolean isTopOrBottom = this.getTabPlacement() == SwingConstants.TOP
				|| this.getTabPlacement() == SwingConstants.BOTTOM;

		// if the pane is empty, the target index is always zero.
		if (this.getTabCount() == 0) return 0;

		for (int i = 0; i < this.getTabCount(); i++) {
			final Rectangle r = this.getBoundsAt(i);
			if (isTopOrBottom) {
				r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
			} else {
				r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
			} // if-else

			if (r.contains(a_point)) return i;
		} // for

		final Rectangle r = this.getBoundsAt(this.getTabCount() - 1);
		if (isTopOrBottom) {
			final int x = r.x + r.width / 2;
			r.setRect(x, r.y, this.getWidth() - x, r.height);
		} else {
			final int y = r.y + r.height / 2;
			r.setRect(r.x, y, r.width, this.getHeight() - y);
		} // if-else

		return r.contains(a_point) ? this.getTabCount() : -1;
	}

	private Point buildGhostLocation(Point a_location) {
		Point retval = new Point(a_location);

		switch (this.getTabPlacement()) {
			case SwingConstants.TOP: {
				retval.y = 1;
				retval.x -= s_glassPane.getGhostWidth() / 2;
			}
				break;

			case SwingConstants.BOTTOM: {
				retval.y = this.getHeight() - 1 - s_glassPane.getGhostHeight();
				retval.x -= s_glassPane.getGhostWidth() / 2;
			}
				break;

			case SwingConstants.LEFT: {
				retval.x = 1;
				retval.y -= s_glassPane.getGhostHeight() / 2;
			}
				break;

			case SwingConstants.RIGHT: {
				retval.x = this.getWidth() - 1 - s_glassPane.getGhostWidth();
				retval.y -= s_glassPane.getGhostHeight() / 2;
			}
				break;
		} // switch

		retval = SwingUtilities.convertPoint(DnDTabbedPane.this, retval, s_glassPane);
		return retval;
	}

	private TabTransferData getTabTransferData(DragSourceDragEvent a_event) {
		try {
			return (TabTransferData) a_event.getDragSourceContext().getTransferable().getTransferData(this.FLAVOR);
		} catch (final Exception e) {
		}

		return null;
	}

	private TabTransferData getTabTransferData(DropTargetDragEvent a_event) {
		try {
			final TabTransferData data = (TabTransferData) a_event.getTransferable().getTransferData(this.FLAVOR);
			return data;
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private void initGlassPane(Component c, Point tabPt, int a_tabIndex) {
		this.getRootPane().setGlassPane(s_glassPane);
		if (this.hasGhost()) {
			final Rectangle rect = this.getBoundsAt(a_tabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics g = image.getGraphics();
			c.paint(g);
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			s_glassPane.setImage(image);
		} // if

		s_glassPane.setPoint(this.buildGhostLocation(tabPt));
		s_glassPane.setVisible(true);
	}

	private void initTargetLeftRightLine(int next, TabTransferData a_data) {
		if (next < 0) {
			this.m_lineRect.setRect(0, 0, 0, 0);
			this.m_isDrawRect = false;
			return;
		} // if

		if (( a_data.getTabbedPane() == this ) && ( a_data.getTabIndex() == next || next - a_data.getTabIndex() == 1 )) {
			this.m_lineRect.setRect(0, 0, 0, 0);
			this.m_isDrawRect = false;
		} else if (this.getTabCount() == 0) {
			this.m_lineRect.setRect(0, 0, 0, 0);
			this.m_isDrawRect = false;
			return;
		} else if (next == 0) {
			final Rectangle rect = this.getBoundsAt(0);
			this.m_lineRect.setRect(-LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			this.m_isDrawRect = true;
		} else if (next == this.getTabCount()) {
			final Rectangle rect = this.getBoundsAt(this.getTabCount() - 1);
			this.m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			this.m_isDrawRect = true;
		} else {
			final Rectangle rect = this.getBoundsAt(next - 1);
			this.m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			this.m_isDrawRect = true;
		}
	}

	private void initTargetTopBottomLine(int next, TabTransferData a_data) {
		if (next < 0) {
			this.m_lineRect.setRect(0, 0, 0, 0);
			this.m_isDrawRect = false;
			return;
		} // if

		if (( a_data.getTabbedPane() == this ) && ( a_data.getTabIndex() == next || next - a_data.getTabIndex() == 1 )) {
			this.m_lineRect.setRect(0, 0, 0, 0);
			this.m_isDrawRect = false;
		} else if (this.getTabCount() == 0) {
			this.m_lineRect.setRect(0, 0, 0, 0);
			this.m_isDrawRect = false;
			return;
		} else if (next == this.getTabCount()) {
			final Rectangle rect = this.getBoundsAt(this.getTabCount() - 1);
			this.m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
			this.m_isDrawRect = true;
		} else if (next == 0) {
			final Rectangle rect = this.getBoundsAt(0);
			this.m_lineRect.setRect(rect.x, -LINEWIDTH / 2, rect.width, LINEWIDTH);
			this.m_isDrawRect = true;
		} else {
			final Rectangle rect = this.getBoundsAt(next - 1);
			this.m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
			this.m_isDrawRect = true;
		}
	}

	public static ArrayList<ChangeListener> getTabbedPaneListeners() {
		return DnDTabbedPane.tabbedPaneListeners;
	}

	public static ArrayList<DnDTabbedPane> getTabbedPanes() {
		return DnDTabbedPane.tabbedPaneList;
	}

	public static void registerTabbedPane(DnDTabbedPane newPane) {
		DnDTabbedPane.tabbedPaneList.add(newPane);
		for (final ChangeListener listener : DnDTabbedPane.tabbedPaneListeners) {
			newPane.addChangeListener(listener);
		}
	}

	public static void registerTabbedPaneListener(ChangeListener listener) {
		DnDTabbedPane.tabbedPaneListeners.add(listener);
		for (final DnDTabbedPane pane : DnDTabbedPane.tabbedPaneList) {
			pane.addChangeListener(listener);
		}
	}

	public interface TabAcceptor {
		boolean isDropAcceptable(DnDTabbedPane a_component, int a_index);
	}

	class CDropTargetListener implements DropTargetListener {
		@Override
		public void dragEnter(DropTargetDragEvent e) {
			// System.out.println("DropTarget.dragEnter: " + DnDTabbedPane.this);

			if (this.isDragAcceptable(e)) {
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			} // if
		}

		@Override
		public void dragExit(DropTargetEvent e) {
			// System.out.println("DropTarget.dragExit: " + DnDTabbedPane.this);
			DnDTabbedPane.this.m_isDrawRect = false;
		}

		@Override
		public void dragOver(final DropTargetDragEvent e) {
			final TabTransferData data = DnDTabbedPane.this.getTabTransferData(e);

			if (DnDTabbedPane.this.getTabPlacement() == SwingConstants.TOP
					|| DnDTabbedPane.this.getTabPlacement() == SwingConstants.BOTTOM) {
				DnDTabbedPane.this.initTargetLeftRightLine(DnDTabbedPane.this.getTargetTabIndex(e.getLocation()), data);
			} else {
				DnDTabbedPane.this.initTargetTopBottomLine(DnDTabbedPane.this.getTargetTabIndex(e.getLocation()), data);
			} // if-else

			DnDTabbedPane.this.repaint();
			if (DnDTabbedPane.this.hasGhost()) {
				s_glassPane.setPoint(DnDTabbedPane.this.buildGhostLocation(e.getLocation()));
				s_glassPane.repaint();
			}
		}

		@Override
		public void drop(DropTargetDropEvent a_event) {
			// System.out.println("DropTarget.drop: " + DnDTabbedPane.this);

			if (this.isDropAcceptable(a_event)) {
				DnDTabbedPane.this.convertTab(DnDTabbedPane.this.getTabTransferData(a_event),
						DnDTabbedPane.this.getTargetTabIndex(a_event.getLocation()));
				a_event.dropComplete(true);
			} else {
				a_event.dropComplete(false);
			} // if-else

			DnDTabbedPane.this.m_isDrawRect = false;
			DnDTabbedPane.this.repaint();
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent e) {
		}

		public boolean isDragAcceptable(DropTargetDragEvent e) {
			// System.out.println("DropTarget.isDragAcceptable: " + DnDTabbedPane.this);

			final Transferable t = e.getTransferable();
			if (t == null) return false;

			final DataFlavor[] flavor = e.getCurrentDataFlavors();
			if (!t.isDataFlavorSupported(flavor[0])) return false;

			final TabTransferData data = DnDTabbedPane.this.getTabTransferData(e);

			if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) return true;

			if (DnDTabbedPane.this != data.getTabbedPane()) {
				if (DnDTabbedPane.this.m_acceptor != null)
					return DnDTabbedPane.this.m_acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
			} // if

			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e) {
			// System.out.println("DropTarget.isDropAcceptable: " + DnDTabbedPane.this);

			final Transferable t = e.getTransferable();
			if (t == null) return false;

			final DataFlavor[] flavor = e.getCurrentDataFlavors();
			if (!t.isDataFlavorSupported(flavor[0])) return false;

			final TabTransferData data = DnDTabbedPane.this.getTabTransferData(e);

			if (DnDTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0) return true;

			if (DnDTabbedPane.this != data.getTabbedPane()) {
				if (DnDTabbedPane.this.m_acceptor != null)
					return DnDTabbedPane.this.m_acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());
			} // if
			return false;
		}
	}

	class TabTransferable implements Transferable {
		private TabTransferData	m_data	= null;

		public TabTransferable(DnDTabbedPane a_tabbedPane, int a_tabIndex) {
			this.m_data = new TabTransferData(DnDTabbedPane.this, a_tabIndex);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) {
			return this.m_data;
			// return DnDTabbedPane.this;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			final DataFlavor[] f = new DataFlavor[1];
			f[0] = DnDTabbedPane.this.FLAVOR;
			return f;
		}

		@Override
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
			this.m_tabbedPane = a_tabbedPane;
			this.m_tabIndex = a_tabIndex;
		}

		public DnDTabbedPane getTabbedPane() {
			return this.m_tabbedPane;
		}

		public int getTabIndex() {
			return this.m_tabIndex;
		}

		public void setTabbedPane(DnDTabbedPane pane) {
			this.m_tabbedPane = pane;
		}

		public void setTabIndex(int index) {
			this.m_tabIndex = index;
		}
	}

	private class PopupListener extends MouseAdapter {

		private final JPopupMenu	menu;

		public PopupListener(JPopupMenu menu) {
			this.menu = menu;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.checkForPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			this.checkForPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.checkForPopup(e);
		}

		private void checkForPopup(MouseEvent event) {
			int clickedIndex = -1;
			for (int i = 0; i < DnDTabbedPane.this.getTabCount(); i++) {
				if (DnDTabbedPane.this.getBoundsAt(i).contains(event.getPoint())) {
					clickedIndex = i;
					break;
				}
			}
			if (event.isPopupTrigger() && clickedIndex > -1) {
				this.menu.setName(clickedIndex + "");
				this.menu.show((Component) event.getSource(), event.getX(), event.getY());
			}
		}

	}

}

class GhostGlassPane extends JPanel {
	public static final long		serialVersionUID	= 1L;
	private final AlphaComposite	m_composite;

	private final Point				m_location			= new Point(0, 0);

	private BufferedImage			m_draggingGhost		= null;

	public GhostGlassPane() {
		this.setOpaque(false);
		this.m_composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	}

	public int getGhostHeight() {
		if (this.m_draggingGhost == null) return 0;

		return this.m_draggingGhost.getHeight(this);
	}

	public int getGhostWidth() {
		if (this.m_draggingGhost == null) return 0;

		return this.m_draggingGhost.getWidth(this);
	}

	@Override
	public void paintComponent(Graphics g) {
		if (this.m_draggingGhost == null) return;

		final Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(this.m_composite);

		g2.drawImage(this.m_draggingGhost, (int) this.m_location.getX(), (int) this.m_location.getY(), null);
	}

	public void setImage(BufferedImage draggingGhost) {
		this.m_draggingGhost = draggingGhost;
	}

	public void setPoint(Point a_location) {
		this.m_location.x = a_location.x;
		this.m_location.y = a_location.y;
	}

}
