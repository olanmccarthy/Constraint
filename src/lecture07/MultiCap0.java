package lecture07;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.util.tools.ArrayUtils;

/*
 * simple multi-capacity scheduling problem, hand-coding the cumulative limits
 * 
 * NOTE: beware of extra constraints and input arrays for testing the model
 *       - make sure they are commented out for the simple test
 */
public class MultiCap0 {

   public static void main(String[] args) {

      /*------PROBLEM SETUP--------*/

      //as input, the duration of each task
      int[] lengths = {3,3,2,2,4,1,3,1,1,1,1};

      //the resource consumption for each task
      int[] consumption = {1,1,2,3,1,5,2,6,4,1,2};

      //get the number of tasks (and quit if the input arrays were not of same length)
      int numberOfTasks = lengths.length;
      if (numberOfTasks != consumption.length) {
         System.exit(1);
      }

      //the capacity of the resource
      int capacity = 8;

      //the maximum number of time units in which everything must be scheduled
      int deadline = 6;

      /*------SOLVER---------------*/

      Model model = new Model("Multi Capacity v0");

      /*------VARIABLES------------*/

      //an array of start time variables, one for each task
      IntVar[] start = model.intVarArray("start", numberOfTasks, 0, deadline-1);

      //an array of end time variables, one for each task
      IntVar[] end = model.intVarArray("end", numberOfTasks, 1, deadline); //a task will be active from start up to end-1

      //an array of Task objects.
      //Each task has a start, a duration and an end, all of them IntVars
      //the durations are fixed as input, so use VF.fixed
      Task[] tasks = new Task[numberOfTasks];
      for (int task = 0; task < numberOfTasks; task++) {
         tasks[task] = new Task(start[task], model.intVar(lengths[task]), end[task]);   	
      }

      //a matrix of activity per time slot
      IntVar activity[][] = model.intVarMatrix(numberOfTasks,  deadline, 0, 1);

      IntVar activityT[][] = ArrayUtils.transpose(activity);

      //an array of resource usage per time slot
      IntVar usage[] = model.intVarArray(deadline,  0, capacity);

      //an array of heights (i.e. the consumption of each task)
      IntVar[] height = new IntVar[numberOfTasks];

      //create a (fixed) IntVar for each height 
      for (int task=0; task < numberOfTasks; task++) {
         height[task] = model.intVar(consumption[task]);
      }

      /*------CONSTRAINTS----------*/

      //the end time for each task is the start time plus the duration
      //Using arithm, we can write this as end-start = (fixed) length
      for (int task = 0; task < numberOfTasks; task++) {
         model.arithm(end[task], "-", start[task], "=", lengths[task]).post();
      }


      //the activity per task
      for (int task = 0; task < numberOfTasks; task++) {
         for (int time = 0; time < deadline; time++) {
            model.ifOnlyIf(model.and(model.arithm(start[task],  "<=", time),
                                     model.arithm(end[task], ">", time)
                                    ),
                           model.arithm(activity[task][time], "=", 1));
         }
      }

      //the consumption per time slot
      for (int time = 0 ; time < deadline; time++) {
         model.scalar(activityT[time], consumption, "=", usage[time]).post();
      }

      //post some other constraints to test the model

      /**/
		model.arithm(end[0],  "<=", start[1]).post();
		model.arithm(end[6], "<=", start[10]).post();
		model.or(model.arithm(end[6],  "<=", start[2]),
			     model.arithm(end[2], "<=", start[6])).post();
		/*
		model.arithm(end[3], "<=", start[6]).post();
       */

      /*------SEARCH STRATEGY-------*/

      /*------SOLUTION-------------*/

      Solver solver = model.getSolver();
      //        if (solver.solve()) {
      while (solver.solve()) { //print the solution
         System.out.println("Solution " + solver.getSolutionCount() + ":");

         System.out.print("   ");
         for (int t = 0; t < deadline; t++) {
            System.out.print(t + " ");
         }
         System.out.println();
         for (int task = 0; task < numberOfTasks; task++) {
            System.out.print(task + ": ");
            int startTime = start[task].getValue();
            for (int t = 0; t < startTime; t++) {
               System.out.print("0 ");
            }
            for (int t=0; t<lengths[task]; t++ ) {
               System.out.print(consumption[task] + " ");
            }
            for (int t = end[task].getValue(); t<deadline; t++) {
               System.out.print("0 ");
            }
            System.out.println();
         }
      }
      System.out.println("MultiCap0.java.");
      solver.printStatistics();
   }

}
