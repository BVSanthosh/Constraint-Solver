import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/*
 * This class contains the Maintaining Arc Consistency algorithm for solving a binary CSP using 2-way search.
 * Uses AC3 to maintain global arc consistency.
 */

public class MAC {
    private LinkedList<Arc> arcsQueue = new LinkedList<>();  //queue used for arc consistency
    private Stack<List<Variable>> previousState = new Stack<>();  //stack used to undopruning
    private List<BinaryConstraint> constraints;   //constraints
    private List<Variable> varDomains;   // list of variables and their domains stored in the Variable class
    private BinaryCSP csp;    //csp instance
    private int[] assignment;   //keeps track of assigned variables
    private int nodesVisited = 0;   //nodes visited
    private int arcRevisions = 0;   //arcs revised
    private long timeTaken = 0;   //time taken to find a solution
    private long startTime = 0;
    private long endTime = 0;
    private boolean emptyDomainFlag = false;   //flag used to check if a domain is empty after arc revision  

    public MAC(BinaryCSP csp) {
        this.csp = csp;
        initialise(); 
    }

    // initialises the variables, domains and constraints
    public void initialise() {
        varDomains = new ArrayList<>();
        constraints = csp.getConstraints();
        assignment = new int[csp.getNoVariables()];

        for (int i = 0; i < csp.getNoVariables(); i++) {
            List<Integer> domain = new ArrayList<>();

            for (int j = csp.getLB(i); j <= csp.getUB(i); j++) {
                domain.add(j);
            }

            Variable variable = new Variable(i, domain);    
            varDomains.add(variable);
            assignment[i] = -1;
        }

        //initialises the queue with all the arcs
        for (BinaryConstraint constraint : constraints) {
            Arc arc1 = new Arc(constraint.getFirstVar(), constraint.getSecondVar());
            Arc arc2 = new Arc(constraint.getSecondVar(), constraint.getFirstVar());

            arcsQueue.add(arc1);
            arcsQueue.add(arc2);
        }
    }

    //starts the search
    public void solve() {
        startTime = System.currentTimeMillis();
        
        if (macAC3()) {   //runs AC3 before starting the search to reduce the variable domains 
            searchMAC(varDomains);
        } else {
            System.out.println("Search terminated: unsolvable csp");
        }
    }

    //recursively searches for a solution
    private void searchMAC(List<Variable> varList) {
        if (solutionFound()) {
            endTime = System.currentTimeMillis();
            timeTaken = endTime - startTime;
            printSolution();
            System.exit(0);
        }

        nodesVisited++;  //increments the nodes visited

        Variable var = selectVar(varList);
        int val = selectVal(var);

        pushCurrentState();   //saves the current state of the search 
        assignValue(var, val);
        setUpQueue(var);   //adds the relevant arcs for the current variable to the queue before running AC3

        if (macAC3()){
            List<Variable> newVarList = removeAssignedVar(varList, var);
            searchMAC(newVarList);
        }

        undoPruning();
        unassignValue(var, val);
        deleteValue(var, val);

        if (!domainEmpty(var)) {
            pushCurrentState();   //saves the current state of the search 
            setUpQueue(var);   //adds the relevant arcs for the current variable to the queue before running AC3
            
            if (macAC3()) {
                searchMAC(varList);
            }

            undoPruning();
        } 

        restoreValue(var, val);
    }

    //sets up the queue before running AC3
    public void setUpQueue(Variable var) {
        for (BinaryConstraint constraint : constraints) {
            boolean contains = false;

            if (constraint.getFirstVar() == var.getVar()) {
                for (int i = 0; i < arcsQueue.size(); i++) {
                    if (arcsQueue.get(i).getFirstVar() == constraint.getSecondVar() && arcsQueue.get(i).getSecondVar() == constraint.getFirstVar()) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    arcsQueue.add(new Arc(constraint.getSecondVar(), constraint.getFirstVar()));
                }
            } else if (constraint.getSecondVar() == var.getVar()) {
                for (int i = 0; i < arcsQueue.size(); i++) {
                    if (arcsQueue.get(i).getFirstVar() == constraint.getFirstVar() && arcsQueue.get(i).getSecondVar() == constraint.getSecondVar()) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    arcsQueue.add(new Arc(constraint.getFirstVar(), constraint.getSecondVar()));
                }
            }
        }       
    }
    
