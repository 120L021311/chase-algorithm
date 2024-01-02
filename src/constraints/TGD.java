package constraints;

import constraints.Constraint;
import datatype.LabeledNull;
import datatype.Value;
import datatype.Variable;
import symbols.*;
import database.*;

import java.util.*;

/**
 * 这个类用来表示约束关系中的 TGD，其形式为：body -> head
 * 其中 body和 head分别由一个或多个 RelationalAtom 组成，RelationalAtom之间用"and"分隔
 * 例：R(x0,x1,x2) -> Q(x2,z1,z2) and P(x0,z3)
 */
public class TGD extends Constraint {
    private List<RelationalAtom> body;
    private List<RelationalAtom> head;

    public TGD(List<RelationalAtom> body, List<RelationalAtom> head) {
        this.body = body;
        this.head = head;
    }

    public List<RelationalAtom> getBody() {
        return body;
    }

    public List<RelationalAtom> getHead() {
        return head;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("bodyAtoms :");
        for (int i = 0; i < body.size() - 1; i++) {
            stringBuilder.append(body.get(i).toString()).append(",");
        }
        stringBuilder.append(body.get(body.size() - 1).toString()).append("\n");
        stringBuilder.append("headAtoms :");
        for (int i = 0; i < head.size() - 1; i++) {
            stringBuilder.append(head.get(i).toString()).append(",");
        }
        stringBuilder.append(head.get(head.size() - 1).toString()).append("\n");
        return stringBuilder.toString();
    }

    /**
     * 对于某一条 TGD中的一个 relationalAtom，获取这个 relationalAtom在数据库下的所有 Trigger
     * @param database 数据库(实例)
     * @return 这个 relationalAtom在当前数据库下的所有 Trigger
     */
    public List<Trigger> getTriggersFromOneRelationAtom(Database database,RelationalAtom bodyAtom) {
        List<Trigger> triggers = new ArrayList<>();
        //只需考虑body中一个表，得到Variable和Value的映射关系
        String relationName = bodyAtom.getRelationName();
        if (!database.getTableNames().contains(relationName)) {
            return triggers;
        }

        //得到Variable列表
        List<Variable> variableList = bodyAtom.getVariableList();
        Table table = database.getTableWithName(relationName);
        assert table != null;

        //得到一条元组，也就得到了这条元组的属性值列表
        List<Tuple> tuples = table.getTuples();
        assert variableList.size() == table.getAttributeNums();

        //由于List是有序的，只需要按顺序构建对应关系即可
        for (Tuple tuple : tuples) {
            HashMap<Variable, Value> temp = new HashMap<>();
            for (int i = 0; i < variableList.size(); i++) {
                temp.put(variableList.get(i), tuple.getAttributeValue().get(i));
            }
            Trigger trigger = new Trigger(temp);
            triggers.add(trigger);
        }

        return triggers;
    }

    /**
     * 对于某一条 TGD，获取它在数据库下的所有 Trigger
     *
     * @param database 数据库(实例)
     * @return 这条 TGD在现在这个数据库下的所有 Trigger
     */
    public List<Trigger> getTriggers(Database database) {
        if (body.size() == 1) { // 首先处理LinearTGD的情况
            return getTriggersFromOneRelationAtom(database,body.get(0));
        } else { // 处理 TGD的body中有多个 relationalAtom的情况
            List<Trigger> prevTriggers = getTriggersFromOneRelationAtom(database, body.get(0)); // 首先获得第一个 relationalAtom的Triggers
            for (int i = 1; i < body.size(); i++) {
                List<Trigger> currTriggers = getTriggersFromOneRelationAtom(database,body.get(i)); // 获得一个后续的 relationalAtom的Triggers
                List<Trigger> temp = new ArrayList<>();

                //进行基于元组的嵌套循环连接
                for (Trigger prevTrigger : prevTriggers) { //获得第一个关系的每一条元组
                    for (Trigger currTrigger : currTriggers) { //获得第二个关系的每一条元组
                        Trigger newTrigger = prevTrigger.mergeTrigger(currTrigger); // 相当于尝试对两条元组进行连接，如果不满足连接条件则返回null
                        if(newTrigger != null){
                            temp.add(newTrigger);
                        }
                    }
                }

                if(temp.isEmpty()){ // 相当于中间的某一步连接中已经没有一个满足条件的元组了，最终结果为空
                    return new ArrayList<>();
                }
                prevTriggers = temp;
            }
            return prevTriggers;
        }
    }

