import java.util.Objects;

/**
 * 这个类用来表示数据库中的常量值
 * 目前支持的数据类型为int，double，string和boolean
 */
public class ConstValue extends Value {
    private int constType; // 用来表示这个位置存放的是int(0)，double(1)，string(2)，boolean(3)
    private int intValue;
    private double doubleValue;
    private String stringValue;
    private boolean booleanValue;

    public ConstValue(int intValue) {
        this.constType = 0;
        this.intValue = intValue;
    }

    public ConstValue(double doubleValue) {
        this.constType = 1;
        this.doubleValue = doubleValue;
    }

    public ConstValue(String stringValue) {
        this.constType = 2;
        this.stringValue = stringValue;
    }

    public ConstValue(boolean booleanValue) {
        this.constType = 3;
        this.booleanValue = booleanValue;
    }

    public int getConstType() {
        return constType;
    }

    public int getIntValue() {
        return intValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    @Override
    public String toString() {
        if (constType == 0) {
            return Integer.toString(intValue);
        } else if (constType == 1) {
            return Double.toString(doubleValue);
        } else if (constType == 2) {
            return stringValue;
        } else {
            return Boolean.toString(booleanValue);
        }
    }

    @Override
    // TODO:编写逻辑来设定什么情况下算常量值相等？(数据库同一列中 14 和 14.0 是否相等？)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstValue that = (ConstValue) o;
        if (this.getConstType() != that.getConstType()) {
            return false;
        } else if (this.getConstType() == 0) {
            return this.getIntValue() == that.getIntValue();
        } else if (this.getConstType() == 1) {
            return this.getDoubleValue() == that.getDoubleValue();
        } else if (this.getConstType() == 2) {
            return this.getStringValue().equals(that.getStringValue());
        } else {
            return this.getBooleanValue() == that.getBooleanValue();
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(constType, intValue, doubleValue, stringValue, booleanValue);
    }
}
