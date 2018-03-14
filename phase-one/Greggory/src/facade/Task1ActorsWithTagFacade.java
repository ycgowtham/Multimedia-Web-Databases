package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task1ActorsWithTagFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task1ActorsWithTagFacade(Connection connection, ArrayList<Integer> tagids) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("actors_count", Integer.class));

        qp1 = new QueryProcessor(prepareQuery(tagids), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(ArrayList<Integer> tagids) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, COUNT(actorid) AS actors_count ")
          .append("FROM ( ")
          .append(      "SELECT DISTINCT movieid, tagid ")
          .append(      "FROM mltags ")
          .append(      "WHERE tagid IN (").append(sn("?, ", tagids.size() - 1)).append("?) ")
          .append(     ") AS mltags ")
          .append("INNER JOIN ( ")
          .append(            "SELECT DISTINCT actorid, movieid ")
          .append(            "FROM movie_actor ")
          .append(           ") AS movie_actor " )
          .append("ON mltags.movieid = movie_actor.movieid ")
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
