import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 这个类用来读入文件类型的输入
 */
public class ReadFiles {
    /**
     * 读入函数依赖集合文件，并保存在集合中
     * fileName 保存所有函数依赖关系的文件
     * @return 所有函数依赖构关系组成的集合
     */
    public static List<FD> readFDs(String fileName) {
        BufferedReader bufferedReader = null;
        List<FD> ret = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
//                strings.add(line);
                String[] split = line.split("->");
                String[] left = split[0].split(",");
                String[] right = split[1].split(",");
                ArrayList<String> leftAttributes = new ArrayList<>(Arrays.asList(left));
                ArrayList<String> rightAttributes = new ArrayList<>(Arrays.asList(right));
                FD fd = new FD(leftAttributes, rightAttributes);
                ret.add(fd);
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

    public static List<List<String>> readDecompositions(String fileName){
        BufferedReader bufferedReader = null;
        List<List<String>> ret = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] attributes = line.split(",");
                ret.add(Arrays.asList(attributes));
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

    public static List<String> readAttributes(String fileName){
        BufferedReader bufferedReader = null;
        List<String> ret = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] attributes = line.split(",");
                ret = Arrays.asList(attributes);
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
