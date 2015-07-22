package za.ac.sun.cs.green.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class NullLogger extends Logger {

    private static final Handler emptyHandlers[] = new Handler[0];
    
    public NullLogger() {
    	super(null, null);
	}

	@Override
	public void log(LogRecord record) {
	}
	
	@Override
	public void log(Level level, String msg) {
	}
	
	@Override
	public void log(Level level, String msg, Object param1) {
	}
	
	@Override
	public void log(Level level, String msg, Object params[]) {
	}
	
	@Override
	public void log(Level level, String msg, Throwable thrown) {
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod,
			String msg, Object param1) {
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod,
			String msg, Object params[]) {
	}
	
	@Override
	public void logp(Level level, String sourceClass, String sourceMethod,
			String msg, Throwable thrown) {
	}
	
	/* Following methods have been deprecated:

	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod,
			String bundleName, String msg) {
	}
	
	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod,
			String bundleName, String msg, Object param1) {
	}
	
	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod,
			String bundleName, String msg, Object params[]) {
	}
	
	@Override
	public void logrb(Level level, String sourceClass, String sourceMethod,
			String bundleName, String msg, Throwable thrown) {
	}
	*/
	
	@Override
	public void entering(String sourceClass, String sourceMethod) {
	}
	
	@Override
	public void entering(String sourceClass, String sourceMethod, Object param1) {
	}
	
	@Override
	public void entering(String sourceClass, String sourceMethod, Object params[]) {
	}
	
	@Override
	public void exiting(String sourceClass, String sourceMethod) {
	}
	
	@Override
	public void exiting(String sourceClass, String sourceMethod, Object result) {
	}
	
	@Override
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
	}
	
	@Override
	public void severe(String msg) {
	}
	
	@Override
	public void warning(String msg) {
	}
	
	@Override
	public void info(String msg) {
	}
	
	@Override
	public void config(String msg) {
	}
	
	@Override
	public void fine(String msg) {
	}
	
	@Override
	public void finer(String msg) {
	}
	
	@Override
	public void finest(String msg) {
	}
	
	@Override
	public void setLevel(Level newLevel) throws SecurityException {
	}
	
	@Override
	public Level getLevel() {
		return Level.OFF;
	}
	
	@Override
	public boolean isLoggable(Level level) {
		return true;
	}
	
	@Override
	public String getName() {
		return "";
	}
	
	@Override
	public synchronized void addHandler(Handler handler) throws SecurityException {
	}
	
	@Override
	public synchronized void removeHandler(Handler handler) throws SecurityException {
	}
	
	@Override
	public synchronized Handler[] getHandlers() {
		return emptyHandlers;
	}
	
	@Override
	public synchronized void setUseParentHandlers(boolean useParentHandlers) {
	}
	
	@Override
	public synchronized boolean getUseParentHandlers() {
		return false;
	}
	
	@Override
	public Logger getParent() {
		return null;
	}
	
	@Override
	public void setParent(Logger parent) {
	}

}
