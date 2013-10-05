package net.bubbaland.trivia;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

// TODO: Auto-generated Javadoc
/**
 * The Class BrowserPanel.
 */
public class BrowserPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7479243065048312762L;

	/**
	 * Instantiates a new browser panel.
	 *
	 * @param url the url
	 */
	public BrowserPanel (final String url) {
		
		super(new GridBagLayout());
				
		GridBagConstraints solo = new GridBagConstraints();		
		solo.fill = GridBagConstraints.BOTH;
		solo.anchor = GridBagConstraints.CENTER;
		solo.weightx = 1.0; solo.weighty = 1.0;
		solo.gridx = 0; solo.gridy = 0;
		
		final JFXPanel fxPanel = new JFXPanel();
		this.add( fxPanel, solo );
		
		fxPanel.addKeyListener(new KeyListener() {

		    public void keyTyped(KeyEvent e) {
		        if (e.getKeyChar() == 10) {
		            e.setKeyChar((char) 13);
		            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
		        }
		    }

		    public void keyPressed(KeyEvent e) {}

		    public void keyReleased(KeyEvent e) {}
		});
		
		
		fxPanel.addMouseListener(new MouseListener() {

		    @Override
			public void mouseClicked(MouseEvent e) {
		    	try {
//		    		System.out.println("Button "+e.getButton());
		    	} catch (NullPointerException e2) {
		    		
		    	}
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub				
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel, url);
            }
        });
		
	}
	
	/**
	 * Inits the fx.
	 *
	 * @param fxPanel the fx panel
	 * @param url the url
	 */
	public static void initFX( JFXPanel fxPanel, String url) {
		Browser pane = new Browser(url);
		Scene scene = new Scene(pane);
		fxPanel.setScene(scene);		
	}
		
	/**
	 * The Class Browser.
	 */
	private static class Browser extends Region {
		
		/** The browser. */
		final private WebView browser = new WebView(  );
    	
	    /** The web engine. */
	    final private WebEngine webEngine = browser.getEngine();
		
        /**
         * Instantiates a new browser.
         *
         * @param url the url
         */
        public Browser(String url) {
        	
        	getStyleClass().add("browser");        	
        	webEngine.load( url );        	
        	getChildren().add(browser);        	
        	
        }
        
        /* (non-Javadoc)
         * @see javafx.scene.Parent#layoutChildren()
         */
        @Override protected void layoutChildren() {
            double w = getWidth();
            double h = getHeight();
            layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
        }
    		
	}


}
