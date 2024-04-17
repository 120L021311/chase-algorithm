package util;

import constraints.TGD;
import datatype.LabeledNull;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 这个类完成数据的预处理工作，主要完成依赖条件的预处理功能
 */
public class Preprocess {

    /**
     * 对于一个待处理的TGD的body或head，把它处理成 Atom之间用"and"分隔，Variable之间用","分隔的形式
     * 思路：1.先拿到每个Atom的变量列表，把其中的","替换成"@"
     * 2.使用","分隔出一个Atom的字符串表示
     * 3.再把Atom的字符串表示中的"@"替换回","
     *
     * @param str 待处理的body或head字符串
     * @return 分离的一个个bodyAtom/headAtom的字符串表示
     */
    public static List<String> preprocessBodyOrHead(String str) {
        List<String> atomStrings = new ArrayList<>(); // 处理完后一个一个分离的bodyAtom的String表示
        do {
            int left = str.indexOf('(');
            int right = str.indexOf(')');
            String variableListSubstring = str.substring(left + 1, right); //Atom的变量列表的字符串
            String replacedVariableListSubstring = variableListSubstring.replace(',', '@'); //替换后的Atom的变量列表的字符串
            //如果要替换的子串包含特殊字符，如点号 (.)、星号 (*)、问号 (?) 等，需要进行转义。
            //可以使用 Pattern.quote() 方法对要替换的子串进行转义，以确保它被视为普通字符串而不是正则表达式。
            str = str.replaceFirst(Pattern.quote(variableListSubstring), replacedVariableListSubstring);
            if (str.indexOf(',') != -1) { //说明后面还有bodyAtom等待处理
                int i1 = str.indexOf(',');
                String atomString = str.substring(0, i1).replace('@', ',');//处理得到的一个bodyAtom的String表示
                atomStrings.add(atomString);
                str = str.substring(i1 + 1).trim();
            } else {
                String atomString = str.replace('@', ',');
                atomStrings.add(atomString);
                str = str.substring(str.indexOf(')') + 1).trim();
            }
        } while (str.indexOf('(') != -1);
        return atomStrings;
    }

