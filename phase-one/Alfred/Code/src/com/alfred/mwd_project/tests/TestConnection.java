package com.alfred.mwd_project.tests;

import static org.testng.Assert.assertNotEquals;

import java.sql.Connection;
import org.testng.annotations.Test;
import com.alfred.mwd_project.connector.ConnectorClass;

public class TestConnection {
	
	@Test(priority=1, description="Test if jdbc driver is available")
	public void testDriverConnection(){
		ConnectorClass connector = new ConnectorClass();
		Connection conn = connector.connectToDB();
		assertNotEquals(null, conn);
	}
}
