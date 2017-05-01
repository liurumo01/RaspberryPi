package space.snowwolf.pi.utils;

public class StringUtils {

	private StringUtils() {

	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().equals("") || str.trim().equals("null");
	}

}
