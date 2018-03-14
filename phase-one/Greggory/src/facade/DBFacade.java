package facade;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class DBFacade extends QueryHandler
{
    protected ArrayList<Thread> threads = new ArrayList<Thread>();
    
    public void executeDBFunction(QueryProcessor qp)
    {
        Thread thread = new Thread(qp);
        
        threads.add(thread);
        
        thread.start();
    }
    
    public void join() throws InterruptedException
    {
        InterruptedException interrupted = null;

        Thread thread = null;
        try 
        {
            for (Thread t : threads)
            {
                thread = t;
                thread.join();
            }
        } 
        catch (InterruptedException e) 
        {
            thread.interrupt();
            e.printStackTrace();
            interrupted = e;
        }
        
        if (interrupted != null)
        {
            throw interrupted;
        }
    }
    
    protected String sn(String s, int n)
    {
        StringBuilder sb = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; ++i)
        {
            sb.append(s);
        }
        
        return sb.toString();
    }
    
    @Override
    public Iterator<Object> iterator()
    {
        return this;
    }
}
