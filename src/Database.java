import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 这个类用来抽象数据库
 */
public class Database {
    private HashSet<String> tableNames; // 数据库中的所有的表名
    private HashSet<Table> tables;
    private static final String inputDirectory = "examples/database_input"; // 保存初始数据库实例的输入数据文件的目录
    private List<String> inputFileNames; // 用来初始化数据库实例的输入数据文件的路径,为examples/database_input目录下的.csv文件

    public Database() {
        tables = new HashSet<>();
        tableNames = new HashSet<>();
    }

    public HashSet<String> getTableNames() {
        return tableNames;
    }

    public void setTableNames(HashSet<String> tableNames) {
        this.tableNames = tableNames;
    }

    public HashSet<Table> getTables() {
        return tables;
    }

    public void setTables(HashSet<Table> tables) {
        this.tables = tables;
    }

    public static String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputFileNames(List<String> inputFileNames) {
        this.inputFileNames = inputFileNames;
    }

    public List<String> getInputFileNames() {
        return inputFileNames;
    }

    /**
     * 获取数据库初始化时所有输入文件的文件名
     *
     * @param directoryPath 保存输入文件的路径，默认为 examples/database_input
     */
    public void setInputFileNames(String directoryPath) {
        List<String> temp = new ArrayList<>();

        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                temp.add(file.getName());
            }
            if (temp.isEmpty()) {
                System.out.println("数据库中没有输入的初始数据");
            }
            inputFileNames = temp;
        } else {
            System.out.println("指定路径不是一个目录！");
        }
    }

    public Table createTable(String tableName) {
        if (tableNames.contains(tableName)) {
            System.out.println("该表在数据库中已经存在，创建失败！");
            return null;
        }
        Table table = new Table(tableName);
        tableNames.add(tableName);
        tables.add(table);
        return table;
    }

    public void initializeTable(String fileName) {
        String tableName = fileName.replace(".csv", "");
        Table table = createTable(tableName);

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(inputDirectory + "/" + fileName));
            String line;
            int count = 1;
            while ((line = bufferedReader.readLine()) != null) {
                if (count == 1) { //读入文件的第一行，即读入表头
                    String[] attributeNames = line.split(",");
                    table.setAttributeNames(Arrays.asList(attributeNames));
                    table.setAttributeNums(attributeNames.length);
                    count++;
                    continue;
                }
                //TODO: 此处初始化数据库实例使得数据库中所有常量类型都用String类型表示了，与之前将ConstValue设计成共用体相矛盾，怎么解决？
                String[] attributeValues = line.split(",");
                List<Value> temp = new ArrayList<>();
                for (String attributeValue : attributeValues) {
                    temp.add(new ConstValue(attributeValue));
                }
                Tuple tuple = new Tuple(temp);
                table.insert(tuple);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void initializeDatabase() {
        setInputFileNames(inputDirectory);
        for (String inputFileName : inputFileNames) {
            initializeTable(inputFileName);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Table table : tables) {
            stringBuilder.append(table.toString());
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        Database database = new Database();
        database.initializeDatabase();
        System.out.println(database);
    }
}
