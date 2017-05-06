package space.snowwolf.pi.monitor;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.Command;
import space.snowwolf.pi.bean.TomcatStatus;
import space.snowwolf.pi.common.CommandExecution;
import space.snowwolf.pi.common.CommandExecutor;
import space.snowwolf.pi.common.Main;
import space.snowwolf.pi.common.SocketHandler;
import space.snowwolf.pi.utils.OSUtils;
import space.snowwolf.pi.utils.StringUtils;

public class WorkerMonitor extends CommandExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(WorkerMonitor.class);

	private String name;
	private String CATALINA_HOME;
	private int port;
	private int peroid;
	
	private Process tomcat;
	private TomcatStatus status;
	
	@CommandExecution
	public void startTomcat(Map<String, String> params) throws IOException {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("win")) {
			tomcat = Runtime.getRuntime().exec(CATALINA_HOME + "\\bin\\catalina.bat run");
		} else if(osName.contains("linux")) {
			String[] startParam = { "/bin/sh", "-c", "cd " + CATALINA_HOME + "/bin;./catalina.sh run" };
			tomcat = Runtime.getRuntime().exec(startParam);
		}
		
		if(tomcat != null) {
			status = TomcatStatus.STARTING;
			Main.getThreadPool().submit(new TomcatConsoleReader(this));
		}
	}
	
	@Override
	public void start() {
		try {
			Command cmd = new Command("register");
			cmd.addParameter("name", name);
			cmd.addParameter("port", String.valueOf(port));
			handler.send(cmd);
			
			Main.getThreadPool().submit(() -> {
				while(running) {
					Command c = new Command("heartBeat");
					c.addParameter("status", status.toString());
					c.addParameter("cpu", String.valueOf(OSUtils.cpuUsage()));
					c.addParameter("mem", String.valueOf(OSUtils.memoryUsage()));
					try {
						handler.send(c);
					} catch (IOException e) {
						logger.error("Failed to send command [" + c + "]", e);
					}
					try {
						Thread.sleep(peroid * 1000);
					} catch (InterruptedException e) {
						logger.error("Thread sleep is interrupted", e);
					}
				}
			});
			
			super.start();

			//2.启动 tomcat，并在子线程中处理输出内容
//			Process tomcat = Runtime.getRuntime().exec(
//					new String[] { "/bin/sh", "-c", "cd /home/snowwolf/apache-tomcat-8.0.42/bin;./catalina.sh run" });
//			System.out.println(tomcat);
			
			//3.开启新线程用于
			//（1）处理 tomcat 输出日志
			//（2）向 LoadBalancer 中的 WorkerManager 发送心跳和系统数据如 CPU 使用率、内存使用率
			//（3）接收 WorkerManager 发来的命令并作出处理
//			new Thread(new TomcatConsoleReader(tomcat, true)).start();;
			
			//4.主线程阻塞等待，若 tomcat 异常终止，则尝试重新启动
//			try {
//				tomcat.waitFor();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			System.out.println("exit");
		} catch (Exception e) {
			logger.error("Error occours while running worker monitor", e);
		}
	}
	
	/**
	 * 1.读取配置信息，包括 
		（1）LoadBalancer 的ip
		（2）接收此 WorkerMonitor 数据的端口
		（3）此 Worker 的名称
		（4）CATALINA_HOME
		（5）tomcat 服务端口
		（6）心跳的发送频率
		2.根据 ip 和端口创建 Socket 用于与 LoadBalancer 的 WorkerManager 通信
	 */
	@Override
	protected void init() {
		Properties props = new Properties();
		String fileName = "WorkerMonitor.properties";
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
			logger.info("Read configuration file [" + fileName + "]");
		} catch (IOException e) {
			throw new RuntimeException("Failed to read configuration file", e);
		}
		
		String ip = props.getProperty("manager.ip");
		if(StringUtils.isEmpty(ip)) {
			throw new RuntimeException("[manager.ip] cannot be empty");
		}
		int managerPort;
		try {
			managerPort = Integer.parseInt(props.getProperty("manager.port"));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Wrong number format of [manager.port]");
		}
		
		try {
			handler = new SocketHandler(new Socket(ip, managerPort));
			logger.info("Connect to worker manager at [" + ip + ":" + port + "]");
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unknown host " + ip, e);
		} catch (IOException e) {
			throw new RuntimeException("Failed to connect to worker manager ", e);
		}
		
		name = props.getProperty("worker.name");
		if(StringUtils.isEmpty(name)) {
			throw new RuntimeException("[worker.name] cannot be null");
		}
		
		CATALINA_HOME = props.getProperty("catalina.home");
		if(StringUtils.isEmpty(CATALINA_HOME)) {
			throw new RuntimeException("[catalina.home] cannot be null");
		}
		File catalinaHome = new File(CATALINA_HOME);
		if(!catalinaHome.exists()) {
			throw new RuntimeException("Directorey [" + catalinaHome.getAbsolutePath() + "] doesnot exist");
		}
		File catalinaBin = new File(CATALINA_HOME + File.separator + "bin");
		File catalinaSh = new File(CATALINA_HOME + File.separator + "bin" + File.separator + "catalina.sh");
		if(!catalinaBin.exists() || !catalinaSh.exists()) {
			throw new RuntimeException("No catalina binary found in [catalina.home]");
		}
		
		try {
			port = Integer.parseInt(props.getProperty("tomcat.http.port"));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Wrong number format of [tomcat.http.port]");
		}
		
		try {
			peroid = Integer.parseInt(props.getProperty("heartbeat.period"));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Wrong number format of [heartbeat.period]");
		}
		
		status = TomcatStatus.STOPPED;
	}
	
	public Process getTomcat() {
		return tomcat;
	}
	
	public void setTomcatStatus(TomcatStatus status) {
		this.status = status;
	}

}
