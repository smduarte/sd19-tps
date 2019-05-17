package utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

	public static Logger Log;

	static {
		Log = Logger.getLogger("Tester");

		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(new SimplerFormatter());
		ch.setLevel(Level.ALL);
		Log.addHandler(ch);

	}

	static class SimplerFormatter extends Formatter {

		@Override
		public String format(LogRecord r) {
			StringBuilder sb = new StringBuilder();

			return sb.append(r.getMessage()).append('\n').toString();
		}

	}
}
