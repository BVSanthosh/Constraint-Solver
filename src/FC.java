import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/* 
 * This class contains the Forward Checking algorithm for solving a binary CSP using 2-way search.
 */

public class FC {
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

    public FC(BinaryCSP csp) {
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
    }

    //starts the search
    public void solve() {
        startTime = System.currentTimeMillis();
        searchFC(varDomains);
    }

    //recursively searches for a solution
    private void searchFC(List<Variable> varList) {
        if (solutionFound()) {
            endTime = System.currentTimeMillis();
            timeTaken = endTime - startTime;
            printSolution();
            System.exit(0);
        }

        nodesVisited++;   //increments the number of nodes visited

        Variable var = selectVar(varList);
        int val = selectVal(var);

        branchLeft(varList, var, val);
        branchRight(varList, var, val);
    }

    //branches left and assigns a value to the current variable
    private void branchLeft(List<Variable> varList, Variable var, int val){
        pushCurrentState();    //saves the current state of the search (List<Variable>) to the stack
        assignValue(var, val);

        if (reviseFutureArcs(varList, var)) {
            List<Variable> updatedList = removeAssignedVar(varList, var);   //passes the updated list of variables for the search
            searchFC(updatedList);
        }

        undoPruning();
        unassignValue(var, val);
    }

    //branches right and removes the current value assigned from  the domain of the variable
    private void branchRight(List<Variable> varList, Variable var, int val){
        deleteValue(var, val);

        if (!domainEmpty(var)) {
            pushCurrentState();   //saves the current state of the search (List<Variable>) to the stack

            if (reviseFutureArcs(varList, var)) {
                searchFC(varList);
            }

            undoPruning();
        } 

        restoreValue(var, val);
    }

    //revises all the future arcs 
    private boolean reviseFutureArcs(List<Variable> varList, Variable var) {
        boolean consistent = true;

        for (int i = 0; i < varList.size(); i++) {
            if (varList.get(i).getVar() != var.getVar()) {
                consistent = revise(var, varList.get(i));

                if (!consistent) {
                    return false;
                }
            }
        }

        return true;
    }

    //revises the arc between the current variable and the future variable
    private boolean revise(Variable var, Variable futureVar) {
        List<Integer> newDomain = new ArrayList<>();
        boolean pruned = false;
        arcRevisions++;  //increments the number of arc revisions

        //goes through all the constraints and checks if the current variable and future variable are in the constraint
        for (BinaryConstraint bc : constraints) {
            if (bc.getFirstVar() == var.getVar() && bc.getSecondVar() == futureVar.getVar()) {
                for(int i = 0; i < varDomains.size(); i++) {
                    if (varDomains.get(i).getVar() == var.getVar()) {
                        for (int j = 0; j < varDomains.get(i).domainSize(); j++) {
                            for (BinaryTuple tuple : bc.getTuples()) {
                                if (tuple.getVal1() == varDomains.get(i).getDomain().get(j)) {
                                    newDomain.add(tuple.getVal2());   //adds valid values to the new domain of the future variable
                                    pruned = true;
                                }
                            }
                        }                //Pyramid of Doom (1)
                    }
                }
            } else if (bc.getFirstVar() == futureVar.getVar() && bc.getSecondVar() == var.getVar()) {
                for(int i = 0; i < varDomains.size(); i++) {
                    if (varDomains.get(i).getVar() == var.getVar()) {
                        for (int j = 0; j < varDomains.get(i).domainSize(); j++) {
                            for (BinaryTuple tuple : bc.getTuples()) {
                                if (tuple.getVal2() == varDomains.get(i).getDomain().get(j)) {  //
                                    newDomain.add(tuple.getVal1());   //adds valid values to the new domain of the future variable
                                    pruned = true;
                                }
                            }
                        }
                    }
                } 
            }
        }

        //checks if the domain of the future variable has been pruned
        if (pruned) {
            List<Integer> updatedDomain = new ArrayList<>();

            for (int num = 0; num < varDomains.size(); num++) {
                if (varDomains.get(num).getVar() == futureVar.getVar()) {
                    for (int i = 0; i < varDomains.get(num).domainSize(); i++) {
                        updatedDomain.add(varDomains.get(num).getDomain().get(i));
                    }

                    for (int j = 0; j < varDomains.get(num).domainSize(); j++) {
                        if (!newDomain.contains(varDomains.get(num).getDomain().get(j))) {
                            int index = updatedDomain.indexOf(varDomains.get(num).getDomain().get(j));
                            updatedDomain.remove(index);
                        }
                    }

                    varDomains.get(futureVar.getVar()).setDomain(updatedDomain);   //updates the domain of the future variable
                }
            }

            if (updatedDomain.isEmpty()) {   //checks if the domain of the future variable is empty
                return false;
            } 
        }

        return true;
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