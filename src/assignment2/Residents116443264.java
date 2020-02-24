package assignment2;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

public class Residents116443264 {
    public static void main(String[] args) {
        //initialising reader and file path
        String filename = "/home/olan/IdeaProjects/Constraint/src/assignment2/residents4.txt";
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
        IntVar[] shiftsDone = new IntVar[reader.getNumResidents()];
        for (int row = 0; row < reader.getNumResidents(); row++){
            shiftsDone[row] = model.intVar("shifts done"+row, reader.getMinShifts(), reader.getNumShifts());
        }

        //create IntVar that stores the total number of shifts done across all residents
        IntVar totalShiftsDone = model.intVar("total shifts done", reader.getMinShifts()*reader.getNumResidents(), reader.getNumShifts()*reader.getNumResidents());

        /* create array of IntVars for total number of residents per shift
           where every index is a shift and every value is total number of residents on that shift
         */
        IntVar[] residentsPerShift = new IntVar[reader.getNumShifts()];
        for(int column = 0; column < reader.getNumShifts(); column ++){
            residentsPerShift[column] = model.intVar("residentsOnShift"+column, reader.getMinResidents()[column], reader.getNumResidents());
        }

        //constrain every value in shiftsDone to be equal to the amount of shifts done in schedule
        for (int row = 0; row < reader.getNumResidents(); row++){
            model.sum(schedule[row], "=", shiftsDone[row]).post();
        }

        //constrain every value in residentsPerShift to be equal to the total number of residents on in that shift in schedule
        for(int column = 0; column < reader.getNumShifts(); column ++){
            model.sum(transpose[column], "=", residentsPerShift[column]).post();
        }

        //constrain totalShiftsDone to be equal to the sum of shiftsDone
        model.sum(shiftsDone, "=", totalShiftsDone).post();

        //constrain the schedule such that residents meet qualifications needed
        //loop through each resident and qualification in QualsNeeded
        for(int resident = 0; resident < reader.getNumResidents(); resident++){
            for(int qual = 0; qual < reader.getNumQuals(); qual ++){
                //if resident requires qualification number 'qual'
                if(reader.getQualsNeeded()[resident][qual] == 1){
                    //find shift qualification is offered in
                    for (int shift = 0; shift < reader.getNumShifts(); shift++){
                        if (reader.getQualsOffered()[qual][shift] == 1){
                            System.out.println("Resident " + resident + " needs qualification " + qual + " at shift " + shift);
                            model.arithm(schedule[resident][shift], "=", 1).post();
                        }
                    }
                }
            }
        }

        //minimize the total number of shifts done across all residents
        model.setObjective(Model.MINIMIZE, totalShiftsDone);
        System.out.println("Minimum number of residents at each shift" + Arrays.toString(reader.getMinResidents()));
        //create the solver
        Solver solver = model.getSolver();

        while(solver.solve()){
            System.out.println("Solution " + solver.getSolutionCount() + ":-------------------------");
            //System.out.println(Arrays.deepToString(schedule));
            System.out.println(totalShiftsDone);
        }
    }
}
