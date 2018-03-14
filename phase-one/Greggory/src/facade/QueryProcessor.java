package facade;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueryProcessor implements Runnable, Iterator<Object> {

    private Statement statement;
    private PreparedStatement preparedStatement;
	private String query;
	private List<AbstractMap.SimpleEntry<String, Class<?>>> columns;
	private ResultSet rs;
	private boolean hasNext;
	
	private QueryProcessor(List<AbstractMap.SimpleEntry<String, Class<?>>> columns)
	{
	    this.columns = columns;
	    hasNext = false;
	}
	
	public QueryProcessor(Statement statement, 
			String query, 
			List<AbstractMap.SimpleEntry<String, Class<?>>> columns)
	{
	    this(columns);
	    this.query = query;
		this.statement = statement;
	}
	
    public QueryProcessor(PreparedStatement preparedStatement, 
            List<AbstractMap.SimpleEntry<String, Class<?>>> columns)
    {
        this(columns);
        this.preparedStatement = preparedStatement;
    }
	
	@Override
	public void run()
	{
		try
		{
	        if (statement != null)
	        {
	            rs = statement.executeQuery(query);
	        }
	        else if (preparedStatement != null)
	        {
	            rs = preparedStatement.executeQuery();
	        }
	        hasNext = rs.next();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private ArrayList<Object> readRow(ResultSet rs) throws SQLException
	{
		ArrayList<Object> row = new ArrayList<Object>();
		for (AbstractMap.SimpleEntry<String, Class<?>> columnMapping : columns)
		{
			if (columnMapping.getValue() == Integer.class)
			{
				row.add(rs.getInt(columnMapping.getKey()));
			}
			else if (columnMapping.getValue() == Double.class)
			{
				row.add(rs.getDouble(columnMapping.getKey()));
			}
			else if (columnMapping.getValue() == String.class)
			{
				row.add(rs.getString(columnMapping.getKey()));
			}
			else if (columnMapping.getValue() == Timestamp.class)
			{
				row.add(rs.getTimestamp(columnMapping.getKey()));
			}
		}
		
		return row;
	}

	@Override
	public boolean hasNext() {
		if (rs == null)
		{
			hasNext = false;
		}
		
		return hasNext;
	}

	@Override
	public Object next() 
	{
		Object rv = null; 
		try 
		{
			rv = readRow(rs);
			hasNext = rs.next();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return rv;
	}

}
