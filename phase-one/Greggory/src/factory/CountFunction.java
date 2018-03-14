package factory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public interface CountFunction
{
    public HashMap<Integer, Integer> getCountForTags(ArrayList<Integer> tagids) throws SQLException, InterruptedException;
}
