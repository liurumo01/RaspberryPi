package space.snowwolf.pi.lb;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.HttpPacket;
import space.snowwolf.pi.utils.IOUtils;

public class Handler extends Thread {
	
	private static final Logger logger = LoggerFactory.getLogger(Handler.class);

	private Socket client;
	private WorkerManager manager;

	@Override
	public void run() {
		HttpPacket request = null;
		try {
			request = IOUtils.read(client.getInputStream());
		} catch (IOException e) {
			logger.error("Failed to read request from client [" + client + "]", e);
		}
		if (request != null) {
			Worker worker = manager.distribute();
			HttpPacket response = null;
			if(worker == null) {
				logger.error("No avaliable worker found to handle requests");
				response = new HttpPacket();
				response.setTitle("HTTP 404 Not Found");
			} else {
				logger.info("Distribute worker [" + worker.getName() + "] to handle request [" + request.hashCode() + "]");
				response = worker.handle(request);
			}
			try {
				IOUtils.write(client.getOutputStream(), true, response);
			} catch (IOException e) {
				logger.error("Failed to send response to client [" + client + "]", e);
			}
		}
		IOUtils.close(client);
	}

	public void bind(Socket client) {
		this.client = client;
	}

	public void setWorkerManager(WorkerManager manager) {
		this.manager = manager;
	}

}
