package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task2GenreTagsFacade;

public class TestTask2GenreTagsFacade extends TestBasic
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
    public void testGenreTagsFacade()
    {
        Task2GenreTagsFacade af = null;
        try
        {
            af = new Task2GenreTagsFacade(connection, fromArray(new Integer[] { 13, 19, 24 }));
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
            assertEquals(fromArray(new Integer[] { 13, 4 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Integer[] {  19, 10 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Integer[] { 24, 7 }), af.next());
            
            assertFalse(af.hasNext());
        }
        else
        {
            fail();
        }

    }

}
