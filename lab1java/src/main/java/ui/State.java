package ui;



public class State implements Comparable<State> {
    State parentState;
    String state;
    Double price;
    Double totalPrice;
    Double totalPriceWithHeuristic;

    public State(String state) {
        this.state = state;
        this.price = null;
        this.parentState = null;
        this.totalPrice = null;
        this.totalPriceWithHeuristic = null;
    }
    public State(String state, Double price) {
        this.state = state;
        this.price = price;
        this.parentState = null;
    }


    public State(String state, Double price, State parentState) {
        this.state = state;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
                ", state='" + state + '\'' +
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