    //AC3 algorithm for maintaining arc consistency
    public boolean macAC3() {
        while (arcsQueue.size() != 0) {
            Arc arc = arcsQueue.pop();

            if (revise(arc)) {   //checks if the domain of the variable in the current arc has been pruned
                boolean contains = false;

                for (BinaryConstraint constraint : constraints) {
                    if (constraint.getSecondVar() == arc.getFirstVar() && constraint.getFirstVar() != arc.getSecondVar()) {
                        for (int i = 0; i < arcsQueue.size(); i++) {
                            if (arcsQueue.get(i).getFirstVar() == constraint.getFirstVar() && arcsQueue.get(i).getSecondVar() == constraint.getSecondVar()) {
                                contains = true;
                                break;
                            }
                        }

                        if (!contains) {
                            arcsQueue.add(new Arc(constraint.getFirstVar(), constraint.getSecondVar()));
                        }
                    } else if (constraint.getFirstVar() == arc.getFirstVar() && constraint.getSecondVar() != arc.getSecondVar()) {
                        for (int i = 0; i < arcsQueue.size(); i++) {
                            if (arcsQueue.get(i).getFirstVar() == constraint.getSecondVar() && arcsQueue.get(i).getSecondVar() == constraint.getFirstVar()) {
                                contains = true;
                                break;
                            }
                        }

                        if (contains) {
                            arcsQueue.add(new Arc(constraint.getSecondVar(), constraint.getFirstVar()));
                        }
                    }
                }
            }

            if (emptyDomainFlag) {   //checks if the domain of the variable has been emptied 
                emptyDomainFlag = false;
                return false;
            }
        }

        return true;
    }

