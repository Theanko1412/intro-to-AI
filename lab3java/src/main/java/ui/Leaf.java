package ui;

public class Leaf implements TreeNode {
  public String value;
  public String name;

  public Leaf(String value) {
    this.value = value;
  }

  public String toString() {
    return "Leaf[" + name + "](" + value + ")";
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
