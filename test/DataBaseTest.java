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
        database.setInputDirectory("examples/test");
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

    @Test
    public void testDatabaseUnion(){
        Database database1 = new Database();
        database1.setInputDirectory("examples/Database_test/union/database1");
        database1.initializeDatabase();

        Database database2 = new Database();
        database2.setInputDirectory("examples/Database_test/union/database2");
        database2.initializeDatabase();

        Database union = Database.databaseUnion(database1, database2);
        System.out.println("------------database1中的内容------------");
        System.out.println(database1);
        System.out.println("------------database2中的内容------------");
        System.out.println(database2);
        System.out.println("------------合并后的数据库中的内容------------");
        System.out.println(union);

        HashSet<Table> tables = union.getTables();
        for (Table table : tables) {
            System.out.println("表名：" + table.getTableName());
            System.out.println("包含的标记空值："+table.getLabeledNullSet());
        }
    }

    @Test
    public void testDatabaseDifference(){
        Database database1 = new Database();
        database1.setInputDirectory("examples/Database_test/difference/database1");
        database1.initializeDatabase();

        Database database2 = new Database();
        database2.setInputDirectory("examples/Database_test/difference/database2");
        database2.initializeDatabase();

        Database difference = Database.databaseDifference(database1, database2);
        System.out.println("------------database1中的内容------------");
        System.out.println(database1);
        System.out.println("------------database2中的内容------------");
        System.out.println(database2);
        System.out.println("------------作差得到结果数据库中的内容------------");
        System.out.println(difference);

        HashSet<Table> tables = difference.getTables();
        for (Table table : tables) {
            System.out.println("表名：" + table.getTableName());
            System.out.println("包含的标记空值："+table.getLabeledNullSet());
        }
    }
}
