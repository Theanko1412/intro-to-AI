package ui;

enum InputType {
    QUERY,
    ADDITION,
    REMOVAL
}

public class UserInput {

    private Clause input;
    private InputType inputType;

    public Clause getInput() {
        return input;
    }

    public void setInput(Clause input) {
        this.input = input;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public UserInput(Clause input, InputType inputType) {
        this.input = input;
        this.inputType = inputType;
    }

    @Override
    public String toString() {
        if(inputType == InputType.QUERY) {
            return input + " " + "?";
        } else if(inputType == InputType.REMOVAL) {
            return input + " " + "-";
        } else {
            return input + " " + "+";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInput userInput = (UserInput) o;

        if (!input.equals(userInput.input)) return false;
        return inputType == userInput.inputType;
    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + inputType.hashCode();
        return result;
    }
}
