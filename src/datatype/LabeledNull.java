package datatype;

import java.util.Objects;

/**
 * this is a class for labeled nulls
 * 设定：1.ConstValue对象小于LabeledNull对象
 *      2.LabelNull之间对象之间的比较是对于下标的比较，下标较小的小
 */
public class LabeledNull extends Value {
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

    /**
     * 比较两个LabeledNull对象之间的大小
     * @param anotherLabeledNull 另一个LabeledNull对象
     * @return 大于等于另一个对象返回true，小于返回false
     */
    public boolean compare(LabeledNull anotherLabeledNull){
        int thisSubscript = this.getSubscript();
        int anotherSubscript = anotherLabeledNull.getSubscript();
        return thisSubscript >= anotherSubscript;
    }

    /**
     * 比较LabeledNull对象和ConstValue对象的大小
     * @param constValue ConstValue对象
     * @return 因为LabeledNull对象永远大于ConstValue对象，故直接返回true
     */
    public boolean compare(ConstValue constValue){
        return true;
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

