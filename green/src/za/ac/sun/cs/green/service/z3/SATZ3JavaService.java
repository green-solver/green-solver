package za.ac.sun.cs.green.service.z3;

import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import com.microsoft.z3.Z3Exception;

import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.expr.VisitorException;
import za.ac.sun.cs.green.service.SATService;

public class SATZ3JavaService extends SATService {
	
	Context ctx;
	Solver Z3solver;
	Z3Wrapper z3Wrapper;
	
	private static class Z3Wrapper {
		private Context ctx;
		private Solver solver;

		private static Z3Wrapper instance = null;

		public static Z3Wrapper getInstance() {
			if (instance != null) {
				return instance;
			}
			return instance = new Z3Wrapper();
		}

		private Z3Wrapper() {			
			HashMap<String, String> cfg = new HashMap<String, String>();
	        cfg.put("model", "true"); //"true" ?
			try{
				ctx = new Context(cfg);		 
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("## Error Z3: Exception caught in Z3 JNI: \n" + e);
		    }
			solver = ctx.mkSolver();
		}

		public Solver getSolver() {
			return this.solver;
		}

		public Context getCtx() {
			return this.ctx;
		}
	}

	
	
	public SATZ3JavaService(Green solver, Properties properties) {
		super(solver);
		
		Z3Wrapper z3Wrapper = Z3Wrapper.getInstance();
		Z3solver = z3Wrapper.getSolver();
		ctx = z3Wrapper.getCtx();
		
		/*
		HashMap<String, String> cfg = new HashMap<String, String>();
        cfg.put("model", "false");
		try{
			ctx = new Context(cfg);		 
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("## Error Z3: Exception caught in Z3 JNI: \n" + e);
	    }
	    */
	}

	@Override
	protected Boolean solve(Instance instance) {
		Boolean result = false;
		// translate instance to Z3 
		Z3JavaTranslator translator = new Z3JavaTranslator(ctx);
		try {
			instance.getExpression().accept(translator);
		} catch (VisitorException e1) {
			log.log(Level.WARNING, "Error in translation to Z3"+e1.getMessage());
		}
		// get context out of the translator
		BoolExpr expr = translator.getTranslation();
		// model should now be in ctx
		try {
			//Z3solver = ctx.mkSolver();
			Z3solver.push();
			Z3solver.add(expr);
		} catch (Z3Exception e1) {
			log.log(Level.WARNING, "Error in Z3"+e1.getMessage());
		}
		//solve 		
		try {
			if (Status.SATISFIABLE == Z3solver.check())
				result = true;
			else {
				result = false;
			}
		} catch (Z3Exception e) {
			log.log(Level.WARNING, "Error in Z3"+e.getMessage());
		}
		// clean up
		int scopes = Z3solver.getNumScopes();
		if (scopes > 0) {
			Z3solver.pop(scopes);
		}
		return result;
	}


}
