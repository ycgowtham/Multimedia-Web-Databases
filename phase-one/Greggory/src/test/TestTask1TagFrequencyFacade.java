package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task1TagFrequencyFacade;

public class TestTask1TagFrequencyFacade extends TestBasic
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
    public void testTagFrequencyFacade()
    {
        Task1TagFrequencyFacade af = null;
        try
        {
            af = new Task1TagFrequencyFacade(connection, 785, fromArray(new Integer[] { 4793, 6270 }));
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
            assertEquals(fromArray(new Object[] { 785, 39 }), af.next());

            assertFalse(af.hasNext());
        }
        else
        {
            fail();
        }

    }

}
