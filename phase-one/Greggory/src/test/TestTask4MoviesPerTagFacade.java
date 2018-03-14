package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task4MoviesPerTagFacade;

public class TestTask4MoviesPerTagFacade extends TestBasic
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
    public void testMoviesPerTagFacade()
    {
        Task4MoviesPerTagFacade af = null;
        try
        {
            af = new Task4MoviesPerTagFacade(connection, fromArray(new String[] { "Thriller", "Romance" }));
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
            assertEquals(fromArray(new Object[] { 13, 1 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] {  19, 12 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 22, 2 }), af.next());
            
            for (int i = 3; i < 342; ++i)
            {
                assertTrue(af.hasNext());
                af.next();
            }
            
            assertFalse(af.hasNext());
            
            try
            {
                assertEquals((Integer) 196, af.getTotal());
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
