package space.snowwolf.pi.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.HttpPacket;

public class IOUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);
	
	public static void close(Closeable ... args) {
		for(Closeable obj : args) {
			if(obj != null) {
				try {
					obj.close();
				} catch (IOException e) {
					logger.error("Failed to close resource", e);
				}
			}
		}
	}

	public static HttpPacket read(InputStream in, boolean print) throws NumberFormatException, IOException {
		if(in == null) {
			return null;
		}
		
		HttpPacket pack = new HttpPacket();
		readHttpHead(in, pack);
		readHttpBody(in, pack);
		
//		if (print) {
//			System.out.println("Readed:");
//			System.out.println(pack.getTitle());
//			System.out.println(pack.getHeaderString());
//		}

		return pack;
	}

	public static void write(OutputStream out, boolean print, HttpPacket pack) throws IOException {
		if(out == null || pack == null) {
			return;
		}
		
		out.write(pack.getTitle().getBytes());
		out.write("\r\n".getBytes());
		out.write(pack.getHeaderString().getBytes());
		// out.write("\r\n".getBytes());
		out.write(pack.getData());
		// out.write("\r\n".getBytes());
		
//		if (print) {
//			System.out.println("Written:");
//			System.out.println(pack.getTitle());
//			System.out.println(pack.getHeaderString());
//		}
	}

	/**
	 * 读取 http 消息头
	 * 
	 * @param in
	 *            SocketInputStream
	 * @param pack
	 *            HttpPacket
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private static void readHttpHead(InputStream in, HttpPacket pack) throws NumberFormatException, IOException {
		StringBuilder builder = new StringBuilder();
		Map<String, String> header = new HashMap<String, String>();
		String line = null;

		// [GET / HTTP/1.1] || [HTTP 200 OK]
		pack.setTitle(readLine(in, 0).trim());
		// xxx: xxx\r\n...
		while (!(line = readLine(in, 0)).equals("\r\n")) {
			String[] split = line.split(":");
			header.put(split[0].trim(), split[1].replaceAll("\r\n", "").trim());
			builder.append(line);
		}
		pack.setHeader(header);
		// \r\n
		builder.append("\r\n");
		pack.setHeaderString(builder.toString());
	}

	private static void readHttpBody(InputStream in, HttpPacket pack) throws IOException {
		// 读取消息内容
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (pack.isChunked()) {
			String line = null;
			// 对于分块传送的内容，每一块第一行为16进制的单块字节数，长度为0表示结束，末尾为两个 \r\n
			int chunkSize = -1;
			while (chunkSize != 0) {
				line = readLine(in, 0);
				chunkSize = Integer.parseInt(line.trim(), 16);
				if (chunkSize == 0) {
					break;
				}
				byte[] buf = new byte[chunkSize];
				in.read(buf);
				baos.write(buf);
				readLine(in, 0);
				readLine(in, 0);
				baos.write("\r\n\r\n".getBytes());
			}
		} else {
			int remain = pack.contenLength();
			byte[] buf = new byte[102400];
			int len = 0;
			if (remain != 0) {
				while(remain > 0) {
					len = in.read(buf, 0, remain > 102400 ? 102400 : remain);
					remain -= len;
					baos.write(buf, 0, len);
				}
			}
		}
		pack.setData(baos.toByteArray());
	}

	private static String readLine(InputStream is, int len) throws IOException {
		if(is == null) {
			return null;
		}
		
		List<Byte> list = new ArrayList<Byte>();
		byte b;
		int total = 0;
		if (len != 0) {
			do {
				b = (byte) is.read();
				list.add(Byte.valueOf(b));
				total++;
			} while (total < len);
		} else {
			do {
				b = (byte) is.read();
				list.add(Byte.valueOf(b));
			} while (b != 10);
		}

		byte[] temp = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			temp[i] = ((Byte) list.get(i)).byteValue();
		}
		list.clear();

		return new String(temp);
	}

}
