package com.alfred.mwd_project.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import com.alfred.mwd_project.connector.ConnectorClass;
import com.alfred.mwd_project.store.DataHandler;

public class IDFModelGenerator {
	
	public HashMap<String, Double> generateActorTagModel() {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<String, Double> tagActorOccurences = new HashMap<>();
		try {
			int totalActors = 0;
			String query = "select count(distinct actorid) from imdb_actor_info;";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			while(rs.next())
				totalActors = rs.getInt(1);
			System.out.println(totalActors);
			query = "select tagid, tag from genome_tags;";
			rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()) {
				int tagID = rs.getInt(1);
				String tagName = rs.getString(2);
				String query2 = "select count(distinct actorid) from mltags natural join movie_actor "
						+ "where tagid=" + tagID + ";";
				ResultSet rs2 = conn.prepareStatement(query2).executeQuery();
				int occurences = 0;
				while(rs2.next()) {
					occurences = rs2.getInt(1);
				}
				double idf_value = Math.log(totalActors * 1.0 / occurences);
				tagActorOccurences.put(tagName, idf_value);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return tagActorOccurences;
	}
	
	public HashMap<String, Double> generateGenreTagModel() {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<String, Double> tagGenreOccurences = new HashMap<>();
		try {
			ArrayList<String> allGenreList = new ArrayList<>();
			String query = "select tagid, tag from genome_tags;";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()) {
				int tagID = rs.getInt(1);
				String tagName = rs.getString(2);
				String query2 = "select genres, tagid from mltags natural join mlmovies where tagid=" + tagID + ";";
				ResultSet rs2 = conn.prepareStatement(query2).executeQuery();
				ArrayList<String> tagGenres = new ArrayList<>();
				while(rs2.next()) {
					String[] genres = rs2.getString(1).split("\\|");
					for(int i=0; i<genres.length; i++) {
						String genre = genres[i];
						if(!tagGenres.contains(genre))
							tagGenres.add(genre);
						if(!allGenreList.contains(genre))
							allGenreList.add(genre);
					}
				}
				int genreCount = tagGenres.size();
				if(genreCount > 0.0)
					tagGenreOccurences.put(tagName, genreCount*1.0);
			}
			int totalGenreCount = allGenreList.size();
			System.out.println(totalGenreCount);
			ArrayList<String> tags = new ArrayList<>(tagGenreOccurences.keySet());
			for (int i=0; i<tags.size(); i++) {
				String tag = tags.get(i);
				double val = tagGenreOccurences.get(tag);
				double idf_value = Math.log(totalGenreCount / val);
				tagGenreOccurences.put(tag, idf_value);
			}			
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return tagGenreOccurences;
	}


	public HashMap<String, Double> generateUserTagModel(LinkedHashMap<String, Double> tfTagVector) {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<String, Double> tagUserOccurences = new HashMap<>();
		try {
			int totalUsers = 0;
			totalUsers = DataHandler.UserMovies.size();
			System.out.println(totalUsers);
			for(String key : tfTagVector.keySet()) {
				String tagName = key;
				String query = "select tagid from genome_tags where tag like '" + tagName + "';";
				ResultSet rs = conn.prepareStatement(query).executeQuery();
				int tagID = 0;
				while(rs.next())
					tagID = rs.getInt(1);
				HashSet<Integer> movieSet = new HashSet<>();
				for(int movieID : DataHandler.MovieTags.keySet()) {
					if(DataHandler.MovieTags.get(movieID).contains(tagID))
						movieSet.add(movieID);
				}
				int occurences = 0;
				HashSet<Integer> userSet = new HashSet<>();
				for(int movieID : movieSet) {
					for(int userID : DataHandler.UserMovies.keySet()) {
						if(DataHandler.UserMovies.get(userID).contains(movieID))
							userSet.add(userID);
					}
				}
				occurences = userSet.size();
				if (occurences > 0) {
					double idf_value = Math.log(totalUsers * 1.0 / occurences);
					tagUserOccurences.put(tagName, idf_value);
				}
			}
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
		return tagUserOccurences;
	}
}
