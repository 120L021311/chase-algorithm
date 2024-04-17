import constraints.TGD;
import datatype.ConstValue;
import datatype.Value;
import datatype.Variable;
import org.junit.Test;
import symbols.Trigger;
import util.ReadFiles;

import java.util.HashMap;
import java.util.List;

public class TriggerTest {

    @Test
    public void testMergeTrigger(){
//        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
//        TGD tgd = tgds.get(0);
//        HashMap<Variable, Value> triggerMap1 = new HashMap<>();
//        triggerMap1.put(new Variable("x0"),new ConstValue("a"));
//        triggerMap1.put(new Variable("x1"),new ConstValue("b"));
//        triggerMap1.put(new Variable("x2"),new ConstValue("c"));
//        Trigger trigger1 = new Trigger(triggerMap1);
//        System.out.println(trigger1);
//        HashMap<Variable, Value> triggerMap2 = new HashMap<>();
//        triggerMap2.put(new Variable("x3"),new ConstValue("e"));
//        triggerMap2.put(new Variable("x4"),new ConstValue("f"));
//        triggerMap2.put(new Variable("x5"),new ConstValue("g"));
//        Trigger trigger2 = new Trigger(triggerMap2);
//        System.out.println(trigger2);
//        Trigger trigger = trigger1.mergeTrigger(trigger2);
//        System.out.println(trigger); // Trigger{map={x0=a, x1=b, x2=c, x3=e, x4=f, x5=g}}

//        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
//        TGD tgd = tgds.get(1);
//        HashMap<Variable, Value> triggerMap1 = new HashMap<>();
//        triggerMap1.put(new Variable("x0"),new ConstValue("a"));
//        triggerMap1.put(new Variable("x1"),new ConstValue("b"));
//        triggerMap1.put(new Variable("x2"),new ConstValue("c"));
//        Trigger trigger1 = new Trigger(triggerMap1);
//        System.out.println(trigger1);
//        HashMap<Variable, Value> triggerMap2 = new HashMap<>();
//        triggerMap2.put(new Variable("x2"),new ConstValue("c"));
//        triggerMap2.put(new Variable("x3"),new ConstValue("d"));
//        triggerMap2.put(new Variable("x4"),new ConstValue("e"));
//        Trigger trigger2 = new Trigger(triggerMap2);
//        System.out.println(trigger2);
//        Trigger trigger = trigger1.mergeTrigger(trigger2);
//        System.out.println(trigger); // Trigger{map={x0=a, x1=b, x2=c, x3=d, x4=e}}

//        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD_test/test1/tgd.txt");
//        TGD tgd = tgds.get(2);
//        HashMap<Variable, Value> triggerMap1 = new HashMap<>();
//        triggerMap1.put(new Variable("x0"),new ConstValue("a"));
//        triggerMap1.put(new Variable("x1"),new ConstValue("b"));
//        triggerMap1.put(new Variable("x2"),new ConstValue("c"));
//        Trigger trigger1 = new Trigger(triggerMap1);
//        System.out.println(trigger1);
//        HashMap<Variable, Value> triggerMap2 = new HashMap<>();
//        triggerMap2.put(new Variable("x1"),new ConstValue("b"));
//        triggerMap2.put(new Variable("x2"),new ConstValue("c"));
//        triggerMap2.put(new Variable("x3"),new ConstValue("g"));
//        Trigger trigger2 = new Trigger(triggerMap2);
//        System.out.println(trigger2);
//        Trigger trigger = trigger1.mergeTrigger(trigger2);
//        System.out.println(trigger); // Trigger{map={x0=a, x1=b, x2=c, x3=g}}

        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD_test/test4/TGD.txt");
        TGD tgd = tgds.get(0);
        HashMap<Variable, Value> triggerMap1 = new HashMap<>();
        triggerMap1.put(new Variable("x"),new ConstValue("a"));
        triggerMap1.put(new Variable("y"),new ConstValue("b"));
        triggerMap1.put(new Variable("z"),new ConstValue("c"));
        Trigger trigger1 = new Trigger(triggerMap1);
        System.out.println(trigger1);
        HashMap<Variable, Value> triggerMap2 = new HashMap<>();
        triggerMap2.put(new Variable("z"),new ConstValue("c"));
        triggerMap2.put(new Variable("p"),new ConstValue("d"));
        triggerMap2.put(new Variable("q"),new ConstValue("e"));
        Trigger trigger2 = new Trigger(triggerMap2);
        System.out.println(trigger2);
        Trigger trigger = trigger1.mergeTrigger(trigger2);
        System.out.println(trigger); // Trigger{map={x0=a, x1=b, x2=c, x3=g}}
    }
}
