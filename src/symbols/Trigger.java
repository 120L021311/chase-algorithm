package symbols;

import datatype.Value;
import datatype.Variable;

import java.util.HashMap;

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

    @Override
    public String toString() {
        return "Trigger{" +
                "map=" + map +
                '}';
    }
}
