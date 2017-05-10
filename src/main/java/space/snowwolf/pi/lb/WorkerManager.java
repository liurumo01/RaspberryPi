package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.TomcatStatus;
import space.snowwolf.pi.common.Main;

public class WorkerManager {
	
	private static Logger logger = LoggerFactory.getLogger(WorkerManager.class);
	
	private String policy;
	private List<Worker> workers;
	private Worker main;
	
	private int current;
	private int maxCpu;
	private int maxMemory;
	
	private ServerSocket manager;
	
	public WorkerManager(int port, String policy) throws IOException {
		this.policy = policy;
		this.current = -1;
		workers = new ArrayList<>();
		manager = new ServerSocket(port);
		logger.info("Start worker manager listening at port " + port);
		//在新线程中接收 WorkerMonitor 的连接
		Main.getThreadPool().submit(() -> {
			Socket monitor = null;
			try {
				while((monitor = manager.accept()) != null) {
					Worker worker = new Worker(monitor, this);
					worker.start();
					workers.add(worker);
					logger.info("Receive a connection : " + monitor);
				}
			} catch (IOException e) {
				logger.error("Failed to listen worker monitor connection", e);
			}
		});
		maxCpu = 80;
		maxMemory = 80;
	}

	public Worker distribute() {
		if(workers .size() == 0) {
			return null;
		}
		Worker result = null;
		if(policy.equals("average")) {
			filterUnavaliable();
			if(current == -1) {
				return null;
			}
			result = workers.get(current);
			current = (current + 1) % workers.size();
		} else if(policy.equals("main")) {
			if(main == null) {
				current = 0;
				filterUnavaliable();
				if(current == -1) {
					return null;
				}
				main = workers.get(current);
			}
			result = main;
		}
		return result;
	}
	
	public void addWorker(Worker worker) {
		workers.add(worker);
	}

	private void filterUnavaliable() {
		if(current == -1) {
			current = 0;
		}
		boolean flag = false;
		int count = 0;
		do {
			Worker p = workers.get(current);
			if(p.getStatus() != TomcatStatus.RUNNING) {
				flag = true;
			} else if(p.getCpu() >= maxCpu) {
				flag = true;
			} else if(p.getMemory() >= maxMemory) {
				flag = true;
			} else {
				flag = false;
			}
			if(flag) {
				current = (current + 1) % workers.size();
				count++;
				if(count == workers.size()) {
					current = -1;
					break;
				}
			}
		} while(flag);
	}
	
	public void setMaxCpu(int maxCpu) {
		this.maxCpu = maxCpu;
	}
	
	public void setMaxMemory(int maxMemory) {
		this.maxMemory = maxMemory;
	}
	
}
