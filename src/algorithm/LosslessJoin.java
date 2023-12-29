package algorithm;

import constraints.FD;
import datatype.ConstValue;
import datatype.Data;
import datatype.LabeledNull;
import util.ReadFiles;

import java.util.*;

public class LosslessJoin {
    static Data[][] table;
    static String fd_fileName = "examples/FD/FDs.txt";
    static List<FD> fds = new ArrayList<>();
    static String attributes_fileName = "examples/FD/attributes.txt";
    static List<String> attributes; // 用来保存属性名的有序列表，一个属性对应table表格中的一列
    static List<ConstValue> constValues = new ArrayList<>(); // 用来保存属性名对应的常量值(与属性名顺序对应)
    static Map<String, Integer> attributeName_to_index = new HashMap<>(); // 用来保存属性名和对应的列的下标
    static String decomposition_fileName = "examples/FD/decompositions.txt";
    static List<List<String>> decompositions; // 保存关系模式的分解，一个分解对应table表格中的一行

    public static List<FD> getFds() {
        return fds;
    }

    /**
     * 根据文件输入初始化table
     */
    @SuppressWarnings({"all"})
    public static void setupTable() {
        fds = ReadFiles.readFDs(fd_fileName);

        //读入总的关系的属性集合，并将属性名与对应的列下标对应
        attributes = ReadFiles.readAttributes(attributes_fileName);
        for (int i = 0; i < attributes.size(); i++) {
            String temp = attributes.get(i);
            ConstValue constValue = new ConstValue(temp.toLowerCase(Locale.ROOT));
            constValues.add(constValue);
            attributeName_to_index.put(temp, i);
        }

        //读入关系模式分解出的子模式
        decompositions = ReadFiles.readDecompositions(decomposition_fileName);
//        System.out.println(decompositions.size());
//        System.out.println(attributes.size());

        //初始化table表
        table = new Data[decompositions.size()][attributes.size()];
        for (int i = 0; i < decompositions.size(); i++) {
            List<String> decomposition = decompositions.get(i);
            int rowIndex = i;
            for (int j = 0; j < decomposition.size(); j++) {
                String temp = decomposition.get(j);
                int columnIndex = attributeName_to_index.get(temp);
                ConstValue constValue = constValues.get(columnIndex);
                table[rowIndex][columnIndex] = constValue;
            }
        }

        for (int i = 0; i < decompositions.size(); i++) {
            for (int j = 0; j < attributes.size(); j++) {
                if (table[i][j] == null) {
                    table[i][j] = new LabeledNull();
                }
            }
        }
    }

    /**
     * 对于 table 应用一个函数依赖 FD 的 chase step
     * @param fd 函数依赖 FD
     */
    public static void chaseStep(FD fd) {
        List<Integer> leftIndex = new ArrayList<>();
        List<String> leftAttributes = fd.getLeft();
        for (String leftAttribute : leftAttributes) {
            leftIndex.add(attributeName_to_index.get(leftAttribute));
        }

        List<String> rightAttributes = fd.getRight();
        Integer rightIndex = attributeName_to_index.get(rightAttributes.get(0));

        HashMap<LabeledNull, Data> changes = new HashMap<>(); //本步chase step结束后将要进行的修改：(修改前，修改后)
        HashMap<HashSet<Data>, Data> map = new HashMap<>();

        for (int i = 0; i < table.length; i++) {
            HashSet<Data> tempKey = new HashSet<>();
            for (Integer index : leftIndex) {
                tempKey.add(table[i][index]);
            }
            if (!map.containsKey(tempKey)) {
                map.put(tempKey, table[i][rightIndex]);
            } else {
                Data data = map.get(tempKey);
                if (data instanceof ConstValue && table[i][rightIndex] instanceof ConstValue) {
                    System.out.println("chase failed!");
                    return;
                } else if (data instanceof ConstValue && table[i][rightIndex] instanceof LabeledNull) {
                    //此处应该把表格中对应的项改成对应的常量值，可以直接修改，也可以放到changes中最后一起修改
//                    table[i][rightIndex] = data; // 直接修改

                    // 将修改添加到changes中
                    HashMap<LabeledNull, Data> newChange = new HashMap<>();
                    newChange.put((LabeledNull) table[i][rightIndex],data);
                    changes = LosslessJoin.normalizeMapping(changes,newChange);
                } else if (data instanceof LabeledNull && table[i][rightIndex] instanceof ConstValue) {
//                    changes.put((datatype.LabeledNull) data, table[i][rightIndex]);
//
//                    //这一部分是在完成重新映射的功能，相当于benchmarking the chase文献中的伪代码第13行
//                    if (!changes.isEmpty()) {
//                        for (datatype.LabeledNull key : changes.keySet()) {
//                            if (changes.get(key).equals(data)) {
//                                changes.put(key, table[i][rightIndex]);
//                            }
//                        }
//                    }

                    HashMap<LabeledNull, Data> newChange = new HashMap<>();
                    newChange.put((LabeledNull) data,table[i][rightIndex]);
                    changes = LosslessJoin.normalizeMapping(changes,newChange);
                } else { // 说明两个都是LabeledNull对象，此时将大的改成小的
//                    if (!changes.containsKey((datatype.LabeledNull) table[i][rightIndex])) {
//                        changes.put((datatype.LabeledNull) table[i][rightIndex], data);
//                    }

                    assert data instanceof LabeledNull;
                    LabeledNull labeledNull_1 = (LabeledNull) data;
                    LabeledNull labeledNull_2 = (LabeledNull) table[i][rightIndex];

                    if(labeledNull_1.compare(labeledNull_2)){ // 说明 labeledNull_1 大于等于 labeledNull_2，应该把 labeledNull_1 改成 labeledNull_2
                        HashMap<LabeledNull, Data> newChange = new HashMap<>();
                        newChange.put(labeledNull_1,labeledNull_2);
                        changes = LosslessJoin.normalizeMapping(changes,newChange);
                    } else { // 应该将 labeledNull_2 改成 labeledNull_1
                        HashMap<LabeledNull, Data> newChange = new HashMap<>();
                        newChange.put(labeledNull_2,labeledNull_1);
                        changes = LosslessJoin.normalizeMapping(changes,newChange);
                    }
                }
            }
        }
        LosslessJoin.changeTable(changes);
    }


