package space.snowwolf.pi.lb;

import java.util.ArrayList;
import java.util.List;

public class WorkerManager {
	
	private String policy;
	private List<Worker> workers;
	private Worker main;
	
	private int current;
	private int ratio;
	
	public void init(String policy) {
		workers = new ArrayList<Worker>();
		this.policy = policy;
		current = -1;
		ratio = 0;
	}

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
				ratio = workers.get(current).getRatio();
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

	public void createWorker(String name, String ip, int port) {
		createWorker(name, ip, port, false, 0);
	}
	
	public void createWorker(String name, String ip, int port, boolean main) {
		createWorker(name, ip, port, main, 0);
	}

	public void createWorker(String name, String ip, int port, int ratio) {
		createWorker(name, ip, port, false, ratio);
	}
	
	private void createWorker(String name, String ip, int port, boolean main, int ratio) {
		Worker worker = new Worker(name, ip, port, main, ratio);
		workers.add(worker);
		if(main) {
			this.main = worker;
		}
	}
	
}