    /**
     * 对于一条 TGD，得到了满足它条件(body)的 Trigger，检查是否满足它结论(head)中的一个原子
     * 如果满足结论则 Trigger 不活跃，否则 Trigger为活跃的(active)
     *
     * @param database 数据库实例
     * @param trigger  一条满足条件的 Trigger
     * @param headAtom TGD的一个结论
     * @return 结论如果不满足则返回 true，否则返回 false
     */
    public boolean checkActive(Database database, Trigger trigger, RelationalAtom headAtom) {
        boolean ret = true;
        String relationName = headAtom.getRelationName();
        if (!database.getTableNames().contains(relationName)) {
            return ret;
        }

        HashMap<Integer, Value> index_value = getIndexValueMap(trigger, headAtom);
        Table table = database.getTableWithName(relationName);
        assert table != null;

        //检查所有已有元组中是否有满足index_value中条件的元组，如果有则为inactive
        List<Tuple> tuples = table.getTuples();
        for (Tuple tuple : tuples) {
            List<Value> attributeValue = tuple.getAttributeValue();
            int flag = 1; // 假设这条元组满足条件
            for (Map.Entry<Integer, Value> entry : index_value.entrySet()) {
                Integer key = entry.getKey();
                Value value = entry.getValue();
                if (!attributeValue.get(key).equals(value)) {
                    flag = 0; // 实际这条元组不满足条件
                    break;
                }
            }
            if (flag == 1) { //说明每个下标对应值都正确，有满足index_value中条件的元组，返回false
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     * 对于 headAtom指定的表，获得满足结论的元组的下标和对应的属性值应当是什么
     *
     * @param trigger  一条满足前提的 symbols.Trigger
     * @param headAtom TGD的一个结论
     * @return 满足结论的元组的下标和对应的属性值的对应关系
     */
    public HashMap<Integer, Value> getIndexValueMap(Trigger trigger, RelationalAtom headAtom) {
        List<Variable> variableList = headAtom.getVariableList();
        HashMap<Variable, Value> map = trigger.getMap();
        HashMap<Integer, Value> index_value = new HashMap<>(); // 此处保存对于 headAtom 指定的表，满足条件的元组的下标和对应的属性值应当是什么
        for (int i = 0; i < variableList.size(); i++) {
            Variable variable = variableList.get(i);
            if (map.containsKey(variable)) {
                index_value.put(variableList.indexOf(variable), map.get(variable));
            }
        }
        return index_value;
    }

    /**
     * 对于一个数据库实例，应用一条 TGD 对数据库中的数据进行修改
     *
     * @param database 数据库实例
     * @param trigger  一条满足前提的 Trigger
     * @param headAtom TGD的一个结论
     */
    public void apply(Database database, Trigger trigger, RelationalAtom headAtom) {
        String relationName = headAtom.getRelationName();
        List<Variable> variableList = headAtom.getVariableList();
        int attributeNums = variableList.size();
        Value[] attributeValues = new Value[attributeNums];

        Table table;
        if (!database.getTableNames().contains(relationName)) {
            table = database.createTable(relationName);
            table.setAttributeNums(attributeNums);
        } else {
            table = database.getTableWithName(relationName);
        }

        HashMap<Integer, Value> index_value = getIndexValueMap(trigger, headAtom);

        for (Map.Entry<Integer, Value> entry : index_value.entrySet()) {
            Integer key = entry.getKey();
            Value value = entry.getValue();
            attributeValues[key] = value;
        }
        for (int i = 0; i < attributeNums; i++) {
            if (attributeValues[i] == null) {
                attributeValues[i] = new LabeledNull();
            }
        }
        Tuple tuple = new Tuple(Arrays.asList(attributeValues));
        table.insert(tuple);
    }
}

/*
TODO:1.如果元组在表中已经存在，不应该再次插入重复元组？
 —————增加判断元组相等的方法(重写Tuple类的equals()、hashCode()方法)
 */
