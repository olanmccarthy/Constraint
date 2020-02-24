package lab03;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

/*
 * simple multi-capacity scheduling problem, using the cumulative constraint
 * 
 */
public class Maintenance {

	public static void main(String[] args) {

		/*------PROBLEM SETUP--------*/
		
		//as input, the duration of each task
		int[] lengths = {7,4,5,1,3,2,4,5};
		
		//the resource consumption for each task
		int[] consumption = {1,4,5,5,1,4,3,2};
		
		//earliest starts for each task
		int[] est = {3,0,1,5,0,2,6,1};
		
		//latest finishes for each task
		int[] lft = {15,12,15,11,18,10,16,18};
		
		//get the number of tasks (and quit if the input arrays were not of same length)
		int numberOfTasks = lengths.length;
		if (numberOfTasks != consumption.length
			|| numberOfTasks != est.length
			|| numberOfTasks != lft.length) {
			System.exit(1);
		}
		
		//the capacity of the resource - i.e. engineers available
		int capacity = 6;
		
		//the maximum number of time units in which everything must be scheduled
		int deadline = 19;
		
		/*------SOLVER---------------*/
		
		Model model = new Model("Maintenance");
		
		/*------VARIABLES------------*/
		
		//an array of start time variables, one for each task
		IntVar[] start = new IntVar[numberOfTasks];

		//an array of end time variables, one for each task
		IntVar[] end = new IntVar[numberOfTasks];
		
		//an array of heights (i.e. the consumption of each task, or the number of staff)
		IntVar[] height = new IntVar[numberOfTasks];

		//create the start, end and height variables for each task
		for (int task=0; task < numberOfTasks; task++) {
			start[task] = model.intVar(est[task], lft[task]-1);
			end[task] = model.intVar(est[task]+1, lft[task]);
			height[task] = model.intVar(consumption[task]);
		}

		//an array of Task objects.
		//Each task has a start, a duration and an end, all of them IntVars
		//the durations are fixed as input
		Task[] tasks = new Task[numberOfTasks];
		for (int task = 0; task < numberOfTasks; task++) {
		    tasks[task] = new Task(start[task], model.intVar(lengths[task]), end[task]);   	
		}
		
		//the first day after all work finished (i.e. the max of the end vars)
		IntVar first = model.intVar(1,deadline);
		
		//a variable for the number of employees
		IntVar workforce = model.intVar(0,capacity);

		/*------CONSTRAINTS----------*/

		//post the cumulative constraint saying all must be slotted into the schedule, respecting the capacity
		model.cumulative(tasks,  height, workforce).post();
		
        //the end time of each task is less than or equal to the first free day
		for (int task = 0; task < numberOfTasks; task++) {
			model.arithm(tasks[task].getEnd(), "<=", first).post();
		}
		
		/*------SEARCH STRATEGY-------*/
		
		model.setObjective(Model.MINIMIZE, first);
		//model.setObjective(Model.MINIMIZE, workforce);

		/*------SOLUTION-------------*/
		
		Solver solver = model.getSolver();
//        if (solver.solve()) {
        while (solver.solve()) { //print the solution
            System.out.println("Solution " + solver.getSolutionCount() + ":");

			System.out.print("   ");
			for (int t = 0; t < deadline; t++) {
				System.out.print(t%10 + " ");
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
			System.out.println("Number of staff = " + workforce.getValue());
		}
        
        solver.printStatistics();
	}

}
