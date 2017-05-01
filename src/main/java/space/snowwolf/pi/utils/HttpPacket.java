package space.snowwolf.pi.utils;

import java.util.Map;

/**
 * 表示一个 http 消息体
 * header为多行[xxx : xxx]格式的字符串，封装为 Map<String, String> 用于判断特殊字段
 * data 可以为字符串或二进制数据
 * @author 天问雪狼
 *
 */
public class HttpPacket {

	private String title;
	private Map<String, String> header;
	private String headerString;
	
	private byte[] data;
	
	public HttpPacket() {
		
	}
	
	public HttpPacket(Map<String, String> header, byte[] data) {
		this.header = header;
		this.data = data;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public void setHeader(Map<String, String> header) {
		this.header = header;
	}

	public String getHeaderString() {
		return headerString;
	}

	public void setHeaderString(String headerString) {
		this.headerString = headerString;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getDataString() {
		return new String(data);
	}
	
	public int contenLength() {
		String contentLength = header.get("Content-Length");
		return contentLength == null ? 0 : Integer.parseInt(contentLength);
	}
	
	public boolean isChunked() {
		String transferEncoding = header.get("Transfer-Encoding");
		return transferEncoding != null && transferEncoding.equals("chunked");
	}

	@Override
	public String toString() {
		return header.toString();
	}
}
