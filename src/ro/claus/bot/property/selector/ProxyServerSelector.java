package ro.claus.bot.property.selector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

public class ProxyServerSelector {

	private List<SocketAddress> proxyList = new ArrayList<SocketAddress>();
	private String fileName;
	private static ProxyServerSelector instance;

	private Logger log = Logger.getLogger(this.getClass().getName());

	private ProxyServerSelector(String fileName) {
		this.fileName = fileName;
		initialize();
	}

	private void initialize() {
		log.info("Reading proxy list...");
		if (!fileName.endsWith(".txt")) {
			throw new RuntimeException("Invalid file specified!");
		}
		File list = new File(fileName);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(list)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				try {
					String[] addr = line.split(":");
					if (addr != null && addr.length == 2) {
						String ip = addr[0];
						String port = addr[1];
						if (ip.trim().length() > 0 && port.trim().length() > 0) {
							proxyList.add(new InetSocketAddress(ip, Integer.valueOf(port)));
						}
					}
				} catch (Exception e) {
					// skip it
				}
			}

			log.info(proxyList.size() + " proxy servers available");
		} catch (Exception e) {
			log.error("Error initializing proxy list", e);
			throw new RuntimeException(e);
		}
	}

	public static ProxyServerSelector getInstance(String fileName) {
		if (instance == null) {
			instance = new ProxyServerSelector(fileName);
		}

		return instance;
	}

	public Proxy getRandomProxy() {
		Proxy proxy = null;

		if (proxyList.size() > 0) {
			SocketAddress socket = proxyList.get(new Random().nextInt(proxyList.size()));
			proxy = new Proxy(Proxy.Type.HTTP, socket);
		}

		return proxy;
	}
}
