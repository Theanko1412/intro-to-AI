package ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Clause {

    List<Literal> literals;

    public Clause(List<Literal> literals) {
        this.literals = literals;
    }

    public List<Literal> getLiterals() {
        return literals;
    }

    public void setLiterals(List<Literal> literals) {
        this.literals = literals;
    }


    @Override
    public String toString() {
        return literals.toString();
    }

    public boolean isEmpty() {
        return literals.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Clause clause = (Clause) o;

        return Objects.equals(literals, clause.literals);
    }

    @Override
    public int hashCode() {
        return literals != null ? literals.hashCode() : 0;
    }

    //if in the same clause A and notA true
    public boolean isIrrelevant() {
        for (Literal literal : literals) {
            if (literals.contains(literal.negate())) {
                return true;
            }
        }
        return false;
    }

    public Set<Literal> getLiteralSet() {
        return new HashSet<>(literals);
    }

    //these 2 functions templates are on the internet, my first thought was making static functions inside the main class
    //that will negate the literals and/or find complements(old artifact that is still there that i didn't have time to change
    //is function negateClause(), so this is not blindly copy-pasted solution
    public boolean hasComplementaryLiteral(Clause other) {
        for (Literal literal1 : literals) {
            for (Literal literal2 : other.getLiterals()) {
                if (literal1.isComplementOf(literal2)) {
                    return true;
                }
            }
        }
        return false;
    }

    //finding if 2 clauses have complementary literals, returning result
    public List<Literal> getComplementaryLiterals(Clause other) {
        List<Literal> resolvedLiterals = new ArrayList<>();

        for (Literal literal1 : literals) {
            boolean hasComplementaryLiteral = false;
            for (Literal literal2 : other.getLiterals()) {
                if (literal1.isComplementOf(literal2)) {
                    hasComplementaryLiteral = true;
                    break;
                }
            }
            if (!hasComplementaryLiteral) {
                resolvedLiterals.add(literal1);
            }
        }
        return resolvedLiterals;
    }
}
