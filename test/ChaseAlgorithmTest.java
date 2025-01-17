import algorithm.ChaseAlgorithm;
import constraints.Constraint;
import constraints.EGD;
import constraints.TGD;
import database.Database;
import database.Table;
import database.Tuple;
import datatype.LabeledNull;
import datatype.Value;
import org.junit.Test;
import symbols.EqualAtom;
import symbols.RelationalAtom;
import symbols.Trigger;
import util.ReadFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ChaseAlgorithmTest {

    @Test
    public void testChaseForTGD() {
        Database database = new Database();
        database.setInputDirectory("examples/TGD_test/test1");
        database.initializeDatabase();
        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD_test/test1/tgd.txt");
        ChaseAlgorithm.chaseForTGD(database, tgds);
        System.out.println(database);
    }

    @Test
    public void testStandardChase() {
        Database database = new Database();
        database.setInputDirectory("examples/TGD_test/test5");
        database.initializeDatabase();
        System.out.println("初始数据库实例：" + "\n" + database);
        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD_test/test5/TGD.txt");
//        List<EGD> egds = ReadFiles.readEGDs("examples/standardChase_test/test1/EGD.txt");
        List<Constraint> constraints = new ArrayList<>();
        constraints.addAll(tgds);
//        constraints.addAll(egds);
        System.out.println(ChaseAlgorithm.standardChase(database, constraints));
    }

    @Test
    public void testParallelChase() {
        Database database = new Database();
        database.setInputDirectory("examples/TGD_test/test5");
        database.initializeDatabase();
        System.out.println("初始数据库实例：" + "\n" + database);
        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD_test/test5/TGD.txt");
//        List<EGD> egds = ReadFiles.readEGDs("examples/standardChase_test/test1/EGD.txt");
        List<Constraint> constraints = new ArrayList<>();
        constraints.addAll(tgds);
//        constraints.addAll(egds);
        System.out.println(ChaseAlgorithm.parallelChase(database, constraints));
    }

    @Test
    public void testObliviousChase() {
        Database database = new Database();
        database.setInputDirectory("examples/test2");
        database.initializeDatabase();
        List<TGD> tgds = ReadFiles.readTGDs("examples/test2/TGD.txt");
        System.out.println(ChaseAlgorithm.obliviousChase(database, tgds));
    }

    @Test
    public void testSemiObliviousChase() {
        Database database = new Database();
        database.setInputDirectory("examples/test2");
        database.initializeDatabase();
        List<TGD> tgds = ReadFiles.readTGDs("examples/test2/TGD.txt");
        System.out.println(ChaseAlgorithm.semiObliviousChase(database, tgds));
    }

}
