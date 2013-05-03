package ro.claus.bot.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Visitor {

	private WebClient client;
	private String url;
	@SuppressWarnings("unused")
	private String name;

	private Logger log = Logger.getLogger(this.getClass().getName());

	public Visitor(WebClient client, String url, String name) {
		this.client = client;
		this.url = url;
		this.name = name;
	}


	public void start() throws FailingHttpStatusCodeException, MalformedURLException, IOException, ProgramEndedException {


		HtmlPage page = client.getPage(url);
		String finalUrl = page.getUrl().getHost();

		Context.isEnding();

		log.info("Successfully connected to " + finalUrl);
		log.info("Now let's navigate through the page");

		// if we reached here it means that we connected successfully
		Context.getInstance().incrementSuccessfull();

		List<HtmlAnchor> anchors = page.getAnchors();
		List<HtmlAnchor> finalAnchors = new ArrayList<HtmlAnchor>();

		Context.isEnding();

		// identify all links that redirect to the same page
		if (anchors != null && anchors.size() > 0) {
			finalAnchors = getLinksRedirectingToMe(finalUrl, anchors);

			// click on a few links
			int clicked = 1;
			int attempts = 0;
			while (clicked < 3 && attempts < 5) {
				try {
					attempts++;
					int rnd1 = new Random().nextInt(finalAnchors.size());
					HtmlAnchor link = finalAnchors.get(rnd1);
					log.debug("Clicking on " + link.getHrefAttribute());
					HtmlPage clickedPage = (HtmlPage) link.openLinkInNewWindow();
					log.info("Successfully clicked on " + link.getHrefAttribute());

					// on the last click we also go deeper into the result page
					if (clicked == 2) {
						Context.isEnding();

						log.debug("Going deep into the page from " + link.getHrefAttribute());
						List<HtmlAnchor> links = getLinksRedirectingToMe(finalUrl, clickedPage.getAnchors());
						links.get(new Random().nextInt(links.size())).click();
						log.debug("Clicked on some other link");
					}
					clicked ++;

				} catch (Exception e) {
					log.error("Error accessing the link");
				}

				Context.isEnding();
				// sleep a little before accessing another link
				int sleep = 1 + new Random().nextInt(10);
				log.info("Sleeping for " + sleep + " seconds");
				try {
					Thread.sleep(sleep * 1000);
				} catch (InterruptedException e) {}
			}
		}

	}


	private List<HtmlAnchor> getLinksRedirectingToMe(String finalUrl, List<HtmlAnchor> anchors) {
		List<HtmlAnchor> finalAnchors = new ArrayList<HtmlAnchor>();
		for (HtmlAnchor a : anchors) {
			String href = a.getAttribute("href");
			if (href.startsWith("http://") && href.trim().contains(finalUrl) ||
					href.startsWith("/")) {
				finalAnchors.add(a);
			}
		}

		return finalAnchors;
	}

}
