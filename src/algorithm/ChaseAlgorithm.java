package algorithm;

import constraints.TGD;
import database.Database;
import symbols.RelationalAtom;
import symbols.Trigger;

import java.util.List;

public class ChaseAlgorithm {

    public static void chaseForLinearTGD(Database database, List<TGD> tgds) {
        boolean changed;
        do {
            changed = false;
            for (TGD tgd : tgds) {
                List<Trigger> triggers = tgd.getTriggers(database);
                for (Trigger trigger : triggers) {
                    List<RelationalAtom> headAtoms = tgd.getHead();
                    for (RelationalAtom headAtom : headAtoms) {
                        if (tgd.checkActive(database, trigger, headAtom)) {
                            tgd.apply(database, trigger, headAtom);
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }

}
