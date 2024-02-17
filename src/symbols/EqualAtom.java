package symbols;

import datatype.Variable;

/**
 * 这个类用来表示约束中的一个相等原子
 * 如MasterSupp(sid,sa,sn,h) and MasterSupp(sid,sa',sn',h') -> sa=sa' and sn=sn' and h=h' 中的 sa=sa'
 */
public class EqualAtom {
    private String str;
    private Variable leftVariable;
    private Variable rightVariable;

    public EqualAtom(String str) {
        this.str = str;
        String[] split = str.split("=");
        leftVariable = new Variable(split[0]);
        rightVariable = new Variable(split[1]);
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public Variable getLeftVariable() {
        return leftVariable;
    }

    public void setLeftVariable(Variable leftVariable) {
        this.leftVariable = leftVariable;
    }

    public Variable getRightVariable() {
        return rightVariable;
    }

    public void setRightVariable(Variable rightVariable) {
        this.rightVariable = rightVariable;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(leftVariable.toString());
        stringBuilder.append("=");
        stringBuilder.append(rightVariable.toString());
        return stringBuilder.toString();
    }
}
