package cli;

import java.sql.SQLException;
import factory.VectorFactory;

public class print_genre_vector extends CLI
{
    public static void main(String[] args)
    {
        if (!validateArguments(args, "an genre and a model"))
        {
            System.exit(1);
        }
        
        try
        {
            VectorFactory vf = new VectorFactory();
            
            processTagVector(vf.buildTask2Vector(args[0], args[1]));
        }
        catch (SQLException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
