package com.alfred.mwd_project.tests;

import org.testng.annotations.Test;

import com.alfred.mwd_project.store.DatabaseCreation;

public class TestDatabase {

	@Test
	public void testTableCreation(){
		DatabaseCreation dbCreation = new DatabaseCreation();
		dbCreation.createTables();
	}
	
	@Test
	public void testLoadTable(){
		DatabaseCreation dbCreation = new DatabaseCreation();
		dbCreation.addDataForTables();
	}
}
