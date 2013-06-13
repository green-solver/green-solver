package za.ac.sun.cs.green.log;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class GreenHandler extends StreamHandler {

	/**
	 * Constructs a <code>StreamHandler</code> that publishes log records to
	 * <code>System.err</code>. The initial configuration is determined by the
	 * <code>LogManager</code> properties described above.
	 */
	public GreenHandler() {
		super(System.out, new GreenFormatter());
	}

	public GreenHandler(Level level) {
		super(System.out, new GreenFormatter());
		setLevel(level);
	}

	/**
	 * Forces any data that may have been buffered to the underlying output
	 * device, but does <i>not</i> close <code>System.err</code>.
	 * 
	 * <p>
	 * In case of an I/O failure, the <code>ErrorManager</code> of this
	 * <code>ConsoleHandler</code> will be informed, but the caller of this
	 * method will not receive an exception.
	 */
	@Override
	public void close() {
		flush();
	}

	/**
	 * Publishes a <code>LogRecord</code> to the console, provided the record
	 * passes all tests for being loggable.
	 * 
	 * <p>
	 * Most applications do not need to call this method directly. Instead, they
	 * will use use a <code>Logger</code>, which will create LogRecords and
	 * distribute them to registered handlers.
	 * 
	 * <p>
	 * In case of an I/O failure, the <code>ErrorManager</code> of this
	 * <code>SocketHandler</code> will be informed, but the caller of this
	 * method will not receive an exception.
	 * 
	 * <p>
	 * The GNU implementation of <code>ConsoleHandler.publish</code> calls
	 * flush() for every request to publish a record, so they appear immediately
	 * on the console.
	 * 
	 * @param record
	 *            the log event to be published.
	 */
	@Override
	public void publish(LogRecord record) {
		super.publish(record);
		flush();
	}

}
