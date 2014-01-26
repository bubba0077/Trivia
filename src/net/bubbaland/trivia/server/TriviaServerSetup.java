package net.bubbaland.trivia.server;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TriviaServerSetup {

	// File name holding the server settings
	final static private String	SETTINGS_FILENAME	= ".trivia-server-settings";

	private static String		SOURCE_URL			= "http://www.bubbaland.net/trivia";
	private static String[]		FILENAMES			= { "triviaClient.jar", "triviaServer.jar",
			"lib/jcommon-1.0.20.jar", "lib/jfreechart-1.0.16.jar", "lib/jfxrt.jar" };
	private static boolean		showGUI;

	private static final String	welcomeMsg			= "This program will download all of the files necessary to host the trivia server and configure the necessary settings.\n\n"
															+ "Before continuing, please make sure you have all of the following required components set up:\n"
															+ "1) An internet-facing html server for the webstart files\n"
															+ "2) Another available internet-facing port for the trivia server (default is 1099)";

	private static final String	requestHTML			= "Enter local html root directory. A folder named trivia will be created there which will serve the trivia files.";
	private static final String	defaultHTML			= "/var/www";

	private static final String	requestURL			= "Enter domain name where the jars and java webstart files will be hosted. Do not include a protocol.";
	private static final String	defaultURL			= "www.bubbaland.net";

	private static final String	requestPort			= "Enter port to use for server. The port must be accept connections from the internet.";
	private static final int	defaultPort			= 1099;

	public static void main(String args[]) {

		if (args.length > 0 && args[0].equals("noGUI")) {
			showGUI = false;
		} else {
			showGUI = true;
		}

		JFrame frame = null;
		JTextArea textArea = null;
		InputStreamReader istream = new InputStreamReader(System.in);
		BufferedReader bufRead = new BufferedReader(istream);

		if (showGUI) {
			frame = new JFrame("Trivia Server Setup");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			textArea = new JTextArea();
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setAutoscrolls(true);
			final DefaultCaret caret = (DefaultCaret) textArea.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			JScrollPane pane = new JScrollPane(textArea);
			frame.getContentPane().add(pane, BorderLayout.CENTER);
			frame.setSize(400, 200);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			JOptionPane.showMessageDialog(frame, welcomeMsg);
		} else {
			System.out.println(welcomeMsg);
		}

		/*
		 * Ask for local html root directory
		 */
		String htmlDirName = null;
		while (htmlDirName == null) {
			if (showGUI) {
				textArea.append("Requesting location of html root directory\n");
				htmlDirName = (String) JOptionPane.showInputDialog(frame, requestHTML, "Trivia Server Setup",
						JOptionPane.PLAIN_MESSAGE, null, null, defaultHTML);
			} else {
				System.out.println(requestHTML + " [" + defaultHTML + "]");
				try {
					htmlDirName = bufRead.readLine();
				} catch (IOException exception) {
					System.out.println("Error reading input stream, exiting");
					System.exit(1);
				}
				if (htmlDirName.equals("")) {
					System.out.println("Using default value: " + defaultHTML);
					htmlDirName = defaultHTML;
				}
			}
			if (htmlDirName == null) {
				if (showGUI) {
					textArea.append("Setup cancelled, exiting!\n");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException exception) {
					}
					System.exit(0);
				}
			}
			if (!new File(htmlDirName).isDirectory()) {
				htmlDirName = null;
				if (showGUI) {
					JOptionPane.showMessageDialog(frame, "Directory does not exist, please try again.");
					textArea.append("Invalid directory specified, repeating request.\n");
				} else {
					System.out.println("Directory does not exist, please try again.");
				}
			}
		}

		/*
		 * Ask for the server URL
		 */
		String serverURL = null;
		if (showGUI) {
			textArea.append("Requesting server URL\n");
			serverURL = (String) JOptionPane.showInputDialog(frame, requestURL, "Trivia Server Setup",
					JOptionPane.PLAIN_MESSAGE, null, null, defaultURL);
		} else {
			System.out.println(requestURL + " [" + defaultURL + "]");
			try {
				serverURL = bufRead.readLine();
			} catch (IOException exception) {
				System.out.println("Error reading input stream, exiting");
				System.exit(1);
			}
			if (serverURL.equals("")) {
				System.out.println("Using default: " + defaultURL);
				serverURL = defaultURL;
			}
		}
		if (serverURL == null) {
			if (showGUI) {
				textArea.append("Setup cancelled, exiting!\n");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException exception) {
				}
				System.exit(0);
			}
		}

		/*
		 * Ask for the server port
		 */
		int port = 0;
		while (port == 0) {
			try {
				if (showGUI) {
					port = Integer.parseInt((String) JOptionPane.showInputDialog(frame, requestPort,
							"Trivia Server Setup", JOptionPane.PLAIN_MESSAGE, null, null, defaultPort));
				} else {
					System.out.println(requestPort + " [" + defaultPort + "]");
					final String portString = bufRead.readLine();
					if (portString.equals("")) {
						port = defaultPort;
					} else {
						port = Integer.parseInt(portString);
					}
				}
			} catch (NumberFormatException exception) {
				port = 0;
			} catch (NullPointerException exception) {
				if (showGUI) {
					textArea.append("Setup cancelled, exiting!\n");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException exception1) {
					}
					System.exit(0);
				}
			} catch (IOException exception) {
				System.out.println("Error reading input stream, exiting");
				System.exit(1);
			}
			if (port < 1 || port > 65535) {
				port = 0;
				if (showGUI) {
					JOptionPane.showMessageDialog(frame, "Port must be an integer between 1 and 65535");
					textArea.append("Invalid port number input, trying again.\n");
				} else {
					System.out.println("Port must be an integer between 1 and 65535");
				}
			}
		}

		/*
		 * Create trivia directories
		 */
		final String triviaDirPath = htmlDirName + "/trivia";
		final File triviaDir = new File(triviaDirPath);
		final File savesDir = new File(triviaDirPath + "/saves");
		final File chartDir = new File(triviaDirPath + "/charts");
		final File libDir = new File(triviaDirPath + "/lib");

		if (!triviaDir.isDirectory()) {
			if (showGUI) {
				textArea.append("Creating " + triviaDirPath + "\n");
			} else {
				System.out.println("Creating " + triviaDirPath + "\n");
			}
			triviaDir.mkdir();
		}
		if (!savesDir.isDirectory()) {
			if (showGUI) {
				textArea.append("Creating " + triviaDirPath + "/saves\n");
			} else {
				System.out.println("Creating " + triviaDirPath + "/saves\n");
			}
			savesDir.mkdir();
		}
		if (!chartDir.isDirectory()) {
			if (showGUI) {
				textArea.append("Creating " + triviaDirPath + "/charts\n");
			} else {
				System.out.println("Creating " + triviaDirPath + "/charts\n");
			}
			chartDir.mkdir();
		}
		if (!libDir.isDirectory()) {
			if (showGUI) {
				textArea.append("Creating " + triviaDirPath + "/lib\n");
			} else {
				System.out.println("Creating " + triviaDirPath + "/lib\n");
			}
			libDir.mkdir();
		}


		/*
		 * Download needed jar files
		 */
		for (String filename : FILENAMES) {
			final File file = new File(triviaDirPath + "/" + filename);
			if (!file.exists()) {
				if (showGUI) {
					textArea.append("Downloading " + filename + "\n");
				} else {
					System.out.println("Downloading " + filename + "\n");
				}
				downloadFile(filename, triviaDirPath, textArea);
			}
		}

		/*
		 * Create webstart files
		 */
		if (showGUI) {
			textArea.append("Creating triviaClient.jnlp\n");
		} else {
			System.out.println("Creating triviaClient.jnlp\n");
		}
		createJNLP(triviaDirPath + "/triviaClient.jnlp", serverURL, port, textArea);
		if (showGUI) {
			textArea.append("Creating triviaClient_IRC.jnlp\n");
		} else {
			System.out.println("Creating triviaClient_IRC.jnlp\n");
		}
		createJNLP(triviaDirPath + "/triviaClient_IRC.jnlp", serverURL, port, true, textArea);

		/*
		 * Create server settings file
		 */
		if (showGUI) {
			textArea.append("Creating " + System.getProperty("user.home") + "/" + SETTINGS_FILENAME + "\n\n");
		} else {
			System.out.println("Creating " + System.getProperty("user.home") + "/" + SETTINGS_FILENAME + "\n\n");
		}
		createServerSettings(triviaDirPath, serverURL, port);

		if (showGUI) {
			textArea.append("Your trivia server is now set up. You can run it with the following command:\n");
			textArea.append(">java -jar " + triviaDirPath + "/triviaServer.jar\n\n");
			textArea.append("Users can access the client through the following links:\n");
			textArea.append("http://" + serverURL + "/trivia/triviaClient.jnlp\n");
			textArea.append("http://" + serverURL + "/trivia/triviaClient_IRC.jnlp");
		} else {
			System.out.println("Your trivia server is now set up. You can run it with the following command:\n");
			System.out.println(">java -jar " + triviaDirPath + "/triviaServer.jar\n\n");
			System.out.println("Users can access the client through the following links:\n");
			System.out.println("http://" + serverURL + "/trivia/triviaClient.jnlp\n");
			System.out.println("http://" + serverURL + "/trivia/triviaClient_IRC.jnlp");
		}
	}

	private static void downloadFile(String filename, String dir, JTextArea textArea) {
		FileOutputStream outstream = null;
		try {
			ReadableByteChannel in = Channels.newChannel(new URL(SOURCE_URL + "/" + filename).openStream());
			outstream = new FileOutputStream(dir + "/" + filename);
			FileChannel out = outstream.getChannel();
			out.transferFrom(in, 0, Long.MAX_VALUE);
		} catch (IOException exception) {
			textArea.append("Couldn't download " + filename + "!");
			textArea.append("Exiting...");
			System.exit(0);
		} finally {
			if (outstream != null) {
				try {
					outstream.close();
				} catch (IOException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
			}
		}
	}

	private static void createJNLP(String filename, String serverURL, int port, JTextArea textArea) {
		createJNLP(filename, serverURL, port, false, textArea);
	}

	private static void createJNLP(String filename, String serverURL, int port, boolean isIRC, JTextArea textArea) {
		try {
			final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.newDocument();

			final Element jnlpElem = doc.createElement("jnlp");
			doc.appendChild(jnlpElem);

			Attr attribute = doc.createAttribute("spec");
			attribute.setValue("1.0+");
			jnlpElem.setAttributeNode(attribute);

			attribute = doc.createAttribute("codebase");
			attribute.setValue("http://" + serverURL + "/trivia/");
			jnlpElem.setAttributeNode(attribute);

			attribute = doc.createAttribute("href");
			attribute.setValue("triviaClient.jnlp");
			jnlpElem.setAttributeNode(attribute);

			Element infElement = doc.createElement("information");
			jnlpElem.appendChild(infElement);

			Element element = doc.createElement("title");
			infElement.appendChild(element);
			element.appendChild(doc.createTextNode("Trivia Client"));

			element = doc.createElement("vendor");
			infElement.appendChild(element);
			element.appendChild(doc.createTextNode("Walter Kolczynski"));

			element = doc.createElement("homepage");
			infElement.appendChild(element);
			attribute = doc.createAttribute("href");
			attribute.setValue("http://www.kneedeepintheses.org");
			element.setAttributeNode(attribute);
			infElement.appendChild(element);

			Element secElement = doc.createElement("security");
			jnlpElem.appendChild(secElement);

			element = doc.createElement("all-permissions");
			secElement.appendChild(element);

			Element resElement = doc.createElement("resources");
			jnlpElem.appendChild(resElement);

			element = doc.createElement("j2se");
			resElement.appendChild(element);

			attribute = doc.createAttribute("version");
			attribute.setValue("1.0+");
			element.setAttributeNode(attribute);
			attribute = doc.createAttribute("href");
			attribute.setValue("http://java.sun.com/products/autodl/j2se");
			element.setAttributeNode(attribute);

			element = doc.createElement("jar");
			resElement.appendChild(element);
			attribute = doc.createAttribute("href");
			attribute.setValue("triviaClient.jar");
			element.setAttributeNode(attribute);
			attribute = doc.createAttribute("main");
			attribute.setValue("true");
			element.setAttributeNode(attribute);

			element = doc.createElement("jar");
			resElement.appendChild(element);
			attribute = doc.createAttribute("href");
			attribute.setValue("lib/jcommon-1.0.20.jar");
			element.setAttributeNode(attribute);

			element = doc.createElement("jar");
			resElement.appendChild(element);
			attribute = doc.createAttribute("href");
			attribute.setValue("lib/jfreechart-1.0.16.jar");
			element.setAttributeNode(attribute);

			Element descElement = doc.createElement("application-desc");
			jnlpElem.appendChild(descElement);

			attribute = doc.createAttribute("main-class");
			attribute.setValue("net.bubbaland.trivia.client.TriviaClient");
			descElement.setAttributeNode(attribute);

			element = doc.createElement("argument");
			element.appendChild(doc.createTextNode("rmi://" + serverURL + ":" + port + "/TriviaInterface"));
			descElement.appendChild(element);

			if (isIRC) {
				element = doc.createElement("jar");
				resElement.appendChild(element);
				attribute = doc.createAttribute("href");
				attribute.setValue("lib/jfxrt.jar");
				element.setAttributeNode(attribute);

				element = doc.createElement("argument");
				element.appendChild(doc.createTextNode("useFX"));
				descElement.appendChild(element);
			}

			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();
			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(new File(filename));
			transformer.transform(source, result);

		} catch (ParserConfigurationException | TransformerException exception) {
			textArea.append("Couldn't create settings file!");
			textArea.append("Exiting...");
			System.exit(0);
		}
	}

	private static void createServerSettings(String triviaDirPath, String serverURL, int port) {

		final Properties properties = new Properties();

		/**
		 * Default properties
		 */
		final InputStream defaults = TriviaServerSetup.class.getResourceAsStream(SETTINGS_FILENAME);
		try {
			properties.load(defaults);
		} catch (final IOException e) {
			System.out.println("Couldn't load default properties file, aborting!");
			System.exit(-1);
		}

		properties.setProperty("ServerURL", serverURL);
		properties.setProperty("Server.Port", port + "");
		properties.setProperty("SaveDir", triviaDirPath + "/saves");
		properties.setProperty("ChartDir", triviaDirPath + "/charts");


		final File file = new File(System.getProperty("user.home") + "/" + SETTINGS_FILENAME);
		try {
			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
			properties.store(outfileBuffer, "TriviaServer");
			outfileBuffer.close();
		} catch (final IOException e) {
			System.out.println("Error saving properties.");
		}
	}


}
