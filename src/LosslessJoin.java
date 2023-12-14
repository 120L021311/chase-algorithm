import java.util.*;

public class LosslessJoin {
    static Data[][] table;
    static String fd_fileName = "examples\\\\FDs.txt";
    static List<FD> fds = new ArrayList<>();
    static String attributes_fileName = "examples\\\\attributes.txt";
    static List<String> attributes; // 用来保存属性名的有序列表，一个属性对应table表格中的一列
    static List<ConstValue> constValues = new ArrayList<>(); // 用来保存属性名对应的常量值(与属性名顺序对应)
    static Map<String, Integer> attributeName_to_index = new HashMap<>(); // 用来保存属性名和对应的列的下标
    static String decomposition_fileName = "examples\\\\decompositions.txt";
    static List<List<String>> decompositions; // 保存关系模式的分解，一个分解对应table表格中的一行

    public static List<FD> getFds() {
        return fds;
    }

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

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (table[i][j] == null) {
                    table[i][j] = new LabeledNull();
                }
            }
        }
    }

    // TODO:将对于某一特定函数依赖的chase step  ->  对fd的chase step
    public static void chaseStep(FD fd) {
        List<Integer> leftIndex = new ArrayList<>();
        List<String> leftAttributes = fd.getLeft();
        for (String leftAttribute : leftAttributes) {
            leftIndex.add(attributeName_to_index.get(leftAttribute));
        }

        List<String> rightAttributes = fd.getRight();
        Integer rightIndex = attributeName_to_index.get(rightAttributes.get(0));

        HashMap<LabeledNull, Data> changes = new HashMap<>(); //(修改前，修改后)
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
                    return;
                } else if (data instanceof ConstValue && table[i][rightIndex] instanceof LabeledNull) {
                    table[i][rightIndex] = data;
                } else if (data instanceof LabeledNull && table[i][rightIndex] instanceof ConstValue) {
                    changes.put((LabeledNull) data, table[i][rightIndex]);

                    //这一部分是在完成重新映射的功能，相当于benchmarking the chase文献中的伪代码第13行
                    if (!changes.isEmpty()) {
                        for (LabeledNull key : changes.keySet()) {
                            if (changes.get(key).equals(data)) {
                                changes.put(key, table[i][rightIndex]);
                            }
                        }
                    }
                } else {
                    if (!changes.containsKey((LabeledNull) table[i][rightIndex])) {
                        changes.put((LabeledNull) table[i][rightIndex], data);
                    }
                }
            }
        }
        LosslessJoin.changeTable(changes);
    }

    public static boolean standard_chase_for_losslessJoin_check(){
        LosslessJoin.setupTable();
        List<FD> fds = LosslessJoin.getFds();
        for (FD fd : fds) {
            LosslessJoin.chaseStep(fd);
        }
        return LosslessJoin.isLosslessJoin();
    }

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

//TODO：1.将这些自己编写的检查自动化————将FD的左边右边列的信息与表格下标的信息联系起来
//      2.仔细考虑将修改添加到changes时增加对重新映射的判断
//      3.目前编写的方法只能处理函数依赖右侧只有一个属性值的情况，如果右侧为有多个属性的属性列表如何处理？是否是多值依赖？(查多值依赖的定义)
//      4.目前的输入格式过于简单，考虑怎样修改能够处理看起来像数据库中符号的输入？————>学习使用正则表达式

