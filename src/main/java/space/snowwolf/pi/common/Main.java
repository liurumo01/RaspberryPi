package space.snowwolf.pi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import space.snowwolf.pi.lb.LoadBalancer;
import space.snowwolf.pi.monitor.WorkerMonitor;

public class Main {
	
	private static final ExecutorService pool = Executors.newCachedThreadPool();

	public static void main(String[] args) {
		if(args.length == 0) {
			System.out.println("start parameter [-lb] or [-wm] needed");
			return;
		}
		
		if(args[0].equals("-lb")) {
			LoadBalancer lb = new LoadBalancer();
			lb.start();
		} else if(args[0].equals("-wm")) {
			WorkerMonitor wm = new WorkerMonitor();
			wm.start();
		}
		
	}
	
	@Test
	public void testLoadBalancer() {
		LoadBalancer lb = new LoadBalancer();
		lb.start();
	}
	
	@Test
	public void testWorkerMonitor() {
		WorkerMonitor wm = new WorkerMonitor();
		wm.start();
	}
	
	@Test
	public void testStartCatalina() throws IOException {
		String CATALINA_HOME = "C:\\长公主\\apache-tomcat-8.0.37";
//		String[] startParam = new String[] {"cd " + CATALINA_HOME + "/bin;catalina run" };
//		Process tomcat = Runtime.getRuntime().exec("CMD.exe /C cd", null, new File(CATALINA_HOME));
		Process tomcat = Runtime.getRuntime().exec(CATALINA_HOME + "\\bin\\catalina.bat run");
		print(tomcat.getErrorStream(), System.err);
		new Thread(() -> {
			try {
				print(tomcat.getInputStream(), System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public void print(InputStream in, PrintStream out) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while((line = reader.readLine()) != null) {
			out.println(line);
		}
	}
	
	public static ExecutorService getThreadPool() {
		return pool;
	}
	
}
