package facade;

import java.util.Iterator;

public abstract class QueryHandler implements Iterable<Object>, Iterator<Object>
{
    public abstract void join() throws InterruptedException;
}
