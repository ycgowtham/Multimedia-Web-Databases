package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task1TagsByMovieActorFacade;

public class TestTask1TagsByMovieActorFacade extends TestBasic
{

    @Before
    public void setUp() throws Exception
    {
        createConnection();
    }

    @After
    public void tearDown() throws Exception
    {
        connection.close();
    }

    @Test
    public void testTagsByMovieActorFacade()
    {
        Task1TagsByMovieActorFacade af = null;
        try
        {
            af = new Task1TagsByMovieActorFacade(connection, 1582699);
            af.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            fail();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail();
        }

        if (af != null)
        {
            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 8772, 0.04347826086956521739, 159, 3.234768645933851 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 8772, 0.04347826086956521739, 285, 0.4391114478081574 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 8772, 0.04347826086956521739, 323, 0.19058029244126884 }), af.next());
            
            for(int i = 0; i < 10; ++i)
            {
                assertTrue(af.hasNext());
                af.next();
            }
            
            assertFalse(af.hasNext());
        }
        else
        {
            fail();
        }

    }

}
