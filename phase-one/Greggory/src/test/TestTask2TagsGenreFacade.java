package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task2TagsByGenreFacade;

public class TestTask2TagsGenreFacade extends TestBasic
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
    public void testTagsByGenreFacade()
    {
        Task2TagsByGenreFacade af = null;
        try
        {
            af = new Task2TagsByGenreFacade(connection, "Thriller");
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
            assertEquals(fromArray(new Object[] { 13, 0.7630103943754116d }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] {  19, 6.864298514958705 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 24, 0.9613823187823978 }), af.next());
            
            for(int i = 0; i < 205; ++i)
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
