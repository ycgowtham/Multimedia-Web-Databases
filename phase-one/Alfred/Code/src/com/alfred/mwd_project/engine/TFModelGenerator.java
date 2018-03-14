package com.alfred.mwd_project.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.DateUtil;

import com.alfred.mwd_project.connector.ConnectorClass;
import com.alfred.mwd_project.store.DataHandler;

public class TFModelGenerator {

	public LinkedHashMap<String, Double> generateActorTagVector(int actorID) {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<Integer, Double> tagWeight = new HashMap<>();
		LinkedHashMap<String, Double> finalTagVector = null;
		try {
			double totalWeight = 0.0;	
			String query = "select tagid, actor_movie_rank, timestamp from mltags natural join movie_actor where actorid=" + actorID + ";";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()) {
				int tagID = rs.getInt(1);
				int movie_rank = rs.getInt(2);
				Date date = new Date(rs.getTimestamp(3).getTime());
				double timestamp = DateUtil.getExcelDate(date);
				double weight = 0.0;
				if(tagWeight.containsKey(tagID)){
					weight = tagWeight.get(tagID);
				}
				double new_weight = (1.0 / movie_rank) * (timestamp - DataHandler.minTimestamp + 1) / (DataHandler.maxTimestamp - DataHandler.minTimestamp);
				totalWeight += new_weight;
				tagWeight.put(tagID, weight+new_weight);
			}
			ModelHelper mHelper = new ModelHelper();
			finalTagVector = mHelper.sortTagVector(conn, tagWeight, totalWeight);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
		return finalTagVector;
	}
	
	public LinkedHashMap<String, Double> generateGenreTagVector(String genre){
		Connection conn = ConnectorClass.connectToDB();
		HashMap<Integer, Double> tagWeight = new HashMap<>();
		LinkedHashMap<String, Double> finalTagVector = null;
		try {
			String query = "select tagid, timestamp from mlmovies natural join mltags where genres like '%" + genre + "%'";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			double totalWeight = 0.0;
			while(rs.next()) {
				int tagID = rs.getInt(1);
				Date date = new Date(rs.getTimestamp(2).getTime());
				double timestamp = DateUtil.getExcelDate(date);
				double weight = 0.0;
				if(tagWeight.containsKey(tagID))
					weight = tagWeight.get(tagID);
				double new_weight = (timestamp - DataHandler.minTimestamp + 1) / (DataHandler.maxTimestamp - DataHandler.minTimestamp); 
				totalWeight += new_weight;
				tagWeight.put(tagID, weight + new_weight);
			}
			ModelHelper mHelper = new ModelHelper();
			finalTagVector = mHelper.sortTagVector(conn, tagWeight, totalWeight);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
		return finalTagVector;
	}
	
	public LinkedHashMap<String, Double> generateUserTagVector(int userID) {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<Integer, Double> tagWeight = new HashMap<>();
		LinkedHashMap<String, Double> finalTagVector = null;
		try {
			String query = "select tagid, timestamp from mltags where movieID IN (select movieID from mltags where userid=" + userID + " UNION "
					+ "select movieID from mlratings where userid=" + userID + ")";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			
			double totalWeight = 0.0;
			while(rs.next()) {
				int tagID = rs.getInt(1);
				Date date = new Date(rs.getTimestamp(2).getTime());
				double timestamp = DateUtil.getExcelDate(date);
				double weight = 0.0;
				if(tagWeight.containsKey(tagID))
					weight = tagWeight.get(tagID);
				double new_weight = (timestamp - DataHandler.minTimestamp + 1) / (DataHandler.maxTimestamp - DataHandler.minTimestamp); 
				totalWeight += new_weight;
				tagWeight.put(tagID, weight + new_weight);
			}
			ModelHelper mHelper = new ModelHelper();
			finalTagVector = mHelper.sortTagVector(conn, tagWeight, totalWeight);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}		
		return finalTagVector;
	}
}
