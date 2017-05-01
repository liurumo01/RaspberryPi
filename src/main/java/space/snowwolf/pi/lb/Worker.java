package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import space.snowwolf.pi.utils.HttpPacket;
import space.snowwolf.pi.utils.IOUtils;

public class Worker {

	private String name;
	private String ip;
	private int port;
	private int ratio;
	private boolean main;
	private Socket socket;

	public Worker(String name, String ip, int port, boolean main, int ratio) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.main = main;
		this.ratio = ratio;
	}

	public HttpPacket handle(HttpPacket request) {
		HttpPacket response = null;
		try {
			socket = new Socket(ip, port);
			IOUtils.write(socket.getOutputStream(), false, request);
			response = IOUtils.read(socket.getInputStream(), false);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("worker ip地址或端口有误");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("读写数据出错");
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

	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

}
