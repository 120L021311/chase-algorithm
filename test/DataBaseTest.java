import database.Database;
import database.Table;
import database.Tuple;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;

public class DataBaseTest {

    @Test
    public void testInitializeTable(){
        Database database = new Database();
        database.initializeDatabase();

        HashSet<Table> tables = database.getTables();
        for (Table table : tables) {
            System.out.println(table.getTableName());
            List<Tuple> tuples = table.getTuples();
            System.out.println(tuples.size());
            for (Tuple tuple : tuples) {
                System.out.println(tuple);
                System.out.println(tuple.isContainLabeledNull());
                System.out.println(tuple.getLabeledNullSet());
            }
        }
    }

    @Test
    public void testSplit(){
        String string = ",,,,";
        String[] split = string.split(",");
        System.out.println(split.length);
        for (String s : split) {
            System.out.println(s);
        }


    }
}
