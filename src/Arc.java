/*
 * This class represents arcs which are used in the queue for MAC
 */

public class Arc {
    private int firstVar;   //first variable in the arc
    private int secondVar;   //second variable in the arc

    public Arc(int firstVar, int secondVar) {
        this.firstVar = firstVar;
        this.secondVar = secondVar;
    }

    public int getFirstVar() {
        return firstVar;
    }

    public int getSecondVar() {
        return secondVar;
    }
}