package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task4MoviesPerTagFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1, qp2;
    private Integer total;
    
    public Task4MoviesPerTagFacade(Connection connection, List<String> genres) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("movie_count", Integer.class));
        
        qp1 = new QueryProcessor(prepareQuery(prepareSubquery(genres)), query1Columns);
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query2Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query2Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("total_count", Integer.class));
        
        qp2 = new QueryProcessor(prepareQuery2(prepareSubquery(genres)), query2Columns);
        
        executeDBFunction(qp1);
        executeDBFunction(qp2);
    }
    
    public PreparedStatement prepareQuery(String subquery) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, ")
          .append(       "COUNT(movieid) AS movie_count ")
          .append("FROM ").append(subquery).append(" ")
          .append("GROUP BY tagid ")
          .append("ORDER BY tagid;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        return ps;
    }
    
    public PreparedStatement prepareQuery2(String subquery) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(DISTINCT movieid) AS total_count ")
          .append("FROM ").append(subquery).append(" ")
          .append(";");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        return ps;
    }
    
    public String prepareSubquery(List<String> genres) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("( ")
          .append("SELECT DISTINCT movies.movieid, mltags.tagid ")
          .append("FROM ")
          .append(     "( ")
          .append(       "SELECT DISTINCT movieid, ")
          .append(                       "UNNEST(STRING_TO_ARRAY(genres, '|')) genres ")
          .append(       "FROM mlmovies ")
          .append(     ") AS movies ")
          .append("INNER JOIN mltags ")
          .append("ON movies.movieid = mltags.movieid ")
          .append("WHERE genres IN ( ").append(sn("?, ", genres.size() - 1)).append("?) ")
          .append(") AS movietags ");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < genres.size(); ++i)
        {
            ps.setString(1 + i, genres.get(i));
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
    
    @SuppressWarnings("unchecked")
    public Integer getTotal() throws Exception
    {
        if (this.total == null)
        {
            if (!qp2.hasNext())
            {
                throw new Exception("Query not finished!");
            }
            
            this.total = (Integer) ((ArrayList<Object>) qp2.next()).get(0);
        }
        
        return this.total;
    }
}
