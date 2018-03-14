package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task1TagGenomeFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task1TagGenomeFacade(Connection connection) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagId", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tag", String.class));

        qp1 = new QueryProcessor(prepareQuery(), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery() throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \"tagId\", tag ") // total terms
          .append("FROM genome_tags;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        return ps;
    }

    @Override
    public boolean hasNext()
    {
        return qp1.hasNext();
    }

    @Override
    public Object next()
    {
        return qp1.next();
    }
}
