package symbols;

import datatype.Variable;
import java.util.List;
import util.*;

/**
 * 这个类用来表示约束关系中的一个关系原子，如 R(x0,x1,x2)、Q(x2,z1,z2)
 */
public class RelationalAtom {
    //TODO : RelationalAtom类的第二个属性类型为 List<Variable>，是否需要修改之前读入解析 RelationalAtom 的方法？
    private String relationName; // 关系名，如 R、Q
    private List<Variable> variableList; // 变量列表，如[x0,x1,x2]、[x2,z1,z2]

    public RelationalAtom(String relationName, List<Variable> variableList) {
        this.relationName = relationName;
        this.variableList = variableList;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public List<Variable> getVariableList() {
        return variableList;
    }

    public void setVariableList(List<Variable> variableList) {
        this.variableList = variableList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(relationName);
        stringBuilder.append("(");
        for (int i = 0; i < variableList.size()-1; i++) {
            stringBuilder.append(variableList.get(i).toString()).append(",");
        }
        stringBuilder.append(variableList.get(variableList.size()-1)).append(")");
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        String relationalAtom = "R(x0,x1,x2)";
        String relationName = ReadFiles.getRelationNameFromRelationalAtom(relationalAtom);
        List<Variable> variableList = ReadFiles.getVariableListFromRelationalAtom(relationalAtom);
        RelationalAtom atom = new RelationalAtom(relationName, variableList);
        System.out.println(atom.getRelationName());
        System.out.println(atom.getVariableList());
    }
}
