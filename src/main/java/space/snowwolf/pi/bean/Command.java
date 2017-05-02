package space.snowwolf.pi.bean;

import java.util.HashMap;
import java.util.Map;

public class Command {

	private String name;
	private Map<String, String> params;
	
	public Command(String name) {
		this.name = name;
		params = new HashMap<>();
	}
	
	public String getName() {
		return name;
	}
	
	public void addParameter(String key, String value) {
		params.put(key, value);
	}
	
	public String getParameter(String key) {
		return params.get(key);
	}
	
	public Map<String, String> getParameterMap() {
		return params;
	}

	@Override
	public String toString() {
		return "Command [name=" + name + ", params=" + params + "]";
	}
	
}
