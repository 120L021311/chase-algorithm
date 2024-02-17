import algorithm.ChaseAlgorithm;
import constraints.TGD;
import database.Database;
import database.Table;
import database.Tuple;
import org.junit.Test;
import util.ReadFiles;

import java.util.HashSet;
import java.util.List;

public class ChaseAlgorithmTest {

    @Test
    public void testChaseForTGD(){
        Database database = new Database();
        database.initializeDatabase();
        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
        ChaseAlgorithm.chaseForTGD(database,tgds);
        System.out.println(database);
    }
}
