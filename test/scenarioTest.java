import algorithm.ChaseAlgorithm;
import constraints.Constraint;
import constraints.EGD;
import constraints.TGD;
import database.Database;
import org.junit.Test;
import util.Evaluate;
import util.ReadFiles;

import java.util.ArrayList;
import java.util.List;

public class scenarioTest {
    @Test
    public void standardChaseTest() {
        Database initalSourceInstanceDatabase = new Database();
        initalSourceInstanceDatabase.setInputDirectory("examples/文献数据/doctors/100k/data");
        initalSourceInstanceDatabase.initializeDatabase();
//        System.out.println("初始数据库实例：" + "\n" + database);
//        long start = System.currentTimeMillis();
        List<TGD> st_tgds = ReadFiles.readTGDs("examples/文献数据/doctors/100k/dependencies/st-tgds.txt");
//        List<TGD> t_tgds = ReadFiles.readTGDs("examples/文献数据/deep/200/dependencies/t-tgds.txt");
        List<EGD> t_egds = ReadFiles.readEGDs("examples/文献数据/doctors/100k/dependencies/t-egds.txt");
        List<Constraint> constraints = new ArrayList<>();
        List<Constraint> sourceToTargetConstraints = new ArrayList<>();
        sourceToTargetConstraints.addAll(st_tgds);
        List<Constraint> targetConstraints = new ArrayList<>();
//        targetConstraints.addAll(t_tgds);
        targetConstraints.addAll(t_egds);

        long start = System.currentTimeMillis();
        Database initialTargetInstanceDatabase = ChaseAlgorithm.standardChase(initalSourceInstanceDatabase, sourceToTargetConstraints);
        long end = System.currentTimeMillis();
        System.out.println("对于source_to_target_constraints的chase的运行时间为： " + (end - start) + "ms");

        start = System.currentTimeMillis();
        Database standardChaseResult = ChaseAlgorithm.standardChase(initialTargetInstanceDatabase, constraints);
        end = System.currentTimeMillis();
        System.out.println("对于target_constraints的chase的运行时间为： " + (end - start) + "ms");

        System.out.println("总共生成LabeledNull的个数：" + Evaluate.getGeneratedLabeledNullNums());
        System.out.println("最终结果数据库实例中元组总数：" + Evaluate.getTupleNumsInDatabase(standardChaseResult));
//        System.out.println(standardChase);
    }

    @Test
    public void parallelChaseTest() {
//        Database database = new Database();
//        database.setInputDirectory("examples/文献数据/correctness/tgdsEgds/data");
//        database.initializeDatabase();
//        System.out.println("初始数据库实例：" + "\n" + database);
//        List<TGD> st_tgds = ReadFiles.readTGDs("examples/文献数据/correctness/tgdsEgds/dependencies/st-tgds.txt");
//        List<TGD> t_tgds = ReadFiles.readTGDs("examples/文献数据/correctness/tgdsEgds/dependencies/t-tgds.txt");
//        List<EGD> egds = ReadFiles.readEGDs("examples/文献数据/correctness/tgdsEgds/dependencies/t-egds.txt");
//        List<Constraint> constraints = new ArrayList<>();
//        constraints.addAll(st_tgds);
//        constraints.addAll(t_tgds);
//        constraints.addAll(egds);
//        System.out.println(ChaseAlgorithm.parallelChase(database, constraints));
        Database initalSourceInstanceDatabase = new Database();
        initalSourceInstanceDatabase.setInputDirectory("examples/文献数据/doctors/500k/data");
        initalSourceInstanceDatabase.initializeDatabase();
//        System.out.println("初始数据库实例：" + "\n" + database);
//        long start = System.currentTimeMillis();
        List<TGD> st_tgds = ReadFiles.readTGDs("examples/文献数据/doctors/500k/dependencies/st-tgds.txt");
//        List<TGD> t_tgds = ReadFiles.readTGDs("examples/文献数据/deep/200/dependencies/t-tgds.txt");
        List<EGD> t_egds = ReadFiles.readEGDs("examples/文献数据/doctors/500k/dependencies/t-egds.txt");
        List<Constraint> constraints = new ArrayList<>();
        List<Constraint> sourceToTargetConstraints = new ArrayList<>();
        sourceToTargetConstraints.addAll(st_tgds);
        List<Constraint> targetConstraints = new ArrayList<>();
//        targetConstraints.addAll(t_tgds);
        targetConstraints.addAll(t_egds);

        long start = System.currentTimeMillis();
        Database initialTargetInstanceDatabase = ChaseAlgorithm.parallelChase(initalSourceInstanceDatabase, sourceToTargetConstraints);
        long end = System.currentTimeMillis();
        System.out.println("对于source_to_target_constraints的chase的运行时间为： " + (end - start) + "ms");

        start = System.currentTimeMillis();
        Database standardChaseResult = ChaseAlgorithm.parallelChase(initialTargetInstanceDatabase, constraints);
        end = System.currentTimeMillis();
        System.out.println("对于target_constraints的chase的运行时间为： " + (end - start) + "ms");

        System.out.println("总共生成LabeledNull的个数：" + Evaluate.getGeneratedLabeledNullNums());
        System.out.println("最终结果数据库实例中元组总数：" + Evaluate.getTupleNumsInDatabase(standardChaseResult));
//        System.out.println(standardChase);
    }

    @Test
    public void obliviousChaseTest() {
        Database database = new Database();
        database.setInputDirectory("examples/文献数据/correctness/tgds/data");
        database.initializeDatabase();
        System.out.println("初始数据库实例：" + "\n" + database);
        List<TGD> st_tgds = ReadFiles.readTGDs("examples/文献数据/correctness/tgds/dependencies/st-tgds.txt");
        List<TGD> t_tgds = ReadFiles.readTGDs("examples/文献数据/correctness/tgds/dependencies/t-tgds.txt");
        List<TGD> tgds = new ArrayList<>();
        tgds.addAll(st_tgds);
        tgds.addAll(t_tgds);
        System.out.println(ChaseAlgorithm.obliviousChase(database, tgds));
    }

    @Test
    public void semiObliviousChaseTest() {
        Database database = new Database();
        database.setInputDirectory("examples/文献数据/correctness/tgds5/data");
        database.initializeDatabase();
        System.out.println("初始数据库实例：" + "\n" + database);
        List<TGD> st_tgds = ReadFiles.readTGDs("examples/文献数据/correctness/tgds5/dependencies/st-tgds.txt");
        List<TGD> t_tgds = ReadFiles.readTGDs("examples/文献数据/correctness/tgds5/dependencies/t-tgds.txt");
        List<TGD> tgds = new ArrayList<>();
        tgds.addAll(st_tgds);
        tgds.addAll(t_tgds);
        System.out.println(ChaseAlgorithm.semiObliviousChase(database, tgds));
    }
}
