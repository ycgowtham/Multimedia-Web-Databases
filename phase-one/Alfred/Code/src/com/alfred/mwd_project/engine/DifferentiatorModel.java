package com.alfred.mwd_project.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.alfred.mwd_project.connector.ConnectorClass;
import com.alfred.mwd_project.store.DataHandler;

public class DifferentiatorModel {

	public double differentiateGenreModel(String genre1, String genre2) {
		double squaredSum = 0.0;
		LinkedHashMap<String, Double> finalTagVector_g1 = null;
		LinkedHashMap<String, Double> finalTagVector_g2 = null;
		
		TFModelGenerator tfGenerator = new TFModelGenerator();
		LinkedHashMap<String, Double> TF_g1 = tfGenerator.generateGenreTagVector(genre1);
		LinkedHashMap<String, Double> TF_g2 = tfGenerator.generateGenreTagVector(genre2);
		Connection conn = ConnectorClass.connectToDB();
		try {
			String query = "select distinct movieid from mlmovies where genres like '%"+ genre1 + "%' or genres like '%" + genre2 + "%'";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			int movieCount = 0;
			ArrayList<Integer> movies = new ArrayList<>();
			while(rs.next()) {
				movies.add(rs.getInt(1));
			}
			movieCount = movies.size();
			HashMap<Integer, Integer> tagCount = new HashMap<>();
			for(int movieID : DataHandler.MovieTags.keySet()) {
				for(int tag : DataHandler.MovieTags.get(movieID)) {
					if(tagCount.containsKey(tag))
						tagCount.put(tag, tagCount.get(tag)+1);
					else
						tagCount.put(tag, 1);
				}
			}		
			
			HashMap<Integer, Double> TFIDF1Weight = new HashMap<>();
			HashMap<Integer, Double> TFIDF2Weight = new HashMap<>();
			for(int tag : tagCount.keySet()){
				query = "select tag from genome_tags where tagid=" + tag + ";";
				rs = conn.prepareStatement(query).executeQuery();
				String tagName = "";
				while(rs.next())
					tagName = rs.getString(1);
				rs.close();
				double tf_1 = 0, tf_2 = 0;
				if(TF_g1.containsKey(tagName))
					tf_1 = TF_g1.get(tagName);
				if(TF_g2.containsKey(tagName))
					tf_2 = TF_g2.get(tagName);
				double idf = Math.log(movieCount*1.0/tagCount.get(tag));
				TFIDF1Weight.put(tag, tf_1*idf);
				TFIDF2Weight.put(tag, tf_2*idf);
			}
			ModelHelper mHelper = new ModelHelper();
			finalTagVector_g1 = mHelper.sortTagVector(conn, TFIDF1Weight, 1);
			finalTagVector_g2 = mHelper.sortTagVector(conn, TFIDF2Weight, 1);
			
			Set<String> tags_g1 = finalTagVector_g1.keySet();
			Set<String> tags_g2 = finalTagVector_g2.keySet();
			Set<String> intersection = finalTagVector_g1.keySet();
			intersection.retainAll(tags_g2);
			for(String tag : tags_g1) {
				if(!intersection.contains(tag))
					squaredSum += Math.pow(finalTagVector_g1.get(tag), 2);
			}
			for(String tag : tags_g2) {
				if(!intersection.contains(tag))
					squaredSum += Math.pow(finalTagVector_g2.get(tag), 2);
			}
			for(String tag : intersection) {
				squaredSum += Math.pow(finalTagVector_g1.get(tag) - finalTagVector_g2.get(tag), 2);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return squaredSum;
	}

	public LinkedHashMap<String, Double> PDIFF_1_Generator(String genre1, String genre2) {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<Integer, Double> tagWeight = new HashMap<>();
		HashSet<Integer> genre1Movies = new HashSet<>(); 
		HashSet<Integer> genre2Movies = new HashSet<>();
		HashSet<Integer> genre1Ugenre2Movies = new HashSet<>();
		LinkedHashMap<String, Double> finalTagVector = null;
		try {
			String q = "select distinct movieid from mlmovies where genres like '%" + genre1 + "%' or genres like '%" + genre2 + "%';";
			ResultSet rs = conn.prepareStatement(q).executeQuery();
			while(rs.next()) {
				genre1Movies.add(rs.getInt(1));
			}
			
			q = "select distinct movieid from mlmovies where genres like '%" + genre2 + "%';";
			rs = conn.prepareStatement(q).executeQuery();
			while(rs.next()) {
				genre2Movies.add(rs.getInt(1));
			}
				
			int R=genre1Movies.size();
			genre1Ugenre2Movies.addAll(genre1Movies);
			genre1Ugenre2Movies.addAll(genre2Movies);
			int M = genre1Ugenre2Movies.size();
			
			String query = "select distinct tagid from mlmovies natural join mltags where genres like '%" + genre1 + "%'";
			rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()) {
				int tagID = rs.getInt(1);
				double weight = 0.0;
				if(tagWeight.containsKey(tagID))
					weight = tagWeight.get(tagID);
				int r=0, m=0;
				String query2 = "select count(distinct movieid) from mlmovies natural join mltags where genres like '%" + genre1 + "%' and" +						
						" tagid=" +  tagID +";";
				ResultSet rs2 = conn.prepareStatement(query2).executeQuery();
				while(rs2.next())
					r = rs2.getInt(1);
				rs2.close();
 						
				query2 = "select count(distinct movieid) from mlmovies natural join mltags where (genres like '%" + genre1 + "%' or genres like '%" + genre2 + "%') and" +						
						" tagid=" +  tagID +";";
				rs2 = conn.prepareStatement(query2).executeQuery();
				while(rs2.next())
					m = rs2.getInt(1);
 				rs2.close();
 				double offset = 0;
				if(r==0 || M-m-R+r==0 || m==r || R==M || R==r)
					offset = 0.5;
				double num = ((r + offset) / (R - r + offset));
				double dem = ((m - r + offset) / (M - m - R + r + offset));
				double logVal = Math.log(num/dem);
				double left = (r + offset)/(R + 2 * offset);
				double right = (m - r + offset)/(M - R + 2 * offset);
				double absVal = Math.abs(left - right);
				double new_weight = logVal * absVal;
				tagWeight.put(tagID, weight + new_weight);
			}
			ModelHelper mHelper = new ModelHelper();
			finalTagVector = mHelper.sortTagVector(conn, tagWeight, 1);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
		return finalTagVector;
	}

	public LinkedHashMap<String, Double> PDIFF_2_Generator(String genre1, String genre2) {
		Connection conn = ConnectorClass.connectToDB();
		HashMap<Integer, Double> tagWeight = new HashMap<>();
		HashSet<Integer> genre1Movies = new HashSet<>(); 
		HashSet<Integer> genre2Movies = new HashSet<>();
		HashSet<Integer> genre1Ugenre2Movies = new HashSet<>();
		LinkedHashMap<String, Double> finalTagVector = null;
		try {
			String q = "select distinct movieid from mlmovies where genres like '%" + genre1 + "%';";
			ResultSet rs = conn.prepareStatement(q).executeQuery();
			while(rs.next()) {
				genre1Movies.add(rs.getInt(1));
			}
			
			q = "select distinct movieid from mlmovies where genres like '%" + genre2 + "%';";
			rs = conn.prepareStatement(q).executeQuery();
			while(rs.next()) {
				genre2Movies.add(rs.getInt(1));
			}
				
			int R=genre2Movies.size();
			genre1Ugenre2Movies.addAll(genre1Movies);
			genre1Ugenre2Movies.addAll(genre2Movies);
			int M = genre1Ugenre2Movies.size();
			String query = "select distinct tagid from mlmovies natural join mltags where genres like '%" + genre1 + "%' or genres like '%" + genre2 + "%'";
			rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()) {
				int tagID = rs.getInt(1);
				double weight = 0.0;
				if(tagWeight.containsKey(tagID))
					weight = tagWeight.get(tagID);
				int r=0, m=0;
				String query2 = "select count(distinct movieid) from mlmovies natural join mltags where genres like '%" + genre2 + "%' and" +						
						" tagid=" +  tagID +";";
				ResultSet rs2 = conn.prepareStatement(query2).executeQuery();
				while(rs2.next())
					r = genre2Movies.size() - rs2.getInt(1);
				rs2.close();
 						
				query2 = "select count(distinct movieid) from mlmovies natural join mltags where (genres like '%" + genre1 + "%' or genres like '%" + genre2 + "%') and" +						
						" tagid=" +  tagID +";";
				rs2 = conn.prepareStatement(query2).executeQuery();
				while(rs2.next())
					m = genre1Ugenre2Movies.size() - rs2.getInt(1);
 				rs2.close();
				double offset = 0;
				if(r==0 || M-m-R+r==0 || m==r || R==M || R==r)
					offset = 0.5;
				double num = ((r + offset) / (R - r + offset));
				double dem = ((m - r + offset) / (M - m - R + r + offset));
				double logVal = Math.log(num/dem);
				double left = (r + offset)/(R + 2 * offset);
				double right = (m - r + offset)/(M - R + 2 * offset);
				double absVal = Math.abs(left - right);
				double new_weight = logVal * absVal;
				tagWeight.put(tagID, weight + new_weight);
			}
			ModelHelper mHelper = new ModelHelper();
			finalTagVector = mHelper.sortTagVector(conn, tagWeight, 1);
		} catch(SQLException e) {
			e.printStackTrace();
		} finally {
			ConnectorClass.closeConnection(conn);
		}
		return finalTagVector;
	}
}


