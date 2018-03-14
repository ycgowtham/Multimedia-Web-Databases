package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task1ActorsWithTagFacade;

public class TestTask1ActorsWithTagFacade extends TestBasic
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
    public void testActorsWithTag()
    {
        Task1ActorsWithTagFacade af = null;
        try
        {
            af = new Task1ActorsWithTagFacade(connection, fromArray(new Integer[] { 8, 13, 45 }));
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
            assertEquals(fromArray(new Object[] { 8, 26 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 13, 34 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 45, 145 }), af.next());
            
            assertFalse(af.hasNext());
        }
        else
        {
            fail();
        }

    }

}
