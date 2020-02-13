import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class InterviewCSP {
   public static void main(String[] args) {

      // Create a Model
      Model model = new Model("the interview problem");

      // Create variables
      IntVar AliceTime = model.intVar("Alice time", 2, 3); // either 2 or 3
      IntVar BobTime = model.intVar("Bob time", 1, 2); // either 1 or 2
      IntVar CarolTime = model.intVar("Carol time", 1, 2); // either 1 or 2

      // Post constraints
      model.arithm(AliceTime, "!=", BobTime).post();   //not at same time
      model.arithm(AliceTime, "!=", CarolTime).post(); 
      model.arithm(BobTime, "!=", CarolTime).post(); 

      // Solve the problem
      Solver solver = model.getSolver();

      if (solver.solve()) {     // Print the solution
         System.out.println(AliceTime); 
         System.out.println(BobTime); 
         System.out.println(CarolTime); 
      }
      else {
         System.out.println("No Solution");
      }
   }
}
	
