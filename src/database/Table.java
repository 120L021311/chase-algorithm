package database;

import datatype.LabeledNull;
import datatype.Value;

import java.util.*;

/**
 * 这个类用来表示数据库中的一张表
 */
public class Table {
    private String tableName; // 表名
    private List<String> attributeNames; // 各个属性名
    private int attributeNums; // 用来表示这个表有多少个属性
    private List<Tuple> tuples; // 这个表中的所有元组
    private boolean containLabeledNull = false;//表中是否包含标记空值的标记变量
    private HashSet<LabeledNull> labeledNullSet = new HashSet<>(); //表中含有的标记空值的集合

    public Table(String tableName) {
        this.tableName = tableName;
        tuples = new ArrayList<>();
    }

    public boolean isContainLabeledNull() {
        return containLabeledNull;
    }

    public HashSet<LabeledNull> getLabeledNullSet() {
        return labeledNullSet;
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
            //TODO: 如果元组在表中已经存在，不应该再次插入重复元组？—————增加判断元组相等的方法(重写Tuple类的equals()、hashCode()方法)
            tuples.add(tuple);

            //判断是否需要修改containLabeledNull和labeledNullSet
            if (tuple.isContainLabeledNull()) { // 如果新插入的元组中包含标记空值，可能需要对表的标记变量做出修改
                if (!containLabeledNull) {
                    containLabeledNull = true;
                }
                labeledNullSet.addAll(tuple.getLabeledNullSet());
            }
            return true;
        }
    }

    /**
     * 完成chase过程中一张表的更新，修改元组中的标记空值的取值
     * 同样需要思考指导信息和并发请求之间的冲突(类似于tuple的冲突)
     *
     * @param mapping 应该同步到数据库中的修改映射关系
     */
    public void updateTable(HashMap<LabeledNull, Value> mapping) {
        HashSet<LabeledNull> mappingKeySet = new HashSet<>(mapping.keySet());
        for (Tuple tuple : tuples) {
            if (tuple.isContainLabeledNull() && !intersection(mappingKeySet, tuple.getLabeledNullSet()).isEmpty()) {
                tuple.update(mapping);
                labeledNullSet.addAll(tuple.getLabeledNullSet());
            }
        }

        labeledNullSet.removeAll(mappingKeySet);
        if (labeledNullSet.isEmpty()) {
            containLabeledNull = false;
        }

//        //更新完表中所有元组后修改这个表的containLabeledNull和labeledNullSet属性的取值
//        //TODO:能否边修改元组边更新labeledNullSet？
//        HashSet<LabeledNull> temp = new HashSet<>();
//        for (Tuple tuple : tuples) {
//            temp.addAll(tuple.getLabeledNullSet());
//        }
//        labeledNullSet = temp;
//        if (labeledNullSet.isEmpty()) {
//            containLabeledNull = false;
//        }
    }

    /**
     * 获得两个HashSet<LabeledNull>的交集，用于进行是否需要更新的判断
     *
     * @param set1 第一个HashSet<LabeledNull>
     * @param set2 第二个HashSet<LabeledNull>
     * @return 交集
     */
    public HashSet<LabeledNull> intersection(HashSet<LabeledNull> set1, HashSet<LabeledNull> set2) {
        HashSet<LabeledNull> result = new HashSet<>();
        for (LabeledNull labeledNull : set1) {
            if (set2.contains(labeledNull)) {
                result.add(labeledNull);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("TableName : ").append(tableName).append("\n");
        if (attributeNames != null) { // 对于某些应用 TGD 新生成的表，可能不存在表头attributeNames
            for (String attributeName : attributeNames) {
                stringBuilder.append(attributeName).append(",");
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("\n");
        for (Tuple tuple : tuples) {
            stringBuilder.append(tuple.toString());
        }
        return stringBuilder.toString();
    }
}
