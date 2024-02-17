import constraints.TGD;
import database.Database;
import org.junit.Test;
import symbols.RelationalAtom;
import symbols.Trigger;
import util.ReadFiles;

import java.util.List;

public class TGDTest {

    @Test
    public void testGetTriggers() {
        Database database = new Database();
        database.initializeDatabase();
//        System.out.println(database);

        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
        for (TGD tgd : tgds) {
            List<Trigger> triggers = tgd.getTriggers(database);
            System.out.println(tgd + "的" + triggers);
            System.out.println(triggers.size());
        }
    }

    @Test
    public void testCheckActive() {
        Database database = new Database();
        database.initializeDatabase();
        System.out.println(database);

        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
        for (TGD tgd : tgds) {
            List<Trigger> triggers = tgd.getTriggers(database);
            List<RelationalAtom> headAtoms = tgd.getHead();
            for (Trigger trigger : triggers) {
                for (RelationalAtom headAtom : headAtoms) {
//                    boolean b = tgd.checkActive(database, trigger, headAtom);
                    boolean b = trigger.checkActive(database, headAtom);
                    System.out.println("对于TGD：" + tgd + "的trigger " + trigger +
                            ",它的" + headAtom + "是否active的结果为" + b + "\n");
                }
            }
        }
    }

    @Test
    public void testApply(){
        Database database = new Database();
        database.initializeDatabase();
        System.out.println(database);

        List<TGD> tgds = ReadFiles.readTGDs("examples/TGD/tgd.txt");
        for (TGD tgd : tgds) {
            List<Trigger> triggers = tgd.getTriggers(database);
            List<RelationalAtom> headAtoms = tgd.getHead();
            for (Trigger trigger : triggers) {
                for (RelationalAtom headAtom : headAtoms) {
                    boolean b = trigger.checkActive(database,headAtom);
                    if(b){
                        tgd.apply(database,trigger,headAtom);
                        System.out.println(database);
                    }
                }
            }
        }

    }
}
