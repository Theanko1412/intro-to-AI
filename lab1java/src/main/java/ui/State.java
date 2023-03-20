package ui;



public class State implements Comparable<State> {
    State parentState;
    String name;
    Double price;
    Double totalPrice;
    Double totalPriceWithHeuristic;

    public State(String name) {
        this.name = name;
        this.price = null;
        this.parentState = null;
        this.totalPrice = null;
        this.totalPriceWithHeuristic = null;
    }
    public State(String name, Double price) {
        this.name = name;
        this.price = price;
        this.parentState = null;
    }


    public State(String name, Double price, State parentState) {
        this.name = name;
        this.price = price;
        this.parentState = parentState;
    }

    public Double getTotalPriceWithHeuristic() {
        return totalPriceWithHeuristic;
    }

    public void setTotalPriceWithHeuristic(Double totalPriceWithHeuristic) {
        this.totalPriceWithHeuristic = totalPriceWithHeuristic;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
        this.totalPriceWithHeuristic = totalPrice;
    }

    public State getParentState() {
        return parentState;
    }

    public void setParentState(State parentState) {
        this.parentState = parentState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "State{" +
                "parentState=" + parentState +
                ", state='" + name + '\'' +
                ", price=" + price +
                ", totalPrice=" + totalPrice +
                ", totalPriceWithHeuristic=" + totalPriceWithHeuristic +
                '}';
    }

    @Override
    public int compareTo(State state) {
        return this.totalPriceWithHeuristic.compareTo(state.totalPriceWithHeuristic);
    }
}
