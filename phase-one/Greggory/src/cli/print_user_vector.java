package cli;

import java.sql.SQLException;
import factory.VectorFactory;

public class print_user_vector extends CLI
{
    public static void main(String[] args)
    {
        if (!validateArguments(args, "an userid and a model"))
        {
            System.exit(1);
        }
        
        Integer actorid = null;
        
        try
        {
            actorid = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Please enter a valid userid!");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            VectorFactory vf = new VectorFactory();
            processTagVector(vf.buildTask3Vector(actorid, args[1]));
        }
        catch (SQLException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
