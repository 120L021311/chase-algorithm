import java.util.ArrayList;
import java.util.Objects;

/**
 * this is a class for labeled nulls
 */
public class LabeledNull extends Data{
    private static int nextSubscript = 1;
    private int subscript;

    public LabeledNull() {
        this.subscript = nextSubscript;
        nextSubscript++;
    }

    public int getSubscript() {
        return subscript;
    }

    public void setSubscript(int subscript) {
        this.subscript = subscript;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof LabeledNull) {
            LabeledNull o1 = (LabeledNull) o;
            return o1.getSubscript() == this.subscript;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscript);
    }

    @Override
    public String toString() {
        return "null_" + subscript;
    }

}

