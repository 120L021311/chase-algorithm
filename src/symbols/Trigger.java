package symbols;

import constraints.TGD;
import database.Database;
import database.Table;
import database.Tuple;
import datatype.Value;
import datatype.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这个类用来保存一个 trigger，trigger的形式为一个 map
 * map中保存的形式：(变量Variable，属性值Value)
 * 例：对于 LinearTGD：R(x0,x1,x2) -> Q(x2,z1,z2) and P(x0,z3)
 * 假设数据库中的表 R 中有两条元组： (a,b,c) 和 (a1,b1,c1)
 * 则在获得该 LinearTGD 的 triggers时，得到的结果集合为：
 * [Trigger{map={x0=a, x1=b, x2=c}}, Trigger{map={x0=a1, x1=b1, x2=c1}}]
 */
public class Trigger {
    HashMap<Variable, Value> map;

    public Trigger(HashMap<Variable, Value> map) {
        this.map = map;
    }

    public HashMap<Variable, Value> getMap() {
        return map;
    }

    /**
     * 将另一个 Trigger合并到当前 Trigger中，相当于对两条元组(或两个 relationalAtom进行自然连接)
     *
     * @param anotherTrigger 待合并的 Trigger
     * @return 合并后得到的新 Trigger
     */
    public Trigger mergeTrigger(Trigger anotherTrigger) {
        HashMap<Variable, Value> anotherTriggerMap = anotherTrigger.getMap();
        HashMap<Variable, Value> tempMap = new HashMap<>(map);
        boolean hasJoinVariable = false;
        List<Variable> joinVariableList = new ArrayList<>();
        for (Variable anotherTriggerKey : anotherTriggerMap.keySet()) {
            if (tempMap.containsKey(anotherTriggerKey)) {
                hasJoinVariable = true;
                joinVariableList.add(anotherTriggerKey);
            }
        }
        if (!hasJoinVariable) { // 两个 relationalAtom之间没有相同名字的变量，直接把两个Trigger拼在一起返回(相当于对两个表做笛卡尔积)
            tempMap.putAll(anotherTriggerMap);
            return new Trigger(tempMap);
        } else { // 判断两个元组是否满足连接条件
            boolean satisfyJoinCondition = true;
            for (Variable variable : joinVariableList) {
                if (!tempMap.get(variable).equals(anotherTriggerMap.get(variable))) {
                    satisfyJoinCondition = false;
                }
            }
            if (satisfyJoinCondition) {
                tempMap.putAll(anotherTriggerMap);
                return new Trigger(tempMap);
            } else {
                return null;
            }
        }
    }

