import constraints.EGD;
import database.*;
import datatype.Data;
import datatype.LabeledNull;
import datatype.Value;
import org.junit.Test;
import symbols.EqualAtom;
import symbols.Trigger;
import util.ReadFiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class EGDTest {

    @Test
    public void testEqualAtom() {
        EqualAtom equalAtom = new EqualAtom("sa=sa'");
        System.out.println(equalAtom.getLeftVariable());
        System.out.println(equalAtom.getRightVariable());
    }

    @Test
    public void testEGD() {
        Database database = new Database();
        database.setInputDirectory("examples/EGD_test/test1");
        database.initializeDatabase();

        EGD egd = ReadFiles.parseEGD("MasterSupp(sid,sa,sn,h) and MasterSupp(sid,sa',sn',h') -> sa=sa' and sn=sn' and h=h'");
        System.out.println(egd);
        System.out.println(egd.getBody());
        System.out.println(egd.getHead());
    }

    @Test
    public void testCheckActive() {
        Database database = new Database();
        database.setInputDirectory("examples/EGD_test/test1");
        database.initializeDatabase();

        EGD egd = ReadFiles.parseEGD("targethospital(doctor,spec,hospital1,npi1,hconf1) and doctor(npi2,doctor,spec,hospital2,conf2) -> hospital1=hospital2 and npi1=npi2");
        List<EqualAtom> head = egd.getHead();
        HashSet<Table> tables = database.getTables();
        for (Table table : tables) {
            System.out.println(table.getTableName());
            List<Tuple> tuples = table.getTuples();
            for (Tuple tuple : tuples) {
                System.out.println(tuple);
            }
        }

        List<Trigger> triggers = egd.getTriggers(database);
        for (Trigger trigger : triggers) {
            System.out.println(trigger);
            for (EqualAtom equalAtom : head) {
                System.out.println(equalAtom);
                boolean b = trigger.checkActive(equalAtom);
                System.out.println(b);
            }
        }

    }


    @Test
    public void testCombineMapping() {
        LabeledNull null_1 = new LabeledNull();
        LabeledNull null_2 = new LabeledNull();
        LabeledNull null_3 = new LabeledNull();
        LabeledNull null_4 = new LabeledNull();
        HashMap<LabeledNull, Value> u = new HashMap<>();
        HashMap<LabeledNull, Value> w = new HashMap<>();
        u.put(null_1, null_2);
        u.put(null_3, null_4);
        w.put(null_2, null_3);
        HashMap<LabeledNull, Value> w_u = EGD.combineMapping(u, w);
        System.out.println(w_u);
        HashMap<LabeledNull, Value> u_w_u = EGD.combineMapping(w_u, u);
        System.out.println(u_w_u);
    }

    @Test
    public void testNormalizeMapping() {
        LabeledNull null_1 = new LabeledNull();
        LabeledNull null_2 = new LabeledNull();
        LabeledNull null_3 = new LabeledNull();
        LabeledNull null_4 = new LabeledNull();
        HashMap<LabeledNull, Value> changes = new HashMap<>();
        changes.put(null_1, null_2);
        changes.put(null_3, null_4);
        HashMap<LabeledNull, Value> newChange = new HashMap<>();
        newChange.put(null_2, null_3);
        HashMap<LabeledNull, Value> map = EGD.normalizeMapping(changes, newChange);
        System.out.println(map);
    }

    @Test
    public void testApply() {
        Database database = new Database();
        database.setInputDirectory("examples/test");
        database.initializeDatabase();

//        EGD egd = ReadFiles.parseEGD("targethospital(doctor,spec,hospital1,npi1,hconf1) and doctor(npi2,doctor,spec,hospital2,conf2) -> hospital1=hospital2 and npi1=npi2");
//        List<EqualAtom> head = egd.getHead();

        List<EGD> egds = ReadFiles.readEGDs("examples/test/EGD.txt");

        HashSet<Table> tables = database.getTables();
        for (Table table : tables) {
            System.out.println(table.getTableName());
            List<Tuple> tuples = table.getTuples();
            for (Tuple tuple : tuples) {
                System.out.println(tuple);
            }
        }


        for (int j = 0; j < 2; j++) {
            int i = 1;
            for (EGD egd : egds) {
                System.out.println("第" + i + "条EGD");
                HashMap<LabeledNull, Value> mapping = new HashMap<>();
                List<EqualAtom> head = egd.getHead();
                List<Trigger> triggers = egd.getTriggers(database);
                for (Trigger trigger : triggers) {
//                System.out.println(trigger);
                    for (EqualAtom equalAtom : head) {
                        if (trigger.checkActive(equalAtom)) {
                            mapping = egd.apply(trigger, equalAtom, mapping);
                            System.out.println("mapping :" + mapping);
                        }
                    }
                }
                for (Table table : tables) {
                    System.out.print("tableName:" + table.getTableName() + "\t" + "containLabeledNull:" + table.isContainLabeledNull() + "\tLabeledNullSet:" + table.getLabeledNullSet()+"\n");
                }
                database.updateDatabase(mapping);
                System.out.println("同步到数据库中");
                i++;
            }

            System.out.println("修改后的数据库实例");
            tables = database.getTables();
            for (Table table : tables) {
                System.out.println(table.getTableName());
                List<Tuple> tuples = table.getTuples();
                for (Tuple tuple : tuples) {
                    System.out.println(tuple);
                }
            }
        }
        }
}
