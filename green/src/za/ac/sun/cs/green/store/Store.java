package za.ac.sun.cs.green.store;

import java.io.Serializable;

import org.apfloat.Apint;

import za.ac.sun.cs.green.util.Reporter;

/**
 * An interface to a data store that can record various kinds of objects.
 * 
 * @author Jaco Geldenhuys <jaco@cs.sun.ac.za>
 */
public interface Store {

	/**
	 * Shuts down the store. For example, in the case of an SQL database, this
	 * routine might close the connection.
	 */
	public void shutdown();

	/**
	 * Shuts down the store. For example, in the case of an SQL database, this
	 * routine might close the connection.
	 */
	public void report(Reporter reporter);
	
	/**
	 * Returns an arbitrary object that is associated with the given key. If
	 * there is nothing associated with the key, the method returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the object that is stored with the key or <code>null</code> if no
	 *         association is found
	 */
	public Object get(String key);

	/**
	 * Returns the string that is associated with the given key. If there is
	 * nothing associated with the key, the method returns <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the string that is stored with the key or <code>null</code> if no
	 *         association is found
	 */
	public String getString(String key);

	/**
	 * Returns the boolean that is associated with the given key. If there is
	 * nothing associated with the key, the method returns <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the boolean that is stored with the key or <code>null</code> if
	 *         no association is found
	 */
	public Boolean getBoolean(String key);

	/**
	 * Returns the integer that is associated with the given key. If there is
	 * nothing associated with the key, the method returns <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the integer that is stored with the key or <code>null</code> if
	 *         no association is found
	 */
	public Integer getInteger(String key);

	/**
	 * Returns the <code>long</code> value that is associated with the given
	 * key. If there is nothing associated with the key, the method returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the <code>long</code> value that is stored with the key or
	 *         <code>null</code> if no association is found
	 */
	public Long getLong(String key);

	/**
	 * Returns the <code>float</code> value that is associated with the given
	 * key. If there is nothing associated with the key, the method returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the <code>float</code> value that is stored with the key or
	 *         <code>null</code> if no association is found
	 */
	public Float getFloat(String key);

	/**
	 * Returns the <code>double</code> value that is associated with the given
	 * key. If there is nothing associated with the key, the method returns
	 * <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the <code>double</code> value that is stored with the key or
	 *         <code>null</code> if no association is found
	 */
	public Double getDouble(String key);

	/**
	 * Returns the Apfloat integer that is associated with the given key. If there is
	 * nothing associated with the key, the method returns <code>null</code>.
	 * 
	 * @param key
	 *            the key to use for the lookup
	 * @return the integer that is stored with the key or <code>null</code> if
	 *         no association is found
	 */
	public Apint getApfloatInteger(String key);

	/**
	 * Associates the given serializable value with the given key.
	 * 
	 * @param key
	 *            the key for the association
	 * @param value
	 *            the serializable value for the association
	 */
	public void put(String key, Serializable value);


}
