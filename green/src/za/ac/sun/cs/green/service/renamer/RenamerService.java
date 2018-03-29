package za.ac.sun.cs.green.service.renamer;

import za.ac.sun.cs.green.Green;
import za.ac.sun.cs.green.Instance;
import za.ac.sun.cs.green.service.BasicService;
import za.ac.sun.cs.green.util.Reporter;

import java.util.logging.Level;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import za.ac.sun.cs.green.expr.Expression;
import za.ac.sun.cs.green.expr.IntConstant;
import za.ac.sun.cs.green.expr.IntVariable;
import za.ac.sun.cs.green.expr.Operation;
import za.ac.sun.cs.green.expr.Variable;
import za.ac.sun.cs.green.expr.Visitor;
import za.ac.sun.cs.green.expr.VisitorException;

/**
 *  RenamerService simply renames the variables of an expression.
 */
public class RenamerService extends BasicService {
        /**
         * Number of times the slicer has been invoked.
         */
        private int invocations = 0;

        public RenamerService(Green solver) {
            super(solver);
        }

        @Override
        public Set<Instance> processRequest(Instance instance) {
            @SuppressWarnings("unchecked")
            Set<Instance> result = (Set<Instance>) instance.getData(getClass());
            if (result == null) {
                final Map<Variable, Variable> map = new HashMap<Variable, Variable>();
                final Expression e = rename(instance.getFullExpression(), map);
                final Instance i = new Instance(getSolver(), instance.getSource(), null, e);
                result = Collections.singleton(i);
                instance.setData(getClass(), result);
            }
            return result;
        }

        @Override
        public void report(Reporter reporter) {
            reporter.report(getClass().getSimpleName(), "invocations = " + invocations);
        }

        public Expression rename(Expression expression,
                                   Map<Variable, Variable> map) {
            try {
                invocations++;
                RenamerVisitor renamingVisitor = new RenamerVisitor();
                expression.accept(renamingVisitor);
                return new Renamer(map,
                        renamingVisitor.getVariableSet()).rename(expression);
            } catch (VisitorException x) {
                log.log(Level.SEVERE,
                        "encountered an exception -- this should not be happening!",
                        x);
            }
            return null;
        }

    /**
     * RenamingVisitor obtains all the unique variables in an expression.
     */
    private static class RenamerVisitor extends Visitor {

            private SortedSet<IntVariable> variableSet;

            private boolean unsatisfiable;

            private boolean linearInteger;

            public RenamerVisitor() {
                variableSet = new TreeSet<IntVariable>();
                unsatisfiable = false;
                linearInteger = true;
            }

            public SortedSet<IntVariable> getVariableSet() {
                return variableSet;
            }

            @Override
            public void postVisit(Variable variable) {
                if (linearInteger && !unsatisfiable) {
                    if (variable instanceof IntVariable) {
                        variableSet.add((IntVariable) variable);
                    } else {
                        linearInteger = false;
                    }
                }
            }
        }

        private static class Renamer extends Visitor {

            private Map<Variable, Variable> map;

            private Stack<Expression> stack;

            public Renamer(Map<Variable, Variable> map,
                           SortedSet<IntVariable> variableSet) {
                this.map = map;
                stack = new Stack<Expression>();
            }

            public Expression rename(Expression expression) throws VisitorException {
                expression.accept(this);
                return stack.pop();
            }

            @Override
            public void postVisit(IntVariable variable) {
                Variable v = map.get(variable);
                if (v == null) {
                    v = new IntVariable("v" + map.size(), variable.getLowerBound(),
                            variable.getUpperBound());
                    map.put(variable, v);
                }
                stack.push(v);
            }

            @Override
            public void postVisit(IntConstant constant) {
                stack.push(constant);
            }

            @Override
            public void postVisit(Operation operation) {
                int arity = operation.getOperator().getArity();
                Expression operands[] = new Expression[arity];
                for (int i = arity; i > 0; i--) {
                    operands[i - 1] = stack.pop();
                }
                stack.push(new Operation(operation.getOperator(), operands));
            }

        }
}
