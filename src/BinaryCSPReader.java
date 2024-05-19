import java.io.* ;
import java.util.* ;

public final class BinaryCSPReader {
  private FileReader inFR ;
  private StreamTokenizer in ;

  public BinaryCSP readBinaryCSP(String fn) {
    try {
      inFR = new FileReader(fn) ;
      in = new StreamTokenizer(inFR) ;
      in.ordinaryChar('(') ;
      in.ordinaryChar(')') ;
      in.nextToken() ;                                     
      int n = (int)in.nval ;
      int[][] domainBounds = new int[n][2] ;
      for (int i = 0; i < n; i++) {
	      in.nextToken() ;                                 
	      domainBounds[i][0] = (int)in.nval ;
		    in.nextToken() ;                                 
		    in.nextToken() ;
	      domainBounds[i][1] = (int)in.nval ;
      }
      ArrayList<BinaryConstraint> constraints = readBinaryConstraints() ;
      BinaryCSP csp = new BinaryCSP(domainBounds, constraints) ;
      inFR.close() ;
      return csp ;
    }
    catch (FileNotFoundException e) {System.out.println(e);}
    catch (IOException e) {System.out.println(e);}
    return null ;
  }

  private ArrayList<BinaryConstraint> readBinaryConstraints() {
    ArrayList<BinaryConstraint> constraints = new ArrayList<BinaryConstraint>() ;
	
    try {
      in.nextToken() ;                                 
      while(in.ttype != in.TT_EOF) {
	      // scope
	      in.nextToken() ;                                    
		    in.nextToken() ;                                   
	      int var1 = (int)in.nval ;
		    in.nextToken() ;                                    
		    in.nextToken() ;                                     
        int var2 = (int)in.nval ;
		    in.nextToken() ;                                     

		    ArrayList<BinaryTuple> tuples = new ArrayList<BinaryTuple>() ;
        in.nextToken() ;             
        while (!"c".equals(in.sval) && (in.ttype != in.TT_EOF)) {
          int val1 = (int)in.nval ;
	        in.nextToken() ;                                  
	        in.nextToken() ;                             
		      int val2 = (int)in.nval ;
		      tuples.add(new BinaryTuple(val1, val2)) ;
		      in.nextToken() ;      
		    }
        BinaryConstraint c = new BinaryConstraint(var1, var2, tuples) ;
        constraints.add(c) ;
      }
	  
      return constraints ;
    }
    catch (IOException e) {System.out.println(e);}
    return null ;  
  }
}