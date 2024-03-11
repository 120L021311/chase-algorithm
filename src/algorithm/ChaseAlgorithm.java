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


//    问题：incrementInstance和database等引用指向的Table对象、Tuple对象的引用实际上都指向同一个对象
//    解决：在需要更新时，先拷贝一份再更新，传入更新后的拷贝(也就是代码中copy变量的作用)

    /**
     * 3.6下午实现的增加了streaming mode优化的标准chase算法
     *
     * @param database    初始数据库实例
     * @param constraints 约束集
     * @return 运行标准chase算法得到的修复结果
     */
    public static Database standardChase(Database database, List<Constraint> constraints) {
        Database incrementInstance = new Database();

        for (Table table : database.getTables()) {
            incrementInstance.addTable(table);
        }

        int count = 0;
        while (!incrementInstance.isEmpty()) {
            Database newlyDerivedSubset = new Database();
            HashMap<LabeledNull, Value> mapping = new HashMap<>();
            for (Constraint constraint : constraints) {
                count++;
                if (constraint instanceof TGD) {
                    TGD tgd = (TGD) constraint;
                    List<Trigger> triggers = tgd.getTriggers(incrementInstance);
                    for (Trigger trigger : triggers) {
                        List<RelationalAtom> headAtoms = tgd.getHead();
                        for (RelationalAtom headAtom : headAtoms) {
                            Database copy = Database.copyDatabase(Database.databaseUnion(newlyDerivedSubset, database));
                            if (trigger.checkActive(copy.updateDatabase(mapping), headAtom)) {
                                tgd.apply(newlyDerivedSubset, trigger, headAtom);
                            }
                        }
                    }
                }
                if (constraint instanceof EGD) {
                    EGD egd = (EGD) constraint;
                    if (egd.isSimple()) {
                        List<EqualAtom> headAtoms = egd.getHead();
                        for (EqualAtom equalAtom : headAtoms) {
                            Database copy = Database.copyDatabase(Database.databaseUnion(newlyDerivedSubset, database));
                            mapping = egd.apply(copy.updateDatabase(mapping), equalAtom, mapping);

//                            mapping = egd.apply(Database.databaseUnion(newlyDerivedSubset,database).updateDatabase(mapping), equalAtom, mapping);
//                            System.out.println("mapping :" + mapping);
                        }
                    } else {
                        List<Trigger> triggers = egd.getTriggers(incrementInstance);
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

            Database copy = Database.copyDatabase(Database.databaseUnion(newlyDerivedSubset, database));
            incrementInstance = Database.databaseDifference(copy.updateDatabase(mapping), database);
            database = Database.databaseUnion(database.updateDatabase(mapping), incrementInstance);
        }

        System.out.println("共检查了" + count + "次依赖关系");
        return database;
    }

    //    2.20实现的之前版本的可以正确运行的standardChase，但没有使用streaming mode优化
//    public static void standardChase(Database database, List<Constraint> constraints) {
//        boolean changed;
//        int i = 0;
//        do {
//            changed = false;
//            HashMap<LabeledNull, Value> mapping = new HashMap<>();
//            for (Constraint constraint : constraints) {
//                i++;
//                if (constraint instanceof TGD) {
//                    TGD tgd = (TGD) constraint;
//                    List<Trigger> triggers = tgd.getTriggers(database);
//                    for (Trigger trigger : triggers) {
//                        List<RelationalAtom> headAtoms = tgd.getHead();
//                        for (RelationalAtom headAtom : headAtoms) {
//                            if (trigger.checkActive(database, headAtom)) {
//                                tgd.apply(database, trigger, headAtom);
//                                changed = true;
//                            }
//                        }
//                    }
//                }
//                if (constraint instanceof EGD) {
//                    EGD egd = (EGD) constraint;
//                    if (egd.isSimple()) {
//                        List<EqualAtom> headAtoms = egd.getHead();
//                        for (EqualAtom equalAtom : headAtoms) {
//                            mapping = egd.apply(database, equalAtom, mapping);
//                            System.out.println("mapping :" + mapping);
//                        }
//                    } else {
//                        List<Trigger> triggers = egd.getTriggers(database);
//                        List<EqualAtom> headAtoms = egd.getHead();
//                        for (Trigger trigger : triggers) {
//                            for (EqualAtom equalAtom : headAtoms) {
//                                if (trigger.checkActive(equalAtom)) {
//                                    mapping = egd.apply(trigger, equalAtom, mapping);
//                                    System.out.println("mapping :" + mapping);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (!mapping.isEmpty()) {
//                database.updateDatabase(mapping);
//                changed = true;
//                System.out.println("同步到数据库中");
//            }
//
//            System.out.println("修改后的数据库实例");
//            HashSet<Table> tables = database.getTables();
//            for (Table table : tables) {
//                System.out.println(table.getTableName());
//                List<Tuple> tuples = table.getTuples();
//                for (Tuple tuple : tuples) {
//                    System.out.print(tuple);
//                }
//            }
//        } while (changed);
//        System.out.println("共检查了" + i + "次依赖");
//    }

    public static Database obliviousChase(Database database, List<TGD> tgds) {
        Database incrementInstance = new Database();

        for (Table table : database.getTables()) {
            incrementInstance.addTable(table);
        }

        while (!incrementInstance.isEmpty()) {
            Database newlyDerivedSubset = new Database();
            HashMap<LabeledNull, Value> mapping = new HashMap<>();
            for (TGD tgd : tgds) {

                List<Trigger> triggers = tgd.getTriggers(incrementInstance);
                for (Trigger trigger : triggers) {
                    List<RelationalAtom> headAtoms = tgd.getHead();
                    for (RelationalAtom headAtom : headAtoms) {
                        tgd.apply(newlyDerivedSubset, trigger, headAtom);
                    }
                }

                incrementInstance = Database.copyDatabase(newlyDerivedSubset);
                database = Database.databaseUnion(database, incrementInstance);
            }
        }
        System.out.println("最终数据库实例：");

        return database;
    }


//    3.4实现的无法终止的oblivious-chase
//    public static void obliviousChase(Database database, List<Constraint> constraints) {
//        boolean changed;
//        do {
//            changed = false;
//            HashMap<LabeledNull, Value> mapping = new HashMap<>();
//            for (Constraint constraint : constraints) {
//                if (constraint instanceof TGD) {
//                    TGD tgd = (TGD) constraint;
//                    List<Trigger> triggers = tgd.getTriggers(database);
//                    List<RelationalAtom> headAtoms = tgd.getHead();
//                    for (Trigger trigger : triggers) {
//                        for (RelationalAtom headAtom : headAtoms) {
//                            tgd.apply(database, trigger, headAtom);
//                            changed = true;
//                        }
//                    }
//                }
//                if (constraint instanceof EGD) {
//                    EGD egd = (EGD) constraint;
//                    if (egd.isSimple()) {
//                        List<EqualAtom> headAtoms = egd.getHead();
//                        for (EqualAtom equalAtom : headAtoms) {
//                            mapping = egd.apply(database, equalAtom, mapping);
//                            System.out.println("mapping :" + mapping);
//                        }
//                    } else {
//                        List<Trigger> triggers = egd.getTriggers(database);
//                        List<EqualAtom> headAtoms = egd.getHead();
//                        for (Trigger trigger : triggers) {
//                            for (EqualAtom equalAtom : headAtoms) {
//                                if (trigger.checkActive(equalAtom)) {
//                                    mapping = egd.apply(trigger, equalAtom, mapping);
//                                    System.out.println("mapping :" + mapping);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (!mapping.isEmpty()) {
//                database.updateDatabase(mapping);
//                changed = true;
//                System.out.println("同步到数据库中");
//            }
//
//            System.out.println("修改后的数据库实例");
//            HashSet<Table> tables = database.getTables();
//            for (Table table : tables) {
//                System.out.println(table.getTableName());
//                List<Tuple> tuples = table.getTuples();
//                for (Tuple tuple : tuples) {
//                    System.out.print(tuple);
//                }
//            }
//        } while (changed);
//    }

    public static Database semiObliviousChase(Database database, List<TGD> tgds) {
        Database incrementInstance = new Database();

        for (Table table : database.getTables()) {
            incrementInstance.addTable(table);
        }

        while (!incrementInstance.isEmpty()) {
            Database newlyDerivedSubset = new Database();
            HashMap<LabeledNull, Value> mapping = new HashMap<>();
            for (TGD tgd : tgds) {
                List<Trigger> triggers = tgd.getTriggers(incrementInstance);
                List<RelationalAtom> headAtoms = tgd.getHead();
                for (RelationalAtom headAtom : headAtoms) {
                    List<Trigger> equivalentTriggers = tgd.getEquivalentTriggers(triggers, headAtom);
                    for (Trigger equivalentTrigger : equivalentTriggers) {
                        tgd.apply(database, equivalentTrigger, headAtom);
                    }
                }

                incrementInstance = Database.copyDatabase(newlyDerivedSubset);
                database = Database.databaseUnion(database.updateDatabase(mapping), incrementInstance);
            }
        }
        System.out.println("最终数据库实例：");

        return database;
    }

//    3.4实现的无法终止的semi-oblivious chase
//    public static void semiObliviousChase(Database database, List<Constraint> constraints) {
//        boolean changed;
//        do {
//            changed = false;
//            HashMap<LabeledNull, Value> mapping = new HashMap<>();
//            for (Constraint constraint : constraints) {
//                if (constraint instanceof TGD) {
//                    TGD tgd = (TGD) constraint;
//                    List<Trigger> triggers = tgd.getTriggers(database);
//                    List<RelationalAtom> headAtoms = tgd.getHead();
//                    for (RelationalAtom headAtom : headAtoms) {
//                        List<Trigger> equivalentTriggers = tgd.getEquivalentTriggers(triggers, headAtom);
//                        for (Trigger equivalentTrigger : equivalentTriggers) {
//                            tgd.apply(database, equivalentTrigger, headAtom);
//                            changed = true;
//                        }
//                    }
//                }
//                if (constraint instanceof EGD) {
//                    EGD egd = (EGD) constraint;
//                    if (egd.isSimple()) {
//                        List<EqualAtom> headAtoms = egd.getHead();
//                        for (EqualAtom equalAtom : headAtoms) {
//                            mapping = egd.apply(database, equalAtom, mapping);
//                            System.out.println("mapping :" + mapping);
//                        }
//                    } else {
//                        List<Trigger> triggers = egd.getTriggers(database);
//                        List<EqualAtom> headAtoms = egd.getHead();
//                        for (Trigger trigger : triggers) {
//                            for (EqualAtom equalAtom : headAtoms) {
//                                if (trigger.checkActive(equalAtom)) {
//                                    mapping = egd.apply(trigger, equalAtom, mapping);
//                                    System.out.println("mapping :" + mapping);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            if (!mapping.isEmpty()) {
//                database.updateDatabase(mapping);
//                changed = true;
//                System.out.println("同步到数据库中");
//            }
//
//            System.out.println("修改后的数据库实例");
//            HashSet<Table> tables = database.getTables();
//            for (Table table : tables) {
//                System.out.println(table.getTableName());
//                List<Tuple> tuples = table.getTuples();
//                for (Tuple tuple : tuples) {
//                    System.out.print(tuple);
//                }
//            }
//        } while (changed);
//    }
}

/*
TODO:1.目前的oblivious chase和semi-oblivious chase在一轮执行时结果与预期的行为相同，但是放在循环中不能终止，怎样修改能够使其终止观察执行效果？
        ————按照2017 benchmarking the chase中Algorithm 1的Line 14,15来同时改进三种 chase算法
        如何实现Line 14,15? 已完成！！
        初步思路：为每一条tuple增加一个modified属性，在每一轮循环开始前modified属性全部设置为false，对于TGD触发增加的tuple的modified属性设为true，对于EGD触发修改过的元组的modified属性设为true
        注意问题：新的数据库实例的更新的指导信息需要正确
     2.修改处理输入的正则表达式(或者对找到的数据进行预处理),使用搜集到的数据测试standard chase算法
 */
