package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task1TotalTagsFacade;

public class TestTask1TotalTagsFacade extends TestBasic
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
    public void testTotalTagsFacade()
    {
        Task1TotalTagsFacade af = null;
        try
        {
            af = new Task1TotalTagsFacade(connection, fromArray(new Integer[] { 4176, 4921, 5169 }));
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
            assertEquals(fromArray(new Object[] { 4 }), af.next());

            assertFalse(af.hasNext());
        }
        else
        {
            fail();
        }

    }

}
