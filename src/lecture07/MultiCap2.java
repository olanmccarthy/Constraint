package lecture07;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/*
 * simple multi-capacity scheduling problem, using the cumulative constraint
 * now with a capacity of the resource that varies over time
 * 
 * NOTE: beware of extra constraints and input arrays for testing the model
 *       - make sure they are commented out for the simple test
 */
public class MultiCap2 {

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

      //the capacity profile of the resource
      int[] capacity = {8, 8, 6, 7, 7, 8, 8};       //example from lecture notes
      //int[] capacity = {8, 8, 8, 8, 8, 8, 8};

      //the length of the capacity profile
      int profileLength = capacity.length;

      //the maximum number of time units in which everything must be scheduled
      int deadline = 6;

      //check that the deadline is not greater than the profile of the resource
      if (deadline > profileLength) {
         System.exit(2);
      }

      //derive the max capacity of the resource
      int maxcap = capacity[0];
      for (int i = 1; i < profileLength; i++) {
         if (capacity[i] > maxcap) {
            maxcap = capacity[i];
         }
      }

      //derive the array of capacity reductions
      int[] capReductions = new int[profileLength];
      int drops = 0;
      for (int t = 0; t<profileLength; t++) {
         if (capacity[t] < maxcap) {
            capReductions[t] = maxcap-capacity[t];
            drops++;
         }
         else {
            capReductions[t] = 0;
         }
      }



      /*------SOLVER---------------*/

      Model model = new Model("Multi Capacity - varying capacity");

      /*------VARIABLES------------*/

      //an array of start time variables, one for each task
      IntVar[] start = model.intVarArray("start", numberOfTasks, 0, deadline-1);

      //an array of end time variables, one for each task
      IntVar[] end = model.intVarArray("end", numberOfTasks, 1, deadline); //a task will be active from start up to end-1

      //create an array of dummy tasks
      Task[] dummy = new Task[drops];
      int[] dummyHeight = new int[drops];
      int time = 0;
      for (int i = 0; i<drops; i++) {
         while (capReductions[time] == 0) {
            time++;
         }
         dummy[i] = new Task(model.intVar(time), model.intVar(1), model.intVar(time+1));
         dummyHeight[i] = capReductions[time];
         System.out.println("Created dummy with start: " + time + "; height: " + capReductions[time]);
         time++;
      }

      //an array of Task objects.
      //Each task has a start, a duration and an end, all of them IntVars
      //the durations are fixed as input
      Task[] tasks = new Task[numberOfTasks + drops];
      for (int task = 0; task < numberOfTasks; task++) {
         tasks[task] = new Task(start[task], model.intVar(lengths[task]), end[task]);   	
      }
      int d = 0;
      for (int task = numberOfTasks; task < numberOfTasks + drops; task++ ) {
         tasks[task] = dummy[d++];
      }

      //an array of heights for cumulative (i.e. the consumption of each task)
      IntVar[] height = new IntVar[numberOfTasks + drops];

      //create a (fixed) IntVar for each height for the cumulative constraint
      for (int task=0; task < numberOfTasks; task++) {
         height[task] = model.intVar(consumption[task]);
      }
      int d2 = 0;
      for (int task = numberOfTasks; task < numberOfTasks + drops; task++ ) {
         height[task] = model.intVar(dummyHeight[d2++]);
      }

      /*------CONSTRAINTS----------*/
      /*
         //Not need if we are using cumulative
		//the end time for each task is the start time plus the duration
		//Using arithm, we can write this as end-start = (fixed) length
		for (int task = 0; task < numberOfTasks; task++) {
			model.arithm(end[task], "-", start[task], "=", lengths[task]).post();
		}
       */		

      //post the cumulative constraint saying all must be slotted into the schedule, respecting the capacity
      model.cumulative(tasks, height, model.intVar(capacity)).post();

      //post some other constraints to test the model
      /* 
		model.arithm(end[0],  "<=", start[1]).post();
		model.arithm(end[6], "<=", start[10]).post();
		model.or(model.arithm(end[6],  "<=", start[2]),
		         model.arithm(end[2], "<=", start[6])).post();
		
		model.arithm(end[3], "<=", start[6]).post();
       */

      /*------SEARCH STRATEGY-------*/

      /*------SOLUTION-------------*/

      Solver solver = model.getSolver();
      //if (solver.solve()) {
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

      solver.printStatistics();
   }

}
