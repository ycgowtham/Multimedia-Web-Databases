package cli;

import java.sql.SQLException;
import java.util.ArrayList;

import factory.TaskVector;
import factory.VectorFactory;

public class differentiate_genre extends CLI
{
    public static void main(String[] args) throws Exception
    {
        if (!validateDifferentiateArguments(args))
        {
            System.exit(1);
        }
        
        Object differentiateArgs = null;
        
        if (args.length > 3)
        {
            for (int i = 3; i < args.length; ++i)
            {
                try
                {
                    differentiateArgs = Integer.parseInt(args[i]);
                }
                catch (NumberFormatException e)
                {
                    if (args[i].toLowerCase().equals("infinity"))
                    {
                        differentiateArgs = Integer.MAX_VALUE;
                    }
                    else if (args[i].toLowerCase().equals("dot"))
                    {
                        differentiateArgs = "dot";
                    }
                    else if (args[i].toLowerCase().equals("cos") || args[i].toLowerCase().equals("cosine"))
                    {
                        differentiateArgs = "cos";
                    }
                    else if (args[i].toLowerCase().equals("ang") || args[i].toLowerCase().equals("angle"))
                    {
                        differentiateArgs = "angle";
                    }
                    else
                    {
                        differentiateArgs = 2;
                    }
                }
            }
        }
        
        try
        {
            VectorFactory vf = new VectorFactory(isDebug);
            
            ArrayList<TaskVector> diffVector = null;
            
            if (getModelType(args).equals("tfidfdiff"))
            {
                diffVector = vf.buildTask4TF_IDF_DIFFVector(args[0], args[1], differentiateArgs);
            }
            else if (getModelType(args).contains("pdiff"))
            {
                Boolean pdiff2 = false;
                if (getModelType(args).equals("pdiff2"))
                {
                    pdiff2 = true;
                }
                diffVector = vf.buildTask4PDIFFVector(args[0], args[1], differentiateArgs, pdiff2);
            }
            
            processTagVector(diffVector);
        }
        catch (SQLException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
