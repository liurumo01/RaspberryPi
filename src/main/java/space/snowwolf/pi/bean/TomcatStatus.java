package space.snowwolf.pi.bean;

/**
 * 表示Tomcat的状态
 * @author Gikyou
 *
 */
public enum TomcatStatus {

	/**
	 * 已停止运行
	 */
	STOPPED,
	
	/**
	 * 正在启动
	 */
	STARTING,
	
	/**
	 * 正在运行
	 */
	RUNNING,
	
	/**
	 * 正在关闭
	 */
	STOPPING,
	
}
