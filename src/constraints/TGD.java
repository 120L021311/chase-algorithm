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
 * <p>
 * 具有限制：对于任何一个bodyAtom，其内部的 variable都不能重复出现
 * 即不能出现形如 R(x0,x1,x0) -> P(x0,z)
 * 如果希望表示这样的语义，则改写成 R(x0,x1,x2) and EQUAL(x0,x2) -> P(x0,z) 2.27已解决
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
     * 具有限制：对于任何一个bodyAtom，其内部的 variable都不能重复出现
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

//        //由于List是有序的，只需要按顺序构建对应关系即可
//        //问题：无法处理bodyAtom内部的 variable重复出现的TGD，例如：R(x0,x1,x0) -> P(x0,z)
//        for (Tuple tuple : tuples) {
//            HashMap<Variable, Value> temp = new HashMap<>();
//            List<Value> attributeValue = tuple.getAttributeValue();
//            for (int i = 0; i < variableList.size(); i++) {
//                temp.put(variableList.get(i), attributeValue.get(i));
//            }
//            Trigger trigger = new Trigger(temp);
//            triggers.add(trigger);
//        }
        for (Tuple tuple : tuples) {
            HashMap<Variable, Value> temp = new HashMap<>();
            List<Value> attributeValue = tuple.getAttributeValue();
            boolean flag = true; // 用来标记这一条是不是一条合法 trigger
            for (int i = 0; i < variableList.size(); i++) {
                Variable variable = variableList.get(i);
                Value value = attributeValue.get(i);
                if (!temp.containsKey(variable)) {
                    temp.put(variable, value);
                } else { // 为了处理bodyAtom的变量列表中有重复的variable
                    if (!value.equals(temp.get(variable))) {
                        flag = false;
                        break;
                    }
                }
            }
            if (flag) {
                Trigger trigger = new Trigger(temp);
                triggers.add(trigger);
            }
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
            return getTriggersFromOneRelationAtom(database, body.get(0));
        } else { // 处理 TGD的body中有多个 relationalAtom的情况
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
    }

    /**
     * 对于headAtom中原子个数大于1(说明需要对head连接处理)的TGD的apply方法
     *
     * @param database 数据库示例
     * @param trigger 对应的trigger
     */
    public void apply(Database database, Trigger trigger) {
        //首先要获得这条TGD的headAtom中的所有Variable,保存在variables中
        List<Variable> variables = new ArrayList<>();
        List<RelationalAtom> head = this.getHead();
        for (RelationalAtom headAtom : head) {
            List<Variable> variableList = headAtom.getVariableList();
            for (Variable variable : variableList) {
                if (!variables.contains(variable)) {
                    variables.add(variable);
                }
            }
        }

        //拿到trigger中确定的取值
        HashMap<Variable, Value> triggerMap = trigger.getMap();
        Set<Variable> triggerKey = triggerMap.keySet();

        //之后确定每一个Variable对应的取值
         HashMap<Variable,Value> map = new HashMap<>();
        for (Variable variable : variables) {
            if(triggerKey.contains(variable)){
                map.put(variable,triggerMap.get(variable));
            } else {
                map.put(variable,new LabeledNull());
            }
        }

        //根据Variable对应的取值生成相应的元组插入到数据库中
        for (RelationalAtom headAtom : head) {
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

            for (int i = 0; i < attributeNums; i++) {
                attributeValues[i] = map.get(variableList.get(i));
            }

            Tuple tuple = new Tuple(Arrays.asList(attributeValues));
            table.insert(tuple);
        }
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

        //首先获得 trigger中指定了哪些variable对应的value是确定的
        HashMap<Variable, Value> triggerMap = trigger.getMap();
        Set<Variable> frontierVariables = triggerMap.keySet();
        HashMap<Variable, Value> variableValueMap = new HashMap<>(); // 用来保存需要添加的元组中，variable和实际放入实例中的value的映射关系

        for (Variable variable : variableList) {
            if (!variableValueMap.containsKey(variable)) {
                if (frontierVariables.contains(variable)) {
                    variableValueMap.put(variable, triggerMap.get(variable));
                } else {
                    variableValueMap.put(variable, new LabeledNull());
                }
            }
        }

        for (int i = 0; i < attributeNums; i++) {
            Variable variable = variableList.get(i);
            attributeValues[i] = variableValueMap.get(variable);
        }

//        问题：如果headAtom中的自由变量出现重复，生成的元组相应位置会是不同的标记空值，与预期不符
//        HashMap<Integer, Value> index_value = trigger.getIndexValueMap(headAtom);
//
//        for (Map.Entry<Integer, Value> entry : index_value.entrySet()) {
//            Integer key = entry.getKey();
//            Value value = entry.getValue();
//            attributeValues[key] = value;
//        }
//        for (int i = 0; i < attributeNums; i++) {
//            if (attributeValues[i] == null) {
//                attributeValues[i] = new LabeledNull();
//            }
//        }

        Tuple tuple = new Tuple(Arrays.asList(attributeValues));
        table.insert(tuple);
    }

    /**
     * 获得 triggers关于某一个 headAtom的等价类，用于 semi-oblivious chase变种算法中 TGD的触发
     * 处理过程中利用了 headAtom和 body中同时出现的 variable列表来定义等价类
     *
     * @param triggers 根据 TGD的 body进行连接得到的所有 trigger，其中每个 variable只可能出现一次
     * @param headAtom TGD的 head中的某一个 atom
     * @return triggers的等价类
     */
    public List<Trigger> getEquivalentTriggers(List<Trigger> triggers, RelationalAtom headAtom) {
        if (triggers.isEmpty()) {
            return triggers;
        }

        List<Trigger> result = new ArrayList<>();

        //首先得到 trigger(body) 和 headAtom 中共同的 variable列表
        Set<Variable> variables1 = triggers.get(0).getMap().keySet();
        List<Variable> variables2 = headAtom.getVariableList();

        List<Variable> variableList = new ArrayList<>(); // trigger(body) 和 headAtom 中共同的 variable列表
        for (Variable variable : variables2) {
            if (variables1.contains(variable)) {
                variableList.add(variable);
            }
        }

        HashSet<List<Value>> variableListValue = new HashSet<>();
        for (Trigger trigger : triggers) {
            List<Value> tempList = new ArrayList<>();
            HashMap<Variable, Value> triggerMap = trigger.getMap();
            for (Variable variable : variableList) {
                tempList.add(triggerMap.get(variable));
            }

            if (!variableListValue.contains(tempList)) {
                variableListValue.add(tempList);
                result.add(trigger);
            }
        }

        return result;
    }
}

