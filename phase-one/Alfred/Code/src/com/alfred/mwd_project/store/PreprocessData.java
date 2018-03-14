package com.alfred.mwd_project.store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import com.alfred.mwd_project.connector.ConnectorClass;

public class PreprocessData {

	public void setMovieTagsList() {
		Connection conn = ConnectorClass.connectToDB();
		try {
			String query = "select movieid, tagid from mltags";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			while (rs.next()) {
				int movieID = rs.getInt(1);
				int tagID = rs.getInt(2);
				if(!DataHandler.MovieTags.containsKey(movieID)) {
					DataHandler.MovieTags.put(movieID, new HashSet<Integer>());
				}
				DataHandler.MovieTags.get(movieID).add(tagID);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
	}

	public void setUserMoviesList() {
		Connection conn = ConnectorClass.connectToDB();
		try {
			String query = "SELECT userid, movieid FROM mltags UNION SELECT userid, movieid FROM mlratings;";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			HashSet<Integer> temp = null;
			while (rs.next()) {
				int userID = rs.getInt(1);
				int movieID = rs.getInt(2);
				if(!DataHandler.UserMovies.containsKey(userID)) {
					temp = new HashSet<Integer>();
				}
				else
					temp = DataHandler.UserMovies.get(userID);
				temp.add(movieID);
				DataHandler.UserMovies.put(userID, temp);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
	}
}
