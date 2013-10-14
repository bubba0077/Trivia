package net.bubbaland.trivia.client;


import java.awt.LayoutManager;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * Super-class for most of the dialog boxes in the trivia GUI.
 * 
 * Implements an AncestorListener that will focus on the element when created. 
 * 
 */
public class TriviaDialogPanel extends JPanel implements AncestorListener, WindowListener {
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= -4127179718225373888L;
	
	
	public TriviaDialogPanel() {
		super();
	}

	public TriviaDialogPanel(LayoutManager layout) {
		super(layout);
	}

	public TriviaDialogPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public TriviaDialogPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	@Override
	public void ancestorAdded(final AncestorEvent event) {
		// Change the focus to the text area when created
		final AncestorListener al= this;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {			
		}
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run()
            {
                JComponent component = (JComponent)event.getComponent();
                component.requestFocusInWindow();
                component.removeAncestorListener( al );
            }
        });
        
	}

	@Override
	public void ancestorMoved(AncestorEvent event) {
	}

	@Override
	public void ancestorRemoved(AncestorEvent event) {

	}	

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
				
	}

	@Override
	public void windowClosed(WindowEvent e) {
			
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

}
