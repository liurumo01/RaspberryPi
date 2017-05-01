package space.snowwolf.pi.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TomcatConsoleReader implements Runnable {

	private boolean print;
	private BufferedReader reader;
	
	public TomcatConsoleReader(Process tomcat) {
		this(tomcat, false);
	}
	
	public TomcatConsoleReader(Process tomcat, boolean print) {
		this.print = print;
		reader = new BufferedReader(new InputStreamReader(tomcat.getErrorStream()));
	}
	
	@Override
	public void run() {
		String line = null;
		try {
			while (true) {
				line = reader.readLine();
				if(line == null || line.equals("Killed")) {
					break;
				}
				if(print) {
					System.out.println(line);					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setPrint(boolean print) {
		this.print = print;
	}

}
