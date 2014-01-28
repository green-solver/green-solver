package za.ac.sun.cs.green.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import org.apfloat.Apint;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.util.Base64;

public abstract class BasicStore implements Store {

	protected final Green solver;

	protected final Logger log;

	public BasicStore(Green solver) {
		this.solver = solver;
		log = solver.getLog();
	}

	@Override
	public void shutdown() {
		// Do nothing by default
	}

	/** Read the object from Base64 string. */
	protected static Object fromString(String s) throws IOException,
			ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
				Base64.decode(s)));
		Object o = ois.readObject();
		ois.close();
		return o;
	}

	/** Write the object to a Base64 string. */
	protected static String toString(Serializable o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		return new String(Base64.encode(baos.toByteArray()));
	}

	@Override
	public String getString(String key) {
		Object value = get(key);
		return (value instanceof String) ? (String) value : null;
	}

	@Override
	public Boolean getBoolean(String key) {
		Object value = get(key);
		return (value instanceof Boolean) ? (Boolean) value : null;
	}

	@Override
	public Integer getInteger(String key) {
		Object value = get(key);
		return (value instanceof Integer) ? (Integer) value : null;
	}

	@Override
	public Long getLong(String key) {
		Object value = get(key);
		return (value instanceof Long) ? (Long) value : null;
	}

	@Override
	public Float getFloat(String key) {
		Object value = get(key);
		return (value instanceof Float) ? (Float) value : null;
	}

	@Override
	public Double getDouble(String key) {
		Object value = get(key);
		return (value instanceof Double) ? (Double) value : null;
	}

	@Override
	public Apint getApfloatInteger(String key) {
		Object value = get(key);
		return (value instanceof Apint) ? (Apint) value : null;
	}

}