    /**
     * 对于 headAtom指定的表，获得满足结论的元组的下标和对应的属性值应当是什么
     *
     * @param headAtom TGD的一个结论
     * @return 满足结论的元组的下标和对应的属性值的对应关系
     */
    public HashMap<Integer, Value> getIndexValueMap(RelationalAtom headAtom) {
        List<Variable> variableList = headAtom.getVariableList();
        HashMap<Variable, Value> map = this.getMap();
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
     * 根据一个trigger，获得headAtom中一个原子所有满足取值要求的元组
     *
     * @param headAtom
     * @return
     */
    public List<Trigger> getConditionalTriggersFromOneHeadAtom(Database database, RelationalAtom headAtom) {
        List<Trigger> result = new ArrayList<>();
        String relationName = headAtom.getRelationName();
        if (!database.getTableNames().contains(relationName)) {
            return result;
        }

        Table table = database.getTableWithName(relationName);
        assert table != null;

        List<Tuple> tuples = table.getTuples();

        List<Variable> variableList = headAtom.getVariableList();
        assert variableList.size() == table.getAttributeNums();

        //首先拿到trigger中对于取值的要求
        HashMap<Variable, Value> triggerMap = this.getMap();

        //根据取值的要求，获得headAtom对应表中满足条件的元组形式
        HashMap<Integer, Value> index_value = getIndexValueMap(headAtom);

        //对于表中的每一条元组，依次检查其是否满足index_value的取值要求
        for (Tuple tuple : tuples) {
            boolean flag = true;// 先假设元组满足取值要求
            List<Value> attributeValue = tuple.getAttributeValue();
            for (Map.Entry<Integer, Value> entry : index_value.entrySet()) {
                Integer key = entry.getKey();
                Value value = entry.getValue();
                if (!attributeValue.get(key).equals(value)) {
                    flag = false; // 实际这条元组不满足条件
                    break;
                }
            }
            if (flag) { //元组满足取值要求,应当把元组作为一条结果添加到返回集合中
                HashMap<Variable, Value> temp = new HashMap<>();
                for (int i = 0; i < variableList.size(); i++) {
                    temp.put(variableList.get(i), attributeValue.get(i));
                }
                Trigger trigger = new Trigger(temp);
                result.add(trigger);
            }
        }

        return result;
    }

    /**
     * @param database
     * @param tgd
     * @return
     */
    public List<Trigger> getConditionalTriggers(Database database, TGD tgd) {
        List<RelationalAtom> head = tgd.getHead();
        if (head.size() == 1) {
            return getConditionalTriggersFromOneHeadAtom(database,head.get(0));
        } else {
            List<Trigger> prevConditionalTriggers = getConditionalTriggersFromOneHeadAtom(database,head.get(0));
            for (int i = 1; i < head.size(); i++) {
                List<Trigger> currConditionalTriggers = getConditionalTriggersFromOneHeadAtom(database,head.get(i));
                List<Trigger> temp = new ArrayList<>();

                //进行基于元组的嵌套循环连接
                for (Trigger prevConditionalTrigger : prevConditionalTriggers) { //获得第一个关系的每一条元组
                    for (Trigger currConditionalTrigger : currConditionalTriggers) { //获得第二个关系的每一条元组
                        Trigger newConditionalTrigger = prevConditionalTrigger.mergeTrigger(currConditionalTrigger); // 相当于尝试对两条元组进行连接，如果不满足连接条件则返回null
                        if (newConditionalTrigger != null) {
                            temp.add(newConditionalTrigger);
                        }
                    }
                }

                if(temp.isEmpty()){
                    return new ArrayList<>();
                }
                prevConditionalTriggers = temp;
            }
            return prevConditionalTriggers;
        }
    }

    public boolean checkActive(Database database,TGD tgd){ // 这里面的tgd的headAtom个数一定>1
        List<Trigger> conditionalTriggers = this.getConditionalTriggers(database, tgd);
        return conditionalTriggers.isEmpty();
    }

    /**
     * 对于一条 TGD，得到了满足它条件(body)的 Trigger，检查是否满足它结论(head)中的一个原子
     * 如果满足结论则 Trigger 不活跃，否则 Trigger为活跃的(active)
     *
     * @param database 数据库实例
     * @param headAtom TGD的一个结论
     * @return 结论如果不满足则返回 true，否则返回 false
     */
    public boolean checkActive(Database database, RelationalAtom headAtom) {
        boolean ret = true;
        String relationName = headAtom.getRelationName();
        if (!database.getTableNames().contains(relationName)) {
            return ret;
        }

        HashMap<Integer, Value> index_value = getIndexValueMap(headAtom);
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
     * 对于满足body的EGD，能够获得Trigger，检查这个Trigger对于某一个EqualAtom是否是active的
     *
     * @param equalAtom head中的某一个EqualAtom
     * @return 如果h(xi)=h(xj)，则返回false，否则返回true
     */
    public boolean checkActive(EqualAtom equalAtom) {
        HashMap<Variable, Value> variableValueHashMap = this.getMap();
        Variable leftVariable = equalAtom.getLeftVariable();
        Variable rightVariable = equalAtom.getRightVariable();

        Value leftVariableValue = variableValueHashMap.get(leftVariable);
        Value rightVariableValue = variableValueHashMap.get(rightVariable);

        return !leftVariableValue.equals(rightVariableValue);
    }

    @Override
    public String toString() {
        return "Trigger{" +
                "map=" + map +
                '}';
    }
}
