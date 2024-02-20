package algorithm;

import constraints.Constraint;
import constraints.EGD;
import constraints.TGD;
import database.Database;
import database.Table;
import database.Tuple;
import datatype.LabeledNull;
import datatype.Value;
import symbols.EqualAtom;
import symbols.RelationalAtom;
import symbols.Trigger;
import util.ReadFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ChaseAlgorithm {

    public static void chaseForTGD(Database database, List<TGD> tgds) {
        boolean changed;
        do {
            changed = false;
            for (TGD tgd : tgds) {
                List<Trigger> triggers = tgd.getTriggers(database);
                for (Trigger trigger : triggers) {
                    List<RelationalAtom> headAtoms = tgd.getHead();
                    for (RelationalAtom headAtom : headAtoms) {
                        if (trigger.checkActive(database, headAtom)) {
                            tgd.apply(database, trigger, headAtom);
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }

    public static void standardChase(Database database, List<Constraint> constraints) {
        boolean changed;
        do {
            changed = false;
            HashMap<LabeledNull, Value> mapping = new HashMap<>();
            for (Constraint constraint : constraints) {
                if (constraint instanceof TGD) {
                    TGD tgd = (TGD) constraint;
                    List<Trigger> triggers = tgd.getTriggers(database);
                    for (Trigger trigger : triggers) {
                        List<RelationalAtom> headAtoms = tgd.getHead();
                        for (RelationalAtom headAtom : headAtoms) {
                            if (trigger.checkActive(database, headAtom)) {
                                tgd.apply(database, trigger, headAtom);
                                changed = true;
                            }
                        }
                    }
                }
                if (constraint instanceof EGD) {
                    EGD egd = (EGD) constraint;
                    if (egd.isSimple()) {
                        List<EqualAtom> headAtoms = egd.getHead();
                        for (EqualAtom equalAtom : headAtoms) {
                            mapping = egd.apply(database, equalAtom, mapping);
                            System.out.println("mapping :" + mapping);
                        }
                    } else {
                        List<Trigger> triggers = egd.getTriggers(database);
                        List<EqualAtom> headAtoms = egd.getHead();
                        for (Trigger trigger : triggers) {
                            for (EqualAtom equalAtom : headAtoms) {
                                if (trigger.checkActive(equalAtom)) {
                                    mapping = egd.apply(trigger, equalAtom, mapping);
                                    System.out.println("mapping :" + mapping);
                                }
                            }
                        }
                    }
                }
            }
            if (!mapping.isEmpty()) {
                database.updateDatabase(mapping);
                changed = true;
                System.out.println("同步到数据库中");
            }

            System.out.println("修改后的数据库实例");
            HashSet<Table> tables = database.getTables();
            for (Table table : tables) {
                System.out.println(table.getTableName());
                List<Tuple> tuples = table.getTuples();
                for (Tuple tuple : tuples) {
                    System.out.println(tuple);
                }
            }
        } while (changed);
    }
}
