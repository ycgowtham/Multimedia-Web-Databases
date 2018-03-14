package com.alfred.mwd_project.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TFIDFModelGenerator {
	
	public LinkedHashMap<String, Double> generateActorTagModel(int actorID) {
		TFModelGenerator tfModel = new TFModelGenerator();
		LinkedHashMap<String, Double> tfTagVector = tfModel.generateActorTagVector(actorID);
		IDFModelGenerator idfModel = new IDFModelGenerator();
		HashMap<String, Double> tagActorIDF = idfModel.generateActorTagModel();
		HashMap<String, Double> tfidfTagVector = new HashMap<>();
		ArrayList<String> tags = new ArrayList<>(tagActorIDF.keySet());
		for (int i=0; i<tags.size(); i++) {
			String tagName = tags.get(i);
			double tf = 0.0;
			if(tfTagVector.containsKey(tagName))
				tf = tfTagVector.get(tagName);
			double idf = tagActorIDF.get(tagName);
			double tf_idf = tf*idf;
			if (tf_idf != 0.0)
				tfidfTagVector.put(tagName, tf_idf);
		}
		ModelHelper mHelper = new ModelHelper();
		LinkedHashMap<String, Double> finalTagVector = mHelper.sortTFIDFTagModel(tfidfTagVector);
		return finalTagVector;

	}

	public LinkedHashMap<String, Double> generateGenreTagModel(String genre) {
		TFModelGenerator tfModel = new TFModelGenerator();
		LinkedHashMap<String, Double> tfTagVector = tfModel.generateGenreTagVector(genre);
		IDFModelGenerator idfModel = new IDFModelGenerator();
		HashMap<String, Double> tagUserIDF = idfModel.generateGenreTagModel();
		HashMap<String, Double> tfidfTagVector = new HashMap<>();
		ArrayList<String> tags = new ArrayList<>(tagUserIDF.keySet());
		for (int i=0; i<tags.size(); i++) {
			String tagName = tags.get(i);
			double tf = 0.0;
			if(tfTagVector.containsKey(tagName))
				tf = tfTagVector.get(tagName);
			double idf = tagUserIDF.get(tagName);
			double tf_idf = tf*idf;
			if (tf_idf != 0.0)
				tfidfTagVector.put(tagName, tf_idf);
		}
		ModelHelper mHelper = new ModelHelper();
		LinkedHashMap<String, Double> finalTagVector = mHelper.sortTFIDFTagModel(tfidfTagVector);
		return finalTagVector;
	}
	
	public LinkedHashMap<String, Double> generateUserTagModel(int userID) {
		TFModelGenerator tfModel = new TFModelGenerator();
		LinkedHashMap<String, Double> tfTagVector = tfModel.generateUserTagVector(userID);
		IDFModelGenerator idfModel = new IDFModelGenerator();
		HashMap<String, Double> tagUserIDF = idfModel.generateUserTagModel(tfTagVector);
		HashMap<String, Double> tfidfTagVector = new HashMap<>();
		for(String tagName : tfTagVector.keySet()) {
			double tf = tfTagVector.get(tagName);
			double idf = tagUserIDF.get(tagName);
			double tf_idf = tf*idf;
			if (tf_idf != 0.0)
				tfidfTagVector.put(tagName, tf_idf);
		}
		ModelHelper mHelper = new ModelHelper();
		LinkedHashMap<String, Double> finalTagVector = mHelper.sortTFIDFTagModel(tfidfTagVector);
		return finalTagVector;
	}
}
