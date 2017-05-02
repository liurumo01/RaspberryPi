package space.snowwolf.pi.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取操作系统的信息，如 CPU 使用率、内存使用率
 * 
 * @author Gikyou
 *
 */
public abstract class OSUtils {

	protected static final Logger logger = LoggerFactory.getLogger(OSUtils.class);

	/**
	 * 针对不同种类的操作系统需要进行不同的实现
	 */
	private static final OSUtils instance;

	/**
	 * 根据操作系统类型创建实例
	 */
	static {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win")) {
			instance = new WindowsOSUtils();
		} else if (osName.contains("linux")) {
			instance = new LinuxOSUtils();
		} else {
			instance = null;
		}
	}

	/**
	 * 获取操作系统当前 CPU 使用率
	 * 
	 * @return
	 */
	public static int cpuUsage() {
		return instance == null ? -1 : instance.cpuUsageImpl();
	}

	/**
	 * 获取操作系统当前内存使用率
	 * 
	 * @return
	 */
	public static int memoryUsage() {
		return instance == null ? -1 : instance.memoryUsageImpl();
	}

	protected abstract int cpuUsageImpl();

	protected abstract int memoryUsageImpl();

}

class WindowsOSUtils extends OSUtils {

	/**
	 * 获取 Windows 系统 CPU 使用率
	 */
	@Override
	protected int cpuUsageImpl() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 获取 Windows 系统内存使用率
	 */
	@Override
	protected int memoryUsageImpl() {
		// TODO Auto-generated method stub
		return 0;
	}

}

class LinuxOSUtils extends OSUtils {
	/**
	 * 获取 Linux 系统 CPU 使用率
	 */
	@Override
	public int cpuUsageImpl() {
		Map<?, ?> map1 = cpuinfo();
		try {
			Thread.sleep(5 * 1000);
		} catch (InterruptedException e) {
			logger.error("Thread sleep is interrupted", e);
		}
		Map<?, ?> map2 = cpuinfo();

		long user1 = Long.parseLong(map1.get("user").toString());
		long nice1 = Long.parseLong(map1.get("nice").toString());
		long system1 = Long.parseLong(map1.get("system").toString());
		long idle1 = Long.parseLong(map1.get("idle").toString());

		long user2 = Long.parseLong(map2.get("user").toString());
		long nice2 = Long.parseLong(map2.get("nice").toString());
		long system2 = Long.parseLong(map2.get("system").toString());
		long idle2 = Long.parseLong(map2.get("idle").toString());

		long total1 = user1 + system1 + nice1;
		long total2 = user2 + system2 + nice2;
		float total = total2 - total1;

		long totalIdle1 = user1 + nice1 + system1 + idle1;
		long totalIdle2 = user2 + nice2 + system2 + idle2;
		float totalidle = totalIdle2 - totalIdle1;

		float cpusage = (total / totalidle) * 100;
		return (int) cpusage;
	}

	/**
	 * 获取 Linux 系统 CPU 使用信息
	 */
	public Map<?, ?> cpuinfo() {
		InputStreamReader inputs = null;
		BufferedReader buffer = null;
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			inputs = new InputStreamReader(new FileInputStream("/proc/stat"));
			buffer = new BufferedReader(inputs);
			String line = "";
			while (true) {
				line = buffer.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("cpu")) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					List<String> temp = new ArrayList<String>();
					while (tokenizer.hasMoreElements()) {
						String value = tokenizer.nextToken();
						temp.add(value);
					}
					map.put("user", temp.get(1));
					map.put("nice", temp.get(2));
					map.put("system", temp.get(3));
					map.put("idle", temp.get(4));
					map.put("iowait", temp.get(5));
					map.put("irq", temp.get(6));
					map.put("softirq", temp.get(7));
					map.put("stealstolen", temp.get(8));
					break;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to read cpu info", e);
		} finally {
			try {
				buffer.close();
				inputs.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * 获取 Linux 系统内存使用率
	 */
	@Override
	public int memoryUsageImpl() {
		Map<String, Object> map = new HashMap<String, Object>();
		InputStreamReader inputs = null;
		BufferedReader buffer = null;
		try {
			inputs = new InputStreamReader(new FileInputStream("/proc/meminfo"));
			buffer = new BufferedReader(inputs);
			String line = "";
			while (true) {
				line = buffer.readLine();
				if (line == null)
					break;
				int beginIndex = 0;
				int endIndex = line.indexOf(":");
				if (endIndex != -1) {
					String key = line.substring(beginIndex, endIndex);
					beginIndex = endIndex + 1;
					endIndex = line.length();
					String memory = line.substring(beginIndex, endIndex);
					String value = memory.replace("kB", "").trim();
					map.put(key, value);
				}
			}

			long memTotal = Long.parseLong(map.get("MemTotal").toString());
			long memFree = Long.parseLong(map.get("MemFree").toString());
			long memused = memTotal - memFree;
			long buffers = Long.parseLong(map.get("Buffers").toString());
			long cached = Long.parseLong(map.get("Cached").toString());

			double usage = (double) (memused - buffers - cached) / memTotal * 100;
			return (int) usage;
		} catch (Exception e) {
			throw new RuntimeException("Failed to read memory info", e);
		} finally {
			try {
				buffer.close();
				inputs.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}