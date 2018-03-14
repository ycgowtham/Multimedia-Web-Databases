package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task4TotalMoviesForGenreFacade;

public class TestTask4TotalMoviesForGenreFacade extends TestBasic
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
    public void testTask4TotalMoviesForGenreFacade()
    {
        Task4TotalMoviesForGenreFacade af = null;
        try
        {
            af = new Task4TotalMoviesForGenreFacade(connection, fromArray(new String[] { "Thriller" }));
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
            try
            {
                assertEquals((Integer) 101, af.getTotal());
            }
            catch (Exception e)
            {
                fail("Unexpected exception thrown!");
            }
        }
        else
        {
            fail();
        }

    }

}
