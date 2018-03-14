package test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import facade.Task3TagsByUserFacade;

public class TestTask3TagsByUserFacade extends TestBasic
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
    public void testTagsByGenreFacadeInMLtags()
    {
        Task3TagsByUserFacade af = null;
        try
        {
            af = new Task3TagsByUserFacade(connection, 146);
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
            assertEquals(fromArray(new Object[] { 13, 0.7630103943754116 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] {  19, 2.6952637255552663 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 22, 0.43487553478608587 }), af.next());
            
            for(int i = 0; i < 310; ++i)
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
    
    @Test
    public void testTagsByGenreFacadeInMLratings()
    {
        Task3TagsByUserFacade af = null;
        try
        {
            af = new Task3TagsByUserFacade(connection, 28365);
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
            assertEquals(fromArray(new Object[] { 19, 2.1790752463983813d }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] {  29, 0.9277299144276546 }), af.next());

            assertTrue(af.hasNext());
            assertEquals(fromArray(new Object[] { 43, 0.466900927709087 }), af.next());
            
            for(int i = 0; i < 158; ++i)
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
