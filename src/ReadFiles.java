import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类用来读入文件类型的输入，依据要读入的数据的不同类型设计不同的正则表达式解析输入
 */
public class ReadFiles {
    private static final String regexForFD = "^(?<leftAttributeList>[^-]+)->(?<rightAttributeList>.+)$"; // FD类型输入格式的正则表达式
    private static final String regexForRelationalAtoms = "^(?<relationName>[^(]+)\\((?<attributeList>[^)]+)\\)$"; // 关系原子类型输入格式的正则表达式，例：R(A,B,C,D,E) 或 R1(A,D)

    /**
     * 读入函数依赖集合文件，并保存在集合中
     * 文件内容要求：文件中一行为一条 FD
     * fileName 保存所有函数依赖关系的文件
     *
     * @return 所有函数依赖构关系组成的集合
     */
    public static List<FD> readFDs(String fileName) {
        BufferedReader bufferedReader = null;
        List<FD> ret = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            Pattern pattern = Pattern.compile(regexForFD);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
//                strings.add(line);
//                String[] split = line.split("->");
//                String[] left = split[0].split(",");
//                String[] right = split[1].split(",");
//                ArrayList<String> leftAttributes = new ArrayList<>(Arrays.asList(left));
//                ArrayList<String> rightAttributes = new ArrayList<>(Arrays.asList(right));
//                FD fd = new FD(leftAttributes, rightAttributes);
//                ret.add(fd);
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String leftAttributeList = matcher.group("leftAttributeList");
                    ArrayList<String> leftAttributes = new ArrayList<>(Arrays.asList(leftAttributeList.split(",")));
                    String rightAttributeList = matcher.group("rightAttributeList");
                    ArrayList<String> rightAttributes = new ArrayList<>(Arrays.asList(rightAttributeList.split(",")));
                    FD fd = new FD(leftAttributes, rightAttributes);
                    ret.add(fd);
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
        }
        return ret;
    }

    /**
     * 对于一个 relationalAtom ，解析得到对应的关系名称
     *
     * @param relationalAtom 输入的 relationalAtom
     * @return 对应的关系名称
     */
    public static String getRelationNameFromRelationalAtom(String relationalAtom) {
        HashMap<String, List<String>> map = ReadFiles.parseRelationalAtom(relationalAtom);
        ArrayList<String> temp = new ArrayList<>(map.keySet());
        return temp.get(0);
    }

    /**
     * 对于一个 relationalAtom ，解析得到关系对应的属性列表
     *
     * @param relationalAtom 输入的 relationalAtom
     * @return 关系对应的属性列表
     */
    public static List<String> getAttributeListFromRelationalAtom(String relationalAtom) {
        HashMap<String, List<String>> map = ReadFiles.parseRelationalAtom(relationalAtom);
        ArrayList<List<String>> temp = new ArrayList<>(map.values());
        return temp.get(0);
    }

    /**
     * 使用正则表达式解析一个 relationalAtom ，每一个 relationalAtom 在无损连接性判断相关输入文件中为一行
     *
     * @param relationalAtom 一个 relationalAtom ，对于无损连接性判断相关输入文件为文件中的一行
     * @return 键值对 (关系名，关系的属性名列表(属性之间用逗号分隔))
     */
    public static HashMap<String, List<String>> parseRelationalAtom(String relationalAtom) {
        Pattern pattern = Pattern.compile(regexForRelationalAtoms);
        Matcher matcher = pattern.matcher(relationalAtom);
        HashMap<String, List<String>> map = new HashMap<>();

        if (matcher.matches()) {
            String relationName = matcher.group("relationName");
            List<String> attributeList = new ArrayList<>(Arrays.asList(matcher.group("attributeList").split(",")));
            map.put(relationName, attributeList);
        }

        return map;
    }

    /**
     * 读入分解后的关系模式文件，返回关系模式分解后的每个模式的属性列表
     * 文件内容要求：文件中一行为一个分解后的关系 Atom
     *
     * @param fileName 保存关系模式分解信息的文件
     * @return 关系模式分解后的每个模式的属性列表构成的列表
     */
    public static List<List<String>> readDecompositions(String fileName) {
        BufferedReader bufferedReader = null;
        List<List<String>> ret = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
//                String[] attributes = line.split(",");
                List<String> attributes = ReadFiles.getAttributeListFromRelationalAtom(line);
                ret.add(attributes);
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
        return ret;
    }

    /**
     * 读入分解前的总的关系模式文件，返回关系模式的属性集
     * 文件内容要求：文件中有一行，内容是未分解的关系 Atom
     *
     * @param fileName 保存未分解关系模式的文件
     * @return 未分解的关系的属性集
     */
    public static List<String> readAttributes(String fileName) {
        BufferedReader bufferedReader = null;
        List<String> ret = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ret = ReadFiles.getAttributeListFromRelationalAtom(line);
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
        return ret;
    }

    public static void main(String[] args) {
//        List<FD> fds = new ArrayList<>();
//        fds = ReadFiles.readFDs("examples\\\\FDs.txt");
//        System.out.println(fds.get(3));
//        List<List<String>> decompositions = new ArrayList<>();
//        decompositions = ReadFiles.readDecompositions("examples\\\\decompositions.txt");
//        for (List<String> temp : decompositions) {
//            for (String s : temp) {
//                System.out.println(s);
//            }
//            System.out.println("------------------------------------");
//        }
        List<String> attributes = new ArrayList<>();
        attributes = ReadFiles.readAttributes("examples\\\\attributes.txt");
        for (String attribute : attributes) {
            System.out.println(attribute);
        }
    }
}
