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

    public void setContainLabeledNull(boolean containLabeledNull) {
        this.containLabeledNull = containLabeledNull;
    }

    public void setLabeledNullSet(HashSet<LabeledNull> labeledNullSet) {
        this.labeledNullSet = labeledNullSet;
    }

    public boolean insert(Tuple tuple) {
        if (tuple.getAttributeNums() != attributeNums) {
            System.out.println("元组的属性个数与表的属性个数不同，插入失败！");
            return false;
        } else {
            //TODO: 如果元组在表中已经存在，是否还应该再次插入重复元组？—————增加判断元组相等的方法(重写Tuple类的equals()、hashCode()方法)
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

    /**
     * 表之间合并的函数，完成关系代数中"并"运算的功能
     *
     * @param table1 第一张表
     * @param table2 第二张表
     * @return 两张表合并之后的新表
     */
    public static Table tableUnion(Table table1, Table table2) {
        if (!table1.tableName.equals(table2.tableName)) {
            System.out.println("两张表表名不同，无法进行并运算！");
            return null;
        }
        if (table1.attributeNums != table2.attributeNums) {
            System.out.println("两张表属性个数不同，无法进行并运算！");
            return null;
        }
        if (table1.attributeNames != null && table2.attributeNames != null) {
            if (!table1.attributeNames.equals(table2.attributeNames)) {
                System.out.println("两张表属性之间不对应，无法进行并运算！");
                return null;
            }
        }
        Table table = new Table(table1.tableName);
        table.setAttributeNums(table1.attributeNums);
        if (table1.attributeNames != null) {
            table.setAttributeNames(table1.attributeNames);
        } else if (table2.attributeNames != null) {
            table.setAttributeNames(table2.attributeNames);
        }
        table.tuples.addAll(table1.tuples);
        table.tuples.addAll(table2.tuples);
        table.containLabeledNull = table1.containLabeledNull || table2.containLabeledNull;
        table.labeledNullSet.addAll(table1.labeledNullSet);
        table.labeledNullSet.addAll(table2.labeledNullSet);

        return table;
    }

    /**
     * 表之间作差的函数，完成关系代数中"差"运算的功能
     *
     * @param table1 第一张表
     * @param table2 第二张表
     * @return 两张表作差之后新的结果表 table1\table2
     */
    public static Table tableDifference(Table table1, Table table2) {
        if (!table1.tableName.equals(table2.tableName)) {
            System.out.println("两张表表名不同，无法进行差运算！");
            return null;
        }
        if (table1.attributeNums != table2.attributeNums) {
            System.out.println("两张表属性个数不同，无法进行差运算！");
            return null;
        }
        if (table1.attributeNames != null && table2.attributeNames != null) {
            if (!table1.attributeNames.equals(table2.attributeNames)) {
                System.out.println("两张表属性之间不对应，无法进行差运算！");
                return null;
            }
        }

        List<Tuple> tuples_1 = table1.getTuples();
        List<Tuple> tuples_2 = table2.getTuples();

        Table table = new Table(table1.tableName);
        table.setAttributeNums(table1.attributeNums);
        if (table1.attributeNames != null) {
            table.setAttributeNames(table1.attributeNames);
        } else if (table2.attributeNames != null) {
            table.setAttributeNames(table2.attributeNames);
        }

        for (Tuple tuple : tuples_1) {
            if (!tuples_2.contains(tuple)) {
                table.insert(tuple);
            }
        }
        return table;
    }

    /**
     * 在内存中拷贝一个 Table对象
     * @return 拷贝后得到的新的 Table对象
     */
    public Table copyTable(){
        Table res = new Table(tableName);
        res.setAttributeNums(attributeNums);

        ArrayList<String> temp1 = new ArrayList<>();
        if(this.getAttributeNames() != null){
            for (String attributeName : attributeNames) {
                temp1.add(new String(attributeName));
            }
            res.setAttributeNames(temp1);
        }

        ArrayList<Tuple> temp2 = new ArrayList<>();
        for (Tuple tuple : this.getTuples()) {
            //调用tuple的copy方法
            temp2.add(tuple.copyTuple());
        }
        res.setTuples(temp2);

        res.setContainLabeledNull(containLabeledNull);

        HashSet<LabeledNull> temp3 = new HashSet<>();
        for (LabeledNull labeledNull : this.getLabeledNullSet()) {
            temp3.add(labeledNull);
        }
        res.setLabeledNullSet(temp3);

        return res;
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
