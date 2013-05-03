package ro.claus.bot.property.selector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gargoylesoftware.htmlunit.BrowserVersion;

public class UserAgentSelector {

	private Map<String, List<BrowserVersion>> agentsList = new HashMap<String, List<BrowserVersion>>();
	private String fileName;
	private static UserAgentSelector instance;

	private Logger log = Logger.getLogger(this.getClass().getName());

	public static final String BROWSER_CHROME = "chrome";
	public static final String BROWSER_FIREFOX = "firefox";
	public static final String BROWSER_IE = "ie";

	// public static final String BROWSER_OPERA = "opera";

	private UserAgentSelector(String fileName) {
		this.fileName = fileName;
		initialize();
	}

	private void initialize() {
		log.info("Reading user agents list...");
		if (!fileName.endsWith(".xml")) {
			throw new RuntimeException("Invalid file specified");
		}
		File file = new File(fileName);
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			doc.getDocumentElement().normalize();

			NodeList types = (NodeList) doc.getElementsByTagName("type");

			int total = 0;

			if (types != null && types.getLength() > 0) {
				for (int i = 0; i < types.getLength(); i++) {
					// this should be the name of the browser (Chrome, IE, Firefox, Opera, etc)
					Node type = types.item(i);

					if (type.getNodeType() == Node.ELEMENT_NODE) {
						Element elemType = (Element) type;

						List<BrowserVersion> browserList = new ArrayList<BrowserVersion>();

						String id = elemType.getAttribute("id");
						if (id != null && id.trim().length() > 0) {
							NodeList browsers = elemType.getElementsByTagName("browser");

							// here we should have a list of browser settings
							if (browsers != null && browsers.getLength() > 0) {
								for (int j = 0; j < browsers.getLength(); j++) {
									Node browser = browsers.item(j);

									if (browser.getNodeType() == Node.ELEMENT_NODE) {
										Element elemBr = (Element) browser;

										String appName = getText(elemBr.getElementsByTagName("applicationName"));
										String appVers = getText(elemBr.getElementsByTagName("applicationVersion"));
										String userAgent = getText(elemBr.getElementsByTagName("userAgent"));
										String versNum = getText(elemBr.getElementsByTagName("versionNumeric"));
										String nickname = getText(elemBr.getElementsByTagName("nickname"));

										if (appName != null && appVers != null && userAgent != null && versNum != null && nickname != null) {
											browserList.add(new BrowserVersion(appName, appVers, userAgent, Float.parseFloat(versNum)));
											++total;
										}
									}

								}
							}
						}

						agentsList.put(id, browserList);
					}
				}
			}

			log.info(total + " user agents found");
		} catch (Exception e) {
			log.error("Unable to initialize the browser list", e);
			throw new RuntimeException(e);
		}

	}

	private String getText(NodeList node) {
		if (node != null && node.getLength() == 1) {
			return node.item(0).getTextContent();
		}
		return null;
	}

	public static UserAgentSelector getInstance(String fileName) {
		if (instance == null) {
			instance = new UserAgentSelector(fileName);
		}

		return instance;
	}

	/**
	 * 
	 * @param browser
	 * @return a list of browser versions for the specified browser
	 */
	public List<BrowserVersion> getUserAgentVersions(String browser) {

		return agentsList.get(browser);
	}

	/**
	 * This returns a random browser version from the list of available user agents.
	 * 
	 * @return version of the browser
	 */
	public BrowserVersion getRandomBrowser() {
		List<BrowserVersion> list = new ArrayList<BrowserVersion>();
		// build a list with all browser versions
		for (Entry<String, List<BrowserVersion>> entry : agentsList.entrySet()) {
			list.addAll(entry.getValue());
		}

		return list.get(new Random().nextInt(list.size()));
	}
}
