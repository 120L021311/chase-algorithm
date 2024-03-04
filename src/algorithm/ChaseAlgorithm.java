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
//            System.out.println(database);
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

    public static void obliviousChase(Database database, List<Constraint> constraints){
        boolean changed;
        do {
            changed = false;
            HashMap<LabeledNull, Value> mapping = new HashMap<>();
            for (Constraint constraint : constraints) {
                if (constraint instanceof TGD) {
                    TGD tgd = (TGD) constraint;
                    List<Trigger> triggers = tgd.getTriggers(database);
                    List<RelationalAtom> headAtoms = tgd.getHead();
                    for (Trigger trigger : triggers) {
                        for (RelationalAtom headAtom : headAtoms) {
                            tgd.apply(database, trigger, headAtom);
                            changed = true;
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

    public static void semiObliviousChase(Database database,List<Constraint> constraints){
        boolean changed;
        do {
            changed = false;
            HashMap<LabeledNull, Value> mapping = new HashMap<>();
            for (Constraint constraint : constraints) {
                if (constraint instanceof TGD) {
                    TGD tgd = (TGD) constraint;
                    List<Trigger> triggers = tgd.getTriggers(database);
                    List<RelationalAtom> headAtoms = tgd.getHead();
                    for (RelationalAtom headAtom : headAtoms) {
                        List<Trigger> equivalentTriggers = tgd.getEquivalentTriggers(triggers, headAtom);
                        for (Trigger equivalentTrigger : equivalentTriggers) {
                            tgd.apply(database,equivalentTrigger,headAtom);
                            changed = true;
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

/*
TODO:1.目前的oblivious chase和semi-oblivious chase在一轮执行时结果与预期的行为相同，但是放在循环中不能终止，怎样修改能够使其终止观察执行效果？
        ————按照2017 benchmarking the chase中Algorithm 1的Line 14,15来同时改进三种 chase算法
        如何实现Line 14,15?
        初步思路：为每一条tuple增加一个modified属性，在每一轮循环开始前modified属性全部设置为false，对于TGD触发增加的tuple的modified属性设为true，对于EGD触发修改过的元组的modified属性设为true
        注意问题：新的数据库实例的更新的指导信息需要正确
     2.修改处理输入的正则表达式(或者对找到的数据进行预处理),使用搜集到的数据测试standard chase算法
 */
