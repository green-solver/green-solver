package za.ac.sun.cs.green.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class GreenFormatter extends Formatter {

	private Date date = new Date();

	private String format = "[%1$tY%1$tm%1$td %1$tH:%1$tM:%1$tS][%2$s][%4$s] %5$s%6$s%n";

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public synchronized String format(LogRecord record) {
		date.setTime(record.getMillis());
		StringBuffer source = new StringBuffer();
		if (record.getSourceClassName() != null) {
			source.append(record.getSourceClassName());
			if (record.getSourceMethodName() != null) {
				source.append(' ');
				source.append(record.getSourceMethodName());
			}
		} else {
			source.append(record.getLoggerName());
		}
		String message = formatMessage(record);
		String throwable = "";
		if (record.getThrown() != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println();
			record.getThrown().printStackTrace(pw);
			pw.close();
			throwable = sw.toString();
		}
		return String.format(format, date, source.toString(),
				record.getLoggerName(), record.getLevel().getLocalizedName(),
				message, throwable);
	}

}
