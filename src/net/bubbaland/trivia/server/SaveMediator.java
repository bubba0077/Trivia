package net.bubbaland.trivia.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.bubbaland.trivia.Answer;
import net.bubbaland.trivia.Question;
import net.bubbaland.trivia.Round;
import net.bubbaland.trivia.Trivia;
import net.bubbaland.trivia.TriviaChartFactory;

public class SaveMediator {

	final private String saveDirectory, chartDirectory;

	public SaveMediator(String saveDirectory, String chartDirectory) {
		this.saveDirectory = saveDirectory;
		this.chartDirectory = chartDirectory;
	}

	/**
	 * Get a list of the available saves.
	 *
	 * @return Array of save file names
	 */
	public String[] listSaves() {
		final File folder = new File(this.saveDirectory);
		final File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// Only get XML files
				if (file.getName().toLowerCase().endsWith(".xml")) return true;
				return false;
			}
		});
		final int nFiles = files.length;
		final String[] filenames = new String[nFiles];
		for (int f = 0; f < nFiles; f++) {
			filenames[f] = files[f].getName();
		}
		Arrays.sort(filenames, Collections.reverseOrder());
		return filenames;
	}

	/**
	 * Loads a trivia state from file.
	 *
	 * @param stateFile
	 *            The name of the file to load
	 */
	public Trivia loadState(final Trivia trivia, String user, String stateFile) {
		// The full qualified file name
		stateFile = this.saveDirectory + "/" + stateFile;

		// Clear all data from the trivia contest
		trivia.reset();

		try {
			// Open the save file
			final File infile = new File(stateFile);
			final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			final Document doc = dBuilder.parse(infile);
			doc.getDocumentElement().normalize();

			// Get the top-level element
			final Element triviaElement = doc.getDocumentElement();

			// Read/set the trivia parameters
			trivia.setNTeams(
					Integer.parseInt(triviaElement.getElementsByTagName("Number_of_Teams").item(0).getTextContent()));
			try {
				trivia.setNVisual(Integer.parseInt(
						triviaElement.getElementsByTagName("Number_of_Visual_Trivia").item(0).getTextContent()));
			} catch (NullPointerException e) {
				trivia.setNVisual(0);
			}
			trivia.setCurrentRound(
					Integer.parseInt(triviaElement.getElementsByTagName("Current_Round").item(0).getTextContent()));

			// Get a list of the round elements
			final NodeList roundElements = triviaElement.getElementsByTagName("Round");

			for (int r = 0; r < roundElements.getLength(); r++) {
				final Element roundElement = (Element) roundElements.item(r);
				// Read the round number
				final int rNumber = Integer.parseInt(roundElement.getAttribute("number"));
				TriviaServer.log("Reading data for round " + rNumber);

				// Read/set if the round is a speed round
				final boolean isSpeed =
						roundElement.getElementsByTagName("Speed").item(0).getTextContent().equals("true");
				trivia.getRound(rNumber).setSpeed(isSpeed);

				try {
					trivia.getRound(rNumber)
							.setShowName(roundElement.getElementsByTagName("Show_Name").item(0).getTextContent());
				} catch (NullPointerException e) {
					trivia.getRound(rNumber).setShowName("");
				}
				try {
					trivia.getRound(rNumber)
							.setShowHost(roundElement.getElementsByTagName("Show_Host").item(0).getTextContent());
				} catch (NullPointerException e) {
					trivia.getRound(rNumber).setShowHost("");
				}

				trivia.getRound(rNumber).setDiscrepencyText(
						roundElement.getElementsByTagName("Discrepancy_Text").item(0).getTextContent());

				// Get a list of the question elements in this round
				final NodeList questionElements = roundElement.getElementsByTagName("Question");

				for (int q = 0; q < questionElements.getLength(); q++) {
					final Element questionElement = (Element) questionElements.item(q);
					// Read the question number
					final int qNumber = Integer.parseInt(questionElement.getAttribute("number"));

					// Read/set question parameters
					final boolean beenOpen =
							questionElement.getElementsByTagName("Been_Open").item(0).getTextContent().equals("true");
					final boolean isOpen =
							questionElement.getElementsByTagName("Is_Open").item(0).getTextContent().equals("true");
					final boolean isCorrect =
							questionElement.getElementsByTagName("Is_Correct").item(0).getTextContent().equals("true");
					final int value =
							Integer.parseInt(questionElement.getElementsByTagName("Value").item(0).getTextContent());
					final String question =
							questionElement.getElementsByTagName("Question_Text").item(0).getTextContent();
					final String answer = questionElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
					final String submitter = questionElement.getElementsByTagName("Submitter").item(0).getTextContent();

					if (beenOpen) {
						trivia.getRound(rNumber).open("From file", qNumber);
						trivia.getRound(rNumber).editQuestion(qNumber, value, question, answer, isCorrect, submitter);
						if (!isOpen) {
							trivia.getRound(rNumber).close(qNumber, null);
						}
					}
				}

				final Element element = (Element) roundElement.getElementsByTagName("Answer_Queue").item(0);

				if (element != null) {
					// Get the list of propsed answer elements in the answer queue
					final NodeList answerElements = element.getElementsByTagName("Proposed_Answer");

					for (int a = 0; a < answerElements.getLength(); a++) {
						final Element answerElement = (Element) answerElements.item(a);

						// Read/set parameters of the answer
						final int qNumber = Integer.parseInt(
								answerElement.getElementsByTagName("Question_Number").item(0).getTextContent());
						final String status = answerElement.getElementsByTagName("Status").item(0).getTextContent();
						final String timestamp =
								answerElement.getElementsByTagName("Timestamp").item(0).getTextContent();
						final String answer =
								answerElement.getElementsByTagName("Answer_Text").item(0).getTextContent();
						final String submitter =
								answerElement.getElementsByTagName("Submitter").item(0).getTextContent();
						final int confidence = Integer
								.parseInt(answerElement.getElementsByTagName("Confidence").item(0).getTextContent());
						final String caller = answerElement.getElementsByTagName("Caller").item(0).getTextContent();
						final String operator = answerElement.getElementsByTagName("Operator").item(0).getTextContent();

						trivia.getRound(rNumber).setAnswer(qNumber, answer, submitter, confidence, status, caller,
								operator, timestamp);
					}
				}
			}

		} catch (final ParserConfigurationException | SAXException | IOException e) {}

		return trivia;
	}

	/**
	 * Save the current trivia state to an xml file.
	 */
	void saveState(final Trivia trivia) {

		// The current date/time
		final Date time = new Date();

		//
		final String roundString = "Rd" + String.format("%02d", trivia.getCurrentRoundNumber());

		// Timestamp used as part of the filename (no spaces, descending precision)
		String filename =
				this.saveDirectory + "/" + roundString + "_" + TriviaServer.fileDateFormat.format(time) + ".xml";
		// Timestamp used in the save file
		final String createTime = TriviaServer.stringDateFormat.format(time);

		try {
			// Create a document
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();

			// Create the top-level element
			final Element triviaElement = doc.createElement("Trivia");
			doc.appendChild(triviaElement);

			// Make the save time an attribute of Trivia
			Attr attribute = doc.createAttribute("Save_Time");
			attribute.setValue(createTime);
			triviaElement.setAttributeNode(attribute);

			// Save the number of teams
			Element element = doc.createElement("Number_of_Teams");
			element.appendChild(doc.createTextNode(trivia.getNTeams() + ""));
			triviaElement.appendChild(element);

			// Save the number of visual trivia
			element = doc.createElement("Number_of_Visual_Trivia");
			element.appendChild(doc.createTextNode(trivia.getNVisual() + ""));
			triviaElement.appendChild(element);

			// Save the current round number
			element = doc.createElement("Current_Round");
			element.appendChild(doc.createTextNode(trivia.getCurrentRoundNumber() + ""));
			triviaElement.appendChild(element);

			for (final Round r : trivia.getRounds()) {
				// Create a round element
				final Element roundElement = doc.createElement("Round");
				triviaElement.appendChild(roundElement);

				// The round number
				attribute = doc.createAttribute("number");
				attribute.setValue(r.getRoundNumber() + "");
				roundElement.setAttributeNode(attribute);

				// Whether it is a speed round
				element = doc.createElement("Speed");
				element.appendChild(doc.createTextNode(r.isSpeed() + ""));
				roundElement.appendChild(element);

				// Whether the score has been announced for this round
				element = doc.createElement("Announced");
				element.appendChild(doc.createTextNode(r.isAnnounced() + ""));
				roundElement.appendChild(element);

				// The announced score for this round
				element = doc.createElement("Announced_Score");
				element.appendChild(doc.createTextNode(r.getAnnouncedPoints() + ""));
				roundElement.appendChild(element);

				// The announced place for this round
				element = doc.createElement("Announced_Place");
				element.appendChild(doc.createTextNode(r.getPlace() + ""));
				roundElement.appendChild(element);

				element = doc.createElement("Show_Name");
				element.appendChild(doc.createTextNode(r.getShowName() + ""));
				roundElement.appendChild(element);

				element = doc.createElement("Show_Host");
				element.appendChild(doc.createTextNode(r.getShowHost() + ""));
				roundElement.appendChild(element);

				// The discrepancy text for this round
				element = doc.createElement("Discrepancy_Text");
				element.appendChild(doc.createTextNode(r.getDiscrepancyText() + ""));
				roundElement.appendChild(element);

				final Element qListElement = doc.createElement("Questions");
				roundElement.appendChild(qListElement);

				for (final Question q : r.getQuestions()) {
					// Create a question element
					final Element questionElement = doc.createElement("Question");
					qListElement.appendChild(questionElement);

					// The question number
					attribute = doc.createAttribute("number");
					attribute.setValue(q.getQuestionNumber() + "");
					questionElement.setAttributeNode(attribute);

					// Whether the question has been open
					element = doc.createElement("Been_Open");
					element.appendChild(doc.createTextNode(q.beenOpen() + ""));
					questionElement.appendChild(element);

					// Whether the question is currently open
					element = doc.createElement("Is_Open");
					element.appendChild(doc.createTextNode(q.isOpen() + ""));
					questionElement.appendChild(element);

					// The value of the question
					element = doc.createElement("Value");
					element.appendChild(doc.createTextNode(q.getQuestionValue() + ""));
					questionElement.appendChild(element);

					// The question text
					element = doc.createElement("Question_Text");
					element.appendChild(doc.createTextNode(q.getQuestionText() + ""));
					questionElement.appendChild(element);

					// The answer
					element = doc.createElement("Answer_Text");
					element.appendChild(doc.createTextNode(q.getAnswerText() + ""));
					questionElement.appendChild(element);

					// Whether this question was answered correctly
					element = doc.createElement("Is_Correct");
					element.appendChild(doc.createTextNode(q.isCorrect() + ""));
					questionElement.appendChild(element);

					// The submitter for a correctly answered question
					element = doc.createElement("Submitter");
					element.appendChild(doc.createTextNode(q.getSubmitter() + ""));
					questionElement.appendChild(element);
				}

				// The size of the answer queue for the current round
				final int queueSize = r.getAnswerQueueSize();

				// Create a queue element
				final Element queueElement = doc.createElement("Answer_Queue");
				roundElement.appendChild(queueElement);

				// The size of the queue
				attribute = doc.createAttribute("size");
				attribute.setValue(queueSize + "");
				queueElement.setAttributeNode(attribute);

				for (final Answer a : r.getAnswerQueue()) {
					// Create a proposed answer element
					final Element answerElement = doc.createElement("Proposed_Answer");
					queueElement.appendChild(answerElement);

					// The question number for this answer
					element = doc.createElement("Question_Number");
					element.appendChild(doc.createTextNode(a.getQNumber() + ""));
					answerElement.appendChild(element);

					// The current status of this answer
					element = doc.createElement("Status");
					element.appendChild(doc.createTextNode(a.getStatusString()));
					answerElement.appendChild(element);

					// The time stamp of this answer
					element = doc.createElement("Timestamp");
					element.appendChild(doc.createTextNode(a.getTimestamp()));
					answerElement.appendChild(element);

					// The proposed answer
					element = doc.createElement("Answer_Text");
					element.appendChild(doc.createTextNode(a.getAnswerText()));
					answerElement.appendChild(element);

					// The submitter of this answer
					element = doc.createElement("Submitter");
					element.appendChild(doc.createTextNode(a.getSubmitter()));
					answerElement.appendChild(element);

					// The confidence in this answer
					element = doc.createElement("Confidence");
					element.appendChild(doc.createTextNode(a.getConfidence() + ""));
					answerElement.appendChild(element);

					// The user who called this answer in
					element = doc.createElement("Caller");
					element.appendChild(doc.createTextNode(a.getCaller()));
					answerElement.appendChild(element);

					// The operator who accepted the answer
					element = doc.createElement("Operator");
					element.appendChild(doc.createTextNode(a.getOperator()));
					answerElement.appendChild(element);
				}
			}

			// write the content into xml file
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(new File(filename));
			transformer.transform(source, result);

			TriviaServer.log("Saved state to " + filename);

		} catch (final ParserConfigurationException | TransformerException e) {
			TriviaServer.log("Couldn't save data to file " + filename);
		}

	}

	public void saveCharts(Trivia trivia, int chartWidth, int chartHeight) {

		final String roundString = "Rd" + String.format("%02d", trivia.getCurrentRoundNumber());

		if (trivia.getRound(1).isAnnounced()) {
			// Save place chart
			String filename = this.chartDirectory + "/" + roundString + "_placeChart.png";
			try {
				final JFreeChart chart = TriviaChartFactory.makePlaceChart(trivia);
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved place chart to " + filename);
				filename = this.chartDirectory + "/latest_placeChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			filename = this.chartDirectory + "/" + roundString + "_scoreByRoundChart.png";
			try {
				final JFreeChart chart = TriviaChartFactory.makeScoreByRoundChart(trivia);
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved place chart to " + filename);
				filename = this.chartDirectory + "/latest_scoreByRoundChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			filename = this.chartDirectory + "/" + roundString + "_cumulativeScoreChart.png";
			try {
				final JFreeChart chart = TriviaChartFactory.makeCumulativePointChart(trivia);
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved cumulative score chart to " + filename);
				filename = this.chartDirectory + "/latest_cumulativeScoreChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			filename = this.chartDirectory + "/" + roundString + "_teamComparisonChart.png";
			try {
				final JFreeChart chart = TriviaChartFactory.makeTeamComparisonChart(trivia);
				File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved team comparison chart to " + filename);
				filename = this.chartDirectory + "/latest_teamComparisonChart.png";
				file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, chart, chartWidth, chartHeight);
				TriviaServer.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save team comparison chart to file " + filename);
			}

			// Save place chart
			filename = this.chartDirectory + "/latest_placeChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makePlaceChart(trivia), chartWidth, chartHeight);
				TriviaServer.log("Saved place chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save place chart to file " + filename);
			}

			// Save score by round chart
			filename = this.chartDirectory + "/latest_scoreByRoundChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeScoreByRoundChart(trivia), chartWidth,
						chartHeight);
				TriviaServer.log("Saved score by round chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save score by round chart to file " + filename);
			}

			// Save cumulative score chart
			filename = this.chartDirectory + "/latest_cumulativeScoreChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeCumulativePointChart(trivia), chartWidth,
						chartHeight);
				TriviaServer.log("Saved cumulative score chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save cumulative score chart to file " + filename);
			}

			// Save team comparison chart
			filename = this.chartDirectory + "/latest_teamComparisonChart.png";
			try {
				final File file = new File(filename);
				ChartUtilities.saveChartAsPNG(file, TriviaChartFactory.makeTeamComparisonChart(trivia), chartWidth,
						chartHeight);
				TriviaServer.log("Saved team comparison chart to " + filename);
			} catch (final IOException exception) {
				TriviaServer.log("Couldn't save team comparison chart to file " + filename);
			}
		}
	}

}
