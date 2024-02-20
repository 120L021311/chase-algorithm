package database;

import datatype.LabeledNull;
import datatype.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * 这个类用来表示数据库中的元组
 */
public class Tuple {
    private int attributeNums; // 元组具有的属性个数
    private List<Value> attributeValues; // 元组的各个属性值
    private boolean containLabeledNull = false;//元组中是否包含标记空值的标记变量
    private HashSet<LabeledNull> labeledNullSet = new HashSet<>(); //元组中含有的标记空值的集合

    public Tuple(List<Value> attributeValues) {
        this.attributeValues = attributeValues;
        this.attributeNums = attributeValues.size();
        for (Value attributeValue : attributeValues) {
            if (attributeValue instanceof LabeledNull) {
                labeledNullSet.add((LabeledNull) attributeValue);
                containLabeledNull = true;
            }
        }
    }

    public boolean isContainLabeledNull() {
        return containLabeledNull;
    }

    public HashSet<LabeledNull> getLabeledNullSet() {
        return labeledNullSet;
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

    /**
     * 完成chase过程中一条元组的更新，修改元组中的标记空值的取值
     * 注意：如果边更新边修改，一定可以保证这条元组全部属性值更新完成后指导信息containLabeledNull和labeledNullSet
     * 取值正确。但是在更新这条元组中间的某一时刻，指导信息可能和元组这一时刻的属性值实际情况不匹配。故在对一条
     * 元组进行更新时，对于这条元组(指导信息)的查询请求可能得到不正确的结果，如果有严格要求，则不可以有其他对于
     * 该条元组的请求。故如果对同一个资源有并发的请求时，需要有阻塞机制(加锁？)
     *
     * @param mapping 应该同步到数据库中的修改映射关系
     */
    public void update(HashMap<LabeledNull, Value> mapping) {
        for (int i = 0; i < attributeValues.size(); i++) {
            Value attributeValue = attributeValues.get(i);
            if (attributeValue instanceof LabeledNull && mapping.containsKey(attributeValue)) {
                attributeValues.set(i, mapping.get(attributeValue));
                labeledNullSet.remove(attributeValue);
                if (mapping.get(attributeValue) instanceof LabeledNull) {
                    labeledNullSet.add((LabeledNull) mapping.get(attributeValue));
                }
            }
        }

        if (labeledNullSet.isEmpty()) {
            containLabeledNull = false;
        }

        //更新一条元组后修改其containLabeledNull和labeledNullSet属性的取值
        //TODO:能否边修改边更新labeledNullSet？
//        HashSet<LabeledNull> temp = new HashSet<>();
//        for (Value attributeValue : attributeValues) {
//            if (attributeValue instanceof LabeledNull) {
//                temp.add((LabeledNull) attributeValue);
//            }
//        }
//        labeledNullSet = temp;
//        if (labeledNullSet.isEmpty()) {
//            containLabeledNull = false;
//        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < attributeNums - 1; i++) {
            stringBuilder.append(attributeValues.get(i).toString()).append(",");
        }
        stringBuilder.append(attributeValues.get(attributeNums - 1).toString()).append("\n");
        return stringBuilder.toString();
    }
}
