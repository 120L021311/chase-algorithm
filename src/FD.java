import java.util.List;

/**
 * This is a class for functional dependency.
 * 函数依赖形式为 left -> right
 * 其中 left 和 right 中可能有多个属性名，用","分隔
 */
public class FD {
    private List<String> left;
    private List<String> right;

    public FD(List<String> left, List<String> right) {
        this.left = left;
        this.right = right;
    }

    public List<String> getLeft() {
        return left;
    }

    public void setLeft(List<String> left) {
        this.left = left;
    }

    public List<String> getRight() {
        return right;
    }

    public void setRight(List<String> right) {
        this.right = right;
    }

    @Override
    public String toString() {
        StringBuilder leftString = new StringBuilder();
        for (int i = 0; i < left.size() - 1; i++) {
            leftString.append(left.get(i)).append(",");
        }
        leftString.append(left.get(left.size() - 1));

        StringBuilder rightString = new StringBuilder();
        for (int i = 0; i < right.size() - 1; i++) {
            rightString.append(right.get(i)).append(",");
        }
        rightString.append(right.get(right.size() - 1));

        return leftString + "->" + rightString;
    }
}
