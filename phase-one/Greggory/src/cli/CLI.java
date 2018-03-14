package cli;

import java.sql.SQLException;
import java.util.ArrayList;

import factory.TaskVector;

public abstract class CLI
{
    protected static boolean isDebug = false;
    protected static long start = System.currentTimeMillis();
    
    protected static boolean validateArguments(String[] args, String error)
    {
        isDebug = debugEnabled(args);
        
        if (args.length < 2)
        {
            System.out.println("Please enter " + error);
            return false;
        }
        
        if (!(args[1].toLowerCase().equals("tf") || 
            args[1].toLowerCase().equals("tfidf")))
        {
            System.out.println("Please enter a model: tf, tfidf");
            return false;
        }
        
        return true;
    }
    
    protected static boolean validateDifferentiateArguments(String[] args)
    {
        isDebug = debugEnabled(args);
        
        if (args.length < 3)
        {
            System.out.print("Please enter two genres and a model");
        }
        
        String model = getModelType(args);
        
        if (!(model.equals("tfidfdiff") || 
                model.equals("pdiff1") ||
                model.equals("pdiff2")))
        {
            System.out.println("Please enter a valid model: TF-IDF-DIFF, P-DIFF1, P-DIFF2");
            return false;
        }
        
        return true;
    }
    
    protected static String getModelType(String[] args)
    {
        return args[2].toLowerCase().replace("-", "").replace("_", "");
    }
    
    protected static boolean debugEnabled(String[] args)
    {
        for (String arg : args)
        {
            if (arg.toLowerCase().equals("debug"))
            {
                return true;
            }
        }
        return false;
    }
    
    protected static void processTagVector(ArrayList<TaskVector> tagVector) throws SQLException, InterruptedException
    {
        if (tagVector != null)
        {
            for (TaskVector v : tagVector)
            {
                if (v.toString().toLowerCase().equals("1-norm"))
                {
                    System.out.println("Printing differentiations...");
                }

                System.out.println("<" + v.toString() + ", " + v.weight.toString() + ">");
                
                if (v.toString().toLowerCase().equals("angle"))
                {
                    System.out.println("\nPrinting tag vectors...");
                }
            }
            
            if(isDebug)
            {
                System.out.println("Total tags: " + tagVector.size());
            }
        }
        
        if (isDebug)
        {
            System.out.println(((Long) (System.currentTimeMillis() - start)).toString() + " ms");
        }
    }
}
