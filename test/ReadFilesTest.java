import constraints.FD;
import constraints.TGD;
import datatype.Variable;
import org.junit.Test;
import util.ReadFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReadFilesTest {

    @Test
    public void testReadFDs() {
        List<FD> fds = ReadFiles.readFDs("examples\\\\FD\\\\FDs.txt");
        for (FD fd : fds) {
            System.out.println(fd);
        }
    }

    @Test
    public void testParseRelationalAtom() {
//        HashMap<String, List<String>> map = util.ReadFiles.parseRelationalAtom("R(A,B,C,D,E)");
        HashMap<String, List<Variable>> map = ReadFiles.parseRelationalAtom("R4(C,D,E)");
        ArrayList<String> temp1 = new ArrayList<>(map.keySet());
        System.out.println("relationName = " + temp1.get(0));
        ArrayList<List<Variable>> temp2 = new ArrayList<>(map.values());
        System.out.println("attributes = " + temp2.get(0));
    }

    @Test
    public void testGetRelationNameFromRelationalAtom() {
        String relationName = ReadFiles.getRelationNameFromRelationalAtom("STUDENT(Sid,Sage,Ssex,Sdept)");
        System.out.println(relationName); // STUDENT
    }

    @Test
    public void testGetVariableListFromRelationalAtom() {
        List<Variable> variableList = ReadFiles.getVariableListFromRelationalAtom("STUDENT(Sid,Sage,Ssex,Sdept)");
        List<String> variableNameList = new ArrayList<>();
        for (Variable variable : variableList) {
            variableNameList.add(variable.getName());
        }
        System.out.println(variableNameList); // [Sid,Sage,Ssex,Sdept]
    }

    @Test
    public void testReadDecompositions(){
        List<List<String>> decompositions = ReadFiles.readDecompositions("examples\\\\FD\\\\decompositions.txt");
        System.out.println(decompositions); // [[A, D], [A, B], [B, E], [C, D, E], [A, E]]
    }

    @Test
    public void testReadAttributes(){
        List<String> attributes = ReadFiles.readAttributes("examples\\\\FD\\\\attributes.txt");
        System.out.println(attributes); // [A, B, C, D, E]
    }

    @Test
    public void testParseTGD(){
        System.out.println(ReadFiles.parseTGD("R(x0,x1,x2) -> Q(x2,z1,z2) and P(x0,z3)"));
        System.out.println(ReadFiles.parseTGD("R(x1,x2,x3) -> Q(x2,x1,z1) and P(x1,r)"));
        System.out.println(ReadFiles.parseTGD("Emprunt(x1,x2) -> Livre(x2,y1,z1) and Lecteur(x1)"));
        System.out.println(ReadFiles.parseTGD("Livre(x1,x2,x3) -> Auteur(x2,y,z)"));
    }

    @Test
    public void testReadTGDs(){
        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
        for (TGD tgd : tgds) {
            System.out.println(tgd);
        }
    }
}
