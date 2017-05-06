package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.Command;
import space.snowwolf.pi.bean.HttpPacket;
import space.snowwolf.pi.bean.TomcatStatus;
import space.snowwolf.pi.common.CommandExecution;
import space.snowwolf.pi.common.CommandExecutor;
import space.snowwolf.pi.common.SocketHandler;
import space.snowwolf.pi.utils.IOUtils;

public class Worker extends CommandExecutor {

	private static final Logger logger = LoggerFactory.getLogger(Worker.class);

	private WorkerManager manager;
	
	private String name;
	private String ip;
	private int port;

	private Socket socket;
	private TomcatStatus status;
	private int cpu;
	private int memory;

	public Worker(Socket monitor, WorkerManager manager) throws IOException {
		this.manager = manager;
		this.handler = new SocketHandler(monitor);
		this.ip = monitor.getInetAddress().getHostName();
	}

	@CommandExecution
	public void register(Map<String, String> params) {
		this.name = params.get("name");
		this.port = Integer.parseInt(params.get("port"));
		manager.addWorker(this);
		Command cmd = new Command("startTomcat");
		try {
			handler.send(cmd);
		} catch (IOException e) {
			logger.error("Failed to send command [" + cmd + "]", e);
		}
	}

	@CommandExecution
	public void heartBeat(Map<String, String> params) {
		this.status = TomcatStatus.valueOf(params.get("status"));
		this.cpu = Integer.parseInt(params.get("cpu"));
		this.memory = Integer.parseInt(params.get("mem"));
	}

	public HttpPacket handle(HttpPacket request) {
		HttpPacket response = null;
		try {
			socket = new Socket(ip, port);
			IOUtils.write(socket.getOutputStream(), false, request);
			response = IOUtils.read(socket.getInputStream());
		} catch (UnknownHostException e) {
			logger.error("Worker's ip or port is wrong", e);
		} catch (Exception e) {
			logger.error("Failed to read or write data", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return response;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public TomcatStatus getTomcatStatus() {
		return status;
	}

	@Override
	protected void init() {

	}

	public TomcatStatus getStatus() {
		return status;
	}

	public void setStatus(TomcatStatus status) {
		this.status = status;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

}
