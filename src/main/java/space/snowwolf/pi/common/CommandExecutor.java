package space.snowwolf.pi.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import space.snowwolf.pi.bean.Command;

/**
 * 命令处理器
 * @author Gikyou
 *
 */
public abstract class CommandExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
	
	protected SocketHandler handler;
	
	protected boolean running;
	
	public CommandExecutor() {
		init();
	}

	/**
	 * 初始化操作，如读取配置文件，设置初始参数等
	 */
	protected abstract void init();
	
	/**
	 * 启动命令处理器
	 */
	public void start() {
		running = true;
		while(running) {
			if(handler == null) {
				continue;
			}
			while(handler.list().isEmpty()) {
				
			}
			List<Command> list = new ArrayList<>(handler.list());
			handler.clear();
			for(Command c : list) {
				handle(c);
			}
		}
	}
	
	public void stop() {
		running = false;
	}
	
	/**
	 * 使用反射机制，以传入的命令名为方法名，以命令参数为方法参数执行命令
	 * @param cmd
	 */
	private final void handle(Command cmd) {
		String name = cmd.getName();
		try {
			Method method = getClass().getDeclaredMethod(name, Map.class);
			method.setAccessible(true);
			method.invoke(this, cmd.getParameterMap());
			logger.info("Execute command [" + cmd + "]");
		} catch(NoSuchMethodException e) {
			logger.debug("Command name [" + name + "] not found", e);
		} catch (Exception e) {
			logger.error("Failed to execute command [" + cmd + "]", e);
		}
	}
	
}
