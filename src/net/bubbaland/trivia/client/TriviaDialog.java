package net.bubbaland.trivia.client;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class TriviaDialog extends JDialog implements WindowListener, PropertyChangeListener {
	
	private static final long	serialVersionUID	= 5954954270512670220L;
	
	private static final String fileName = ".trivia-settings";
	
	private final JOptionPane optionPane;
	
	public TriviaDialog(Frame frame, String title) {
		this(frame, title, new JOptionPane());
	}

	public TriviaDialog(Frame frame, String title, Object message, int messageType, int optionType, Icon icon, Object[] options,
			Object initialValue) {
		optionPane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);		
	}

	public TriviaDialog(Frame frame, String title, Object message, int messageType, int optionType, Icon icon, Object[] options) {
		this(frame, title, new JOptionPane(message, messageType, optionType, icon, options));
	}

	public TriviaDialog(Frame frame, String title, Object message, int messageType, int optionType, Icon icon) {
		this(frame, title, new JOptionPane(message, messageType, optionType, icon));
		
	}

	public TriviaDialog(Frame frame, String title, Object message, int messageType, int optionType) {
		this(frame, title, new JOptionPane(message, messageType, optionType));
		
	}

	public TriviaDialog(Frame frame, String title, Object message, int messageType) {
		this(frame, title, new JOptionPane(message, messageType));		
	}

	public TriviaDialog(Frame frame, String title, Object message) {
		this(frame, title, new JOptionPane(message));
	}
	
	public TriviaDialog(Frame frame, String title, final JOptionPane optionPane) {
		super(frame, title, true);
		this.optionPane = optionPane;
		this.addWindowListener(this);

		 // Register an event handler that reacts to option pane state changes.
	    this.optionPane.addPropertyChangeListener(this);
	    
		this.setContentPane(this.optionPane);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setResizable(true);
		loadPosition();
		this.setVisible(true);
	}
	
	@Override
	public void dispose() {
		savePosition();
		super.dispose();
	}

	@Override
	public void windowOpened(WindowEvent e) {	}

	@Override
	public void windowClosing(WindowEvent e) {
		this.optionPane.setValue(JOptionPane.CLOSED_OPTION);
	}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}
	
	public Object getValue() {
		return this.optionPane.getValue();
	}
	
	public void savePosition() {
		File file = new File(fileName);
		Properties props = new Properties();
		BufferedReader infileBuffer;
		try {
			infileBuffer = new BufferedReader(new FileReader(file));
			props.load(infileBuffer);
		} catch (IOException e) {}
			
		Rectangle r = this.getBounds();
		int x = (int)r.getX();
		int y = (int)r.getY();
		int width = (int)r.getWidth();
		int height = (int)r.getHeight();
		
		String dialogID = this.getTitle().replaceAll(" " , "_");
		System.out.println(dialogID);
		
		props.setProperty(dialogID + "_x", x + "");
		props.setProperty(dialogID+ "_y", y + "");
		props.setProperty(dialogID + "_width", width + "");
		props.setProperty(dialogID + "_height", height + "");
				
		try {
			BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
			props.store(outfileBuffer, "Trivia");
			outfileBuffer.close();
		} catch (IOException e) {
			System.out.println("Error saving window position.");
		}
	}
	
	public void loadPosition() {
		File file = new File(fileName);
		Properties props = new Properties();
		try {
			BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			props.load(fileBuffer);
			
			String dialogID = this.getTitle().replaceAll(" " , "_");
			System.out.println(dialogID);
			
			int x = Integer.parseInt(props.getProperty(dialogID + "_x"));
			int y = Integer.parseInt(props.getProperty(dialogID + "_y"));
			int width = Integer.parseInt(props.getProperty(dialogID + "_width"));
			int height = Integer.parseInt(props.getProperty(dialogID + "_height"));
			
			this.setBounds(x, y, width, height);
			
		} catch (IOException | NumberFormatException e ) {
			System.out.println("Couldn't load window position, may not exist yet.");
			this.pack();
			this.setLocationRelativeTo(null);
		}
	}
	
	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
	    String prop = e.getPropertyName();	    
	    if (isVisible()
	        && (e.getSource() == optionPane)
	        && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY
	            .equals(prop))) {
	    	this.dispose();
	    }
	}

}
