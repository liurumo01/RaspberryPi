package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import space.snowwolf.pi.common.ErrorCode;

public class Main {

	private static ServerSocket server;
	private static WorkerManager manager;
	private static ExecutorService pool;

	public static void main(String[] args) {
		ErrorCode result = init();
		switch (result) {
			case ERROR_READ_CONFIGURATION_FILE:
				System.out.println("读取配置文件失败");
				break;
			case ERROR_READ_SERVER_PORT:
				System.out.println("server.port 有误");
				break;
			case ERROR_PORT_IN_USE:
				System.out.println("端口被占用");
				break;
			case ERROR_READ_THREAD_POOL_SIZE:
				System.out.println("thread.pool.size 有误");
				break;
			case SUCCESS:
				handle();
				break;
			default:
				System.out.println("未知错误");
		}
		
		if(server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(pool != null) {
			pool.shutdown();
		}
	}

	/**
	 * 读取配置文件，创建 worker 和端口监听
	 */
	private static ErrorCode init() {
		// 读取配置文件
		Properties props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("LoadBalancer.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			return ErrorCode.ERROR_READ_CONFIGURATION_FILE;
		}

		// 读取负载均衡策略
		String policy = props.getProperty("worker.policy");
		if (policy == null || policy.equals("")) {
			policy = "average";
		}
		manager = new WorkerManager();
		manager.init(policy);

		String main = props.getProperty("worker.main");

		String[] names = props.getProperty("worker.name").split(",");
		for (String name : names) {
			String ip = props.getProperty("worker." + name + ".ip");
			String portString = props.getProperty("worker." + name + ".port");
			int port;
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println("worker." + name + ".port 有误");
				continue;
			}
			if (policy.equals("main")) {
				manager.createWorker(name, ip, port, name.equals(main));
			} else if (policy.equals("ratio")) {
				String ratioString = props.getProperty("worker." + name + ".ratio");
				int ratio;
				try {
					ratio = Integer.parseInt(ratioString);
				} catch (NumberFormatException e) {
					e.printStackTrace();
					System.out.println("worker." + name + ".ratio 有误");
					continue;
				}
				manager.createWorker(name, ip, port, ratio);
			} else {
				manager.createWorker(name, ip, port);
			}
		}

		//创建线程池
		pool = Executors.newCachedThreadPool();
		
		// 建立端口监听
		try {
			String serverPortString = props.getProperty("server.port");
			int serverPort = Integer.parseInt(serverPortString);
			server = new ServerSocket(serverPort);
			System.out.println("Load Balancer started at port " + serverPort + ".");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return ErrorCode.ERROR_READ_SERVER_PORT;
		} catch (IOException e) {
			e.printStackTrace();
			return ErrorCode.ERROR_PORT_IN_USE;
		}
		
		return ErrorCode.SUCCESS;
	}

	private static void handle() {
		while (server != null) {
			Socket client = null;
			try {
				client = server.accept();
//				System.out.println("Receive a connection from [" + client.getInetAddress().getHostAddress() + ":"
//						+ client.getPort() + "].");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (client != null) {
				Handler handler = new Handler();
				handler.bind(client);
				handler.setWorkerManager(manager);
				pool.execute(handler);
			}
		}
	}

}
