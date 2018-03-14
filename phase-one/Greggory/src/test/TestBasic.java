package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class TestBasic
{
    protected Connection connection;
    
    protected <T> ArrayList<T> fromArray(T[] arr)
    {
        ArrayList<T> intList = new ArrayList<T>();
        
        for(int i = 0; i < arr.length; ++i)
        {
            intList.add(arr[i]);
        }
        
        return intList;
    }
    
    protected void createConnection() throws SQLException
    {
        Properties props = new Properties();
        props.put("user", "greggoryscherer");

        connection = DriverManager.getConnection("jdbc:postgresql:greggoryscherer", props);
    }
}
