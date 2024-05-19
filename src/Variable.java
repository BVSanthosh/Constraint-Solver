import java.util.List;

/*
 * This class represents a variable in a CSP.
 */

public class Variable {
    private int var;   //the variable 
    private List<Integer> domain;   //its domain

    public Variable(int var, List<Integer> domain) {
        this.var = var;
        this.domain = domain;
    }

    public int getVar() {
        return var;
    }

    public List<Integer> getDomain() {
        return domain;
    }

    public void setDomain(List<Integer> domain) {
        this.domain = domain;
    }

    public int domainSize() {
        return domain.size();
    }
}