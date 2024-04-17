package util;

import database.Database;
import database.Table;
import datatype.LabeledNull;

import java.util.HashSet;

/**
 * 这个类是一个工具类，其中的方法都是用来获取chase及其变种算法的相关指标
 */
public class Evaluate {
    /**
     * 获取chase算法执行全过程中，共生成了多少个LabeledNull
     *
     * @return 生成LabeledNull的个数
     */
    public static int getGeneratedLabeledNullNums() {
        return LabeledNull.getNextSubscript() - 1;
    }

    /**
     * 获取chase算法终止后的数据库实例中总共的元组(Facts)条数
     *
     * @param database chase算法终止后得到的数据库实例
     * @return 实例中总共的元组(Facts)条数
     */
    public static int getTupleNumsInDatabase(Database database) {
        int res = 0;
        if (database != null) {
            HashSet<Table> tables = database.getTables();
            for (Table table : tables) {
                if (table != null) {
                    res = res + table.getTuples().size();
                }
            }
        }
        return res;
    }
}
