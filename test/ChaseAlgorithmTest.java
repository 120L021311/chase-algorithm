import algorithm.ChaseAlgorithm;
import constraints.TGD;
import database.Database;
import org.junit.Test;
import util.ReadFiles;

import java.util.List;

public class ChaseAlgorithmTest {

    @Test
    public void testChaseForLinearTGD(){
        Database database = new Database();
        database.initializeDatabase();

        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
        ChaseAlgorithm.chaseForLinearTGD(database,tgds);
        System.out.println(database);
    }
}
