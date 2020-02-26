package assignment2;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Residents116443264 {
    public static void main(String[] args) {
        //initialising reader and file path
        String filename = "/home/olan/IdeaProjects/Constraint/src/assignment2/residents4.txt";
        ResidentsReader reader = new ResidentsReader(filename);

        //initialise the model
        Model model = new Model("Residents");

        //create m*n matrix of IntVars where each row represents a resident and each column is a shift
        IntVar[][] schedule = model.intVarMatrix(
                "schedule", reader.getNumResidents(), reader.getNumShifts(),0,1
        );

        //create transpose of above matrix to be able to easily work with the shift columns
        IntVar[][] transpose = ArrayUtils.transpose(schedule);
        //create a flattened version of the matrix for search strategy
        IntVar[] flatVars = ArrayUtils.flatten(schedule);

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

        //constrain the total amount of shifts done by each resident to be less than or equal to
        //total number of shifts - minimum amount of breaks i.e every resident is getting at least the
        //minimum amount of breaks
        for(int resident = 0; resident < reader.getNumResidents(); resident++){
            model.arithm(shiftsDone[resident], "<=", reader.getNumShifts() - reader.getBreakPeriod()).post();
        }

        //create the regex files for break period and minBreakSize
        //regex: any amount of 1s or 0s then a break period of at least getBreakPeriod followed by any amount of 1s or 0s
        FiniteAutomaton breakPeriodRegex = new FiniteAutomaton("[0,1]*0{"+ reader.getBreakPeriod() + ",}[0,1]*");
        //regex: any occurrence of 0 surrounded by 1s must be at least size getRestPeriod
        FiniteAutomaton minBreakSizeRegex = new FiniteAutomaton("([1]*0{" + reader.getRestPeriod() + ",}[1]*)*");

        //loop through all the residents and post the above regex constraints
        for(int res = 0; res < reader.getNumResidents(); res++){
            model.regular(schedule[res], breakPeriodRegex).post();
            model.regular(schedule[res], minBreakSizeRegex).post();
        }

        //the sum of all blocks in a size of getMaxBlock +1 must be less than or equal to getMaxBlock
        for(int res = 0; res < reader.getNumResidents(); res++){
            //finish indexing at numShifts - getMaxBlock so you don't index out of range
            for(int shift=0; shift < reader.getNumShifts() - reader.getMaxBlock(); shift++){
                //create copy array from index shift + getMaxBlock + 1
                IntVar[] scheduleCopy = Arrays.copyOfRange(schedule[res], shift, shift + reader.getMaxBlock() + 1);
                //constrain the sum of the copy array to be lte getMaxBlock
                model.sum(scheduleCopy, "<=", reader.getMaxBlock()).post();
            }
        }


        //constrain totalShiftsDone to be equal to the sum of shiftsDone
        model.sum(shiftsDone, "=", totalShiftsDone).post();

        //constrain the schedule such that residents meet qualifications needed
        //loop through each resident and qualification in QualsNeeded
        for(int resident = 0; resident < reader.getNumResidents(); resident++){
            for(int qual = 0; qual < reader.getNumQuals(); qual ++){
                //if resident requires qualification number 'qual'
                if(reader.getQualsNeeded()[resident][qual] == 1){
                    //create array for possible shifts qualification is offered in
                    List<Integer> shifts = new ArrayList<Integer>();
                    //find shift qualification is offered in
                    for (int shift = 0; shift < reader.getNumShifts(); shift++){
                        if (reader.getQualsOffered()[qual][shift] == 1){
                            //add the shift to the array
                            shifts.add(shift);
                        }
                    }
                    //convert the array to a list
                    int[] shiftsArray = new int[shifts.size()];
                    for (int i=0; i < shiftsArray.length; i++) {
                        shiftsArray[i] = shifts.get(i);
                    }
                    //create intVar who's possible values are all values in array
                    IntVar index = model.intVar("index", shiftsArray);
                    //constraint each resident to have to be at one of the tutorials
                    model.element(model.intVar(1), schedule[resident], index, 0).post();
                }
            }
        }

        //minimize the total number of shifts done across all residents
        model.setObjective(Model.MINIMIZE, totalShiftsDone);
        //create the solver
        Solver solver = model.getSolver();

        while(solver.solve()){
            System.out.println("Solution " + solver.getSolutionCount() + ":-------------------------");
            for (int res = 0; res < reader.getNumResidents(); res++){
                for(int sch =0; sch < reader.getNumShifts(); sch++){
                    System.out.print(schedule[res][sch].getValue() + " ");
                }
                System.out.println("");
            }
            System.out.println(totalShiftsDone);
        }
        solver.printStatistics();
    }
}
