package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task2TagsByGenreFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task2TagsByGenreFacade(Connection connection, String genre) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("time_weight", Double.class));

        qp1 = new QueryProcessor(prepareQuery(genre), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(String genre) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, ")
          .append(       "SUM((1 + extract(epoch from timestamp) - 1136748928) / (1 + 94346601)) AS time_weight ") // time weight = (t - min_t)/(max_t + 1 - min_t) 
          .append("FROM mlmovies ") 
          .append("LEFT OUTER JOIN mltags ")
          .append("ON mlmovies.movieid = mltags.movieid ")
          .append("WHERE genres LIKE '%' || ? || '%' ")
          .append("GROUP BY tagid ")
          .append("ORDER BY tagid;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        ps.setString(1, genre);
        
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
