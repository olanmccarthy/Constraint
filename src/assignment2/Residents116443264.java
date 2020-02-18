package assignment2;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

public class Residents116443264 {
    public static void main(String[] args) {
        //initialising reader and file path
        String filename = "/home/olan/IdeaProjects/Constraint/src/assignment2/residents0.txt";
        ResidentsReader reader = new ResidentsReader(filename);
        //initialise the model
        Model model = new Model("Residents");
        //create m*n matrix of IntVars where each row represents a resident and each column is a shift
        //TODO change this to be boolean matrix
        IntVar[][] schedule = model.intVarMatrix(
                "schedule", reader.getNumResidents(), reader.getNumShifts(),0,1
        );
        //create transpose of above matrix to be able to easily work with the shift columns
        IntVar[][] transpose = ArrayUtils.transpose(schedule);
        /* create array of IntVars for total number of shifts every resident has worked
           where every index is resident number and each value is total number of shifts they have worked
         */
        IntVar[] totalShiftsDone = new IntVar[reader.getNumResidents()];
        for (int row = 0; row < reader.getNumResidents(); row++){
            for (int column = 0; column < reader.getNumShifts(); column++){
                //totalShiftsDone[row] += schedule[row][column]
            }
        }


    }
}