    /**
     * 添加一个新的(修改前，修改后)的键值对到 changes 中，并进行映射的标准化
     *
     * @param changes   添加新修改前已有的修改映射关系
     * @param newChange 新的修改映射关系 (newKey,newValue)
     * @return 添加新修改并标准化后的 changes 集合
     */
    @SuppressWarnings({"all"})
    public static HashMap<LabeledNull, Data> normalizeMapping(HashMap<LabeledNull, Data> changes, HashMap<LabeledNull, Data> newChange) {
        if(changes.isEmpty()){
            return newChange;
        }

        HashMap<LabeledNull, Data> intermediateMapping = LosslessJoin.combineMapping(changes, newChange);
        HashMap<LabeledNull, Data> result = LosslessJoin.combineMapping(intermediateMapping, changes);
        return result;
    }

    /**
     * 此函数完成两个映射的合成
     *
     * @param u 第一个映射 u
     * @param w 第二个映射 w
     * @return 映射的合成 w●u
     */
    public static HashMap<LabeledNull, Data> combineMapping(HashMap<LabeledNull, Data> u, HashMap<LabeledNull, Data> w) {
        HashMap<LabeledNull, Data> result = new HashMap<>();

        for (Map.Entry<LabeledNull, Data> w_entry : w.entrySet()) {
            LabeledNull w_key = w_entry.getKey();
            Data w_value = w_entry.getValue();
            if (!u.containsKey(w_key)) {
                result.put(w_key, w_value);
            }
            for (Map.Entry<LabeledNull, Data> u_entry : u.entrySet()) {
                LabeledNull u_key = u_entry.getKey();
                Data u_value = u_entry.getValue();
                if (u_value.equals(w_key)) {
                    result.put(u_key, w_value);
                } else {
                    result.put(u_key, u_value);
                }
            }
        }

        return result;
    }

    /**
     * 根据文件输入判断对于给定的关系模式的分解是否为无损连接分解
     * @return 是返回 true，否则返回 false
     */
    public static boolean standard_chase_for_losslessJoin_check() {
        LosslessJoin.setupTable();
        List<FD> fds = LosslessJoin.getFds();
        for (FD fd : fds) {
            LosslessJoin.chaseStep(fd);
        }
        return LosslessJoin.isLosslessJoin();
    }

    /**
     * 检查 table 中是否存在一行，其属性值全为 datatype.ConstValue，从而判断分解是否是无损连接分解
     * @return 存在则返回 true，否则返回 false
     */
    public static boolean isLosslessJoin() {
        for (int i = 0; i < table.length; i++) {
            boolean flag = true;
            for (int j = 0; j < table[0].length; j++) {
                if (table[i][j] instanceof LabeledNull) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将某轮 chase step 后应该对 table 进行的修改作用到 table 上
     * @param changes 本轮应该对 table 进行修改的映射关系
     */
    public static void changeTable(Map<LabeledNull, Data> changes) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (table[i][j] instanceof LabeledNull && changes.containsKey((LabeledNull) table[i][j])) {
                    table[i][j] = changes.get((LabeledNull) table[i][j]);
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(LosslessJoin.standard_chase_for_losslessJoin_check());
    }
}

//TODO：1.目前编写的方法只能处理函数依赖右侧只有一个属性值的情况，如果右侧为有多个属性的属性列表如何处理？是否是多值依赖？(查多值依赖的定义)
//      2.尝试考虑处理约束的 body 或 head 中有多个 relationalAtom 的情况
//      3.尝试设计管理 body 和 head 都只有一个 relationalAtom 的 TGD
