package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task3UsersPerTagFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task3UsersPerTagFacade(Connection connection, List<Integer> tagids) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("user_count", Integer.class));
        
        qp1 = new QueryProcessor(prepareQuery(tagids, prepareSubquery(tagids)), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(List<Integer> tagids, String subquery) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, ")
          .append(       "COUNT(userid) AS user_count ")
          .append("FROM ").append(subquery).append(" ")
          .append("GROUP BY tagid ")
          .append("ORDER BY tagid;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < tagids.size(); ++i)
        {
            ps.setInt(1 + i, tagids.get(i));
        }
        
        return ps;
    }
    
    public String prepareSubquery(List<Integer> tagids) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("( ")
          .append("SELECT DISTINCT reduced_ratings.userid, mltags.tagid ")
          .append("FROM ")
          .append(     "( ")
          .append(       "SELECT DISTINCT reduced_ratings.userid, ")
          .append(                       "reduced_ratings.movieid ")
          .append(       "FROM reduced_ratings ")
          .append(       "UNION ")
          .append(       "SELECT DISTINCT mltags.userid, ")
          .append(                       "mltags.movieid ")
          .append(       "FROM mltags ")
          .append(       "WHERE mltags.tagid IN ( ").append(sn("?, ", tagids.size() - 1)).append(" ?) ")
          .append(     ") AS reduced_ratings ")
          .append("INNER JOIN mltags ")
          .append("ON reduced_ratings.movieid = mltags.movieid ")
          .append("WHERE mltags.tagid IN ( ").append(sn("?, ", tagids.size() - 1)).append(" ?) ")
          .append(") AS mltags ");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < tagids.size(); ++i)
        {
            ps.setInt(1 + i + tagids.size(), tagids.get(i));
        }
        
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
