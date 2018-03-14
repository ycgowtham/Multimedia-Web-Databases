package com.alfred.mwd_project.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import com.alfred.mwd_project.engine.TFIDFModelGenerator;
import com.alfred.mwd_project.store.PreprocessData;

public class TestTFIDFModelGenerator {
	
	@Test
	public void testActorTagModel() {
		int actorID = 1582699;
		TFIDFModelGenerator tfidfModelGenerator = new TFIDFModelGenerator();
		HashMap<String, Double> tagVector = tfidfModelGenerator.generateActorTagModel(actorID);
		assertNotNull(tagVector);
		assertTrue(tagVector.size() > 0);
		assertEquals(tagVector.get("boxing"), 2.0273182999349992);
	}

	@Test
	public void testGenreTagModel() {
		String genre = "Thriller";
		TFIDFModelGenerator tfidfModelGenerator = new TFIDFModelGenerator();
		HashMap<String, Double> tagVector = tfidfModelGenerator.generateGenreTagModel(genre);
		assertNotNull(tagVector);
		assertTrue(tagVector.size() > 0);
		assertEquals(tagVector.get("philip k. dick"), 0.029727630109899483);
	}

	@Test
	public void testUserTagModel() {
		int userID = 146;
		PreprocessData pData = new PreprocessData();
		pData.setMovieTagsList();
		pData.setUserMoviesList();
		TFIDFModelGenerator tfidfModelGenerator = new TFIDFModelGenerator();
		HashMap<String, Double> tagVector = tfidfModelGenerator.generateUserTagModel(userID);
		assertNotNull(tagVector);
		assertTrue(tagVector.size() > 0);
		assertEquals(tagVector.get("vampires"), 0.03378510193106403);
	}
}
