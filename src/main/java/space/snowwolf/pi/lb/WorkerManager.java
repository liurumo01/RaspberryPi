package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.common.Main;

public class WorkerManager {
	
	private static Logger logger = LoggerFactory.getLogger(WorkerManager.class);
	
	private String policy;
	private List<Worker> workers;
	private Worker main;
	
	private int current;
	private int ratio;
	
	private ServerSocket manager;
	
	public WorkerManager(int port) throws IOException {
		workers = new ArrayList<>();
		manager = new ServerSocket(port);
		logger.info("Start worker manager listening at port " + port);
		//在新线程中接收 WorkerMonitor 的连接
		Main.getThreadPool().submit(() -> {
			Socket monitor = null;
			try {
				while((monitor = manager.accept()) != null) {
					Worker worker = new Worker(monitor);
					worker.start();
					workers.add(worker);
					logger.info("Receive a connection : " + monitor);
				}
			} catch (IOException e) {
				logger.error("Failed to listen worker monitor connection", e);
			}
		});
	}

	@Deprecated
	public Worker distribute() {
		if(workers .size() == 0) {
			return null;
		}
		Worker result = null;
		if(policy.equals("average")) {
			if(current == -1) {
				current = 0;
			}
			result = workers.get(current);
			current = (current + 1) % workers.size();
		} else if(policy.equals("main")) {
			if(main == null) {
				main = workers.get(0);
			}
			result = main;
		} else if(policy.equals("ratio")) {
			int start = current;
			while(ratio == 0) {
				current = (current + 1) % workers.size();
//				ratio = workers.get(current).getRatio();
				if(current == start + workers.size()) {
					break;
				}
			}
			if(current == start + workers.size()) {
				System.out.println("找不到可用 worker");
				return null;
			}
			result = workers.get(current);
			ratio--;
		}
		return result;
	}
	
}
