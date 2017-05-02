package space.snowwolf.pi.common;

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
	
	public static ExecutorService getThreadPool() {
		return pool;
	}
	
}
