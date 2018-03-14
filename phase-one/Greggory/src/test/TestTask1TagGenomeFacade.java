package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task1TagGenomeFacade;

public class TestTask1TagGenomeFacade extends TestBasic
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
    public void testTagGenomeFacade()
    {
        Task1TagGenomeFacade af = null;
        try
        {
            af = new Task1TagGenomeFacade(connection);
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
            assertEquals(fromArray(new Object[] { 8, "1970s" }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 13, "80s" }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 17, "abortion" }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 19, "action" }), af.next());
            
            for (int i = 0; i < 553; ++i)
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
