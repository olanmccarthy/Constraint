package lab03;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

public class Hollywood {

   public static void main(String[] args ) {

      //Input parameters

      int numScenes = 6; //the number of scenes to be shot
      int numActors = 5; //the number of actors
      int numTrailers = 3; //the total number of trailers available


      //for each actor, for each scene, a 1 means the actor is needed when that scene is being shot
      int[][] neededInScene = {
            {0,0,0,1,0,1},
            {1,0,0,0,0,0},
            {0,1,0,1,1,0},
            {1,1,1,0,0,0},
            {0,0,1,0,1,1}
      };		

      //each actor has a daily cost of being on set
      int[] dailyCost = {5,3,8,6,9};

      //compute the max possible cost - assume everyone there for every day
      int maxDailyCost = 0;
      for (int actor = 0; actor < numActors; actor++) {
         maxDailyCost += dailyCost[actor];
      }
      int maxCost = maxDailyCost * numScenes;

      //Model
      Model model = new Model();

      //Variables

      //for each scene, need to state on which day it will be shot
      //length of array is numScenes, since there are the same number of days as scenes
      //(since we only shoot 1 scene per day, and there is no reason to leave any gap)
      IntVar[] days = model.intVarArray(numScenes, 0, numScenes-1);

      //for each actor, we need a task to say when he or she needs a trailer
      //The end of the task is the first day the actor is not there, after previously being on set
      Task[] stay = new Task[numActors];
      for (int actor = 0; actor < numActors; actor++) {
         stay[actor] = new Task(model.intVar(0,numScenes-1), //start day
               model.intVar(1, numScenes),  //duration - at least 1, at most numScenes
               model.intVar(1, numScenes)   //end day - at least 1
               );
      }

      //the cost of having them on set
      IntVar cost = model.intVar(0, maxCost);

      //a separate array of just the duration variables
      IntVar[] durations = new IntVar[numActors];
      for (int actor = 0; actor < numActors; actor++) {
         durations[actor] = stay[actor].getDuration();
      }

      //the number of trailers occupied by each actor
      //the value is always 1, but defined as an IntVar for use in cumulative
      IntVar[] occupancy = model.intVarArray(numActors, 1,1);

      //Constraints

      //for each actor, for each scene, the stay must start on or before the scene's shooting day,
      //and must finish after the scene's day
      for (int actor = 0; actor < numActors; actor++) {
         for (int scene = 0; scene < numScenes; scene++) {
            if (neededInScene[actor][scene] == 1) {
               model.arithm(stay[actor].getStart(), "<=", days[scene]).post();
               model.arithm(stay[actor].getEnd(), ">", days[scene]).post();
            }
         }
      }

      //the days array must be all different (1 scene per day)
      model.allDifferent(days).post();

      //the total cost is the scalar product of the durations with the daily cost
      model.scalar(durations, dailyCost, "=", cost).post();

      //the number of trailers is a constant, but again defined as a variable
      //for use in cumulative
      IntVar outlay = model.intVar(numTrailers);
      
      //no more than numTrailers actors on set on any one day
      model.cumulative(stay, occupancy, outlay).post();

      //Search
      Solver solver = model.getSolver();
      model.setObjective(Model.MINIMIZE, cost);
      
      System.out.println("The actors are required for the following scenes");
      System.out.print("Sc:");
      for (int scene = 0; scene < numScenes; scene++) {
         System.out.print(scene + " ");
      }
      System.out.println();
      
      for (int actor = 0; actor < numActors; actor++) {
         System.out.print(actor + ": ");
         for (int scene = 0; scene < numScenes; scene++) {
            System.out.print(neededInScene[actor][scene] + " ");
         }
         System.out.println("$ = " + dailyCost[actor]);
      }

      

      //      if (solver.solve()) {
      while (solver.solve()) { //print the solution
         System.out.println("Solution " + solver.getSolutionCount() + ":");

         System.out.print("Sc:");
         for (int scene = 0; scene < numScenes; scene++) {
            System.out.print(scene + " ");
         }
         System.out.println();

         System.out.print("D: ");
         for (int scene = 0; scene < numScenes; scene++) {
            System.out.print(days[scene].getValue() + " ");
         }
         System.out.println();


         for (int actor = 0; actor < numActors; actor++) {
            int start = stay[actor].getStart().getValue();
            int end = stay[actor].getEnd().getValue();
            System.out.print(actor + ": ");
            for (int day = 0; day < numScenes; day++)
               if (start <= day && end > day) {
                  System.out.print("1 ");
               }
               else {
                  System.out.print("0 ");
               }
            System.out.println("$ = " + ((end-start)*dailyCost[actor]));
         }

         System.out.println("Total cost = " + cost.getValue());
      }
      solver.printStatistics();
   }
}
