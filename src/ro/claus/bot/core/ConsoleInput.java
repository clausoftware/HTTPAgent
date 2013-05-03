package ro.claus.bot.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


public class ConsoleInput extends Thread {

	private Logger log = Logger.getLogger(this.getClass().getName());

	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			if (reader.readLine().equals("q")) {
				log.warn("Ending the application...Please wait!");
				Context.COMMAND_END = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
