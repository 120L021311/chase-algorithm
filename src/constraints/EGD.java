package constraints;

import database.Database;
import database.Table;
import database.Tuple;
import datatype.*;
import symbols.EqualAtom;
import symbols.RelationalAtom;
import symbols.Trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EGD extends Constraint {
    private List<RelationalAtom> body;
    private boolean simple = true; // 用来判断EGD的body中是否只有两个RelationalAtom,是则为true
    private List<EqualAtom> head;

    public EGD(List<RelationalAtom> body, List<EqualAtom> head) {
        this.body = body;
        this.head = head;
        if (body.size() > 2) {
            simple = false;
        }
    }

    public boolean isSimple() {
        return simple;
    }

    public List<RelationalAtom> getBody() {
        return body;
    }

    public void setBody(List<RelationalAtom> body) {
        this.body = body;
    }

    public List<EqualAtom> getHead() {
        return head;
    }

    public void setHead(List<EqualAtom> head) {
        this.head = head;
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
     * 对于某一条 EGD中的一个 relationalAtom，获取这个 relationalAtom在数据库下的所有 Trigger
     *
     * @param database 数据库(实例)
     * @return 这个 relationalAtom在当前数据库下的所有 Trigger
     */
    public List<Trigger> getTriggersFromOneRelationAtom(Database database, RelationalAtom bodyAtom) {
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
            List<Value> attributeValue = tuple.getAttributeValue();
            for (int i = 0; i < variableList.size(); i++) {
                temp.put(variableList.get(i), attributeValue.get(i));
            }
            Trigger trigger = new Trigger(temp);
            triggers.add(trigger);
        }

        return triggers;
    }

    /**
     * 对于某一条 EGD，获取它在数据库下的所有 Trigger
     * 适用于处理EGD的body需要连接处理的情况(body.size()>2)
     *
     * @param database 数据库(实例)
     * @return 这条 EGD在现在这个数据库下的所有 Trigger
     */
    public List<Trigger> getTriggers(Database database) {
        List<Trigger> prevTriggers = getTriggersFromOneRelationAtom(database, body.get(0)); // 首先获得第一个 relationalAtom的Triggers
        for (int i = 1; i < body.size(); i++) {
            List<Trigger> currTriggers = getTriggersFromOneRelationAtom(database, body.get(i)); // 获得一个后续的 relationalAtom的Triggers
            List<Trigger> temp = new ArrayList<>();

            //进行基于元组的嵌套循环连接
            for (Trigger prevTrigger : prevTriggers) { //获得第一个关系的每一条元组
                for (Trigger currTrigger : currTriggers) { //获得第二个关系的每一条元组
                    Trigger newTrigger = prevTrigger.mergeTrigger(currTrigger); // 相当于尝试对两条元组进行连接，如果不满足连接条件则返回null
                    if (newTrigger != null) {
                        temp.add(newTrigger);
                    }
                }
            }

            if (temp.isEmpty()) { // 相当于中间的某一步连接中已经没有一个满足条件的元组了，最终结果为空
                return new ArrayList<>();
            }
            prevTriggers = temp;
        }
        return prevTriggers;
    }

    /**
     * 添加一个新的(修改前，修改后)的键值对到 changes 中，并进行映射的标准化
     *
     * @param changes   添加新修改前已有的修改映射关系
     * @param newChange 新的修改映射关系 (newKey,newValue)
     * @return 添加新修改并标准化后的 changes 集合
     */
    @SuppressWarnings({"all"})
    public static HashMap<LabeledNull, Value> normalizeMapping(HashMap<LabeledNull, Value> changes, HashMap<LabeledNull, Value> newChange) {
        if (changes.isEmpty()) {
            return newChange;
        }

        HashMap<LabeledNull, Value> intermediateMapping = combineMapping(changes, newChange);
        HashMap<LabeledNull, Value> result = combineMapping(intermediateMapping, changes);
        return result;
    }

    /**
     * 此函数完成两个映射的合成
     * 举例：如果 u={null_1=null_2,null_3=null_4},w={null_2=null3},则返回值为{null_1=null_2,null_2=null_3,null_3=null_4}
     * 如果 u={null_1=null_3, null_2=null_3, null_3=null_4},w={null_1=null_2,null_3=null_4},则返回值为{null_1=null_4, null_2=null_4, null_3=null_4}
     *
     * @param u 原有映射 u
     * @param w 新来映射 w
     * @return 映射的合成 w●u
     */
    public static HashMap<LabeledNull, Value> combineMapping(HashMap<LabeledNull, Value> u, HashMap<LabeledNull, Value> w) {
        HashMap<LabeledNull, Value> result = new HashMap<>(u);

        for (Map.Entry<LabeledNull, Value> w_entry : w.entrySet()) {
            LabeledNull w_key = w_entry.getKey();
            Value w_value = w_entry.getValue();
            if (!u.containsKey(w_key)) {
                result.put(w_key, w_value);
            }
//            for (Map.Entry<LabeledNull, Value> u_entry : u.entrySet()) {
//                LabeledNull u_key = u_entry.getKey();
//                Value u_value = u_entry.getValue();
//                if (u_value.equals(w_key)) {
//                    result.put(u_key, w_value);
//                } else {
//                    result.put(u_key, u_value);
//                }
//            }

            //2.12修改的新的映射合成方法
            //TODO：遗留问题：如果原有映射u={null_4=null_2},新来映射w={null_4=null_1},合成后结果是否应当为{null_4=null_1}?
            for (Map.Entry<LabeledNull, Value> result_entry : result.entrySet()) {
                LabeledNull result_key = result_entry.getKey();
                Value result_value = result_entry.getValue();
                if (result_value.equals(w_key)) {
                    result.put(result_key, w_value);
                }
            }
        }

        return result;
    }

    /**
     * 对于不满足的相等关系，应用EGD增加一条修改的映射关系，添加到总的映射关系中
     * 应用于EGD是simple的情况，此时不需要嵌套循环连接来获得triggers
     * 分成两种情况：
     * 1.例如 MasterSupp(sid,sa,sn,h) and MasterSupp(sid,sa',sn',h') -> sa=sa' and sn=sn' and h=h',两个RelationalAtom完全对应，可以只扫描一遍
     * 2.例如targethospital(doctor,spec,hospital1,npi1,hconf1) and doctor(npi2,doctor,spec,hospital2,conf2) -> hospital1=hospital2 and npi1=npi2,需要扫描两个表各一遍
     *
     * @param equalAtom 不满足的相等关系
     * @param oldMap    已有的映射修改关系
     * @return 增加新的修改后的总的映射关系
     */
    public HashMap<LabeledNull, Value> apply(Database database, EqualAtom equalAtom, HashMap<LabeledNull, Value> oldMap) {
        RelationalAtom relationalAtom1 = body.get(0); // EGD的body中的第1个RelationalAtom
        RelationalAtom relationalAtom2 = body.get(1); // EGD的body中的第2个RelationalAtom
        HashMap<LabeledNull, Value> result = new HashMap<>(oldMap);

        //先找到同时出现的属性的列表和下标顺序
        List<Variable> relationalAtom1_variableList = relationalAtom1.getVariableList();
        List<Variable> relationalAtom2_variableList = relationalAtom2.getVariableList();
        List<Variable> joinVariable = new ArrayList<>(); // 两个RelationalAtom中出现的相同的Variable
        List<Integer> joinVariableIndex1 = new ArrayList<>(); //相同的Variable在第1个RelationalAtom的变量列表中出现的下标
        List<Integer> joinVariableIndex2 = new ArrayList<>(); //相同的Variable在第2个RelationalAtom的变量列表中出现的下标
        for (Variable variable : relationalAtom1_variableList) {
            if (relationalAtom2_variableList.contains(variable)) {
                joinVariable.add(variable);
                joinVariableIndex1.add(relationalAtom1_variableList.indexOf(variable));
                joinVariableIndex2.add(relationalAtom2_variableList.indexOf(variable));
            }
        }

        //之后处理equalAtom
        Variable leftVariable = equalAtom.getLeftVariable();
        Variable rightVariable = equalAtom.getRightVariable();

        int equalVariableIndex1; // equalAtom中的Variable在第1个RelationalAtom的变量列表中出现的下标
        int equalVariableIndex2; // equalAtom中的Variable在第2个RelationalAtom的变量列表中出现的下标

        if (relationalAtom1_variableList.contains(leftVariable)) {
            equalVariableIndex1 = relationalAtom1_variableList.indexOf(leftVariable);
            equalVariableIndex2 = relationalAtom2_variableList.indexOf(rightVariable);
        } else {
            equalVariableIndex1 = relationalAtom1_variableList.indexOf(rightVariable);
            equalVariableIndex2 = relationalAtom2_variableList.indexOf(leftVariable);
        }

        //之后应该扫描两个表增加修改映射关系
        String tableName1 = relationalAtom1.getRelationName();
        String tableName2 = relationalAtom2.getRelationName();
        Table table1 = database.getTableWithName(tableName1);
        Table table2 = database.getTableWithName(tableName2);
        List<Tuple> tuples1 = table1.getTuples();

        HashMap<List<Value>, Value> map = new HashMap<>();//保存tempKey到value的对应关系

        //如果两个RelationalAtom的关系名、joinVariable、joinVariableIndex、equalVariableIndex都完全相同，可以只扫描一遍
        if (tableName1.equals(tableName2) && joinVariableIndex1.equals(joinVariableIndex2) && equalVariableIndex1 == equalVariableIndex2) {
            for (Tuple tuple : tuples1) {
                List<Value> attributeValue = tuple.getAttributeValue();
                List<Value> joinVariableValue1 = new ArrayList<>();
                for (Integer integer : joinVariableIndex1) {
                    joinVariableValue1.add(attributeValue.get(integer));
                }
                Value equalVariableValue1 = attributeValue.get(equalVariableIndex1);
                if (!map.containsKey(joinVariableValue1)) {
                    map.put(joinVariableValue1, equalVariableValue1);
                } else {
                    Value value = map.get(joinVariableValue1);
                    if (!value.equals(equalVariableValue1)) {
                        if (value instanceof ConstValue && equalVariableValue1 instanceof ConstValue) {
                            System.out.println("chase failed!");
                            return null;
                        } else if (value instanceof ConstValue && equalVariableValue1 instanceof LabeledNull) {
                            HashMap<LabeledNull, Value> newChange = new HashMap<>();
                            newChange.put((LabeledNull) equalVariableValue1, value);
                            result = normalizeMapping(result, newChange);
                        } else if (value instanceof LabeledNull && equalVariableValue1 instanceof ConstValue) {
                            HashMap<LabeledNull, Value> newChange = new HashMap<>();
                            newChange.put((LabeledNull) value, equalVariableValue1);
                            result = normalizeMapping(result, newChange);
                        } else {
                            LabeledNull leftLabeledNull = (LabeledNull) value;
                            LabeledNull rightLabeledNull = (LabeledNull) equalVariableValue1;
                            HashMap<LabeledNull, Value> newChange = new HashMap<>();

                            if (leftLabeledNull.compare(rightLabeledNull)) {
                                newChange.put(leftLabeledNull, rightLabeledNull);
                            } else {
                                newChange.put(rightLabeledNull, leftLabeledNull);
                            }

                            result = normalizeMapping(result, newChange);
                        }
                    }
                }
            }
            return result;
        }

        //如果两个表的Index信息不完全对应，则扫描两遍
        List<Tuple> tuples2 = table2.getTuples();

        for (Tuple tuple : tuples1) {
            List<Value> attributeValue = tuple.getAttributeValue();
            List<Value> joinVariableValue1 = new ArrayList<>();
            for (Integer integer : joinVariableIndex1) {
                joinVariableValue1.add(attributeValue.get(integer));
            }
            Value equalVariableValue1 = attributeValue.get(equalVariableIndex1);
            if (!map.containsKey(joinVariableValue1)) {
                map.put(joinVariableValue1, equalVariableValue1);
            } else {
                if (map.get(joinVariableValue1) instanceof LabeledNull && ((LabeledNull) map.get(joinVariableValue1)).compare(equalVariableValue1)) {
                    map.put(joinVariableValue1, equalVariableValue1);
                }
            }
        }

        for (Tuple tuple : tuples2) {
            List<Value> attributeValue = tuple.getAttributeValue();
            List<Value> joinVariableValue2 = new ArrayList<>();
            for (Integer integer : joinVariableIndex2) {
                joinVariableValue2.add(attributeValue.get(integer));
            }
            Value equalVariableValue2 = attributeValue.get(equalVariableIndex2);
            if (map.containsKey(joinVariableValue2) && !map.get(joinVariableValue2).equals(equalVariableValue2)) {
                Value value = map.get(joinVariableValue2);
                if (value instanceof ConstValue && equalVariableValue2 instanceof ConstValue) {
                    System.out.println("chase failed!");
                    return null;
                } else if (value instanceof ConstValue && equalVariableValue2 instanceof LabeledNull) {
                    HashMap<LabeledNull, Value> newChange = new HashMap<>();
                    newChange.put((LabeledNull) equalVariableValue2, value);
                    result = normalizeMapping(result, newChange);
                } else if (value instanceof LabeledNull && equalVariableValue2 instanceof ConstValue) {
                    HashMap<LabeledNull, Value> newChange = new HashMap<>();
                    newChange.put((LabeledNull) value, equalVariableValue2);
                    result = normalizeMapping(result, newChange);
                } else {
                    LabeledNull leftLabeledNull = (LabeledNull) value;
                    LabeledNull rightLabeledNull = (LabeledNull) equalVariableValue2;
                    HashMap<LabeledNull, Value> newChange = new HashMap<>();

                    if (leftLabeledNull.compare(rightLabeledNull)) {
                        newChange.put(leftLabeledNull, rightLabeledNull);
                    } else {
                        newChange.put(rightLabeledNull, leftLabeledNull);
                    }

                    result = normalizeMapping(result, newChange);
                }
            }
        }

        return result;
    }

    /**
     * 对于不满足的相等关系，应用EGD增加一条修改的映射关系，添加到总的映射关系中
     *
     * @param trigger   满足EGD条件的一个trigger
     * @param equalAtom 不满足的相等关系
     * @param oldMap    已有的映射修改关系
     * @return 增加新的修改后的总的映射关系
     */
    public HashMap<LabeledNull, Value> apply(Trigger trigger, EqualAtom equalAtom, HashMap<LabeledNull, Value> oldMap) {
        HashMap<Variable, Value> variableValueHashMap = trigger.getMap();
        Variable leftVariable = equalAtom.getLeftVariable();
        Variable rightVariable = equalAtom.getRightVariable();

        Value leftVariableValue = variableValueHashMap.get(leftVariable);
        Value rightVariableValue = variableValueHashMap.get(rightVariable);

        if (leftVariableValue instanceof ConstValue && rightVariableValue instanceof ConstValue) {
            System.out.println("chase failed!");
            return null;
        } else if (leftVariableValue instanceof ConstValue && rightVariableValue instanceof LabeledNull) {
            HashMap<LabeledNull, Value> newChange = new HashMap<>();
            newChange.put((LabeledNull) rightVariableValue, leftVariableValue);
            return normalizeMapping(oldMap, newChange);
        } else if (leftVariableValue instanceof LabeledNull && rightVariableValue instanceof ConstValue) {
            HashMap<LabeledNull, Value> newChange = new HashMap<>();
            newChange.put((LabeledNull) leftVariableValue, rightVariableValue);
            return normalizeMapping(oldMap, newChange);
        } else {
            LabeledNull leftLabeledNull = (LabeledNull) leftVariableValue;
            LabeledNull rightLabeledNull = (LabeledNull) rightVariableValue;
            HashMap<LabeledNull, Value> newChange = new HashMap<>();

            if (leftLabeledNull.compare(rightLabeledNull)) {
                newChange.put(leftLabeledNull, rightLabeledNull);
            } else {
                newChange.put(rightLabeledNull, leftLabeledNull);
            }

            return normalizeMapping(oldMap, newChange);
        }
    }
}

/*TODO:1.如果是两个相同的关系组成的body，在连接的时候相当于对自己做笛卡尔乘积，效率很低，需要优化，如何优化？ 完成！
       2.将checkActive()修改成Trigger类下的方法————使用函数重载
         对于TGD:checkActive(Database database,RelationalAtom relationalAtom);     完成！
         对于EGD:checkActive(EqualAtom equalAtom)                                  完成！
       3.调整类的继承关系，抽象TGD和EGD的共同方法到Constraint中       好像没有什么太大的必要？
       4.在将修改同步到数据库实例中时，有的表/元组中没有标记空值，不会发生更新，没有必要进行扫描       完成！
         解决方法：在Table类和Tuple类中加入指导信息(索引)，能够得知某张表或某条元组是否具有标记空值，有哪些标记空值
 */
