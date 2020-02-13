import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import java.io.IOException;

public class Pioneer116443264 {
    public static void main(String[] args) throws IOException {
        //wasn't sure how to make a path that would work with my IDE without being very specific like this
        //please change for correction
        String filename = "/home/olan/IdeaProjects/Constraint/src/pioneer2.txt";

        try {
            //create the reader of the txt files
            PioneerData reader = new PioneerData(filename);
            //create the model
            Model model = new Model("Pioneer");

            final int numberOfTypes = reader.getNumTypes(); //number of experiment types

            //CREATING THE VARIABLES

            //array containing the number of experiments done of each type
            //i.e at index 0 if there is a value of 1 there then 1 experiment of type 0 was done
            //value at index 0 indicates experiments of type 0
            IntVar[] experimentsDone = new IntVar[numberOfTypes];
            for (int type=0; type < numberOfTypes; type++){
                experimentsDone[type] = model.intVar("experiment"+type, 0, reader.getTotals()[type]);
            }

            //the total hours used
            IntVar totalHours = model.intVar("total hours", 0, reader.getTotalHours());
            //the total value of experiments
            IntVar totalValue = model.intVar("total value", 0, reader.getMaxValues());

            //using knapsack global constraint to properly constrain our variables without making it NP Hard
            model.knapsack(experimentsDone, totalHours, totalValue, reader.getHours(), reader.getValues()).post();

            //loop through all befores and constrain the first item in the befores to be greater than or equal to
            //the second value in the before
            for(var i =0; i < reader.getNumBefores(); i++){
                model.arithm(experimentsDone[reader.getBefores()[i][0]], ">=", experimentsDone[reader.getBefores()[i][1]]).post();
            }

            //maximise the total value gained from the experiments
            model.setObjective(Model.MAXIMIZE, totalValue);

            Solver solver = model.getSolver();

            while (solver.solve()){
                System.out.println("Solution " + solver.getSolutionCount() + ":  --------------------------------------");
                for(int i=0; i < reader.getNumTypes(); i++){
                    System.out.print(experimentsDone[i].getValue() + " ");
                }
                System.out.println("\n" + totalValue + " " + totalHours);
            }
        }

        catch (IOException e) {
            System.out.println("ERROR: IOException");
            System.out.println(e);
        }
    }
}
