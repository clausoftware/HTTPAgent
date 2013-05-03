package ro.claus.bot.property.selector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DisplaySelector {

	private List<Display> displayList = new ArrayList<Display>();
	private String fileName;
	private static DisplaySelector instance;

	private Logger log = Logger.getLogger(this.getClass().getName());

	private DisplaySelector(String fileName) {
		this.fileName = fileName;
		initialize();
	}

	private void initialize() {
		log.info("Reading display list...");
		if (!fileName.endsWith(".xml")) {
			throw new RuntimeException("Invalid file specified");
		}
		File file = new File(fileName);
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			doc.getDocumentElement().normalize();

			NodeList versions = (NodeList) doc.getElementsByTagName("version");

			if (versions != null && versions.getLength() > 0) {
				for (int i = 0; i < versions.getLength(); i++) {
					Node version = versions.item(i);

					if (version.getNodeType() == Node.ELEMENT_NODE) {
						Element elemVer = (Element) version;

						String pixels = getText(elemVer.getElementsByTagName("pixels"));
						String windowPixels = getText(elemVer.getElementsByTagName("windowPixels"));
						String resolution = getText(elemVer.getElementsByTagName("resolution"));

						if (pixels != null && windowPixels != null && resolution != null) {
							displayList.add(new Display(pixels, windowPixels, resolution));
						}
					}
				}
			}

			log.info(displayList.size() + " displays initialized...");
		} catch (Exception e) {
			log.error("Unable to initialize the display list", e);
			throw new RuntimeException(e);
		}
	}

	public static DisplaySelector getInstance(String fileName) {
		if (instance == null) {
			instance = new DisplaySelector(fileName);
		}

		return instance;
	}

	private String getText(NodeList node) {
		if (node != null && node.getLength() == 1) {
			return node.item(0).getTextContent();
		}
		return null;
	}

	public Display getRandomDisplay() {
		Display display = new Display("", "", "");

		if (displayList.size() > 0) {
			display = displayList.get(new Random().nextInt(displayList.size()));
		}

		return display;
	}

	/**
	 * Store the display properties 
	 */
	public class Display {

		public String pixels;
		public String windowPixels;
		public String resolution;

		public Display(String pixels, String windowPixels, String resolution) {
			this.pixels = pixels;
			this.windowPixels = windowPixels;
			this.resolution = resolution;
		}
	}
}
