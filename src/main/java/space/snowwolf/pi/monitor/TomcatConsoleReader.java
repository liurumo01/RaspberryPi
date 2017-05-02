package space.snowwolf.pi.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.TomcatStatus;

public class TomcatConsoleReader implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(TomcatConsoleReader.class);

	private BufferedReader reader;
	private WorkerMonitor monitor;
	
	public TomcatConsoleReader(WorkerMonitor monitor) {
		this.monitor = monitor;
		reader = new BufferedReader(new InputStreamReader(monitor.getTomcat().getErrorStream()));
	}
	
	@Override
	public void run() {
		String line = null;
		try {
			while (true) {
				line = reader.readLine();
				if(line == null || line.equals("Killed")) {
					break;
				} else if(line.contains("server start up in")) {
					monitor.setTomcatStatus(TomcatStatus.RUNNING);
				}
				logger.trace(line);
			}
			monitor.setTomcatStatus(TomcatStatus.STOPPED);
		} catch (IOException e) {
			logger.error("Failed to read tomcat error stream", e);
		}
	}

}
