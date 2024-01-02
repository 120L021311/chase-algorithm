package symbols;

import datatype.Value;
import datatype.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 这个类用来保存一个 trigger，trigger的形式为一个 map
 * map中保存的形式：(变量Variable，属性值Value)
 * 例：对于 LinearTGD：R(x0,x1,x2) -> Q(x2,z1,z2) and P(x0,z3)
 *     假设数据库中的表 R 中有两条元组： (a,b,c) 和 (a1,b1,c1)
 *     则在获得该 LinearTGD 的 triggers时，得到的结果集合为：
 *     [Trigger{map={x0=a, x1=b, x2=c}}, Trigger{map={x0=a1, x1=b1, x2=c1}}]
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
     * @param anotherTrigger 待合并的 Trigger
     * @return 合并后得到的新 Trigger
     */
    public Trigger mergeTrigger(Trigger anotherTrigger){
        HashMap<Variable, Value> anotherTriggerMap = anotherTrigger.getMap();
        HashMap<Variable, Value> tempMap = new HashMap<>(map);
        boolean hasJoinVariable = false;
        List<Variable> joinVariableList = new ArrayList<>();
        for (Variable anotherTriggerKey : anotherTriggerMap.keySet()) {
            if(tempMap.containsKey(anotherTriggerKey)){
                hasJoinVariable = true;
                joinVariableList.add(anotherTriggerKey);
            }
        }
        if(!hasJoinVariable){ // 两个 relationalAtom之间没有相同名字的变量，直接把两个Trigger拼在一起返回(相当于对两个表做笛卡尔积)
            tempMap.putAll(anotherTriggerMap);
            return new Trigger(tempMap);
        } else { // 判断两个元组是否满足连接条件
            boolean satisfyJoinCondition = true;
            for (Variable variable : joinVariableList) {
                if(!tempMap.get(variable).equals(anotherTriggerMap.get(variable))){
                    satisfyJoinCondition = false;
                }
            }
            if(satisfyJoinCondition){
                tempMap.putAll(anotherTriggerMap);
                return new Trigger(tempMap);
            } else {
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return "Trigger{" +
                "map=" + map +
                '}';
    }
}
