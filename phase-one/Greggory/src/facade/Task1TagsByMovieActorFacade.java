package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task1TagsByMovieActorFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task1TagsByMovieActorFacade(Connection connection, Integer actorid) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("movieid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("movie_weight", Double.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("time_weight", Double.class));

        qp1 = new QueryProcessor(prepareQuery(actorid), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(Integer actorid) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ma.movieid, ")
          .append(       "(1.0 / actor_movie_rank) AS movie_weight, ") // movie weight
          .append(       "tagid, ")
          .append(       "SUM((1 + extract(epoch from timestamp) - 1136748928) / (1 + 94346601)) AS time_weight ") // time weight = (t - min_t)/(max_t + 1 - min_t) 
          .append("FROM  movie_actor AS ma ")
          .append("LEFT OUTER JOIN mltags AS mt ")
          .append("ON ma.movieid = mt.movieid ")
          .append("WHERE ma.actorid = ? ")
          .append("GROUP BY ma.movieid, movie_weight, tagid ")
          .append("ORDER BY tagid, time_weight;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        ps.setInt(1, actorid);
        
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
