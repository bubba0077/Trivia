package net.bubbaland.trivia.client;

// imports for GUI
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.bubbaland.trivia.Answer;
import net.bubbaland.trivia.Answer.Agreement;
import net.bubbaland.trivia.ClientMessage.ClientMessageFactory;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.client.TriviaGUI.QueueSort;

/**
 * A panel that shows the submitted answers for the current round.
 *
 * @author Walter Kolczynski
 *
 */
public class AnswerQueuePanel extends TriviaMainPanel implements MouseListener, ActionListener {

	/** The Constant serialVersionUID. */
	private static final long				serialVersionUID	= 784049314825719490L;

	/** Valid statuses for queue items */
	private static final String[]			STATUSES			= { "Duplicate", "Not Called In", "Calling",
			"Incorrect", "Partial", "Correct" };

	/** Sort icons */
	private static final ImageIcon			upArrow				= new ImageIcon(
			AnswerQueuePanel.class.getResource("images/upArrow.png"));
	private static final ImageIcon			downArrow			= new ImageIcon(
			AnswerQueuePanel.class.getResource("images/downArrow.png"));

	private static int						nBlinks;
	private static int						blinkSpeed;
	final private JTextField				qFilterTextField;

	// Set up layout constraints
	private static final GridBagConstraints	buttonConstraints	= new GridBagConstraints();

	static {
		buttonConstraints.fill = GridBagConstraints.BOTH;
		buttonConstraints.anchor = GridBagConstraints.CENTER;
		buttonConstraints.weightx = 1.0;
		buttonConstraints.weighty = 1.0;
		buttonConstraints.gridx = 0;
		buttonConstraints.gridy = 0;
		buttonConstraints.fill = GridBagConstraints.NONE;
	}

	private static Color				oddRowBackgroundColor, evenRowBackgroundColor, duplicateColor, notCalledInColor,
			callingColor, incorrectColor, partialColor, correctColor, agreeColor, disagreeColor;
	private static int					rowHeight, timeWidth, qNumWidth, answerWidth, confidenceWidth, agreementWidth,
			subCallerWidth, operatorWidth, statusWidth;
	private static float				fontSize, qNumFontSize;

	/**
	 * GUI elements that will be updated
	 */
	final private JLabel				timestampLabel, qNumberLabel, answerLabel, confidenceLabel, subCallerLabel,
			operatorLabel, statusLabel, queueSizeLabel;
	final private JPanel				spacer;
	final private JScrollPane			scrollPane;
	private final JPopupMenu			contextMenu;
	private int							rNumber, blinkCount;
	private boolean						live, blink;
	final private Timer					blinkTimer;

	/** The workflow queue sub panel */
	final private AnswerQueueSubPanel	answerQueueSubPanel;

	private Color						headerColor;
	private Color						headerBackgroundColor;

	public AnswerQueuePanel(TriviaClient client, TriviaFrame frame, int rNumber) {
		this(client, frame, rNumber, false);
	}

	public AnswerQueuePanel(TriviaClient client, TriviaFrame frame) {
		this(client, frame, client.getTrivia().getCurrentRoundNumber(), true);
	}

