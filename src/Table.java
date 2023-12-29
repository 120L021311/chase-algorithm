import java.util.ArrayList;
import java.util.List;

/**
 * 这个类用来表示数据库中的一张表
 */
public class Table {
    private String tableName; // 表名
    private List<String> attributeNames; // 各个属性名
    private int attributeNums; // 用来表示这个表有多少个属性
    private List<Tuple> tuples; // 这个表中的所有元组

    public Table(String tableName) {
        this.tableName = tableName;
        tuples = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public int getAttributeNums() {
        return attributeNums;
    }

    public void setAttributeNums(int attributeNums) {
        this.attributeNums = attributeNums;
    }

    public List<Tuple> getTuples() {
        return tuples;
    }

    public void setTuples(List<Tuple> tuples) {
        this.tuples = tuples;
    }

    public boolean insert(Tuple tuple) {
        if (tuple.getAttributeNums() != attributeNums) {
            System.out.println("元组的属性个数与表的属性个数不同，插入失败！");
            return false;
        } else {
            tuples.add(tuple);
            return true;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("TableName : ").append(tableName).append("\n");
        for (String attributeName : attributeNames) {
            stringBuilder.append(attributeName).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("\n");
        for (Tuple tuple : tuples) {
            stringBuilder.append(tuple.toString());
        }
        return stringBuilder.toString();
    }
}
