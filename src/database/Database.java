package database;

import datatype.ConstValue;
import datatype.LabeledNull;
import datatype.Value;

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
    private String inputDirectory; // 保存初始数据库实例的输入数据文件的目录
    private List<String> inputFileNames; // 用来初始化数据库实例的输入数据文件的路径,为inputDirectory目录下的.csv文件

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

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public void setInputFileNames(List<String> inputFileNames) {
        this.inputFileNames = inputFileNames;
    }

    public List<String> getInputFileNames() {
        return inputFileNames;
    }

    /**
     * 通过表名获得数据库中相对应的表
     *
     * @param tableName 表名
     * @return 与表名相对应的表 (Table对象)
     */
    public Table getTableWithName(String tableName) {
        if (!tableNames.contains(tableName)) {
            System.out.println("表 " + tableName + " 不存在！");
        } else {
            HashSet<Table> tables = this.getTables();
            for (Table table : tables) {
                if (table.getTableName().equals(tableName)) {
                    return table;
                }
            }
        }
        return null;
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

    /**
     * 在数据库中创建一个表
     *
     * @param tableName 要创建的表的名称
     * @return 创建后得到的表 (Table对象)
     */
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

    /**
     * 利用文件输入初始化数据库中的一个表
     *
     * @param fileName 文件输入路径，路径中.csv前的部分为表名，文件内部保存了该表中的数据
     */
    public void initializeTable(String fileName) {
        if (!fileName.endsWith(".csv")) { //说明不是csv类型的文件，不是数据库中的数据文件
            return;
        }
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
                if (line.endsWith(",")) {
                    line = line + " ";
                }
                String[] attributeValues = line.split(",");
                List<Value> temp = new ArrayList<>();
                for (String attributeValue : attributeValues) {
                    if (attributeValue.trim().isEmpty()) { //说明这个位置的数据值缺失了,创建一个标记空值占位
                        temp.add(new LabeledNull());
                    } else {
                        temp.add(new ConstValue(attributeValue));
                    }
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

    /**
     * 利用文件初始化数据库
     */
    public void initializeDatabase() {
        setInputFileNames(inputDirectory);
        for (String inputFileName : inputFileNames) {
            initializeTable(inputFileName);
        }
    }

    /**
     * 完成chase过程中整个数据库实例的更新，修改元组中的标记空值的取值
     *
     * @param mapping 应该同步到数据库中的修改映射关系
     */
    public Database updateDatabase(HashMap<LabeledNull, Value> mapping) {
        HashSet<LabeledNull> mappingKeySet = new HashSet<>(mapping.keySet());
        for (Table table : tables) {
            if (table.isContainLabeledNull() && !intersection(mappingKeySet, table.getLabeledNullSet()).isEmpty()) {
                table.updateTable(mapping);
            }
        }
        return this;
    }

    /**
     * 获得两个HashSet<LabeledNull>的交集，用于进行是否需要更新的判断
     *
     * @param set1 第一个HashSet<LabeledNull>
     * @param set2 第二个HashSet<LabeledNull>
     * @return 交集
     */
    public HashSet<LabeledNull> intersection(HashSet<LabeledNull> set1, HashSet<LabeledNull> set2) {
        HashSet<LabeledNull> result = new HashSet<>();
        for (LabeledNull labeledNull : set1) {
            if (set2.contains(labeledNull)) {
                result.add(labeledNull);
            }
        }
        return result;
    }

    /**
     * 向数据库中添加一个表
     *
     * @param table 要添加的表
     * @return 添加成功返回true，否则返回false
     */
    public boolean addTable(Table table) {
        String tableName = table.getTableName();
        if (tableNames.contains(tableName)) {
            System.out.println("表" + tableName + "已经存在，添加失败!");
            return false;
        } else {
            tableNames.add(tableName);
            tables.add(table);
            return true;
        }
    }

    /**
     * 数据库实例之间合并的函数，完成关系代数中"并"运算的功能
     *
     * @param database1 第一个数据库实例
     * @param database2 第二个数据库实例
     * @return 两个数据库实例合并后的新实例
     */
    public static Database databaseUnion(Database database1, Database database2) {
        //思路：对于database1中的每一个表，判断database2中有没有同名的表
        //如果没有同名的表，则直接加入结果数据库实例中
        //如果有同名的表，调用表之间的union操作
        Database res = new Database();
        HashSet<String> tableNames_1 = database1.getTableNames();
        HashSet<String> tableNames_2 = database2.getTableNames();
        for (String tableName : tableNames_1) {
            Table table_1 = database1.getTableWithName(tableName);
            if (!tableNames_2.contains(tableName)) {
                res.addTable(table_1);
            } else {
                Table table_2 = database2.getTableWithName(tableName);
                Table newTable = Table.tableUnion(table_1, table_2);
                res.addTable(newTable);
            }
        }
        for (String tableName : tableNames_2) {
            Table table_2 = database2.getTableWithName(tableName);
            if(!tableNames_1.contains(tableName)){
                res.addTable(table_2);
            }
        }
        return res;
    }

    /**
     * 数据库实例之间作差的函数，完成关系代数中"差"运算的功能
     *
     * @param database1 第一个数据库实例
     * @param database2 第二个数据库实例
     * @return 得到的新实例 database1\database2
     */
    //TODO:考虑在DBMS中利用explain命令观察集合差操作的查询执行计划
    public static Database databaseDifference(Database database1, Database database2) {
        //思路：对于database1中的每一个表，判断database2中有没有同名的表
        //如果没有同名的表，则直接把该表加到结果数据库实例中；
        //如果有同名的表，则调用表之间的difference操作
        Database res = new Database();
        HashSet<String> tableNames_1 = database1.getTableNames();
        HashSet<String> tableNames_2 = database2.getTableNames();
        for (String tableName : tableNames_1) {
            Table table_1 = database1.getTableWithName(tableName);
            if (!tableNames_2.contains(tableName)) {
                res.addTable(table_1);
            } else {
                //调用表之间的difference操作
                Table table_2 = database2.getTableWithName(tableName);
                Table newTable = Table.tableDifference(table_1, table_2);

                //如果作差得到的表中一条元组都没有，则不用添加到结果数据库实例中
                if (!newTable.getTuples().isEmpty()) {
                    res.addTable(newTable);
                }
            }
        }
        return res;
    }

    /**
     * 判断数据库实例是否为空(没有表)
     *
     * @return 为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return tables.isEmpty();
    }

    /**
     * 在内存中拷贝一个 Database对象
     * @param database 要拷贝的 Database对象
     * @return 拷贝后得到的新的 Database对象
     */
    public static Database copyDatabase(Database database){
        Database res = new Database();
        HashSet<String> tableNames = new HashSet<>();
        for (String tableName : database.tableNames) {
            tableNames.add(new String(tableName));
        }
        res.setTableNames(tableNames);

        HashSet<Table> tables = new HashSet<>();
        for (Table table : database.getTables()) {
            //调用Table的copy方法
            tables.add(table.copyTable());
        }
        res.setTables(tables);

        return res;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Table table : tables) {
            stringBuilder.append(table.toString());
        }
        return stringBuilder.toString();
    }

}
