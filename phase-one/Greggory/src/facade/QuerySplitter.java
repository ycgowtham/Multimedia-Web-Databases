package facade;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

public class QuerySplitter<T> extends QueryHandler
{
    private List<T> queryParameters;
    private int bucketSize, currentIndex, endIndex;
    private Class<?> QueryProcessor;
    private QueryHandler queryProcessor;
    private QueryHandler nextQueryProcessor;
    private boolean nextCalled;
    private Connection connection;
    
    public QuerySplitter(Class<?> QueryProcessor, Connection connection, List<T> queryParameters, int bucketSize)
    {
        this.queryParameters = queryParameters;
        this.bucketSize = bucketSize;
        this.QueryProcessor = QueryProcessor;
        this.currentIndex = 0;
        this.endIndex = Math.min(bucketSize, queryParameters.size());
        this.nextCalled = false;
        this.connection = connection;
        
        setNextQueryProcessor();
        
        queryProcessor = nextQueryProcessor;
        nextQueryProcessor = null;
    }
    
    private void updateQueryProcessor()
    {
        queryProcessor = nextQueryProcessor;
        nextQueryProcessor = null;
        nextCalled = false;
        try
        {
            queryProcessor.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    private void setNextQueryProcessor()
    {
        if (currentIndex >= endIndex - 1)
        {
            return;
        }
        
        Constructor<?> ctor = null;
        try
        {
            ctor = QueryProcessor.getConstructor(Connection.class, List.class);
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            e.printStackTrace();
            return;
        }
        
        List<T> nextSet = queryParameters.subList(currentIndex, endIndex);
        
        currentIndex += bucketSize;
        endIndex += bucketSize;
        
        endIndex = Math.min(endIndex, queryParameters.size());
        
        try
        {
            nextQueryProcessor = (QueryHandler) ctor.newInstance(connection, nextSet);
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<Object> iterator()
    {
        return this;
    }

    @Override
    public boolean hasNext()
    {
        if (!queryProcessor.hasNext() && nextQueryProcessor != null)
        {
            updateQueryProcessor();
            nextCalled = false;
        }
        
        return queryProcessor.hasNext();
    }

    @Override
    public Object next()
    {
        if (!nextCalled)
        {
            setNextQueryProcessor();
            nextCalled = true;
        }
        return queryProcessor.next();
    }

    @Override
    public void join() throws InterruptedException
    {
        queryProcessor.join();
    }
}
