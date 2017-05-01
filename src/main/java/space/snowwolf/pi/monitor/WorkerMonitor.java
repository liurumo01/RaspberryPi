package space.snowwolf.pi.monitor;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import space.snowwolf.pi.utils.StringUtils;

public class WorkerMonitor {
	
//	private static Properties props;
	private static Socket manager;
	private static String name;
	private static String CATALINA_HOME;
	private static int port;
	private static int peroid;

	public static void main(String[] args) {
		try {
			boolean result = init();
			if(!result) {
				return;
			}
			
			//2.启动 tomcat，并在子线程中处理输出内容
			Process tomcat = Runtime.getRuntime().exec(
					new String[] { "/bin/sh", "-c", "cd /home/snowwolf/apache-tomcat-8.0.42/bin;./catalina.sh run" });
			System.out.println(tomcat);
			
			//3.开启新线程用于
			//（1）处理 tomcat 输出日志
			//（2）向 LoadBalancer 中的 WorkerManager 发送心跳和系统数据如 CPU 使用率、内存使用率
			//（3）接收 WorkerManager 发来的命令并作出处理
			new Thread(new TomcatConsoleReader(tomcat, true)).start();;
			
			//4.主线程阻塞等待，若 tomcat 异常终止，则尝试重新启动
			try {
				tomcat.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("exit");
		} catch (IOException e) {
			e.printStackTrace();
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
	private static boolean init() {
		Properties props = new Properties();
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("WorkerMonitor.properties"));
		} catch (IOException e) {
			System.out.println("读取配置文件[WorkerMonitor.properties]失败");
			e.printStackTrace();
			return false;
		}
		
		String ip = props.getProperty("LoadBalancer.ip");
		if(StringUtils.isEmpty(ip)) {
			System.out.println("LoadBalancer.ip 不能为空");
			return false;
		}
		int managerPprt = -1;
		try {
			managerPprt = Integer.parseInt(props.getProperty("LoadBalancer.WorkerManager.port"));
		} catch (NumberFormatException e) {
			System.out.println("LoadBalancer.WorkerManager.port 配置有误");
			e.printStackTrace();
			return false;
		}
		
		try {
			manager = new Socket(ip, managerPprt);
			System.out.println(manager);
		} catch (UnknownHostException e) {
			System.out.println("LoadBalancer.ip 有误，未知的主机名称");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.out.println("建立与 WorkerManager 的连接失败");
			e.printStackTrace();
			return false;
		}
		
		name = props.getProperty("worker.name");
		if(StringUtils.isEmpty(name)) {
			System.out.println("worker.name 不能为空");
			return false;
		}
		
		CATALINA_HOME = props.getProperty("catalina.home");
		if(StringUtils.isEmpty(CATALINA_HOME)) {
			System.out.println("catalina.home 不能为空");
			return false;
		}
		File catalinaHome = new File(CATALINA_HOME);
		if(!catalinaHome.exists()) {
			System.out.println("catalina.home 目录[“ + CATALINA_HOME + ”]不存在");
			return false;
		}
		File catalinaBin = new File(CATALINA_HOME + File.separator + "bin");
		File catalinaSh = new File(CATALINA_HOME + File.separator + "bin" + File.separator + "catalina.sh");
		if(!catalinaBin.exists() || !catalinaSh.exists()) {
			System.out.println("catalina.home 不是合法的 tomcat 目录");
			return false;
		}
		
		try {
			port = Integer.parseInt(props.getProperty("tomcat.http.port"));
			System.out.println(port);
		} catch (NumberFormatException e) {
			System.out.println("tomcat.http.port 配置有误");
			e.printStackTrace();
			return false;
		}
		
		try {
			peroid = Integer.parseInt(props.getProperty("heartbeat.period"));
			System.out.println(peroid);
		} catch (NumberFormatException e) {
			System.out.println("tomcat.http.port 配置有误");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
