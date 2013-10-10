package net.bubbaland.trivia.client;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.JPanel;

/**
 * Creates a browser inside a Swing Panel using JavaFX.
 * 
 * @author Walter Kolczynski
 */
public class BrowserPanel extends JPanel {

	/**
	 * A Web Browser
	 */
	private static class Browser extends Region {

		/** The browser. */
		final private WebView	browser		= new WebView();

		/** The web engine. */
		final private WebEngine	webEngine	= this.browser.getEngine();

		/**
		 * Instantiates a new browser
		 * 
		 * @param url
		 *            the URL to load
		 */
		public Browser(String url) {
			this.getStyleClass().add("browser");
			this.webEngine.load(url);
			this.getChildren().add(this.browser);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javafx.scene.Parent#layoutChildren()
		 */
		@Override
		protected void layoutChildren() {
			// make the browser fill the entire area
			final double w = this.getWidth();
			final double h = this.getHeight();
			this.layoutInArea(this.browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
		}

	}

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7479243065048312762L;

	/**
	 * Initializes the JavaFX panel and load the given URL.
	 * 
	 * @param fxPanel
	 *            The JavaFX Panel that will hold the browser
	 * @param url
	 *            The URL to load
	 */
	public static void initFX(JFXPanel fxPanel, String url) {
		final Browser pane = new Browser(url);
		final Scene scene = new Scene(pane);
		fxPanel.setScene(scene);
	}

	/**
	 * Creates a new panel with a browser inside
	 * 
	 * @param url
	 *            The URL to load
	 */
	public BrowserPanel(final String url) {

		super(new GridBagLayout());

		// Set up layout constraints
		final GridBagConstraints solo = new GridBagConstraints();
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0;
		solo.weighty = 1.0;
		solo.gridx = 0;
		solo.gridy = 0;

		// Create a JavaFX panel that fills the entire Swing panel
		final JFXPanel fxPanel = new JFXPanel();
		this.add(fxPanel, solo);

		// Enter key was appearing as line feed (ASCII 10) instead of carriage return (ASCII 13), so intercept and
		// repost as correct key
		fxPanel.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == 10) {
					e.setKeyChar((char) 13);
					Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
				}
			}
		});

		// This is from a test of higher-numbered mouse buttons not working
		fxPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// try {
				// System.out.println("Button "+e.getButton());
				// } catch (NullPointerException e2) {
				// e2.printStackTrace();
				// }
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
		});

		// Initialize the JavaFX Panel
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel, url);
			}
		});

	}


}
