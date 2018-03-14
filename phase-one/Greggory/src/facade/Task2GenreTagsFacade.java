package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task2GenreTagsFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task2GenreTagsFacade(Connection connection, ArrayList<Integer> tagids) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("genre_count", Integer.class));

        qp1 = new QueryProcessor(prepareQuery(tagids), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(ArrayList<Integer> tagids) throws SQLException
    {    
        //select tagid, genres from mlmovies INNER JOIN mltags ON mlmovies.movieid = mltags.movieid WHERE tagid in (13, 19, 24) ORDER BY tagid;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, ")
          .append(       "COUNT(genres) as genre_count ") 
          .append("FROM ( ")
          .append(        "SELECT DISTINCT tagid, genres ")
          .append(        "FROM ( ")
          .append(                "SELECT DISTINCT movieid, UNNEST(STRING_TO_ARRAY(genres, '|')) AS genres ")
          .append(                "FROM mlmovies ")
          .append(             ") AS mlmovies ")
          .append(        "INNER JOIN mltags ")
          .append(        "ON mlmovies.movieid = mltags.movieid ")
          .append(        "WHERE tagid IN (").append(sn("?, ", tagids.size() - 1)).append("?) ")
          .append(     ") AS genre_tags ")
          .append("GROUP BY tagid ")
          .append("ORDER BY tagid;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        for (int i = 0; i < tagids.size(); ++i)
        {
            ps.setInt(i + 1, tagids.get(i));
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
