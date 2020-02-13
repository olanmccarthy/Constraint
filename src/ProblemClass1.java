import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class ProblemClass1 {
    public static void main(String[] args) {

        Model model = new Model("Assignment 1");

        IntVar aPosition = model.intVar("A Position", 0, 5);
        IntVar bPosition = model.intVar("B Position", 0, 5);
        IntVar cPosition = model.intVar("C Position", 0, 5);
        IntVar dPosition = model.intVar("D Position", 0, 5);
        IntVar ePosition = model.intVar("E Position", 0, 5);
        IntVar fPosition = model.intVar("F Position", 0, 5);

        //declaring all positions need to be different
        model.arithm(aPosition, "!=", bPosition).post();
        model.arithm(aPosition, "!=", cPosition).post();
        model.arithm(aPosition, "!=", dPosition).post();
        model.arithm(aPosition, "!=", ePosition).post();
        model.arithm(aPosition, "!=", fPosition).post();

        model.arithm(bPosition, "!=", cPosition).post();
        model.arithm(bPosition, "!=", dPosition).post();
        model.arithm(bPosition, "!=", ePosition).post();
        model.arithm(bPosition, "!=", fPosition).post();

        model.arithm(cPosition, "!=", dPosition).post();
        model.arithm(cPosition, "!=", ePosition).post();
        model.arithm(cPosition, "!=", fPosition).post();

        model.arithm(dPosition, "!=", ePosition).post();
        model.arithm(dPosition, "!=", fPosition).post();

        model.arithm(fPosition, "!=", ePosition).post();

        model.distance(aPosition, bPosition, ">", 1).post();
        model.distance(aPosition, cPosition, ">", 1).post();
        model.distance(bPosition, cPosition, ">", 1).post();
        model.distance(aPosition, dPosition, ">", 2).post();
        model.distance(bPosition, fPosition, ">", 2).post();
        model.distance(bPosition, ePosition, ">", 3).post();

        Solver solver = model.getSolver();

        String[] solution;

        if (solver.solve()){
            System.out.println("Solution: \n");
            System.out.println("A Position:" + aPosition);
            System.out.println("B Position:" + bPosition);
            System.out.println("C Position:" + cPosition);
            System.out.println("D Position:" + dPosition);
            System.out.println("E Position:" + ePosition);
            System.out.println("F Position:" + fPosition);

        }

    }
}
