package facade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Task1TagFrequencyFacade extends DBFacade
{
    private Connection connection;
    private QueryProcessor qp1;
    
    public Task1TagFrequencyFacade(Connection connection, Integer tagid, ArrayList<Integer> movieids) throws SQLException
    {
        this.connection = connection;
        
        List<AbstractMap.SimpleEntry<String, Class<?>>> query1Columns = new ArrayList<AbstractMap.SimpleEntry<String, Class<?>>>();
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tagid", Integer.class));
        query1Columns.add(new AbstractMap.SimpleEntry<String, Class<?>>("tag_count", Integer.class));

        qp1 = new QueryProcessor(prepareQuery(tagid, movieids), query1Columns);
        
        executeDBFunction(qp1);
    }
    
    public PreparedStatement prepareQuery(Integer tagid, ArrayList<Integer> movieids) throws SQLException
    {    
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT tagid, ")
          .append(       "COUNT(tagid) AS tag_count ") // term frequency
          .append("FROM mltags ")
          .append("WHERE tagid = ? AND ")
          .append(      "movieid IN (").append(sn("?,", movieids.size() - 1)).append("?) ")
          .append("GROUP BY tagid;");
        
        PreparedStatement ps = connection.prepareStatement(sb.toString());
        
        ps.setInt(1, tagid);
        
        for (int i = 0; i < movieids.size(); ++i)
        {
            ps.setInt(i + 2, movieids.get(i));
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
