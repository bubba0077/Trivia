package net.bubbaland.trivia.client.tabpanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.bubbaland.trivia.Question;
import net.bubbaland.trivia.Round;
import net.bubbaland.trivia.client.TristateCheckBox;
import net.bubbaland.trivia.client.TriviaClient;
import net.bubbaland.trivia.client.TriviaFrame;
import net.bubbaland.trivia.client.TriviaGUI;
import net.bubbaland.trivia.client.TriviaMainPanel;
import net.bubbaland.trivia.client.TristateCheckBox.TristateState;

public class SearchPanel extends TriviaMainPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long			serialVersionUID	= 3589815467416864653L;

	// Pattern regexPatterns = Pattern.compile("[.^]|\\d|\\D|\\s|\\S|\\w|\\W");

	private final static int			N_ROWS				= 5;

	private final JTextField			searchQueryTextField;
	private final JButton				searchButton;
	private final JToggleButton			filterButton;
	private final JLabel				lastQueryLabel;

	/** The sub-panel holding the questions */
	private final FilterSubPanel		filterSubPanel;
	private final HeaderSubPanel		headerSubPanel;
	private final SearchResultsSubPanel	searchResultsSubPanel;
	private final JScrollPane			searchResultsPane;

	/**
	 * Instantiates a new question list panel.
	 *
	 * @param client
	 *            The local trivia client
	 * @param live
	 *            whether this panel should always show the current round
	 * @param rNumber
	 *            the round number
	 */
	public SearchPanel(TriviaClient client, TriviaFrame parent) {

		super(client, parent);

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 1.0;
		constraints.weighty = 0.0;

		constraints.gridwidth = 3;
		constraints.weightx = 0.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.enclosedLabel(" Search query (regex allowed, case-insensitive by default)", constraints,
				SwingConstants.LEFT, SwingConstants.BOTTOM);

		constraints.gridwidth = 2;
		constraints.weightx = 1.0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.searchQueryTextField = new JTextField("");
		this.add(this.searchQueryTextField, constraints);

		constraints.gridwidth = 1;
		constraints.weightx = 0.0;
		constraints.gridx = 2;
		constraints.gridy = 1;
		this.searchButton = new JButton("Search");
		this.searchButton.setActionCommand("Search");
		this.searchButton.addActionListener(this);
		this.add(this.searchButton, constraints);

		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 3;
		this.filterSubPanel = new FilterSubPanel(client, parent);
		this.filterSubPanel.setVisible(false);
		this.add(this.filterSubPanel, constraints);

		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.filterButton = new JToggleButton("Show Filters");
		this.filterButton.setActionCommand("Show Filters");
		this.filterButton.addActionListener(this);
		this.add(this.filterButton, constraints);

		constraints.gridwidth = 2;
		constraints.gridx = 1;
		constraints.gridy = 2;
		this.lastQueryLabel = this.enclosedLabel("", constraints, SwingConstants.LEFT, SwingConstants.CENTER);

		constraints.gridwidth = 1;
		constraints.gridwidth = 3;
		constraints.gridx = 0;
		constraints.gridy = 4;
		this.headerSubPanel = new HeaderSubPanel(client, parent);
		this.add(this.headerSubPanel, constraints);

		/**
		 * Create the question list sub-panel and place in a scroll pane
		 */
		constraints.gridx = 0;
		constraints.gridy = 8 + N_ROWS;
		constraints.weighty = 1.0;
		this.searchResultsSubPanel = new SearchResultsSubPanel(client, parent);
		this.searchResultsPane = new JScrollPane(this.searchResultsSubPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.searchResultsPane.setBorder(BorderFactory.createEmptyBorder());
		this.searchResultsPane.setPreferredSize(new Dimension(0, 200));
		this.searchResultsPane.getVerticalScrollBar().setUnitIncrement(3);
		this.add(this.searchResultsPane, constraints);
		constraints.weighty = 0.0;

		this.addEnterOverride(this.searchQueryTextField, this.searchButton);

		this.loadProperties(TriviaGUI.PROPERTIES);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see net.bubbaland.trivia.TriviaPanel#update()
	 */
	@Override
	public synchronized void updateGUI(boolean force) {
		this.searchResultsSubPanel.updateGUIonEDT(force);
	}

	@Override
	protected void loadProperties(Properties properties) {
		this.filterSubPanel.loadProperties(properties);
		this.headerSubPanel.loadProperties(properties);
		this.searchResultsSubPanel.loadProperties(properties);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		switch (command) {
			case "Search":
				this.search();
				break;
			case "Show Filters":
				this.filterSubPanel.setVisible(true);
				this.filterButton.setText("Hide Filters");
				this.filterButton.setActionCommand("Hide Filters");
				break;
			case "Hide Filters":
				this.filterSubPanel.setVisible(false);
				this.filterButton.setText("Show Filters");
				this.filterButton.setActionCommand("Show Filters");
				break;
			default:
				System.out.println("Unknown action command " + command + " received by SearchPanel");
				break;
		}
	}

	private class FilterSubPanel extends TriviaMainPanel implements ActionListener {

		private static final long	serialVersionUID	= 2982616874485932222L;

		final JCheckBox				questionCheckBox;
		final JCheckBox				answerCheckBox;
		final TristateCheckBox		allRoundCheckbox;
		final JCheckBox[]			roundCheckBoxArray;

		public FilterSubPanel(TriviaClient client, TriviaFrame parent) {

			super(client, parent);

			Round[] rounds = this.client.getTrivia().getRounds();

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;
			constraints.weightx = 1.0;
			constraints.weighty = 0.0;

			constraints.gridx = 0;
			constraints.gridy = 0;
			this.questionCheckBox = new JCheckBox("Questions");
			this.questionCheckBox.setSelected(true);
			this.add(this.questionCheckBox, constraints);

			constraints.gridx = 2;
			constraints.gridy = 0;
			this.answerCheckBox = new JCheckBox("Answers");
			this.answerCheckBox.setSelected(true);
			this.add(this.answerCheckBox, constraints);

			constraints.gridx = 0;
			constraints.gridy = 1;
			this.allRoundCheckbox = new TristateCheckBox("All Rounds");
			this.allRoundCheckbox.addActionListener(this);
			this.allRoundCheckbox.setActionCommand("None");
			this.allRoundCheckbox.setState(TristateState.SELECTED);
			this.add(this.allRoundCheckbox, constraints);

			this.roundCheckBoxArray = new JCheckBox[rounds.length];
			for (int r = 0; r < rounds.length; r++) {
				constraints.gridx = r / N_ROWS;
				constraints.gridy = 6 + r % N_ROWS;

				this.roundCheckBoxArray[r] = new JCheckBox("Round " + rounds[r].getRoundNumber());
				this.roundCheckBoxArray[r].setSelected(true);
				this.roundCheckBoxArray[r].addActionListener(this);
				this.roundCheckBoxArray[r].setActionCommand("Round");
				this.roundCheckBoxArray[r].setName(rounds[r].getRoundNumber() + "");
				this.add(this.roundCheckBoxArray[r], constraints);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			switch (command) {
				case "All Rounds":
					switch (this.allRoundCheckbox.getState()) {
						case DESELECTED:
							Arrays.stream(this.roundCheckBoxArray).parallel().forEach(c -> c.setSelected(false));
							break;
						case INDETERMINATE:
							break;
						case SELECTED:
							Arrays.stream(this.roundCheckBoxArray).parallel().forEach(c -> c.setSelected(true));
							break;
					}
					break;
				case "Round":
					if (Arrays.stream(this.roundCheckBoxArray).parallel().allMatch(c -> c.isSelected())) {
						this.allRoundCheckbox.setState(TristateState.SELECTED);
					} else if (Arrays.stream(this.roundCheckBoxArray).parallel().noneMatch(c -> c.isSelected())) {
						this.allRoundCheckbox.setState(TristateState.DESELECTED);
					} else {
						this.allRoundCheckbox.setState(TristateState.INDETERMINATE);
					}
					break;
				default:
					System.out.println("Unknown action command " + command + " received by SearchPanel.FilterSubPanel");
					break;
			}
		}

		@Override
		protected void updateGUI(boolean forceUpdate) {

		}

		@Override
		protected void loadProperties(Properties properties) {

		}

	}

	private class HeaderSubPanel extends TriviaMainPanel {

		private static final long	serialVersionUID	= -8968557123435896115L;

		private final JLabel		rNumberLabel, qNumberLabel, earnedLabel, valueLabel, questionLabel, answerLabel,
				subOpLabel, blank;

		public HeaderSubPanel(TriviaClient client, TriviaFrame parent) {

			super(client, parent);

			// Set up the layout constraints
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.CENTER;

			constraints.gridx = 0;
			constraints.gridy = 0;

			constraints.weightx = 0.0;
			constraints.weighty = 0.0;

			/**
			 * Create the header row
			 */
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.rNumberLabel = this.enclosedLabel("Rd", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 1;
			constraints.gridy = 0;
			this.qNumberLabel = this.enclosedLabel("Q#", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 2;
			constraints.gridy = 0;
			this.earnedLabel = this.enclosedLabel("Earned", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 3;
			constraints.gridy = 0;
			this.valueLabel = this.enclosedLabel("Value", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
			constraints.weightx = 0.0;

			constraints.gridx = 4;
			constraints.gridy = 0;
			constraints.weightx = 0.6;
			this.questionLabel =
					this.enclosedLabel("Question", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
			constraints.weightx = 0.0;

			constraints.gridx = 5;
			constraints.gridy = 0;
			constraints.weightx = 0.4;
			this.answerLabel = this.enclosedLabel("Answer", constraints, SwingConstants.LEFT, SwingConstants.CENTER);
			constraints.weightx = 0.0;

			constraints.gridx = 6;
			constraints.gridy = 0;
			this.subOpLabel = this.enclosedLabel("Credit", constraints, SwingConstants.CENTER, SwingConstants.CENTER);

			constraints.gridx = 7;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			this.blank = this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER);
		}

		@Override
		protected void updateGUI(boolean forceUpdate) {
			// Nothing to do
		}

		@Override
		protected void loadProperties(Properties properties) {
			/**
			 * Colors
			 */
			final Color headerColor =
					new Color(new BigInteger(properties.getProperty("RoundQuestions.Header.Color"), 16).intValue());
			final Color headerBackgroundColor = new Color(
					new BigInteger(properties.getProperty("RoundQuestions.Header.BackgroundColor"), 16).intValue());
			/**
			 * Sizes
			 */
			final int headerHeight = Integer.parseInt(properties.getProperty("RoundQuestions.Header.Height"));

			final int qNumWidth = Integer.parseInt(properties.getProperty("RoundQuestions.QNumber.Width"));
			final int earnedWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Earned.Width"));
			final int valueWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Value.Width"));
			final int questionWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Question.Width"));
			final int answerWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Answer.Width"));
			final int subOpWidth = Integer.parseInt(properties.getProperty("RoundQuestions.SubOp.Width"));

			/**
			 * Font sizes
			 */
			final float headerFontSize = Float.parseFloat(properties.getProperty("RoundQuestions.Header.FontSize"));

			setLabelProperties(this.rNumberLabel, qNumWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			setLabelProperties(this.qNumberLabel, qNumWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			setLabelProperties(this.earnedLabel, earnedWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			setLabelProperties(this.valueLabel, valueWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			setLabelProperties(this.questionLabel, questionWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			setLabelProperties(this.answerLabel, answerWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			setLabelProperties(this.subOpLabel, subOpWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
			final int scrollBarWidth;
			if (UIManager.getLookAndFeel().getName().equals("Nimbus")) {
				scrollBarWidth = (int) UIManager.get("ScrollBar.thumbHeight");
			} else {
				scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
			}
			setLabelProperties(this.blank, scrollBarWidth, headerHeight, headerColor, headerBackgroundColor,
					headerFontSize);
		}
	}

	/**
	 * A panel that displays the question data for a round.
	 */
	private class SearchResultsSubPanel extends TriviaMainPanel {

		private static final long						serialVersionUID	= 3825357215129662133L;

		/**
		 * GUI Elements that will need to be updated
		 */
		private final HashMap<Integer, JLabel>			rNumberLabels, qNumberLabels, earnedLabels, valueLabels;
		private final HashMap<Integer, QuestionPane>	questionTextPane;
		private final HashMap<Integer, QuestionPane>	answerTextAreas;
		private final HashMap<Integer, JTextPane>		submitterTextPanes;
		private final HashMap<Integer, JSeparator>		separators;
		private final JPanel							spacer;

		private final static int						MAX_RESULTS			= 1000;

		/** Search data */
		private LinkedHashMap<Question, Round>			results;

		/** Data source */
		private final TriviaClient						client;

		/**
		 * Instantiates a new question list sub-panel.
		 *
		 * @param server
		 *            the server
		 * @param client
		 *            the client application
		 * @param live
		 *            whether this panel should always show the current round
		 * @param rNumber
		 *            the round number
		 */
		public SearchResultsSubPanel(TriviaClient client, TriviaFrame parent) {
			super(client, parent);

			this.client = client;
			this.results = new LinkedHashMap<Question, Round>();

			this.rNumberLabels = new HashMap<Integer, JLabel>();
			this.qNumberLabels = new HashMap<Integer, JLabel>();
			this.earnedLabels = new HashMap<Integer, JLabel>();
			this.valueLabels = new HashMap<Integer, JLabel>();
			this.submitterTextPanes = new HashMap<Integer, JTextPane>();
			this.questionTextPane = new HashMap<Integer, QuestionPane>();
			this.answerTextAreas = new HashMap<Integer, QuestionPane>();
			this.separators = new HashMap<Integer, JSeparator>();

			/**
			 * Extra row at the bottom to soak up any extra space
			 */
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.fill = GridBagConstraints.BOTH;
			constraints.anchor = GridBagConstraints.NORTH;
			constraints.weightx = 0.0;

			constraints.gridx = 0;
			constraints.gridy = MAX_RESULTS;
			constraints.gridwidth = 8;
			constraints.weighty = 1.0;
			this.spacer = new JPanel();
			this.spacer.setPreferredSize(new Dimension(0, 0));
			this.add(this.spacer, constraints);
		}


		/*
		 * (non-Javadoc)
		 *
		 * @see net.bubbaland.trivia.TriviaPanel#update()
		 */
		@Override
		public synchronized void updateGUI(boolean force) {

		}

		private void updateResults(String query, LinkedHashMap<Question, Round> results) {
			this.results = results;

			Pattern pattern = Pattern.compile(query);

			int nResults = results.size();

			Question[] questions = results.keySet().toArray(new Question[nResults]);

			boolean newRow = false;

			while (this.rNumberLabels.size() < nResults && this.rNumberLabels.size() < MAX_RESULTS) {
				newRow = true;
				int row = this.rNumberLabels.size();

				final GridBagConstraints constraints = new GridBagConstraints();
				constraints.fill = GridBagConstraints.BOTH;
				constraints.anchor = GridBagConstraints.NORTH;
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;

				/**
				 * Plot this row
				 */
				constraints.gridheight = 1;
				constraints.gridx = 0;
				constraints.gridy = row;
				this.rNumberLabels.put(row,
						this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER));

				constraints.gridx = 1;
				constraints.gridy = row;
				this.qNumberLabels.put(row,
						this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER));

				constraints.gridx = 2;
				constraints.gridy = row;
				this.earnedLabels.put(row,
						this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER));

				constraints.gridx = 3;
				constraints.gridy = row;
				this.valueLabels.put(row,
						this.enclosedLabel("", constraints, SwingConstants.CENTER, SwingConstants.CENTER));

				constraints.gridx = 4;
				constraints.gridy = row;
				constraints.weightx = 0.6;
				QuestionPane newQTextPane = this.hyperlinkedTextPane(this.client, "", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.questionTextPane.put(row, newQTextPane);
				newQTextPane.setEditable(false);

				constraints.weightx = 0.0;
				constraints.weighty = 0.0;
				constraints.gridx = 5;
				constraints.gridy = row;
				constraints.weightx = 0.0;
				JSeparator newSeparator = new JSeparator(SwingConstants.VERTICAL);
				this.separators.put(row, newSeparator);
				this.add(newSeparator, constraints);

				constraints.gridx = 6;
				constraints.gridy = row;
				constraints.weightx = 0.4;
				QuestionPane newAnswerPane = this.hyperlinkedTextPane(this.client, "", constraints,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				this.answerTextAreas.put(row, newAnswerPane);
				newAnswerPane.setEditable(false);
				constraints.weightx = 0.0;
				constraints.weighty = 0.0;

				constraints.gridx = 7;
				constraints.gridy = row;
				JTextPane newSubmitterPane =
						this.scrollableTextPane("", constraints, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
								ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				StyleConstants.setAlignment(
						( (StyledDocument) newSubmitterPane.getDocument() ).getStyle(StyleContext.DEFAULT_STYLE),
						StyleConstants.ALIGN_CENTER);
				DefaultCaret caret = (DefaultCaret) newSubmitterPane.getCaret();
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
				newSubmitterPane.setEditable(false);
				this.submitterTextPanes.put(row, newSubmitterPane);
			}

			if (newRow) {
				this.loadProperties(TriviaGUI.PROPERTIES);
			}

			for (int row = 0; row < nResults && row < this.rNumberLabels.size(); row++) {
				Question q = questions[row];
				int rNumber = results.get(q).getRoundNumber();

				String highlightedQuestion = q.getQuestionText();
				if (pattern.matcher(highlightedQuestion).find()) {
					highlightedQuestion = pattern.matcher(highlightedQuestion)
							.replaceAll("<span style='background-color:yellow;font-weight:bold'>$0</span>");
				}
				String highlightedAnswer = q.getAnswerText();
				if (pattern.matcher(highlightedAnswer).find()) {
					highlightedAnswer = pattern.matcher(highlightedAnswer)
							.replaceAll("<span style='background-color:yellow;font-weight:bold'>$0</span>");
				}

				this.rNumberLabels.get(row).setText(rNumber + "");
				this.qNumberLabels.get(row).setText(q.getQuestionNumber() + "");
				this.questionTextPane.get(row).setText(highlightedQuestion);
				this.answerTextAreas.get(row).setText(highlightedAnswer);
				this.earnedLabels.get(row).setText(q.getEarned() + "");
				this.valueLabels.get(row).setText(q.getQuestionValue() + "");
				this.submitterTextPanes.get(row).setText(q.getSubmitter());
			}

			for (int row = 0; row < this.rNumberLabels.size(); row++) {
				boolean visible = row < nResults;
				this.rNumberLabels.get(row).setVisible(visible);
				this.qNumberLabels.get(row).setVisible(visible);
				this.earnedLabels.get(row).setVisible(visible);
				this.valueLabels.get(row).setVisible(visible);
				this.rNumberLabels.get(row).getParent().setVisible(visible);
				this.qNumberLabels.get(row).getParent().setVisible(visible);
				this.earnedLabels.get(row).getParent().setVisible(visible);
				this.valueLabels.get(row).getParent().setVisible(visible);
				this.questionTextPane.get(row).setVisible(visible);
				this.answerTextAreas.get(row).setVisible(visible);
				this.submitterTextPanes.get(row).setVisible(visible);
			}

		}


		@Override
		protected void loadProperties(Properties properties) {
			/**
			 * Colors
			 */
			final Color headerBackgroundColor = new Color(
					new BigInteger(properties.getProperty("RoundQuestions.Header.BackgroundColor"), 16).intValue());
			final Color oddColor =
					new Color(new BigInteger(properties.getProperty("RoundQuestions.OddRow.Color"), 16).intValue());
			final Color evenColor =
					new Color(new BigInteger(properties.getProperty("RoundQuestions.EvenRow.Color"), 16).intValue());
			final Color oddBackgroundColor = new Color(
					new BigInteger(properties.getProperty("RoundQuestions.OddRow.BackgroundColor"), 16).intValue());
			final Color evenBackgroundColor = new Color(
					new BigInteger(properties.getProperty("RoundQuestions.EvenRow.BackgroundColor"), 16).intValue());

			/**
			 * Sizes
			 */
			final int rowHeight = Integer.parseInt(properties.getProperty("RoundQuestions.Row.Height"));

			final int qNumWidth = Integer.parseInt(properties.getProperty("RoundQuestions.QNumber.Width"));
			final int earnedWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Earned.Width"));
			final int valueWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Value.Width"));
			final int questionWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Question.Width"));
			final int answerWidth = Integer.parseInt(properties.getProperty("RoundQuestions.Answer.Width"));
			final int subOpWidth = Integer.parseInt(properties.getProperty("RoundQuestions.SubOp.Width"));

			/**
			 * Font sizes
			 */
			final float fontSize = Float.parseFloat(properties.getProperty("RoundQuestions.FontSize"));
			final float largeFontSize = Float.parseFloat(properties.getProperty("RoundQuestions.LargeFontSize"));

			for (int q = 0; q < this.results.size(); q++) {
				// Set the color for this row
				Color color, bColor;
				if (q % 2 == 1) {
					color = oddColor;
					bColor = oddBackgroundColor;
				} else {
					color = evenColor;
					bColor = evenBackgroundColor;
				}

				setLabelProperties(this.rNumberLabels.get(q), qNumWidth, rowHeight, color, bColor, largeFontSize);
				setLabelProperties(this.qNumberLabels.get(q), qNumWidth, rowHeight, color, bColor, largeFontSize);
				setLabelProperties(this.earnedLabels.get(q), earnedWidth, rowHeight, color, bColor, largeFontSize);
				setLabelProperties(this.valueLabels.get(q), valueWidth, rowHeight, color, bColor, largeFontSize);
				setTextPaneProperties(this.questionTextPane.get(q), questionWidth, rowHeight, color, bColor, fontSize);
				setTextPaneProperties(this.answerTextAreas.get(q), answerWidth, rowHeight, color, bColor, fontSize);
				setTextPaneProperties(this.submitterTextPanes.get(q), subOpWidth, rowHeight / 3, color, bColor,
						fontSize);
			}
			this.spacer.setBackground(headerBackgroundColor);

		}
	}

	private synchronized void search() {
		String query = this.searchQueryTextField.getText();
		boolean searchQuestions = this.filterSubPanel.questionCheckBox.isSelected();
		boolean searchAnswers = this.filterSubPanel.answerCheckBox.isSelected();

		ArrayList<Integer> rNumbers = new ArrayList<Integer>();
		Arrays.stream(this.filterSubPanel.roundCheckBoxArray).sequential().filter(c -> c.isSelected())
				.forEach(c -> rNumbers.add(Integer.parseInt(c.getName())));

		LinkedHashMap<Question, Round> results =
				this.client.getTrivia().search(query, searchQuestions, searchAnswers, rNumbers);

		String resultText = "    " + results.size() + " results for \"" + query + "\"";
		if (results.size() > SearchResultsSubPanel.MAX_RESULTS) {
			resultText = resultText + " (only first " + SearchResultsSubPanel.MAX_RESULTS + " shown)";
		}
		this.lastQueryLabel.setText(resultText);
		this.searchResultsSubPanel.updateResults(query, results);
	}

}
