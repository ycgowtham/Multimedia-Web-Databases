package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task1TotalTagsFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task1TotalTagsFacade(Connection connection, ArrayList<Integer> movieids) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("total_tags", Integer.class));

        qp1 = new QueryProcessor(prepareQuery(movieids), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(ArrayList<Integer> movieids) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(DISTINCT tagid) AS total_tags ") // total terms
          .append("FROM mltags ")
          .append("WHERE movieid IN ( ").append(sn("?, ", movieids.size() - 1)).append("?);");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < movieids.size(); ++i)
        {
            ps.setInt(i + 1, movieids.get(i));
        }
        
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
