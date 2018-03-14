package com.alfred.mwd_project.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;

import com.alfred.mwd_project.engine.TFModelGenerator;
import com.alfred.mwd_project.engine.ModelHelper;

public class TestModelGenerator {

	@Test(priority=1)
	public void testActorTagVector(){
		ModelHelper mHelper = new ModelHelper();
		mHelper.setMinMaxTimestamp();
		TFModelGenerator model = new TFModelGenerator();
		LinkedHashMap<String, Double> tagVector = model.generateActorTagVector(1582699);
		assertNotNull(tagVector);
		assertTrue(tagVector.size() > 0);
		assertEquals(tagVector.get("boxing"), 0.37474565314579045);
	}
	
	@Test(priority=2)
	public void testGenreTagVector(){
		ModelHelper mHelper = new ModelHelper();
		mHelper.setMinMaxTimestamp();
		TFModelGenerator model = new TFModelGenerator();
		LinkedHashMap<String, Double> tagVector = model.generateGenreTagVector("Thriller");
		assertNotNull(tagVector);
		assertTrue(tagVector.size() > 0);
		assertEquals(tagVector.get("time travel"), 0.04526830510030622);
	}
	
	@Test(priority=3)
	public void testUserTagVector(){
		ModelHelper mHelper = new ModelHelper();
		mHelper.setMinMaxTimestamp();
		TFModelGenerator model = new TFModelGenerator();
		LinkedHashMap<String, Double> tagVector = model.generateUserTagVector(146);
		assertNotNull(tagVector);
		assertTrue(tagVector.size() > 0);
		System.out.println(tagVector);
		assertEquals(tagVector.get("time travel"), 0.03487886482724353);
	}
	
}
