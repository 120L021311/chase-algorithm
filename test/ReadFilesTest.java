import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ReadFilesTest {

    @Test
    public void testReadFDs() {
        List<FD> fds = ReadFiles.readFDs("examples\\\\FDs.txt");
        for (FD fd : fds) {
            System.out.println(fd);
        }
    }

    @Test
    public void testParseRelationalAtom() {
//        HashMap<String, List<String>> map = ReadFiles.parseRelationalAtom("R(A,B,C,D,E)");
        HashMap<String, List<String>> map = ReadFiles.parseRelationalAtom("R4(C,D,E)");
        ArrayList<String> temp1 = new ArrayList<>(map.keySet());
        System.out.println("relationName = " + temp1.get(0));
        ArrayList<List<String>> temp2 = new ArrayList<>(map.values());
        System.out.println("attributes = " + temp2.get(0));
    }

    @Test
    public void testGetRelationNameFromRelationalAtom() {
        String relationName = ReadFiles.getRelationNameFromRelationalAtom("STUDENT(Sid,Sage,Ssex,Sdept)");
        System.out.println(relationName); // STUDENT
    }

    @Test
    public void testGetAttributeListFromRelationalAtom() {
        List<String> attributeList = ReadFiles.getAttributeListFromRelationalAtom("STUDENT(Sid,Sage,Ssex,Sdept)");
        System.out.println(attributeList); // [Sid,Sage,Ssex,Sdept]
    }

    @Test
    public void testReadDecompositions(){
        List<List<String>> decompositions = ReadFiles.readDecompositions("examples\\\\decompositions.txt");
        System.out.println(decompositions); // [[A, D], [A, B], [B, E], [C, D, E], [A, E]]
    }

    @Test
    public void testReadAttributes(){
        List<String> attributes = ReadFiles.readAttributes("examples\\\\attributes.txt");
        System.out.println(attributes); // [A, B, C, D, E]
    }
}
