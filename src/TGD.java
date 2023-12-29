import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这个类用来表示约束关系中的 TGD，其形式为：body -> head
 * 其中 body和 head分别由一个或多个 RelationalAtom 组成，RelationalAtom之间用"and"分隔
 * 例：R(x0,x1,x2) -> Q(x2,z1,z2) and P(x0,z3)
 */
public class TGD {
    private List<RelationalAtom> body;
    private List<RelationalAtom> head;

    public TGD(List<RelationalAtom> body, List<RelationalAtom> head) {
        this.body = body;
        this.head = head;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("bodyAtoms :");
        for (int i = 0; i < body.size() - 1; i++) {
            stringBuilder.append(body.get(i).toString()).append(",");
        }
        stringBuilder.append(body.get(body.size() - 1).toString()).append("\n");
        stringBuilder.append("headAtoms :");
        for (int i = 0; i < head.size() - 1; i++) {
            stringBuilder.append(head.get(i).toString()).append(",");
        }
        stringBuilder.append(head.get(head.size() - 1).toString()).append("\n");
        return stringBuilder.toString();
    }

}
