package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task3UsersPerTagFacade;

public class TestTask3UsersPerTagFacade extends TestBasic
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
    public void testUsersPerTagFacade()
    {
        Task3UsersPerTagFacade af = null;
        try
        {
            af = new Task3UsersPerTagFacade(connection, fromArray(new Integer[] { 19, 13, 22 }));
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
            assertEquals(fromArray(new Object[] { 13, 259 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] {  19, 21353 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 22, 6878 }), af.next());
            
            assertFalse(af.hasNext());
        }
        else
        {
            fail();
        }

    }

}
