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
    private static final String inputDirectory = "examples/test2"; // 保存初始数据库实例的输入数据文件的目录
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
        if(!fileName.endsWith(".csv")){ //说明不是csv类型的文件，不是数据库中的数据文件
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
    public void updateDatabase(HashMap<LabeledNull, Value> mapping) {
        HashSet<LabeledNull> mappingKeySet = new HashSet<>(mapping.keySet());
        for (Table table : tables) {
            if(table.isContainLabeledNull() && !intersection(mappingKeySet,table.getLabeledNullSet()).isEmpty()){
                table.updateTable(mapping);
            }
        }
    }

    /**
     * 获得两个HashSet<LabeledNull>的交集，用于进行是否需要更新的判断
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Table table : tables) {
            stringBuilder.append(table.toString());
        }
        return stringBuilder.toString();
    }

}
