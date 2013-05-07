package ro.claus.bot.core;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.dom.NamedNodeMap;

import ro.claus.bot.property.selector.DisplaySelector.Display;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HTMLParserListener;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;


public class Main {

	private static Logger log = Logger.getLogger(Main.class);
	private static Context context;

	public static void main(String[] args) {
		PropertyConfigurator.configure("config/log4j.properties");

		log.info("Initializing console input...");
		ConsoleInput console = new ConsoleInput();
		console.start();

		context = Context.getInstance();

		String myUrl = context.getUrl();

		connectHTMLUnit(myUrl);

		log.info("Successfully clicked the banner " + context.getSuccessfull() + " times");
		log.info("--------- THE END --------\n");

	}

	private static void connectHTMLUnit(String myUrl) {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		WebClient homeClient = null;
		try {
			log.info("Connecting to " + myUrl + " to gather information");

			homeClient = new WebClient(BrowserVersion.CHROME_16);
			setErrorHandlers(homeClient);

			HtmlPage page = homeClient.getPage(myUrl);

			Context.isEnding();

			// the footer is in a frame and the banner is the last frame
			List<FrameWindow> frames = page.getFrames();
			if (frames == null || (frames != null && frames.size() == 0)) {
				log.error("Could not find the footer in the page");
				throw new RuntimeException("Could not find the footer in the page");
			}

			Context.isEnding();

			FrameWindow banner = frames.get(frames.size() - 1);
			HtmlPage enclosedPage = (HtmlPage) banner.getEnclosedPage();
			DomNodeList<DomElement> divs = enclosedPage.getElementsByTagName("div");

			Context.isEnding();

			List<String> finalLinks = new ArrayList<String>();
			// the links are in divs with class="text"
			// <div class="text"><a href="THE_LINK"></div>
			if (divs != null && divs.getLength() > 0) {
				for (DomElement d : divs) {
					if (d.getAttribute("class").equalsIgnoreCase("text")) {
						DomNode firstChild = d.getFirstElementChild();
						NamedNodeMap attributes = firstChild.getAttributes();
						String theLink = attributes.getNamedItem("href").getNodeValue();

						finalLinks.add(theLink);
					}
				}
			}

			Context.isEnding();

			if (finalLinks.size() == 0) {
				log.error("Could not find any link in the footer");
				throw new RuntimeException("Could not find any link in the footer");
			}

			log.info("Found " + finalLinks.size() + " links in the footer");

			while (!Context.COMMAND_END) {
				WebClient client = null;
				try {
					InetSocketAddress addr = (InetSocketAddress) context.getProxySelector().getRandomProxy().address();
					Display display = context.getDisplaySelector().getRandomDisplay();
					BrowserVersion browser = context.getBrowserSelector().getRandomBrowser();

					client = new WebClient(browser, addr.getHostString(), addr.getPort());
					client.addRequestHeader("UA-pixels:", display.pixels);
					client.addRequestHeader("UA-windowpixels:", display.windowPixels);
					client.addRequestHeader("UA-resolution:", display.resolution);
					client.addRequestHeader("Referer:", myUrl);

					setErrorHandlers(client);

					String threadName = "Visitor-" + context.getTotal();

					// randomly pick how many links to open
					int loops = 1 + new Random().nextInt(3);
					for (int i = 0; i <= loops; i ++) {
						String theLink = finalLinks.get(new Random().nextInt(finalLinks.size()));

						Context.isEnding();

						log.info(threadName + " attempting to connect to " + theLink + " through proxy server " + addr.getHostString() + " using " + 
								"\n\t- Browser: " + browser.getUserAgent() + 
								"\n\t- Display Pixels: " + display.pixels + 
								"\n\t- Display Window Pixels: " + display.windowPixels + 
								"\n\t- Display resolution: " + display.resolution + "dpi");

						try {
							// now we visit the page
							Visitor visitor = new Visitor(client, theLink, threadName);
							visitor.start();

						} catch (ProgramEndedException ex) {
							// do nothing
						} catch (Exception e) {
							log.error(threadName + " could not redirect to " + theLink, e);
						} 
					}

					Context.isEnding();

					// sleep before connecting again
					long sleep = (context.getMin() + (new Random().nextInt(context.getMax() - context.getMin())));
					log.info("Sleeping for " + sleep + " seconds before continuing...");
					try {
						Thread.sleep(sleep * 1000);
					} catch (InterruptedException e) {}

				} catch (ProgramEndedException ex) {
					// do nothing
				} catch (Exception e) {
					log.error("Something bad happened ", e);
				} finally {
					if (client != null) {
						client.closeAllWindows();
						client = null;
					}
				}
			}
		} catch (ProgramEndedException ex) {
			// do nothing
		} catch (Exception e) {
			log.error("OOOOPSSS! Something bad happened ", e);
			throw new RuntimeException("OOOOPSSS! Something bad happened ", e);
		} finally {
			if (homeClient != null) {
				homeClient.closeAllWindows();
				homeClient = null;
			}
		}
	}

	private static void setErrorHandlers(WebClient client) {
		client.getOptions().setTimeout(30000);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setPrintContentOnFailingStatusCode(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);

		client.setJavaScriptErrorListener(new JavaScriptErrorListener() {

			@Override
			public void timeoutError(HtmlPage arg0, long arg1, long arg2) {
			}

			@Override
			public void scriptException(HtmlPage arg0, ScriptException arg1) {
			}

			@Override
			public void malformedScriptURL(HtmlPage arg0, String arg1, MalformedURLException arg2) {
			}

			@Override
			public void loadScriptError(HtmlPage arg0, URL arg1, Exception arg2) {
			}
		});

		client.setCssErrorHandler(new ErrorHandler() {

			@Override
			public void warning(CSSParseException arg0) throws CSSException {
			}

			@Override
			public void fatalError(CSSParseException arg0) throws CSSException {
			}

			@Override
			public void error(CSSParseException arg0) throws CSSException {
			}
		});

		client.setIncorrectnessListener(new IncorrectnessListener() {

			@Override
			public void notify(String arg0, Object arg1) {
			}
		});

		client.setHTMLParserListener(new HTMLParserListener() {

			@Override
			public void warning(String arg0, URL arg1, String arg2, int arg3, int arg4, String arg5) {
			}

			@Override
			public void error(String arg0, URL arg1, String arg2, int arg3, int arg4, String arg5) {
			}
		});
	}

}
