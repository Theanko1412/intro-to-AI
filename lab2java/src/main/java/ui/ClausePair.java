package ui;

public class ClausePair {
    private Clause first;
    private Clause second;

    public ClausePair(Clause first, Clause second) {
        this.first = first;
        this.second = second;
    }

    public Clause getFirst() {
        return first;
    }

    public Clause getSecond() {
        return second;
    }

    public void setFirst(Clause first) {
        this.first = first;
    }

    public void setSecond(Clause second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClausePair that = (ClausePair) o;

        if (!first.equals(that.first)) return false;
        return second.equals(that.second);
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }
}
