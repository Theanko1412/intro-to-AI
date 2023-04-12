package ui;

import java.util.Objects;

public class Literal {
    String variable;
    boolean isNegated;

    boolean isGoal;

    public Literal(String variable, boolean isNegated, boolean isGoal) {
        this.variable = variable;
        this.isNegated = isNegated;
        this.isGoal = isGoal;
    }

    public Literal(String variable, boolean isNegated) {
        this.variable = variable;
        this.isNegated = isNegated;
        this.isGoal = false;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public boolean isNegated() {
        return isNegated;
    }

    public void setNegated(boolean negated) {
        isNegated = negated;
    }

    public boolean isGoal() {
        return isGoal;
    }

    public void setGoal(boolean goal) {
        isGoal = goal;
    }

    @Override
    public String toString() {
        return (isNegated ? "~" : "") + variable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable, isNegated);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Literal other = (Literal) obj;
        return isNegated == other.isNegated && Objects.equals(variable, other.variable);
    }

    public Literal negate() {
        return new Literal(variable, !isNegated);
    }

    //these 2 functions templates are on the internet, my first thought was making static functions inside the main class
    //that will negate the literals and/or find complements(old artifact that is still there that i didn't have time to change
    //is function negateClause(), so this is not blindly copy-pasted solution
    public boolean isComplementOf(Literal other) {
        return variable.equals(other.getVariable()) && isNegated != other.isNegated();
    }
}
