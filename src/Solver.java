import java.util.Scanner;

/*
 * This class is the starting point of running the search algorithms.
 * It prompts the user to choose between FC or MAC.
 */

public class Solver {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    BinaryCSPReader reader = new BinaryCSPReader() ;
    BinaryCSP csp;
    FC fcSolver;
    MAC macSolver;
  
    if (args.length != 1) {
      System.out.println("Usage: java BinaryCSPReader <file.csp>") ;
      scanner.close();
      return;
    }
  
    csp = reader.readBinaryCSP(args[0]) ;

    if (csp == null) {
      System.out.println("Error reading CSP file: " + args[0]) ;
      scanner.close();
      return;
    }

    System.out.println("Choose the search algorithm to run. \n 1: Forward Checking \n 2: Maintaining Arc Consistency");
    int choice = scanner.nextInt();

    if (choice == 1) {
      System.out.println("Initiating Forward Checking...");
      fcSolver = new FC(csp);
      fcSolver.solve();
    } else if (choice == 2) {
      System.out.println("Initiating Maintaining Arc Consistency...");
      macSolver = new MAC(csp);
      macSolver.solve();
    } else {
      System.out.println("Invalid response");
    }

    scanner.close();
  }
}