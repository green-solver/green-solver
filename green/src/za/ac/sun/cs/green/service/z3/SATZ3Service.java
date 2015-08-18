package za.ac.sun.cs.green.service.z3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.service.smtlib.SATSMTLIBService;

public class SATZ3Service extends SATSMTLIBService {

	private final String DEFAULT_Z3_PATH = "/usr/bin/z3";

	private final String DEFAULT_Z3_ARGS = "-smt2 -in";
	
	private final String z3Command;
	
	public SATZ3Service(Green solver, Properties properties) {
		super(solver);
		String p = properties.getProperty("green.z3.path", DEFAULT_Z3_PATH);
		String a = properties.getProperty("green.z3.args", DEFAULT_Z3_ARGS);
		z3Command = p + ' ' + a;
	}

	@Override
	protected Boolean solve0(String smtQuery) {
		String output = "";
		try {
			Process process = Runtime.getRuntime().exec(z3Command);
			OutputStream stdin = process.getOutputStream();
			InputStream stdout = process.getInputStream();
			BufferedReader outReader = new BufferedReader(new InputStreamReader(stdout));
			stdin.write((smtQuery + "(exit)\n").getBytes());
			stdin.flush();
			stdin.close();
			output = outReader.readLine();
			stdout.close();
			process.destroy();
		} catch (IOException x) {
			log.log(Level.SEVERE, x.getMessage(), x);
		}
		if (output.equals("sat")) {
			return true;
		} else if (output.equals("unsat")) {
			return false;
		} else {
			log.severe("Z3 returned a null" + output) ;
			return null;
		}
	}

}
