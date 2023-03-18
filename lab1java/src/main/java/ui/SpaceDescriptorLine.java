package ui;

import java.util.List;

public class SpaceDescriptorLine {
    String state;
    List<State> nextStateList;

    public SpaceDescriptorLine(String state, List<State> nextStateList) {
        this.state = state;
        this.nextStateList = nextStateList;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<State> getNextStateList() {
        return nextStateList;
    }

    public void setNextStateList(List<State> nextStateList) {
        this.nextStateList = nextStateList;
    }

    @Override
    public String toString() {
        return "{" +
                "state='" + state + '\'' +
                ", nextStateList=" + nextStateList +
                '}';
    }
}
