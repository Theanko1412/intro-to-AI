package ui;

import java.util.List;

public class Node implements TreeNode {
    public String value;
    public List<TreeNode> children;
    public String parentValue;

    public Node(String value, List<TreeNode> children) {
        this.value = value;
        this.children = children;
    }

    public String toString() {
        return "Node(" + "[" + parentValue + "] " + value + ", " + children + ")";
    }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<TreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<TreeNode> children) {
    this.children = children;
  }

  public String getParentValue() {
    return parentValue;
  }

  public void setParentValue(String parentValue) {
    this.parentValue = parentValue;
  }
}