	/**
	 * Instantiates a new workflow queue panel.
	 *
	 * @param client
	 *            The local trivia client
	 * @param frame
	 *            The parent top-level frame
	 */
	public AnswerQueuePanel(TriviaClient client, TriviaFrame frame, int rNumber, boolean isLive) {
		super(client, frame);
		this.live = isLive;
		this.rNumber = rNumber;
		this.blinkTimer = new Timer(blinkSpeed, this);
		this.blinkTimer.setActionCommand("Blink");
		this.blink = false;
		this.blinkCount = 0;

		/**
		 * Build context menu
		 */
		this.contextMenu = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Filter by Q#");
		menuItem.setActionCommand("FilterQ#");
		menuItem.addActionListener(this);
		this.contextMenu.add(menuItem);

		JMenu subMenu = new JMenu("Filter by Text");
		this.qFilterTextField = new JTextField("");
		this.qFilterTextField.setPreferredSize(new Dimension(200, 25));
		this.qFilterTextField.setActionCommand("Set Filter Text");
		this.qFilterTextField.addActionListener(this);
		this.contextMenu.add(this.qFilterTextField);
		subMenu.add(this.qFilterTextField);
		this.contextMenu.add(subMenu);

		menuItem = new JMenuItem("Clear Q# Filters");
		menuItem.setActionCommand("Clear Q# Filters");
		menuItem.addActionListener(this);
		this.contextMenu.add(menuItem);

		menuItem = new JMenuItem("Clear Text Filters");
		menuItem.setActionCommand("Clear Text Filters");
		menuItem.addActionListener(this);
		this.contextMenu.add(menuItem);

		menuItem = new JMenuItem("Clear All Filters");
		menuItem.setActionCommand("Clear All Filters");
		menuItem.addActionListener(this);
		this.contextMenu.add(menuItem);

		this.add(this.contextMenu);


		// Set up layout constraints
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		/**
		 * Create the header row
		 */
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.timestampLabel = this.enclosedLabel("Time", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		this.timestampLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		this.timestampLabel.addMouseListener(this);

		constraints.gridx = 1;
		constraints.gridy = 0;
		this.qNumberLabel = this.enclosedLabel("Q#", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		this.qNumberLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		this.qNumberLabel.addMouseListener(this);
		this.qNumberLabel.addMouseListener(new PopupListener(this.contextMenu));

		constraints.gridx = 2;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		this.answerLabel = this.enclosedLabel("Proposed Answer", constraints, SwingConstants.LEFT,
				SwingConstants.CENTER);
		this.answerLabel.addMouseListener(new PopupListener(this.contextMenu));
		constraints.weightx = 0.0;

		constraints.gridx = 3;
		constraints.gridy = 0;
		this.confidenceLabel = this.enclosedLabel("Conf", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 4;
		constraints.gridy = 0;
		this.subCallerLabel = this.enclosedLabel("Sub/Caller", constraints, SwingConstants.CENTER,
				SwingConstants.CENTER);

		constraints.gridx = 5;
		constraints.gridy = 0;
		this.operatorLabel = this.enclosedLabel("Operator", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		constraints.gridx = 6;
		constraints.gridy = 0;
		this.statusLabel = this.enclosedLabel("Status", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		this.statusLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		this.statusLabel.addMouseListener(this);

		/**
		 * Create the sub-panel that will show the queue data and put it in a scroll pane
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 8;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		final JPanel scrollPanel = new JPanel(new GridBagLayout());
		this.scrollPane = new JScrollPane(scrollPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(5);
		this.add(this.scrollPane, constraints);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		this.answerQueueSubPanel = new AnswerQueueSubPanel();
		scrollPanel.add(this.answerQueueSubPanel, constraints);
		constraints.weightx = 0.0;
		constraints.weighty = 0.0;

		constraints.gridx = 7;
		constraints.gridy = 0;
		this.queueSizeLabel = this.enclosedLabel("0", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

		/**
		 * Create a blank spacer row at the bottom
		 */
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		this.spacer = new JPanel();
		this.spacer.setPreferredSize(new Dimension(0, 0));
		scrollPanel.add(this.spacer, constraints);

		this.loadProperties(TriviaGUI.PROPERTIES);

		this.updateGUI(true);
	}

	public void setRoundNumber(int newRoundNumber) {
		this.rNumber = newRoundNumber;
	}

	private class PopupListener extends MouseAdapter {

		private final JPopupMenu menu;

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
			final JComponent source = (JComponent) event.getSource();
			if (event.isPopupTrigger()) {
				this.menu.show(source, event.getX(), event.getY());
			}
		}

	}

	private void blink() {
		this.blinkTimer.start();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public synchronized void actionPerformed(ActionEvent event) {
		final String command = event.getActionCommand();
		switch (command) {
			case "FilterQ#":
				this.gui.showNumberFilterDialog();
				break;
			case "Set Filter Text":
				this.gui.setTextFilter(this.qFilterTextField.getText());
				break;
			case "Clear Q# Filters":
				this.gui.resetNumberFilter();
				break;
			case "Clear Text Filters":
				this.gui.resetTextFilter();
				break;
			case "Clear All Filters":
				this.gui.resetNumberFilter();
				this.gui.resetTextFilter();
				break;
			case "Blink":
				this.blinkCount++;
				Color headerColor, headerBackgroundColor;
				if (this.blink) {
					headerColor = this.headerColor;
					headerBackgroundColor = this.headerBackgroundColor;
					this.blink = false;
				} else {
					headerColor = this.headerBackgroundColor;
					headerBackgroundColor = this.headerColor;
					this.blink = true;
				}
				if (this.blinkCount % ( nBlinks * 2 ) == 0) {
					this.blinkTimer.stop();
				}
				this.timestampLabel.getParent().setBackground(headerBackgroundColor);
				this.timestampLabel.setForeground(headerColor);
				this.qNumberLabel.getParent().setBackground(headerBackgroundColor);
				this.qNumberLabel.setForeground(headerColor);
				this.answerLabel.getParent().setBackground(headerBackgroundColor);
				this.answerLabel.setForeground(headerColor);
				this.confidenceLabel.getParent().setBackground(headerBackgroundColor);
				this.confidenceLabel.setForeground(headerColor);
				this.subCallerLabel.getParent().setBackground(headerBackgroundColor);
				this.subCallerLabel.setForeground(headerColor);
				this.operatorLabel.getParent().setBackground(headerBackgroundColor);
				this.operatorLabel.setForeground(headerColor);
				this.statusLabel.getParent().setBackground(headerBackgroundColor);
				this.statusLabel.setForeground(headerColor);
				this.queueSizeLabel.getParent().setBackground(headerBackgroundColor);
				this.queueSizeLabel.setForeground(headerColor);
			default:
				break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		final JComponent source = (JComponent) event.getSource();
		final QueueSort sortMethod = this.frame.getQueueSort();
		if (source.equals(this.timestampLabel)) {
			if (sortMethod.equals(QueueSort.TIMESTAMP_ASCENDING)) {
				this.frame.setSort(QueueSort.TIMESTAMP_DESCENDING);
			} else {
				this.frame.setSort(QueueSort.TIMESTAMP_ASCENDING);
			}
		} else if (source.equals(this.qNumberLabel)) {
			if (sortMethod.equals(QueueSort.QNUMBER_ASCENDING)) {
				this.frame.setSort(QueueSort.QNUMBER_DESCENDING);
			} else {
				this.frame.setSort(QueueSort.QNUMBER_ASCENDING);
			}
		} else if (source.equals(this.statusLabel)) {
			if (sortMethod.equals(QueueSort.STATUS_ASCENDING)) {
				this.frame.setSort(QueueSort.STATUS_DESCENDING);
			} else {
				this.frame.setSort(QueueSort.STATUS_ASCENDING);
			}
		}
		this.updateGUI();
	}

	@Override
	public void mouseEntered(MouseEvent event) {
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mousePressed(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent event) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		// Update the queue size
		if (this.live) {
			this.setRoundNumber(this.client.getTrivia().getCurrentRoundNumber());
			// this.answerQueueSubPanel.resetAgreement();
		}

		if (this.gui.getFilterNumbers().size() == 0) {
			this.qNumberLabel.setText("Q#");
		} else {
			this.qNumberLabel.setText("Q#*");
		}

		this.qFilterTextField.setText(this.gui.getFilterTextPattern().toString());

		final int queueSize = this.client.getTrivia().getAnswerQueueSize(this.rNumber);
		this.queueSizeLabel.setText(queueSize + "");
		if (this.gui.getFilterTextPattern().pattern().equals("")) {
			this.answerLabel.setText("Proposed Answer");
		} else {
			this.answerLabel.setText("Proposed Answer (filtered by: \"" + this.gui.getFilterTextPattern() + "\")");
		}
		this.answerQueueSubPanel.updateGUI(force);
		final QueueSort sortMethod = this.frame.getQueueSort();

		switch (sortMethod) {
			case TIMESTAMP_ASCENDING:
				this.timestampLabel.setIcon(downArrow);
				this.qNumberLabel.setIcon(null);
				this.statusLabel.setIcon(null);
				break;
			case QNUMBER_ASCENDING:
				this.timestampLabel.setIcon(null);
				this.qNumberLabel.setIcon(downArrow);
				this.statusLabel.setIcon(null);
				break;
			case STATUS_ASCENDING:
				this.timestampLabel.setIcon(null);
				this.qNumberLabel.setIcon(null);
				this.statusLabel.setIcon(downArrow);
				break;
			case TIMESTAMP_DESCENDING:
				this.timestampLabel.setIcon(upArrow);
				this.qNumberLabel.setIcon(null);
				this.statusLabel.setIcon(null);
				break;
			case QNUMBER_DESCENDING:
				this.timestampLabel.setIcon(null);
				this.qNumberLabel.setIcon(upArrow);
				this.statusLabel.setIcon(null);
				break;
			case STATUS_DESCENDING:
				this.timestampLabel.setIcon(null);
				this.qNumberLabel.setIcon(null);
				this.statusLabel.setIcon(upArrow);
				break;
		}
	}

	/**
	 * Load properties from the client and apply.
	 */
	@Override
	protected void loadProperties(Properties properties) {
		/**
		 * Colors
		 */
		this.headerColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Header.Color"), 16).intValue());
		this.headerBackgroundColor = new Color(
				new BigInteger(properties.getProperty("AnswerQueue.Header.BackgroundColor"), 16).intValue());
		oddRowBackgroundColor = new Color(
				new BigInteger(properties.getProperty("AnswerQueue.OddRow.BackgroundColor"), 16).intValue());
		evenRowBackgroundColor = new Color(
				new BigInteger(properties.getProperty("AnswerQueue.EvenRow.BackgroundColor"), 16).intValue());
		duplicateColor = new Color(
				new BigInteger(properties.getProperty("AnswerQueue.Duplicate.Color"), 16).intValue());
		notCalledInColor = new Color(
				new BigInteger(properties.getProperty("AnswerQueue.NotCalledIn.Color"), 16).intValue());
		callingColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Calling.Color"), 16).intValue());
		incorrectColor = new Color(
				new BigInteger(properties.getProperty("AnswerQueue.Incorrect.Color"), 16).intValue());
		partialColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Partial.Color"), 16).intValue());
		correctColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Correct.Color"), 16).intValue());
		agreeColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Agree.Color"), 16).intValue());
		disagreeColor = new Color(new BigInteger(properties.getProperty("AnswerQueue.Disagree.Color"), 16).intValue());

		/**
		 * Sizes
		 */
		final int headerHeight = Integer.parseInt(properties.getProperty("AnswerQueue.Header.Height"));
		rowHeight = Integer.parseInt(properties.getProperty("AnswerQueue.Row.Height"));

		timeWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Timestamp.Width"));
		qNumWidth = Integer.parseInt(properties.getProperty("AnswerQueue.QNumber.Width"));
		answerWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Answer.Width"));
		confidenceWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Confidence.Width"));
		agreementWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Agreement.Width"));
		subCallerWidth = Integer.parseInt(properties.getProperty("AnswerQueue.SubCaller.Width"));
		operatorWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Operator.Width"));
		statusWidth = Integer.parseInt(properties.getProperty("AnswerQueue.Status.Width"));

		/**
		 * Font sizes
		 */
		final float headerFontSize = Float.parseFloat(properties.getProperty("AnswerQueue.Header.FontSize"));
		qNumFontSize = Float.parseFloat(properties.getProperty("AnswerQueue.QNumber.FontSize"));
		fontSize = Float.parseFloat(properties.getProperty("AnswerQueue.FontSize"));

		/**
		 *
		 */
		AnswerQueuePanel.nBlinks = Integer.parseInt(properties.getProperty("AnswerQueue.Blink.N"));
		AnswerQueuePanel.blinkSpeed = Integer.parseInt(properties.getProperty("AnswerQueue.Blink.Speed"));
		this.blinkTimer.setInitialDelay(blinkSpeed);
		this.blinkTimer.setDelay(blinkSpeed);

		/** The number of open questions to show at one time */
		final int answersShow = Integer.parseInt(properties.getProperty("AnswerQueue.AnswersShow"));

		setLabelProperties(this.timestampLabel, timeWidth, headerHeight, this.headerColor, this.headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.qNumberLabel, qNumWidth, headerHeight, this.headerColor, this.headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.answerLabel, answerWidth, headerHeight, this.headerColor, this.headerBackgroundColor,
				headerFontSize);
		setLabelProperties(this.confidenceLabel, confidenceWidth + agreementWidth, headerHeight, this.headerColor,
				this.headerBackgroundColor, headerFontSize);
		setLabelProperties(this.subCallerLabel, subCallerWidth, headerHeight, this.headerColor,
				this.headerBackgroundColor, headerFontSize);
		setLabelProperties(this.operatorLabel, operatorWidth, headerHeight, this.headerColor,
				this.headerBackgroundColor, headerFontSize);
		setLabelProperties(this.statusLabel, statusWidth, headerHeight, this.headerColor, this.headerBackgroundColor,
				headerFontSize);

		final int scrollBarWidth;
		if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
			scrollBarWidth = (int) UIManager.get("ScrollBar.thumbHeight");
		} else {
			scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
		}
		setLabelProperties(this.queueSizeLabel, scrollBarWidth, headerHeight, this.headerColor,
				this.headerBackgroundColor, headerFontSize);

		this.scrollPane.setPreferredSize(new Dimension(0, answersShow * rowHeight + 3));
		this.scrollPane.setMinimumSize(new Dimension(0, rowHeight + 3));

		this.spacer.setBackground(this.headerBackgroundColor);

		this.answerQueueSubPanel.loadProperties(properties);
	}

	/**
	 * A panel that will show the current answer queue
	 */
	private class AnswerQueueSubPanel extends TriviaMainPanel
			implements ItemListener, ActionListener, PopupMenuListener {

		/** The Constant serialVersionUID */
		private static final long					serialVersionUID	= -5462544756397828556L;

		/** The last status (used for determining if the status has changed) */
		private volatile ArrayList<String>			lastStatus;

		/**
		 * GUI elements that will be updated
		 */
		final private ArrayList<JLabel>				queuenumberLabels, timestampLabels, qNumberLabels, confidenceLabels,
				agreementLabels, submitterLabels, callerLabels;
		final private ArrayList<JComboBox<String>>	statusComboBoxes;
		final private ArrayList<JTextArea>			answerTextAreas;
		final private ArrayList<JTextPane>			operatorTextPanes;
		final private ArrayList<JToggleButton>		agreeButtons, disagreeButtons;
		final private ArrayList<ButtonGroup>		buttonGroups;
		private final JPopupMenu					contextMenu;
		private String								activeComboBox;

		/**
		 * Data
		 */
		private Answer[]							answerQueue;

		/**
		 * Instantiates a new workflow queue sub panel.
		 *
		 * @param server
		 *            the server
		 * @param client
		 *            the client
		 */
		public AnswerQueueSubPanel() {
			super(AnswerQueuePanel.this.client, AnswerQueuePanel.this.frame);

			this.answerQueue = new Answer[0];

			/**
			 * Build context menu
			 */
			this.contextMenu = new JPopupMenu();

			final JMenuItem viewItem = new JMenuItem("View");
			viewItem.setActionCommand("View");
			viewItem.addActionListener(this);
			this.contextMenu.add(viewItem);
			this.add(this.contextMenu);

			// Set up layout constraints
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			this.queuenumberLabels = new ArrayList<JLabel>(0);
			this.timestampLabels = new ArrayList<JLabel>(0);
			this.qNumberLabels = new ArrayList<JLabel>(0);
			this.confidenceLabels = new ArrayList<JLabel>(0);
			this.agreementLabels = new ArrayList<JLabel>(0);
			this.agreeButtons = new ArrayList<JToggleButton>(0);
			this.disagreeButtons = new ArrayList<JToggleButton>(0);
			this.buttonGroups = new ArrayList<ButtonGroup>(0);
			this.submitterLabels = new ArrayList<JLabel>(0);
			this.operatorTextPanes = new ArrayList<JTextPane>(0);
			this.callerLabels = new ArrayList<JLabel>(0);
			this.statusComboBoxes = new ArrayList<JComboBox<String>>(0);
			this.answerTextAreas = new ArrayList<JTextArea>(0);
			this.lastStatus = new ArrayList<String>(0);

			this.activeComboBox = null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public synchronized void actionPerformed(ActionEvent event) {
			final Trivia trivia = this.client.getTrivia();
			if (AnswerQueuePanel.this.live) {
				AnswerQueuePanel.this.rNumber = trivia.getCurrentRoundNumber();
			}
			final String command = event.getActionCommand();
			final int queueIndex;
			switch (command) {
				case "View":
					queueIndex = Integer.parseInt(this.contextMenu.getName());
					final int qNumber = trivia.getAnswerQueueQNumber(queueIndex);
					final int qValue = trivia.getValue(AnswerQueuePanel.this.rNumber, qNumber);
					final String qText = trivia.getQuestionText(AnswerQueuePanel.this.rNumber, qNumber);
					final String aText = trivia.getAnswerQueueAnswer(queueIndex);
					new ViewAnswerDialog(AnswerQueuePanel.this.rNumber, qNumber, qValue, qText, aText);
					break;
				case "Agree":
				case "Disagree":
				case "Neutral":
					final JToggleButton source = ( (JToggleButton) event.getSource() );
					queueIndex = Integer.parseInt(source.getName());
					Agreement agreement = null;
					switch (command) {
						case "Agree":
							agreement = Agreement.AGREE;
							break;
						case "Disagree":
							agreement = Agreement.DISAGREE;
							break;
						case "Neutral":
							agreement = Agreement.NEUTRAL;
							break;
					}
					final Agreement agreementF = agreement;
					( new SwingWorker<Void, Void>() {
						@Override
						public Void doInBackground() {
							AnswerQueueSubPanel.this.client
									.sendMessage(ClientMessageFactory.changeAgreement(queueIndex, agreementF));
							return null;
						}

						@Override
						public void done() {
							AnswerQueueSubPanel.this.client
									.log("Changed agreement on #" + ( queueIndex + 1 ) + " to " + agreementF.name());
						}
					} ).execute();
					break;
				default:
					break;
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public synchronized void itemStateChanged(ItemEvent event) {
			final JComponent source = (JComponent) event.getSource();
			if (source instanceof JComboBox<?> && event.getStateChange() == ItemEvent.SELECTED) {
				// Event was a change to the status combo box
				final String newStatus = (String) ( ( (JComboBox<String>) source ).getSelectedItem() );
				String comboBoxName = null;
				if (this.activeComboBox != null) {
					comboBoxName = this.activeComboBox;
				} else {
					comboBoxName = ( (JComboBox<String>) source ).getName();
				}
				final int queueIndex = Integer.parseInt(comboBoxName);
				final String lastStatus = AnswerQueueSubPanel.this.lastStatus.get(queueIndex);
				switch (newStatus) {
					case "Duplicate":
						( new SwingWorker<Void, Void>() {
							@Override
							public Void doInBackground() {
								AnswerQueueSubPanel.this.client
										.sendMessage(ClientMessageFactory.markDuplicate(queueIndex));
								return null;
							}

							@Override
							public void done() {
							}
						} ).execute();
						break;
					case "Not Called In":
						( new SwingWorker<Void, Void>() {
							@Override
							public Void doInBackground() {
								AnswerQueueSubPanel.this.client
										.sendMessage(ClientMessageFactory.markUncalled(queueIndex));
								return null;
							}

							@Override
							public void done() {
							}
						} ).execute();
						break;
					case "Calling":
						( new SwingWorker<Void, Void>() {
							@Override
							public Void doInBackground() {
								AnswerQueueSubPanel.this.client.sendMessage(ClientMessageFactory.callIn(queueIndex));
								return null;
							}

							@Override
							public void done() {
							}
						} ).execute();
						break;
					case "Incorrect":
						( new SwingWorker<Void, Void>() {
							@Override
							public Void doInBackground() {
								AnswerQueueSubPanel.this.client
										.sendMessage(ClientMessageFactory.markIncorrect(queueIndex));
								return null;
							}

							@Override
							public void done() {
							}
						} ).execute();
						new OperatorDialog(this.client, "Marking Answer Incorrect", queueIndex,
								( (JComboBox<String>) source ), lastStatus);
						break;
					case "Partial":
						( new SwingWorker<Void, Void>() {
							@Override
							public Void doInBackground() {
								AnswerQueueSubPanel.this.client
										.sendMessage(ClientMessageFactory.markPartial(queueIndex));
								return null;
							}

							@Override
							public void done() {
							}
						} ).execute();
						new OperatorDialog(this.client, "Marking Answer Partially Correct", queueIndex,
								( (JComboBox<String>) source ), lastStatus);
						break;
					case "Correct":
						( new SwingWorker<Void, Void>() {
							@Override
							public Void doInBackground() {
								AnswerQueueSubPanel.this.client
										.sendMessage(ClientMessageFactory.markCorrect(queueIndex));
								return null;
							}

							@Override
							public void done() {
							}
						} ).execute();
						new OperatorDialog(this.client, "Marking Answer Correct", queueIndex,
								( (JComboBox<String>) source ), lastStatus);
						break;
					default:
						break;
				}
				this.client.log("Changed status of answer #" + ( queueIndex + 1 ) + " in the queue to " + newStatus);

			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public synchronized void updateGUI(boolean force) {
			// Get the current Trivia data object
			final Trivia trivia = this.client.getTrivia();

			final boolean hideClosed = AnswerQueuePanel.this.gui.isHideClosed();
			final boolean hideDuplicates = AnswerQueuePanel.this.gui.isHideDuplicates();
			final QueueSort sortMethod = AnswerQueuePanel.this.gui.getQueueSort();

			// Get the queue data
			final Answer[] newAnswerQueue = trivia.getAnswerQueue(AnswerQueuePanel.this.rNumber);

			switch (sortMethod) {
				case TIMESTAMP_ASCENDING:
					Arrays.sort(newAnswerQueue, new Answer.TimestampCompare());
					break;
				case QNUMBER_ASCENDING:
					Arrays.sort(newAnswerQueue, new Answer.QNumberCompare());
					break;
				case STATUS_ASCENDING:
					Arrays.sort(newAnswerQueue, new Answer.StatusCompare());
					break;
				case TIMESTAMP_DESCENDING:
					Arrays.sort(newAnswerQueue, new Answer.TimestampCompareReverse());
					break;
				case QNUMBER_DESCENDING:
					Arrays.sort(newAnswerQueue, new Answer.QNumberCompareReverse());
					break;
				case STATUS_DESCENDING:
					Arrays.sort(newAnswerQueue, new Answer.StatusCompareReverse());
					break;
			}

			final int queueSize = newAnswerQueue.length;

			// Determine if each item in the queue has been updated
			final boolean[] qUpdated = new boolean[queueSize];
			for (int a = 0; a < queueSize; a++) {
				if (a < this.answerQueue.length) {
					qUpdated[a] = !( this.answerQueue[a].equals(newAnswerQueue[a]) );
				} else {
					qUpdated[a] = true;
				}
			}

			if (AnswerQueuePanel.this.live && this.answerQueue.length != newAnswerQueue.length) {
				AnswerQueuePanel.this.frame.playNewAnswerSound();
				AnswerQueuePanel.this.blink();
			}

			this.answerQueue = newAnswerQueue;

			// We need to track how many rows have been shown to do alternating background colors correctly
			int shownRows = 0;

			while (this.queuenumberLabels.size() < this.answerQueue.length) {
				this.makeNewRow();
			}

			final String user = this.client.getUser();

			for (int a = 0; a < queueSize; a++) {

				// Get the new data from the answer queue
				final int newQueueNumber = this.answerQueue[a].getQueueLocation();
				final String newTimestamp = this.answerQueue[a].getTimestamp();
				final int newQNumber = this.answerQueue[a].getQNumber();
				final String newAnswer = this.answerQueue[a].getAnswer();
				final int newConfidence = this.answerQueue[a].getConfidence();
				final int newAgreement = this.answerQueue[a].getAgreement();
				final Agreement myAgreement = this.answerQueue[a].getAgreement(user);
				final String newSubmitter = this.answerQueue[a].getSubmitter();
				final String newOperator = this.answerQueue[a].getOperator();
				final String newCaller = this.answerQueue[a].getCaller();
				final String newStatus = this.answerQueue[a].getStatusString();

				this.lastStatus.set(a, newStatus);
				final boolean closed;
				if (AnswerQueuePanel.this.live) {
					closed = !trivia.isOpen(newQNumber);
				} else {
					closed = false;
				}

				final boolean filtered = this.gui.getFilterNumbers().contains(newQNumber)
						|| !( AnswerQueuePanel.this.gui.getFilterTextPattern().pattern().equals("")
								|| AnswerQueuePanel.this.gui.getFilterTextPattern().matcher(newAnswer).find() );

				final boolean showRow = !( ( hideClosed && closed )
						|| ( hideDuplicates && newStatus.equals("Duplicate") ) || filtered );

				if (showRow) {
					shownRows++;
				}

				if (qUpdated[a] || force) {
					// If the status has changed, update the labels and color
					Color color = notCalledInColor;
					final int statusIndex = Arrays.asList(STATUSES).indexOf(newStatus);
					switch (newStatus) {
						case "Duplicate":
							color = duplicateColor;
							break;
						case "Not Called In":
							color = notCalledInColor;
							break;
						case "Calling":
							color = callingColor;
							break;
						case "Incorrect":
							color = incorrectColor;
							break;
						case "Partial":
							color = partialColor;
							break;
						case "Correct":
							color = correctColor;
							break;
						default:
							color = notCalledInColor;
							break;
					}

					// Alternate background color based on rows shown
					Color bColor;
					if (shownRows % 2 == 1) {
						bColor = oddRowBackgroundColor;
					} else {
						bColor = evenRowBackgroundColor;
					}

					this.queuenumberLabels.get(a).setText("#" + newQueueNumber);
					this.queuenumberLabels.get(a).setForeground(color);
					this.queuenumberLabels.get(a).getParent().setBackground(bColor);
					this.queuenumberLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					this.timestampLabels.get(a).setText(newTimestamp);
					this.timestampLabels.get(a).setForeground(color);
					this.timestampLabels.get(a).getParent().setBackground(bColor);
					this.timestampLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					this.qNumberLabels.get(a).setText(newQNumber + "");
					this.qNumberLabels.get(a).setForeground(color);
					this.qNumberLabels.get(a).getParent().setBackground(bColor);
					this.qNumberLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					this.answerTextAreas.get(a).setText(newAnswer);
					// this.answerTextAreas.get(a).setToolTipText(newAnswer);
					this.answerTextAreas.get(a).setForeground(color);
					this.answerTextAreas.get(a).setBackground(bColor);
					this.answerTextAreas.get(a).setName("" + ( newQueueNumber - 1 ));

					this.confidenceLabels.get(a).setText(newConfidence + "");
					this.confidenceLabels.get(a).setForeground(color);
					this.confidenceLabels.get(a).getParent().setBackground(bColor);
					this.confidenceLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					final String agreementText = String.format("%+d", newAgreement);
					this.agreementLabels.get(a).setText(agreementText);
					this.agreementLabels.get(a).setForeground(color);
					this.agreementLabels.get(a).getParent().setBackground(bColor);
					this.agreementLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					switch (myAgreement) {
						case AGREE:
							this.agreeButtons.get(a).setSelected(true);
							this.agreeButtons.get(a).setBackground(agreeColor);
							this.agreeButtons.get(a).setActionCommand("Neutral");
							this.disagreeButtons.get(a).setActionCommand("Disagree");
							this.disagreeButtons.get(a).setBackground(null);
							break;
						case DISAGREE:
							this.disagreeButtons.get(a).setSelected(true);
							this.agreeButtons.get(a).setBackground(null);
							this.agreeButtons.get(a).setActionCommand("Agree");
							this.disagreeButtons.get(a).setActionCommand("Neutral");
							this.disagreeButtons.get(a).setBackground(disagreeColor);
							break;
						case NEUTRAL:
							this.buttonGroups.get(a).clearSelection();
							this.agreeButtons.get(a).setBackground(null);
							this.agreeButtons.get(a).setActionCommand("Agree");
							this.disagreeButtons.get(a).setActionCommand("Disagree");
							this.disagreeButtons.get(a).setBackground(null);
							break;
					}

					this.agreeButtons.get(a).setForeground(color);
					this.agreeButtons.get(a).getParent().setBackground(bColor);
					this.agreeButtons.get(a).setName("" + ( newQueueNumber - 1 ));

					this.disagreeButtons.get(a).setForeground(color);
					this.disagreeButtons.get(a).getParent().setBackground(bColor);
					this.disagreeButtons.get(a).setName("" + ( newQueueNumber - 1 ));

					this.submitterLabels.get(a).setText(newSubmitter);
					this.submitterLabels.get(a).setForeground(color);
					this.submitterLabels.get(a).getParent().setBackground(bColor);
					this.submitterLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					this.operatorTextPanes.get(a).setText(newOperator);
					this.operatorTextPanes.get(a).setForeground(color);
					this.operatorTextPanes.get(a).setBackground(bColor);
					this.operatorTextPanes.get(a).setName("" + ( newQueueNumber - 1 ));

					this.callerLabels.get(a).setText(newCaller);
					this.callerLabels.get(a).setForeground(color);
					this.callerLabels.get(a).getParent().setBackground(bColor);
					this.callerLabels.get(a).setName("" + ( newQueueNumber - 1 ));

					// Temporarily remove the status box listener to prevent trigger when we change it to match server
					// status
					final ItemListener[] listeners = this.statusComboBoxes.get(a).getItemListeners();
					for (final ItemListener listener : listeners) {
						this.statusComboBoxes.get(a).removeItemListener(listener);
					}
					this.statusComboBoxes.get(a).setForeground(bColor);
					this.statusComboBoxes.get(a).setBackground(color);
					this.statusComboBoxes.get(a).getParent().setBackground(bColor);
					this.statusComboBoxes.get(a).setName(( newQueueNumber - 1 ) + "");
					this.statusComboBoxes.get(a).setSelectedIndex(statusIndex);
					// Add the status box listener back to monitor user changes
					if (AnswerQueuePanel.this.live) {
						for (final ItemListener listener : listeners) {
							this.statusComboBoxes.get(a).addItemListener(listener);
						}
					}

					if (!showRow) {
						// Hide this row
						this.queuenumberLabels.get(a).setVisible(false);
						this.timestampLabels.get(a).setVisible(false);
						this.qNumberLabels.get(a).setVisible(false);
						this.answerTextAreas.get(a).setVisible(false);
						this.answerTextAreas.get(a).getParent().setVisible(false);
						this.answerTextAreas.get(a).getParent().getParent().setVisible(false);
						this.confidenceLabels.get(a).setVisible(false);
						this.agreementLabels.get(a).setVisible(false);
						this.agreeButtons.get(a).setVisible(false);
						this.disagreeButtons.get(a).setVisible(false);
						this.submitterLabels.get(a).setVisible(false);
						this.operatorTextPanes.get(a).setVisible(false);
						this.callerLabels.get(a).setVisible(false);
						this.statusComboBoxes.get(a).setVisible(false);

						this.queuenumberLabels.get(a).getParent().setVisible(false);
						this.timestampLabels.get(a).getParent().setVisible(false);
						this.qNumberLabels.get(a).getParent().setVisible(false);
						this.answerTextAreas.get(a).getParent().setVisible(false);
						this.answerTextAreas.get(a).getParent().getParent().setVisible(false);
						this.confidenceLabels.get(a).getParent().setVisible(false);
						this.agreementLabels.get(a).getParent().setVisible(false);
						this.agreeButtons.get(a).getParent().setVisible(false);
						this.disagreeButtons.get(a).getParent().setVisible(false);
						this.submitterLabels.get(a).getParent().setVisible(false);
						this.operatorTextPanes.get(a).getParent().setVisible(false);
						this.operatorTextPanes.get(a).getParent().getParent().setVisible(false);
						this.callerLabels.get(a).getParent().setVisible(false);
						this.statusComboBoxes.get(a).getParent().setVisible(false);
					} else {
						// Make this row visible
						this.queuenumberLabels.get(a).setVisible(true);
						this.timestampLabels.get(a).setVisible(true);
						this.qNumberLabels.get(a).setVisible(true);
						this.answerTextAreas.get(a).setVisible(true);
						this.answerTextAreas.get(a).getParent().setVisible(true);
						this.answerTextAreas.get(a).getParent().setVisible(true);
						this.confidenceLabels.get(a).setVisible(true);
						this.agreementLabels.get(a).setVisible(true);
						if (AnswerQueuePanel.this.live) {
							this.agreeButtons.get(a).setVisible(true);
							this.disagreeButtons.get(a).setVisible(true);
						} else {
							this.agreeButtons.get(a).setVisible(false);
							this.disagreeButtons.get(a).setVisible(false);
						}
						this.submitterLabels.get(a).setVisible(true);
						this.operatorTextPanes.get(a).setVisible(true);
						this.callerLabels.get(a).setVisible(true);
						this.statusComboBoxes.get(a).setVisible(true);

						this.queuenumberLabels.get(a).getParent().setVisible(true);
						this.timestampLabels.get(a).getParent().setVisible(true);
						this.qNumberLabels.get(a).getParent().setVisible(true);
						this.answerTextAreas.get(a).getParent().setVisible(true);
						this.answerTextAreas.get(a).getParent().getParent().setVisible(true);
						this.confidenceLabels.get(a).getParent().setVisible(true);
						this.agreementLabels.get(a).getParent().setVisible(true);
						this.agreeButtons.get(a).getParent().setVisible(true);
						this.disagreeButtons.get(a).getParent().setVisible(true);
						this.submitterLabels.get(a).getParent().setVisible(true);
						this.operatorTextPanes.get(a).getParent().setVisible(true);
						this.operatorTextPanes.get(a).getParent().getParent().setVisible(true);
						this.callerLabels.get(a).getParent().setVisible(true);
						this.statusComboBoxes.get(a).getParent().setVisible(true);
					}

				}

			}

			// Hide unused rows
			for (int a = queueSize; a < this.queuenumberLabels.size(); a++) {
				this.queuenumberLabels.get(a).setVisible(false);
				this.timestampLabels.get(a).setVisible(false);
				this.qNumberLabels.get(a).setVisible(false);
				this.answerTextAreas.get(a).setVisible(false);
				this.confidenceLabels.get(a).setVisible(false);
				this.agreementLabels.get(a).setVisible(false);
				this.agreeButtons.get(a).setVisible(false);
				this.disagreeButtons.get(a).setVisible(false);
				this.submitterLabels.get(a).setVisible(false);
				this.operatorTextPanes.get(a).setVisible(false);
				this.callerLabels.get(a).setVisible(false);
				this.statusComboBoxes.get(a).setVisible(false);

				this.queuenumberLabels.get(a).getParent().setVisible(false);
				this.timestampLabels.get(a).getParent().setVisible(false);
				this.qNumberLabels.get(a).getParent().setVisible(false);
				this.answerTextAreas.get(a).getParent().setVisible(false);
				this.answerTextAreas.get(a).getParent().getParent().setVisible(false);
				this.confidenceLabels.get(a).getParent().setVisible(false);
				this.agreementLabels.get(a).getParent().setVisible(false);
				this.agreeButtons.get(a).getParent().setVisible(false);
				this.disagreeButtons.get(a).getParent().setVisible(false);
				this.submitterLabels.get(a).getParent().setVisible(false);
				this.operatorTextPanes.get(a).getParent().setVisible(false);
				this.operatorTextPanes.get(a).getParent().getParent().setVisible(false);
				this.callerLabels.get(a).getParent().setVisible(false);
				this.statusComboBoxes.get(a).getParent().setVisible(false);
			}

		}

		/**
		 * Load properties from the client and apply.
		 */
		@Override
		protected void loadProperties(Properties properties) {

			for (int a = 0; a < this.queuenumberLabels.size(); a++) {
				setLabelProperties(this.queuenumberLabels.get(a), timeWidth, rowHeight / 2, null, null, fontSize);
				setLabelProperties(this.timestampLabels.get(a), timeWidth, rowHeight / 2, null, null, fontSize);
				setLabelProperties(this.qNumberLabels.get(a), qNumWidth, rowHeight, null, null, qNumFontSize);
				setTextAreaProperties(this.answerTextAreas.get(a), answerWidth, rowHeight, null, null, fontSize);
				setLabelProperties(this.confidenceLabels.get(a), confidenceWidth, rowHeight / 2, null, null, fontSize);
				setLabelProperties(this.agreementLabels.get(a), confidenceWidth, rowHeight / 2, null, null, fontSize);
				setButtonProperties(this.agreeButtons.get(a), agreementWidth, rowHeight / 2, null, fontSize);
				setButtonProperties(this.disagreeButtons.get(a), agreementWidth, rowHeight / 2, null, fontSize);
				setPanelProperties((JPanel) this.agreeButtons.get(a).getParent(), agreementWidth, rowHeight / 2, null);
				setPanelProperties((JPanel) this.disagreeButtons.get(a).getParent(), agreementWidth, rowHeight / 2,
						null);
				setLabelProperties(this.submitterLabels.get(a), subCallerWidth, rowHeight / 2, null, null, fontSize);
				setTextPaneProperties(this.operatorTextPanes.get(a), operatorWidth, rowHeight, null, null, fontSize);
				setPanelProperties((JPanel) this.operatorTextPanes.get(a).getParent(), operatorWidth, rowHeight, null);
				setLabelProperties(this.callerLabels.get(a), subCallerWidth, rowHeight / 2, null, null, fontSize);

				setPanelProperties((JPanel) this.statusComboBoxes.get(a).getParent(), statusWidth, rowHeight, null);
				this.statusComboBoxes.get(a).setFont(this.statusComboBoxes.get(a).getFont().deriveFont(fontSize));
			}
		}

		/**
		 * Make a new answer row.
		 */
		@SuppressWarnings("unchecked")
		private void makeNewRow() {
			final int a = this.queuenumberLabels.size();

			// Set up layout constraints
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			constraints.gridheight = 1;
			constraints.gridx = 0;
			constraints.gridy = 2 * a;
			this.queuenumberLabels.add(this.enclosedLabel("", timeWidth, rowHeight / 2, null, null, constraints,
					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
			this.queuenumberLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));

			constraints.gridx = 0;
			constraints.gridy = 2 * a + 1;
			this.timestampLabels.add(this.enclosedLabel("", timeWidth, rowHeight / 2, null, null, constraints, fontSize,
					SwingConstants.CENTER, SwingConstants.CENTER));
			this.timestampLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));

			constraints.gridheight = 2;
			constraints.gridx = 1;
			constraints.gridy = 2 * a;
			this.qNumberLabels.add(this.enclosedLabel("", qNumWidth, rowHeight, null, null, constraints, qNumFontSize,
					SwingConstants.CENTER, SwingConstants.CENTER));
			this.qNumberLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));

			constraints.gridx = 2;
			constraints.gridy = 2 * a;
			constraints.weightx = 1.0;
			this.answerTextAreas.add(this.scrollableTextArea("", answerWidth, rowHeight, null, null, constraints,
					fontSize, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED));
			this.answerTextAreas.get(a).setEditable(false);
			this.answerTextAreas.get(a).addMouseListener(new PopupListener(this.contextMenu));
			constraints.weightx = 0.0;

			constraints.gridheight = 1;
			constraints.gridx = 3;
			constraints.gridy = 2 * a;
			this.confidenceLabels.add(this.enclosedLabel("", confidenceWidth, rowHeight / 2, null, null, constraints,
					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
			this.confidenceLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));

			constraints.gridx = 3;
			constraints.gridy = 2 * a + 1;
			this.agreementLabels.add(this.enclosedLabel("", confidenceWidth, rowHeight / 2, null, null, constraints,
					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
			this.agreementLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));

			this.buttonGroups.add(new ButtonGroup() {
				private static final long serialVersionUID = 1L;

				@Override
				public void setSelected(ButtonModel model, boolean selected) {
					if (selected) {
						super.setSelected(model, selected);
					} else {
						this.clearSelection();
					}
				}
			});

			constraints.gridx = 4;
			constraints.gridy = 2 * a;
			JPanel buttonPanel = new JPanel(new GridBagLayout());
			buttonPanel.setPreferredSize(new Dimension(agreementWidth, rowHeight / 2));
			buttonPanel.setMinimumSize(new Dimension(agreementWidth, rowHeight / 2));
			this.add(buttonPanel, constraints);
			this.agreeButtons.add(new JToggleButton("+1"));
			this.agreeButtons.get(a).setToolTipText("Agree with this answer");
			this.agreeButtons.get(a).setBorder(BorderFactory.createEmptyBorder());
			this.agreeButtons.get(a).setMargin(new Insets(0, 0, 0, 0));
			this.agreeButtons.get(a).setActionCommand("Agree");
			setButtonProperties(this.agreeButtons.get(a), agreementWidth, rowHeight / 2, null, fontSize);
			if (AnswerQueuePanel.this.live) {
				this.agreeButtons.get(a).addActionListener(this);
			} else {
				this.agreeButtons.get(a).setEnabled(false);
			}
			buttonPanel.add(this.agreeButtons.get(a), buttonConstraints);
			this.buttonGroups.get(a).add(this.agreeButtons.get(a));

			constraints.gridx = 4;
			constraints.gridy = 2 * a + 1;
			buttonPanel = new JPanel(new GridBagLayout());
			buttonPanel.setPreferredSize(new Dimension(agreementWidth, rowHeight / 2));
			buttonPanel.setMinimumSize(new Dimension(agreementWidth, rowHeight / 2));
			this.add(buttonPanel, constraints);

			this.disagreeButtons.add(new JToggleButton("-1"));
			this.disagreeButtons.get(a).setToolTipText("Disagree with this answer");
			this.disagreeButtons.get(a).setBorder(BorderFactory.createEmptyBorder());
			this.disagreeButtons.get(a).setMargin(new Insets(0, 0, 0, 0));
			this.disagreeButtons.get(a).setActionCommand("Disagree");
			setButtonProperties(this.disagreeButtons.get(a), agreementWidth, rowHeight / 2, null, fontSize);
			if (AnswerQueuePanel.this.live) {
				this.disagreeButtons.get(a).addActionListener(this);
			} else {
				this.disagreeButtons.get(a).setEnabled(false);
			}
			this.buttonGroups.get(a).add(this.disagreeButtons.get(a));
			buttonPanel.add(this.disagreeButtons.get(a), buttonConstraints);

			constraints.gridheight = 1;
			constraints.gridx = 5;
			constraints.gridy = 2 * a;
			this.submitterLabels.add(this.enclosedLabel("", subCallerWidth, rowHeight / 2, null, null, constraints,
					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
			this.submitterLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));

			constraints.gridx = 5;
			constraints.gridy = 2 * a + 1;
			this.callerLabels.add(this.enclosedLabel("", subCallerWidth, rowHeight / 2, null, null, constraints,
					fontSize, SwingConstants.CENTER, SwingConstants.CENTER));
			this.callerLabels.get(a).addMouseListener(new PopupListener(this.contextMenu));
			constraints.gridheight = 2;

			constraints.gridx = 6;
			constraints.gridy = 2 * a;
			this.operatorTextPanes.add(this.scrollableTextPane("", operatorWidth, rowHeight, null, null, constraints,
					fontSize, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER));
			this.operatorTextPanes.get(a).setEditable(false);
			StyleConstants.setAlignment(( (StyledDocument) this.operatorTextPanes.get(a).getDocument() )
					.getStyle(StyleContext.DEFAULT_STYLE), StyleConstants.ALIGN_CENTER);
			DefaultCaret caret = (DefaultCaret) this.operatorTextPanes.get(a).getCaret();
			caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			this.operatorTextPanes.get(a).addMouseListener(new PopupListener(this.contextMenu));

			constraints.gridx = 7;
			constraints.gridy = 2 * a;
			final JPanel panel = new JPanel(new GridBagLayout());
			panel.setPreferredSize(new Dimension(statusWidth, rowHeight));
			this.add(panel, constraints);
			this.statusComboBoxes.add(new JComboBox<String>(STATUSES));
			this.statusComboBoxes.get(a).setName(a + "");
			if (AnswerQueuePanel.this.live) {
				this.statusComboBoxes.get(a).addItemListener(this);
				this.statusComboBoxes.get(a).addPopupMenuListener(this);
			} else {
				this.statusComboBoxes.get(a).setEditable(false);
				this.statusComboBoxes.get(a).setEnabled(false);
			}
			this.statusComboBoxes.get(a)
					.setRenderer(new StatusCellRenderer(
							(ListCellRenderer<String>) this.statusComboBoxes.get(a).getRenderer(),
							this.statusComboBoxes.get(a)));

			panel.add(this.statusComboBoxes.get(a));

			this.lastStatus.add("new");
		}

		/**
		 * A custom renderer to control how the status combo box is displayed.
		 *
		 * @author Walter Kolczynski
		 *
		 */
		public class StatusCellRenderer implements ListCellRenderer<String> {
			private final ListCellRenderer<String>	wrapped;
			private final JComboBox<String>			comboBox;

			public StatusCellRenderer(ListCellRenderer<String> listCellRenderer, JComboBox<String> comboBox) {
				this.wrapped = listCellRenderer;
				this.comboBox = comboBox;
			}

			@Override
			public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
					boolean isSelected, boolean cellHasFocus) {
				final String displayName = String.valueOf(value); // customize here
				final Component renderer = this.wrapped.getListCellRendererComponent(list, displayName, index,
						isSelected, cellHasFocus);
				if (renderer instanceof JLabel) {
					Color color = notCalledInColor;
					switch (value) {
						case "Duplicate":
							color = duplicateColor;
							break;
						case "Not Called In":
							color = notCalledInColor;
							break;
						case "Calling":
							color = callingColor;
							break;
						case "Incorrect":
							color = incorrectColor;
							break;
						case "Partial":
							color = partialColor;
							break;
						case "Correct":
							color = correctColor;
							break;
						default:
							color = notCalledInColor;
							break;
					}
					if (isSelected) {
						( (JLabel) renderer ).setForeground(color);
						( (JLabel) renderer ).setBackground(this.comboBox.getForeground());
					} else {
						( (JLabel) renderer ).setForeground(this.comboBox.getForeground());
						( (JLabel) renderer ).setBackground(color);
					}

				}
				return renderer;
			}
		}

		/**
		 * Listener for events to display pop-up menu.
		 *
		 * @author Walter Kolczynski
		 *
		 */
		private class PopupListener extends MouseAdapter {

			private final JPopupMenu menu;

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
				final JComponent source = (JComponent) event.getSource();
				if (event.isPopupTrigger() && !source.getName().equals("")) {
					this.menu.setName(source.getName());
					this.menu.show(source, event.getX(), event.getY());
				}
			}

		}

		@SuppressWarnings("unchecked")
		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			this.activeComboBox = ( (JComboBox<String>) e.getSource() ).getName();
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			this.activeComboBox = null;
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
	}
}
