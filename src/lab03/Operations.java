package lab03;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

public class Operations {

   public static void main(String[] args ) {

      //Input parameters

      int numTasks = 7; //the number of operations		

      //for each task X, for each other task Y, a 1 in before[X][Y] Y cannot start until X is finished
      int[][] before = {{0,1,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,1,0,0,0},
            {0,0,0,0,1,1,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,1,0}
      };

      int[][] disjoint = {{0,0,0,1,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,1,0,0,0,0}
      };

      //each task has a fixed duration
      int[] duration = {7,2,2,4,2,5,3};

      //compute the max possible time - assume everything done in a single sequence
      int maxTime = 0;
      for (int task = 0; task < numTasks; task++) {
         maxTime += duration[task];
      }

      //Model
      Model model = new Model();

      //Variables

      //for each task, we need a Task object
      Task[] tasks = new Task[numTasks];
      for (int task = 0; task < numTasks; task++) {
         tasks[task] = new Task(
               model.intVar(0,maxTime-duration[task]), //start time
               model.intVar(duration[task]),  //duration - fixed
               model.intVar(1, maxTime)   //end time - at least 1
            );
      }

      //the total time required
      IntVar makespan = model.intVar(0, maxTime);


      //Constraints

      //for each task X, for each other task Y, 
      //   if X is before Y, impose a constraint
      for (int taskX = 0; taskX < numTasks; taskX++) {
         for (int taskY = 0; taskY < numTasks; taskY++) {
            if (before[taskX][taskY] == 1) {
               model.arithm(tasks[taskX].getEnd(), "<=", tasks[taskY].getStart()).post();
            }
         }
      }

      //for each task X, for each other task Y after X in the lex ordering, 
      //   if X and Y are disjoint Y, impose a constraint
      for (int taskX = 0; taskX < numTasks-1; taskX++) {
         for (int taskY = 1; taskY < numTasks; taskY++) {
            if (disjoint[taskX][taskY] == 1) {
               model.or(
                     model.arithm(tasks[taskX].getEnd(), "<=", tasks[taskY].getStart()),
                     model.arithm(tasks[taskY].getEnd(), "<=",  tasks[taskX].getStart())
                     ).post();
            }
         }
      }

      //for each task, its end time is less than or equal to the makespan
      for (int task = 0; task < numTasks; task++) {
         model.arithm(tasks[task].getEnd(), "<=",  makespan).post();
      }

      //Search
      Solver solver = model.getSolver();
      model.setObjective(Model.MINIMIZE, makespan);

      //      if (solver.solve()) {
      while (solver.solve()) { //print the solution
         System.out.println("Solution " + solver.getSolutionCount() + ":");

         for (int task = 0; task < numTasks; task++) {
            System.out.println("task " + task + ": " 
                  + tasks[task].getStart().getValue() + "--" 
                  + tasks[task].getEnd().getValue()
                  );
         }

         System.out.println("Makespan = " + makespan.getValue());
      }
      solver.printStatistics();
   }
}
