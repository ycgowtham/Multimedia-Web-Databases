package com.alfred.mwd_project.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.DateUtil;

import com.alfred.mwd_project.connector.ConnectorClass;
import com.alfred.mwd_project.store.DataHandler;

public class ModelHelper {

	public void setMinMaxTimestamp(){
		Connection conn = ConnectorClass.connectToDB();
		try {
			String query = "select min(timestamp) from mltags";
			ResultSet rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()){
				Date date = new Date(rs.getTimestamp(1).getTime());
				DataHandler.minTimestamp = DateUtil.getExcelDate(date);
			}
			query = "select max(timestamp) from mltags";
			rs = conn.prepareStatement(query).executeQuery();
			while(rs.next()){
				Date date = new Date(rs.getTimestamp(1).getTime());
				DataHandler.maxTimestamp = DateUtil.getExcelDate(date);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public LinkedHashMap<String, Double> sortTagVector(Connection conn, HashMap<Integer, Double> tagWeight, double totalWeight) {
		Set<Entry<Integer, Double>> entrySet = tagWeight.entrySet();
		Comparator<Entry<Integer, Double>> valueComparator = new Comparator<Entry<Integer, Double>>() {	
			@Override 
			public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) { 
				Double v1 = e1.getValue(); 
				Double v2 = e2.getValue(); 
				return v2.compareTo(v1); 
			} 
		};
		List<Entry<Integer, Double>> newList = new ArrayList<>(entrySet);
		Collections.sort(newList, valueComparator);
		LinkedHashMap<String, Double> sortedHashMap = new LinkedHashMap<>();
		String tagName = "";
		for(Entry<Integer, Double> entry : newList) {
			String query = "select tag from genome_tags where tagid=" + entry.getKey() + ";";
			try {
				ResultSet rs = conn.prepareStatement(query).executeQuery();
				while(rs.next()){
					tagName = rs.getString(1);
				}
			} catch(SQLException e){
				e.printStackTrace();
			} 
			sortedHashMap.put(tagName, entry.getValue()/totalWeight);
		}
		return sortedHashMap;
	}
	
	public LinkedHashMap<String, Double> sortTFIDFTagModel(HashMap<String, Double> tfidfTagVector) {
		Set<Entry<String, Double>> entrySet = tfidfTagVector.entrySet();
		Comparator<Entry<String, Double>> valueComparator = new Comparator<Entry<String, Double>>() {	
			@Override 
			public int compare(Entry<String, Double> e1, Entry<String, Double> e2) { 
				Double v1 = e1.getValue(); 
				Double v2 = e2.getValue(); 
				return v2.compareTo(v1); 
			} 
		};
		List<Entry<String, Double>> newList = new ArrayList<>(entrySet);
		Collections.sort(newList, valueComparator);
		LinkedHashMap<String, Double> sortedHashMap = new LinkedHashMap<>();
		for(Entry<String, Double> entry : newList) {
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}
	
	public void displayTagVector(LinkedHashMap<String, Double> tagVector) {
		ArrayList<String> keys = new ArrayList<>(tagVector.keySet());
		for(int i=0; i<keys.size(); i++) {
			System.out.println(keys.get(i) + " : " + tagVector.get(keys.get(i)));
		}
	}


}
