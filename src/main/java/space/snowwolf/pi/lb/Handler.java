package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.Socket;

import space.snowwolf.pi.utils.HttpPacket;
import space.snowwolf.pi.utils.IOUtils;

public class Handler extends Thread {

	private Socket client;
	private WorkerManager manager;

	@Override
	public void run() {
		HttpPacket request = null;
		try {
			request = IOUtils.read(client.getInputStream(), true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (request != null) {
			Worker worker = manager.distribute();
			System.out.println("分配Worker[" + worker.getName() + "]处理请求[" + request.hashCode() + "]");
			HttpPacket response = worker.handle(request);
			try {
				IOUtils.write(client.getOutputStream(), true, response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void bind(Socket client) {
		this.client = client;
	}

	public void setWorkerManager(WorkerManager manager) {
		this.manager = manager;
	}

}
