import java.util.List;

/**
 * 这个类用来表示数据库中的元组
 */
public class Tuple {
    private int attributeNums; // 元组具有的属性个数
    private List<Value> attributeValues; // 元组的各个属性值

    public Tuple(List<Value> attributeValues) {
        this.attributeValues = attributeValues;
        this.attributeNums = attributeValues.size();
    }

    public int getAttributeNums() {
        return attributeNums;
    }

    public void setAttributeNums(int attributeNums) {
        this.attributeNums = attributeNums;
    }

    public List<Value> getAttributeValue() {
        return attributeValues;
    }

    public void setAttributeValue(List<Value> attributeValues) {
        this.attributeValues = attributeValues;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < attributeNums - 1; i++) {
            stringBuilder.append(attributeValues.get(i).toString()).append(",");
        }
        stringBuilder.append(attributeValues.get(attributeNums-1).toString()).append("\n");
        return stringBuilder.toString();
    }
}
