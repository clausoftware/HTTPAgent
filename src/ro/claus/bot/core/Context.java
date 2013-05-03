package ro.claus.bot.core;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ro.claus.bot.property.selector.DisplaySelector;
import ro.claus.bot.property.selector.ProxyServerSelector;
import ro.claus.bot.property.selector.UserAgentSelector;


public class Context {

	private String url;
	private int min;
	private int max;

	private ProxyServerSelector proxySelector;
	private UserAgentSelector browserSelector;
	private DisplaySelector displaySelector;

	private int successfull = 0;
	private int total = 0;

	public static boolean COMMAND_END = false; 
	private static Context instance;

	private Logger log = Logger.getLogger(this.getClass().getName());

	private Context() {
		try {
			initialize();
		} catch (Exception e) {
			log.error("Error reading settings ", e);
			throw new RuntimeException(e);
		}
	}

	public static Context getInstance() {
		if (instance == null) {
			instance = new Context();
		}

		return instance;
	}

	private void initialize() throws SAXException, IOException, ParserConfigurationException {
		String fileBrowser = null;
		String fileProxy = null;
		String fileDisplay = null;

		File file = new File("config/settings.xml");
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);

		doc.getDocumentElement().normalize();

		NodeList nodeUrl = doc.getElementsByTagName("webSite");
		NodeList nodeBrowsers = doc.getElementsByTagName("browsers");
		NodeList nodeProxy = doc.getElementsByTagName("proxyServers");
		NodeList nodeDisplay = doc.getElementsByTagName("displays");
		NodeList nodeInterval = doc.getElementsByTagName("accessInterval");

		if (nodeUrl != null) {
			url = nodeUrl.item(0).getTextContent().trim();
		}

		if (nodeBrowsers != null) {
			fileBrowser = nodeBrowsers.item(0).getTextContent().trim();
		}

		if (nodeProxy != null) {
			fileProxy = nodeProxy.item(0).getTextContent().trim();
		}

		if (nodeDisplay != null) {
			fileDisplay = nodeDisplay.item(0).getTextContent().trim();
		}

		if (nodeInterval != null) {
			Element elem = (Element) nodeInterval.item(0);

			String min = elem.getElementsByTagName("min").item(0).getTextContent().trim();
			String max = elem.getElementsByTagName("max").item(0).getTextContent().trim();

			if (min.length() > 0) {
				this.min = Integer.valueOf(min);
				this.max = Integer.valueOf(max);
			}
		}

		proxySelector = ProxyServerSelector.getInstance(fileProxy);
		browserSelector = UserAgentSelector.getInstance(fileBrowser);
		displaySelector = DisplaySelector.getInstance(fileDisplay);
	}

	public String getUrl() {
		return url;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public ProxyServerSelector getProxySelector() {
		return proxySelector;
	}

	public UserAgentSelector getBrowserSelector() {
		return browserSelector;
	}

	public DisplaySelector getDisplaySelector() {
		return displaySelector;
	}

	public synchronized void incrementSuccessfull() {
		successfull++;
	}

	public int getSuccessfull() {
		return successfull;
	}

	public int getTotal() {
		return ++total;
	}

	public static boolean isEnding() throws ProgramEndedException {
		if (COMMAND_END) {
			throw new ProgramEndedException();
		}
		return false;
	}
}
