package za.ac.sun.cs.green.store.redis;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;

import redis.clients.jedis.Jedis;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.store.BasicStore;
import za.ac.sun.cs.green.util.Configuration;
import za.ac.sun.cs.green.util.Reporter;

/**
 * An implementation of a {@link za.ac.sun.cs.green.store.Store} based on redis (<code>http://www.redis.io</code>).
 * 
 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
 */
public class RedisStore extends BasicStore {

	/**
	 * The time (in seconds) of inactivity until the connection to the redis store timeout.
	 */
	private static final int TIMEOUT = 2000;
	
	/**
	 * Connection to the redis store.
	 */
	private Jedis db = null;

	/**
	 * Number of times <code>get(...)</code> was called.
	 */
	private int retrievalCount = 0;

	/**
	 * Number of times <code>put(...)</code> was called.
	 */
	private int insertionCount = 0;

	/**
	 * The default host of the redis server.
	 */
	private final String DEFAULT_REDIS_HOST = "localhost";

	/**
	 * Options passed to the LattE executable.
	 */
	private final int DEFAULT_REDIS_PORT = 6379;
	
	/**
	 * Constructor to create a default connection to a redis store running on the local computer.
	 */
	public RedisStore(Green solver, Properties properties) {
		super(solver);
		String h = properties.getProperty("green.redis.host", DEFAULT_REDIS_HOST);
		int p = Configuration.getIntegerProperty(properties, "green.redis.port", DEFAULT_REDIS_PORT);
		db = new Jedis(h, p, TIMEOUT);
	}
	
	/**
	 * Constructor to create a connection to a redis store given the host and the port.
	 * 
	 * @param host the host on which the redis store is running
	 * @param port the port on which the redis store is listening
	 */
	public RedisStore(Green solver, String host, int port) {
		super(solver);
		db = new Jedis(host, port, TIMEOUT);
	}

	@Override
	public void report(Reporter reporter) {
		reporter.report(getClass().getSimpleName(), "retrievalCount = " + retrievalCount);
		reporter.report(getClass().getSimpleName(), "insertionCount = " + insertionCount);
	}
	
	@Override
	public synchronized Object get(String key) {
		retrievalCount++;
		try {
			String s = db.get(key);
			return (s == null) ? null : fromString(s);
		} catch (IOException x) {
			log.log(Level.SEVERE, "io problem", x);
		} catch (ClassNotFoundException x) {
			log.log(Level.SEVERE, "class not found problem", x);
		}
		return null;
	}

	@Override
	public synchronized void put(String key, Serializable value) {
		insertionCount++;
		try {
			db.set(key, toString(value));
		} catch (IOException x) {
			log.log(Level.SEVERE, "io problem", x);
		}
	}

}
