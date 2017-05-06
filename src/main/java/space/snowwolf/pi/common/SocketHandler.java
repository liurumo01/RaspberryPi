package space.snowwolf.pi.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.Command;
import space.snowwolf.pi.utils.IOUtils;
import space.snowwolf.pi.utils.StringUtils;

public class SocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SocketHandler.class);

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	private List<Command> queue;
	
	public SocketHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.in = socket.getInputStream();
		this.out = socket.getOutputStream();
		reader = new BufferedReader(new InputStreamReader(in));
		writer = new BufferedWriter(new OutputStreamWriter(out));
		
		queue = new Vector<>();
		Main.getThreadPool().submit(() -> {
			while(socket != null && !socket.isClosed() && socket.isConnected()) {
				logger.debug((socket == null) + " " + socket.isClosed() + " " + socket.isConnected());
				try {
					queue.add(read());
				} catch (IOException e) {
					logger.error("Failed to read command", e);
				}
			}
		});
	}
	
	private Command read() throws IOException {
		String name = reader.readLine();
		Command cmd = new Command(name);
		String line;
		while((line = reader.readLine()) != null && !line.equals("")) {
			int index = line.indexOf("=");
			if(index < 0) {
				continue;
			}
			String key = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();
			cmd.addParameter(key, value);
		}
		logger.info("Read command [" + cmd + "]");
		return cmd;
	}
	
	public List<Command> list() {
		return queue;
	}
	
	public void send(Command cmd) throws IOException {
		if(cmd == null || StringUtils.isEmpty(cmd.getName())) {
			return;
		}
		writer.write(cmd.getName());
		writer.newLine();
		Set<Entry<String, String>> set = cmd.getParameterMap().entrySet();
		for (Entry<String, String> e : set) {
			writer.write(e.getKey());
			writer.write("=");
			writer.write(e.getValue());
			writer.newLine();
		}
		writer.newLine();
		writer.flush();
		logger.info("Send command [" + cmd + "]");
	}
	
	public void close() {
		IOUtils.close(in, out, socket);
	}

	public void clear() {
		queue.clear();
	}
	
}
