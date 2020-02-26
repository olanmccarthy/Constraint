package lecture08;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

/*
 * A class for solving a warehouse location problem.
 * uses a basic model with only sum or scalar global constraints
 * Data values are hard coded into the class
 */

public class Warehouse2 {

   public static void main(String[] args) {

      /*--------------- PROBLEM PARAMETERS ----------------*/
      /**/
      int numWHs = 5;           //number of warehouses
      int numShops = 10;        //number of shops
      int maintenance = 30;     //common maintenance cost per warehouse

      int[] capacity = {1,4,2,1,3}; //capacity of each warehouse

      int[][] supply = {          //supply[i][j] is the cost of supplying shop i from warehouse j
            {20, 24, 11, 25, 30},
            {28, 27, 82, 83, 74},
            {74, 97, 71, 96, 70},
            {2, 55, 73, 69, 61},
            {46, 96, 59, 83, 4},
            {42, 22, 29, 67, 59},
            {1, 5, 73, 59, 56},
            {10, 73, 13, 43, 96},
            {93, 35, 63, 85, 46},
            {47, 65, 55, 71, 95}};
      /**/
      /* SIMPLE TEST CASE 
		int numWHs = 3;
		int numShops = 4;
		int maintenance = 5;

		int[] capacity = {2,4,3};

		int[][] supply = {
				{4,6,8},
				{7,7,5},
				{4,5,6},
				{9,6,5}
		};
       */

      int maxShopCost = 0;    //the maximum supply cost for any shop
      for (int shop = 0; shop < numShops; shop++) {
         for (int wh = 0; wh < numWHs; wh++) {
            if (supply[shop][wh] > maxShopCost) {
               maxShopCost = supply[shop][wh];
            }
         }
      }

      int maxCapacity = 0; //the maximum capacity for any warehouse
      for (int wh = 0; wh < numWHs; wh++) {
         if (capacity[wh] > maxCapacity) {
            maxCapacity = capacity[wh];
         }
      }

      //display the computed problem parameters
      System.out.println("maxShopCost = " + maxShopCost);
      System.out.println("maxCapacity = " + maxCapacity);

      //Create the model

      Model model = new Model("Warehouse location");

      //Create the variables
      
      //a matrix of 0,1 IntVars, stating for each shop/warehouse pair, whether the warehouse supplies the shop
      IntVar[][] decisions = model.intVarMatrix(numShops,  numWHs, 0, 1);

      //the transpose of the above matrix, so that we can work with an array for each object
      IntVar[][] decisionsT = ArrayUtils.transpose(decisions);

      //an array of IntVars, stating the cost of supplying to each shop from its chosen warehouse
      IntVar[] shopCost = model.intVarArray("shopCost",  numShops,  0,  maxShopCost);

      //the cost of supplying all the shops
      IntVar allShopCost = model.intVar("allShopCost",  0,  maxShopCost*numShops);

      //an array of booleans, stating for each warehouse whether or not it is active
      IntVar[] whActive = model.intVarArray("whActive", numWHs, 0, 1);

      //the number of active warehouses
      IntVar numberActive = model.intVar("numberActive",  0,  numWHs);
      
      //the total maintenance cost
      IntVar totalMaintenanceCost = model.intScaleView(numberActive, maintenance);

      //an array of Intvars, stating for each warehouse how many shops it supplies
      IntVar[] supplyCount = new IntVar[numWHs];
      for (int wh = 0; wh < numWHs; wh++) {
         supplyCount[wh] = model.intVar("supplyCount",  0,  capacity[wh]);
      }

      //the total cost of the chosen warehouses and warehouse,shop pairs
      IntVar totalCost = model.intVar("totalCost", 0, maxShopCost*numShops + numWHs*maintenance);

      /* ------------- CONSTRAINTS ---------- */

      //every shop is supplied by exactly one warehouse
      //and the cost of a shop is the supply cost from its chosen warehouse
      //(only one entry of decisions[shop] will have a '1' (rest are '0'), so take a scalar with supply costs)
      for (int shop = 0; shop < numShops; shop++) {
         model.sum(decisions[shop], "=", 1).post();
         model.scalar(decisions[shop], supply[shop], "=", shopCost[shop]).post();
      }

      //the cost of supply is the sum of the shop supply costs
      model.sum(shopCost, "=", allShopCost).post();

      
      //for each warehouse, the sum of the shops supplied by it is its supplyCount
      //and it is active if and only if its supplyCOunt is bigger than 0
      //(alternative) and it's activity is equal to the max of the whActive array 
      //and  no warehouse can supply more than its capacity of shops

      for (int wh = 0; wh < numWHs; wh++) {
         model.sum(decisionsT[wh], "=", supplyCount[wh]).post();
         model.ifOnlyIf(model.arithm(supplyCount[wh], ">", 0), model.arithm(whActive[wh], "=", 1));
         //model.max(whActive[wh], decisionsT[wh]).post();
         model.arithm(supplyCount[wh], "<=", capacity[wh]).post();
      }
            
      //the number of active warehouses is the sum of the array of activeWh
      model.sum(whActive, "=", numberActive).post();
      
      //the total cost is the sum of all shop costs plus the maintenance cost
      model.arithm(totalMaintenanceCost, "+", allShopCost, "=", totalCost).post();


      //Solve the problem

      Solver solver = model.getSolver();
      
      //IntVar[] searchVars = ArrayUtils.concat(ArrayUtils.flatten(decisions), totalCost);
            
      //Search Strategy
      //solver.setSearch(Search.domOverWDegSearch(searchVars));
      //solver.setSearch(Search.inputOrderLBSearch(searchVars));
      //solver.setSearch(Search.activityBasedSearch(searchVars)); 		 
      //solver.setSearch(new ImpactBased(searchVars, true));

      model.setObjective(Model.MINIMIZE, totalCost);

      int numsolutions = 0;
      //      if (solver.solve()) {
      while (solver.solve()) { //print the solution
         numsolutions++;
         System.out.print("Solution " + numsolutions + ": ");

         //print out our own solution

         System.out.print("Active warehouses: ");
         for (int wh=0; wh < numWHs; wh++) {
            if (whActive[wh].getValue() == 1)
               System.out.print(wh + " ");
         }
         System.out.println(" (Maintenance cost = " + maintenance*numberActive.getValue() + ")");
         for (int shop = 0; shop < numShops; shop++) {
            //System.out.println("Shop: " + shop + ": w " + shopSupply[shop].getValue() + " (cost " + shopCost[shop].getValue() + ")");
         }
         System.out.println("Total cost: " + totalCost.getValue());
      }
      solver.printStatistics();

   }

}
