import algorithm.LosslessJoin;
import constraints.FD;
import datatype.Data;
import datatype.LabeledNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class LosslessJoinTest {

    @Test
    public void testNormalizeMapping() {
        LabeledNull null_1 = new LabeledNull();
        LabeledNull null_2 = new LabeledNull();
        LabeledNull null_3 = new LabeledNull();
        LabeledNull null_4 = new LabeledNull();
        HashMap<LabeledNull, Data> changes = new HashMap<>();
        changes.put(null_1, null_2);
        changes.put(null_3, null_4);
        HashMap<LabeledNull, Data> newChange = new HashMap<>();
        newChange.put(null_2, null_3);
        HashMap<LabeledNull, Data> map = LosslessJoin.normalizeMapping(changes, newChange);
        System.out.println(map);
    }

    @Test
    public void testCombineMapping() {
        LabeledNull null_1 = new LabeledNull();
        LabeledNull null_2 = new LabeledNull();
        LabeledNull null_3 = new LabeledNull();
        LabeledNull null_4 = new LabeledNull();
        HashMap<LabeledNull, Data> u = new HashMap<>();
        HashMap<LabeledNull, Data> w = new HashMap<>();
        u.put(null_1, null_2);
        u.put(null_3, null_4);
        w.put(null_2, null_3);
        HashMap<LabeledNull, Data> w_u = LosslessJoin.combineMapping(u, w);
        System.out.println(w_u);
        HashMap<LabeledNull, Data> u_w_u = LosslessJoin.combineMapping(w_u, u);
        System.out.println(u_w_u);
    }

    @Test
    @SuppressWarnings({"all"})
    public void test_standard_chase_for_losslessJoin_check() {
        LosslessJoin.setupTable();
        List<FD> fds = LosslessJoin.getFds();
        for (FD fd : fds) {
            LosslessJoin.chaseStep(fd);
        }
        assert LosslessJoin.isLosslessJoin() == true;
    }
}
