package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task3TagsByUserFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task3TagsByUserFacade(Connection connection, Integer userid) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("time_weight", Double.class));
        
        qp1 = new QueryProcessor(prepareQuery(userid, prepareSubquery(userid)), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(Integer userid, String subquery) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, ")
          .append(       "SUM((1 + extract(epoch from timestamp) - 1136748928) / (1 + 94346601)) AS time_weight ") // time weight = (t - min_t)/(max_t + 1 - min_t) 
          .append("FROM ").append(subquery).append(" ")
          .append("GROUP BY tagid ")
          .append("ORDER BY tagid;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        ps.setInt(1, userid);
        
        return ps;
    }
    
    public String prepareSubquery(Integer userid) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("( ")
          .append("SELECT DISTINCT mltags.* ")
          .append("FROM ")
          .append(     "( ")
          .append(       "SELECT DISTINCT reduced_ratings.userid, ")
          .append(                       "reduced_ratings.movieid ")
          .append(       "FROM reduced_ratings ")
          .append(       "WHERE reduced_ratings.userid = ? ")
          .append(       "UNION ")
          .append(       "SELECT DISTINCT mltags.userid, ")
          .append(                       "mltags.movieid ")
          .append(       "FROM mltags ")
          .append(       "WHERE mltags.userid = ? ")
          .append(     ") AS reduced_ratings ")
          .append("INNER JOIN mltags ")
          .append("ON reduced_ratings.movieid = mltags.movieid ")
          .append(") AS mltags ");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        ps.setInt(2, userid);
        
        return ps.toString();
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