/*
TODO:1.如果元组在表中已经存在，不应该再次插入重复元组？ —————增加判断元组相等的方法(重写Tuple类的equals()、hashCode()方法)
     2.对于semi-oblivious chase变种
     需要对于一堆 triggers根据 headAtom和 body中相同的变量信息获得这个 headAtom对于这堆 triggers的等价类
     实现可以增加一个 getEquivalentTriggers(List<Trigger> triggers,RelationalAtom headAtom)的函数，根据 headAtom和 body中相同的变量信息获得这个 headAtom对于这堆 triggers的等价类
     在处理时先用 triggers = getTriggers(Database database); 获得所有的trigger，再用 equivalentTriggers = getEquivalentTriggers(triggers,headAtom) 获得等价类
     3.对于同一个 bodyAtom中有相同的 variable的 TGD(例如：R(x,y,x) -> R(a,b,x))怎么处理？      完成！
            ————考虑在 getTriggersFromOneRelationAtom(Database database, RelationalAtom bodyAtom)函数中进行修改，在获得 triggers时加入判断，保证获得的 trigger的第1个位置和第3个位置相同 (同样维护 variable到 value的映射)
     4.对于同一个 headAtom中有相同的 variable的 TGD(例如：R(x,y,z) -> R(a,a,x))怎么处理？      完成！
            ————考虑在 apply(Database database, Trigger trigger, RelationalAtom headAtom)函数 中进行修改，先把 variable映射到 value，再把 index 映射到 variable，最后合成
 */