    //revises the domain of the current variable
    public boolean revise(Arc arc) {
        List<Integer> updatedDomain = new ArrayList<>();
        boolean changed = false;
        boolean empty = false;
        arcRevisions++;   //increments the arc revisions
        
        for (Variable var : varDomains) {    
            if (var.getVar() == arc.getFirstVar()) {
                for (int i = 0; i < var.domainSize(); i++) {
                    boolean supported = false;

                    for (Variable var2 : varDomains) {
                        if (var2.getVar() == arc.getSecondVar()) {
                            for (int j = 0; j < var2.domainSize(); j++) {
                                for (BinaryConstraint constraint : constraints) {
                                    if (constraint.getFirstVar() == var.getVar() && constraint.getSecondVar() == var2.getVar()) {
                                        for (BinaryTuple tuple : constraint.getTuples()) {
                                            if (tuple.getVal1() == var.getDomain().get(i) && tuple.getVal2() == var2.getDomain().get(j)) {
                                                supported = true;
                                                break;
                                            }                     //Pyramid of Doom (2)
                                        }
                                    } else if (constraint.getFirstVar() == var2.getVar() && constraint.getSecondVar() == var.getVar()) {
                                        for (BinaryTuple tuple : constraint.getTuples()) {
                                            if (tuple.getVal1() == var2.getDomain().get(j) && tuple.getVal2() == var.getDomain().get(i)) {
                                                supported = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!supported) {    
                        changed = true;
                        updatedDomain.add(var.getDomain().get(i));   //adds the unsupported value to be removed from the domain
                    }
                }
            }
        }

        for (int i = 0; i < varDomains.size(); i++) {
            if (varDomains.get(i).getVar() == arc.getFirstVar()) {
                for (int j = 0; j < varDomains.get(i).domainSize(); j++) {
                    if (updatedDomain.contains(varDomains.get(i).getDomain().get(j))) {
                        int index = varDomains.get(i).getDomain().indexOf(varDomains.get(i).getDomain().get(j));
                        varDomains.get(i).getDomain().remove(index);   //removes any unsupported values from the domain
                    }
                }

                if (varDomains.get(i).getDomain().isEmpty()) {
                    empty = true;
                }
            }
        }

        if (empty) {   //checks if a domain has been emptied 
            emptyDomainFlag = true;
            arcsQueue.clear();
            return false;
        }

        return changed;
    } 
    
    //undoes the pruning by restoring the previous state of the search
    private void undoPruning() {
        while (!previousState.isEmpty()) {
            varDomains = previousState.pop();
            break;
        }
    }

    //saves the current state of the search (List<Variable>) to the stack by making a copy of the list
    public void pushCurrentState() {
        List<Variable> copyVarList = new ArrayList<>();

        for (Variable var : varDomains) {
            List<Integer> domain = new ArrayList<>();

            for (int i = 0; i < var.domainSize(); i++) {
                domain.add(var.getDomain().get(i));
            }

            Variable variable = new Variable(var.getVar(), domain);
            copyVarList.add(variable);

        }

        previousState.push(copyVarList);
    }

    //removes the current variable that has been assigned a value
    private List<Variable> removeAssignedVar(List<Variable> varList, Variable removeVar) {
        List<Variable> updatedList = new ArrayList<>();

        for (Variable var : varList) {
            List<Integer> domain = new ArrayList<>();
            Variable variable;

            for (int i = 0; i < var.domainSize(); i++) {
                domain.add(var.getDomain().get(i));
            }

            if (var.getVar() != removeVar.getVar()) {
                variable = new Variable(var.getVar(), domain);
                updatedList.add(variable);
            }
        }

        return updatedList;
    }

    //selects the variable with the smallest domain 
    private Variable selectVar(List<Variable> varList) {
        Variable smallestDomain = varList.get(0);

        for (int i = 0; i < varList.size(); i++) {
            if (smallestDomain.domainSize() > varList.get(i).domainSize()) {
                smallestDomain = varList.get(i);
            }
        }

        return smallestDomain;
    }

    //selects the first value in the domain of the variable in ascending order
    private int selectVal(Variable var) {
        int selectedVal = 0;

        for (int i = 0; i < varDomains.size(); i++) {
            if (varDomains.get(i).getVar() == var.getVar()) {
                varDomains.get(i).getDomain().sort(null);
                selectedVal = varDomains.get(i).getDomain().get(0);
                break;
            }
        }

        return selectedVal;
    }

    //assigns a value to the current variable
    private void assignValue(Variable var, int val) {
        List<Integer> varDomain = new ArrayList<>();
        assignment[var.getVar()] = val;

        //updates the domain of that variable so that the assigned value is the only value in the domain
        for (int i = 0; i < varDomains.size(); i++) {
            if (varDomains.get(i).getVar() == var.getVar()) {
                varDomain.add(val);
                varDomains.get(i).setDomain(varDomain);
                break;
            }
        }
    }

    //unassigns the value from the current variable
    private void unassignValue(Variable var, int val) {
        assignment[var.getVar()] = -1;
    }

    //restores the value to the domain of the current variable
    private void restoreValue(Variable var, int val) {
        for (int i = 0; i < varDomains.size(); i++) {
            if (varDomains.get(i).getVar() == var.getVar()) {
                varDomains.get(i).getDomain().add(val);
                break;
            }
        }
    }

    //delets the value from the domain of the current variable
    private void deleteValue(Variable var, int val) {
        for (int i = 0; i < varDomains.size(); i++) {
            if (varDomains.get(i).getVar() == var.getVar()) {
                int index = varDomains.get(i).getDomain().indexOf(val);
                varDomains.get(i).getDomain().remove(index);
                break;
            }
        }
    }

    //checks if the domain of the current variable is empty after removing a value from its domain
    private boolean domainEmpty(Variable var) {
        boolean empty = false;

        for (int i = 0; i < varDomains.size(); i++) {
            if (varDomains.get(i).getVar() == var.getVar()) {
                empty = varDomains.get(i).getDomain().isEmpty();
                break;
            }
        }

        return empty;
    }

    //checks if a solution has been found
    private boolean solutionFound() {
        for (int i = 0; i < assignment.length; i++) {
            if (assignment[i] == -1) {
                return false;
            }
        }

        return true;
    }

    //prints the solution
    public void printSolution() {
        System.out.println("Solution: ");

        for (int i = 0; i < assignment.length; i++) {
            System.out.println("Variable " + i + " = " + assignment[i]);
        }

        System.out.println("Nodes visited: " + nodesVisited);
        System.out.println("Arc revisions: " + arcRevisions);
        System.out.println("Time taken: " + timeTaken + "ms");
    }
}