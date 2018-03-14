package cli;

import java.sql.SQLException;
import factory.VectorFactory;

public class print_actor_vector extends CLI
{
    public static void main(String[] args)
    {
        if (!validateArguments(args, "an actorid and a model"))
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
            System.out.println("Please enter a valid actorid!");
            e.printStackTrace();
            System.exit(1);
        }
        
        try
        {
            VectorFactory vf = new VectorFactory();
            
            processTagVector(vf.buildTask1Vector(actorid, args[1]));
        }
        catch (SQLException | InterruptedException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