    /**
     * 预处理：将测试用例中的一条TGD约束处理成符合输入假设的约束形式
     *
     * @param line 测试用例中的一条约束的字符串表示
     * @return 符合输入假设的约束的字符串表示
     */
    public static String preprocessOneTGD(String line) {
        line = line.replace(" (", "(");
        line = line.substring(0, line.lastIndexOf(".")).trim();
        String[] split = line.split("->");
        String bodyAtomsString = split[0].trim(); //等待处理的String形式的bodyAtoms
        String headAtomsString = split[1].trim(); //等待处理的String形式的headAtoms
        System.out.println("bodyAtomsString = " + bodyAtomsString);
        System.out.println("headAtomsString = " + headAtomsString);

        List<String> bodyAtomStrs = Preprocess.preprocessBodyOrHead(bodyAtomsString);
        List<String> headAtomStrs = Preprocess.preprocessBodyOrHead(headAtomsString);
        System.out.println("bodyAtomStrs = " + bodyAtomStrs);
        System.out.println("headAtomStrs = " + headAtomStrs);

        //拼接出符合输入格式的依赖关系的字符串表示
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bodyAtomStrs.size() - 1; i++) {
            stringBuilder.append(bodyAtomStrs.get(i).trim()).append(" and ");
        }
        stringBuilder.append(bodyAtomStrs.get(bodyAtomStrs.size() - 1).trim());
        stringBuilder.append(" -> ");
        for (int i = 0; i < headAtomStrs.size() - 1; i++) {
            stringBuilder.append(headAtomStrs.get(i).trim()).append(" and ");
        }
        stringBuilder.append(headAtomStrs.get(headAtomStrs.size() - 1).trim());
        return stringBuilder.toString();
    }

    /**
     * 预处理一个TGD文件，完成格式标准化和TGD的分解，结果保存到文件中
     *
     * @param fileName1 未处理的TGD文件的路径
     * @param fileName2 处理后的结果文件的路径
     */
    public static void preprocessTGDFile(String fileName1, String fileName2) {
        BufferedReader bufferedReader = null;
        List<TGD> tgds = new ArrayList<>();
        BufferedWriter bufferedWriter = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(fileName1));
            bufferedWriter = new BufferedWriter(new FileWriter(fileName2));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String tgdStr = preprocessOneTGD(line);
                List<String> tgdStrs = normalizeOneTGD(tgdStr);
                for (String str : tgdStrs) {
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
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
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 预处理一条EGD，完成格式的标准化
     *
     * @param line 一条EGD的字符串形式表示
     * @return 格式标准化后的一条EGD
     */
    public static String preprocessOneEGD(String line) {
        line = line.substring(0, line.lastIndexOf(".")).trim();
        String[] split = line.split("->");
        String bodyAtomsString = split[0].trim(); //等待处理的String形式的bodyAtoms
        String headAtomsString = split[1].trim();
        headAtomsString = headAtomsString.replace(" = ", "=");
        System.out.println("bodyAtomsString = " + bodyAtomsString);
        System.out.println("headAtomsString = " + headAtomsString);

        List<String> bodyAtomStrs = Preprocess.preprocessBodyOrHead(bodyAtomsString);
        System.out.println("bodyAtomStrs = " + bodyAtomStrs);
        String[] split1 = headAtomsString.split(",");
        List<String> headAtomStrs = new ArrayList<>(Arrays.asList(split1));
        System.out.println("headAtomStrs = " + headAtomStrs);

        //拼接出符合输入格式的依赖关系的字符串表示
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bodyAtomStrs.size() - 1; i++) {
            stringBuilder.append(bodyAtomStrs.get(i).trim()).append(" and ");
        }
        stringBuilder.append(bodyAtomStrs.get(bodyAtomStrs.size() - 1).trim());
        stringBuilder.append(" -> ");
        for (int i = 0; i < headAtomStrs.size() - 1; i++) {
            stringBuilder.append(headAtomStrs.get(i).trim()).append(" and ");
        }
        stringBuilder.append(headAtomStrs.get(headAtomStrs.size() - 1).trim());
        return stringBuilder.toString();
    }

    /**
     * 预处理一个EGD文件，结果保存到一个文件中
     *
     * @param fileName1 未处理的文件路径
     * @param fileName2 预处理工作完成后的EGD结果文件
     */
    public static void preprocessEGDFile(String fileName1, String fileName2) {
        BufferedReader bufferedReader = null;
        List<TGD> tgds = new ArrayList<>();
        BufferedWriter bufferedWriter = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(fileName1));
            bufferedWriter = new BufferedWriter(new FileWriter(fileName2));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String egdStr = preprocessOneEGD(line);
                bufferedWriter.write(egdStr);
                bufferedWriter.newLine();
                bufferedWriter.flush();
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
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 对于一条TGD，尝试将其标准化（分解）
     *
     * @param tgdStr 一条TGD的字符串形式表示
     * @return 这条TGD标准化后得到的(多条)TGD的字符串形式集合
     */
    public static List<String> normalizeOneTGD(String tgdStr) {
        List<String> result = new ArrayList<>();
        String[] split = tgdStr.split("->");
        String bodyString = split[0].trim();
        String headString = split[1].trim();

        //首先需要拿到所有的自由变量
        HashSet<String> bodyVariableStrs = new HashSet<>();//body中出现的Variable的字符串集合
        HashSet<String> headVariableStrs = new HashSet<>();//head中出现的Variable的字符串集合

        List<String> bodyAtomStrs = new ArrayList<>(); // 此列表中保存了一个个headAtom的字符串形式
        String[] split1 = bodyString.split("and");
        for (String s : split1) {
            bodyAtomStrs.add(s.trim());
        }
        for (String bodyAtomStr : bodyAtomStrs) {
            int left = bodyAtomStr.indexOf('(');
            int right = bodyAtomStr.indexOf(')');
            String substring = bodyAtomStr.substring(left + 1, right);//一个Atom变量列表那部分子串
            String[] variableStrs = substring.split(",");
            bodyVariableStrs.addAll(Arrays.asList(variableStrs));
        }

        List<String> headAtomStrs = new ArrayList<>(); // 此列表中保存了一个个headAtom的字符串形式
        String[] split2 = headString.split("and");
        for (String s : split2) {
            headAtomStrs.add(s.trim());
        }
        for (String headAtomStr : headAtomStrs) {
            int left = headAtomStr.indexOf('(');
            int right = headAtomStr.indexOf(')');
            String substring = headAtomStr.substring(left + 1, right);//一个Atom变量列表那部分子串
            String[] variableStrs = substring.split(",");
            headVariableStrs.addAll(Arrays.asList(variableStrs));
        }

        headVariableStrs.removeAll(bodyVariableStrs);
        HashSet<String> freeVariableStrs = new HashSet<>(headVariableStrs);//head中出现的自由变量的字符串集合
//        System.out.println(freeVariableStrs);

        //构建headAtomStr和它其中包含的自由变量的映射关系
        HashMap<String, Set<String>> map = new HashMap<>();
        for (String headAtomStr : headAtomStrs) {
            HashSet<String> strings = new HashSet<>(); // 每个headAtomStr其中包含的自由变量的字符串集合
            int left = headAtomStr.indexOf('(');
            int right = headAtomStr.indexOf(')');
            String substring = headAtomStr.substring(left + 1, right);//一个Atom变量列表那部分子串
            String[] variableStrs = substring.split(",");
            for (String variableStr : variableStrs) {
                if (freeVariableStrs.contains(variableStr)) {
                    strings.add(variableStr);
                }
            }
            map.put(headAtomStr, strings);
        }
//        System.out.println(map);

        while (!headAtomStrs.isEmpty()) {
            String s = headAtomStrs.get(0);
            if (map.get(s).isEmpty()) {
                result.add(bodyString + " -> " + s);
                headAtomStrs.remove(0);
                continue;
            }

            List<String> tempHeadAtomStrs = new ArrayList<>(); // 左侧headAtom的字符串集合
            tempHeadAtomStrs.add(s);
            HashSet<String> tempFreeVariableStrs = new HashSet<>(); // 左侧自由变量的字符串集合
            tempFreeVariableStrs.addAll(map.get(s));
            headAtomStrs.remove(s);
            boolean changed;
            do {
                changed = false;
                List<String> newHeadAtomStrs = new ArrayList<>(); // 本次循环扫描要添加到左侧的headAtom的字符串集合
                HashSet<String> newFreeVariableStrs = new HashSet<>(); // 本次循环扫描要添加到左侧的自由变量的字符串集合
                for (String headAtomStr : headAtomStrs) {
                    //如果新的headAtomStr的自由变量与左侧的自由变量有交集，则本轮把它添加到左侧
                    Set<String> strings = map.get(headAtomStr); // 新的headAtomStr的自由变量
                    if (!Preprocess.intersection(strings, tempFreeVariableStrs).isEmpty()) {
                        newHeadAtomStrs.add(headAtomStr);
                        newFreeVariableStrs.addAll(strings);
                        changed = true;
                    }
                }
                tempHeadAtomStrs.addAll(newHeadAtomStrs);
                tempFreeVariableStrs.addAll(newFreeVariableStrs);
                headAtomStrs.removeAll(newHeadAtomStrs);
            } while (changed);

            //构建一条TGD加入
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(bodyString).append(" -> ");
            for (int i = 0; i < tempHeadAtomStrs.size() - 1; i++) {
                stringBuilder.append(tempHeadAtomStrs.get(i)).append(" and ");
            }
            stringBuilder.append(tempHeadAtomStrs.get(tempHeadAtomStrs.size() - 1));
            result.add(stringBuilder.toString());
        }
        return result;
    }

    /**
     * 检查两个RelationalAtom中有无公共的Variable，用于TGD分解
     *
     * @param set1 第一个RelationalAtom中的Variable的字符串集合
     * @param set2 第二个RelationalAtom中的Variable的字符串集合
     * @return 两个集合的交集
     */
    public static HashSet<String> intersection(Set<String> set1, Set<String> set2) {
        HashSet<String> result = new HashSet<>();
        for (String string : set1) {
            if (set2.contains(string)) {
                result.add(string);
            }
        }
        return result;
    }

    /**
     * 为 Deep这一测试场景下所有的初始输入数据文件添加表头"c0,c1,c2,c3"
     * @param directoryPath Deep这一测试场景下所有的初始输入数据存放目录的路径
     */
    public static void addHeaderForDeepScenarioData(String directoryPath){
        File directory = new File(directoryPath);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if(files != null){
                for (File file : files) {
                    // 读取原始文件内容
                    StringBuilder content = new StringBuilder();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line.equals("")){
                                continue;
                            }
                            content.append(line).append(System.lineSeparator());
                        }
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // 要插入的内容
                    String insertContent = "c0,c1,c2,c3" + System.lineSeparator();

                    // 在读取的内容之前插入要插入的内容
                    content.insert(0, insertContent);

                    // 将修改后的内容写入文件
                    try {
                        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                        bufferedWriter.write(content.toString().trim());
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("指定路径不是一个目录！");
        }
    }

    @Test
    public void testAddHeaderForDeepScenarioData(){
        String directoryPath = "examples/文献数据/deep/300/data";
//        Preprocess.addHeaderForDeepScenarioData(directoryPath);
    }

    @Test
    public void testNormalize() {
        Preprocess.normalizeOneTGD("s1(?x1,?x5,?x6,?x7) -> t1(?x1,?x6,?Y3) and t1(?x6,?x7,?Y2) and t1(?x7,?x6,?Y3)");
    }

    public static void main(String[] args) {
//        String line1 = "s0(?x1,?x2,?x3,?x4), s1(?x1,?x5,?x6,?x7) -> t1(?x1,?x2,?x3),t2(?x1,?Y2,?x4),t3(?x6,?x5,?Y2) .";
//        System.out.println(Preprocess.preprocessOneTGD(line1));
        //TODO:变量和表前的空格是否需要处理？需要
//        String line2 = "touch_ad_1_nl0_ce0(?nut_ad_1_nl0_ae0ke0, ?slope_ad_1_nl0_ae1, ?measure_ad_1_nl0_ae2, ?cheese_ad_1_nl0_ae3), touch_ad_1_nl0_ce0(?nut_ad_1_nl0_ae0ke0, ?slope_ad_1_nl0_ae10, ?measure_ad_1_nl0_ae21, ?cheese_ad_1_nl0_ae32) -> ?measure_ad_1_nl0_ae2 = ?measure_ad_1_nl0_ae21, ?cheese_ad_1_nl0_ae3 = ?cheese_ad_1_nl0_ae32, ?slope_ad_1_nl0_ae1 = ?slope_ad_1_nl0_ae10 .";
//        System.out.println(Preprocess.preprocessOneEGD(line2));
        preprocessTGDFile("examples/文献数据/doctors/10k/ST-ONLY/doctors.st-tgds.txt", "examples/文献数据/doctors/10k/ST-ONLY/st-tgds.txt");
//        preprocessEGDFile("examples/文献数据/doctors/10k/dependencies/doctors.t-egds.txt", "examples/文献数据/doctors/10k/dependencies/t-egds.txt");
    }
}
