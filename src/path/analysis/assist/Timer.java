package path.analysis.assist;

import java.util.Date;

public class Timer {
	public static Date lastDate = new Date();
	public static String tempOut = "";
	
	public static void printSystemTime() {
		Date nowDate = new Date();
		long interval = nowDate.getTime() - lastDate.getTime();
		tempOut += interval + " ms\n";
		lastDate = nowDate;
	}
}
