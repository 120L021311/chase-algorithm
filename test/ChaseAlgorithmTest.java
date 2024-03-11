import algorithm.ChaseAlgorithm;
import constraints.Constraint;
import constraints.EGD;
import constraints.TGD;
import database.Database;
import database.Table;
import database.Tuple;
import org.junit.Test;
import util.ReadFiles;

import java.util.ArrayList;
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
        database.setInputDirectory("examples/standardChase_test/test1");
        database.initializeDatabase();
        System.out.println("初始数据库实例：" + "\n" + database);
        List<TGD> tgds = ReadFiles.readTGDs("examples/standardChase_test/test1/TGD.txt");
        List<EGD> egds = ReadFiles.readEGDs("examples/standardChase_test/test1/EGD.txt");
        List<Constraint> constraints = new ArrayList<>();
        constraints.addAll(tgds);
        constraints.addAll(egds);
        System.out.println(ChaseAlgorithm.standardChase(database, constraints));
    }

    @Test
    public void testObliviousChase() {
        Database database = new Database();
        database.setInputDirectory("examples/test2");
        database.initializeDatabase();
        List<TGD> tgds = ReadFiles.readTGDs("examples/test2/TGD.txt");
        List<Constraint> constraints = new ArrayList<>(tgds);
        System.out.println(ChaseAlgorithm.obliviousChase(database, constraints));
    }

    @Test
    public void testSemiObliviousChase() {
        Database database = new Database();
        database.setInputDirectory("examples/test2");
        database.initializeDatabase();
        List<TGD> tgds = ReadFiles.readTGDs("examples/test2/TGD.txt");
        List<Constraint> constraints = new ArrayList<>(tgds);
        System.out.println(ChaseAlgorithm.semiObliviousChase(database, constraints));
    }

}
