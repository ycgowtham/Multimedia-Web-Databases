package com.alfred.mwd_project.store;

import java.sql.Connection;
import java.sql.SQLException;

import com.alfred.mwd_project.connector.ConnectorClass;

public class DatabaseCreation {

	public void createTables() {
		Connection conn = ConnectorClass.connectToDB();
		String create_genome_tags = "CREATE TABLE IF NOT EXISTS genome_tags (tagId INTEGER, tag VARCHAR(80))";
		String create_imdb_actor_info = "CREATE TABLE IF NOT EXISTS imdb_actor_info (actorid INTEGER, name VARCHAR(60), gender VARCHAR(1))";
		String create_mlmovies = "CREATE TABLE IF NOT EXISTS mlmovies (movieid INTEGER, moviename VARCHAR(80), genres VARCHAR(60))";
		String create_mlratings = "CREATE TABLE IF NOT EXISTS mlratings (movieid INTEGER, userid INTEGER, imdbid INTEGER, rating INTEGER, timestamp TIMESTAMP,  PRIMARY KEY (movieid, userid, imdbid))";
		String create_mltags = "CREATE TABLE IF NOT EXISTS mltags (userid INTEGER, movieid INTEGER,  tagid INTEGER, timestamp TIMESTAMP)";
		String create_mlusers = "CREATE TABLE IF NOT EXISTS mlusers (userid INTEGER)";
		String create_movie_actor = "CREATE TABLE IF NOT EXISTS movie_actor (movieid INTEGER, actorid INTEGER, actor_movie_rank INTEGER, PRIMARY KEY (movieid, actorid))";
		try {
			conn.prepareStatement(create_genome_tags).execute();
			conn.prepareStatement(create_imdb_actor_info).execute();
			conn.prepareStatement(create_mlmovies).execute();
			conn.prepareStatement(create_mlratings).execute();
			conn.prepareStatement(create_mltags).execute();
			conn.prepareStatement(create_mlusers).execute();
			conn.prepareStatement(create_movie_actor).execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
	}
	
	public void addDataForTables() {
		Connection conn = ConnectorClass.connectToDB();
		String[] csvFiles = {"genome-tags", "imdb-actor-info", "mlmovies", "mlratings", "mltags", "mlusers", "movie-actor"};
		String[] tables = {"genome_tags", "imdb_actor_info", "mlmovies", "mlratings", "mltags", "mlusers", "movie_actor"};
		try {
			for(int i=0; i<7; i++) {
				String loadCommand = "LOAD DATA LOCAL INFILE 'phase1_testdata/" + csvFiles[i] + ".csv' "
						+ "INTO TABLE " + tables[i] + " "
						+ "FIELDS TERMINATED BY ',' "
						+ "ENCLOSED BY '\"' "
						+ "LINES TERMINATED BY '\n' "
						+ "IGNORE 1 lines;";
				conn.prepareStatement(loadCommand).execute();
				System.out.println("Loaded table " + tables[i]);
			}
		
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
		
	}

}
