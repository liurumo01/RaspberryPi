package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.common.Main;

public class LoadBalancer {

	private static final Logger logger = LoggerFactory.getLogger(LoadBalancer.class);

	private ServerSocket server;
	private WorkerManager manager;
	private String policy;

	public void start() {
		try {
			init();
			handle();
		} catch (Exception e) {
			logger.error("Error occours while running load balancer", e);
		}

		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 读取配置文件，创建 worker 和端口监听
	 * 
	 * @throws IOException
	 */
	protected void init() {
		// 读取配置文件
		Properties props = new Properties();
		try {
			String fileName = "LoadBalancer.properties";
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
			logger.info("Read configuration file [" + fileName + "]");
		} catch (IOException e) {
			throw new RuntimeException("Failed to read configuration file", e);
		}
		
		//读取负载均衡策略
		this.policy = props.getProperty("worker.policy");
		
		
		// 读取并启动 WorkerMonitor 监听端口
		int managerPort;
		try {
			managerPort = Integer.parseInt(props.getProperty("manager.port"));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Wrong number format of [manager.port]", e);
		}
		try {
			manager = new WorkerManager(managerPort, policy);
		} catch (IOException e) {
			throw new RuntimeException("Failed to start WorkerManager", e);
		}

		// 读取并启动端口监听 ClientConnection
		int serverPort;
		try {
			serverPort = Integer.parseInt(props.getProperty("server.port"));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Wrong number format of [server.port]", e);
		}
		try {
			server = new ServerSocket(serverPort);
			logger.info("Start browser listening at port " + serverPort);
		} catch (IOException e) {
			throw new RuntimeException("Failed to start borwser listening", e);
		}

	}

	private void handle() {
		while (server != null) {
			Socket client = null;
			try {
				client = server.accept();
				logger.info("Receive a connection from [" + client + "]");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (client != null) {
				Handler handler = new Handler();
				handler.bind(client);
				handler.setWorkerManager(manager);
				Main.getThreadPool().submit(handler);
			}
		}
	}

}
