package com.alfred.mwd_project.tests;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;

import com.alfred.mwd_project.engine.DifferentiatorModel;
import com.alfred.mwd_project.engine.ModelHelper;
import com.alfred.mwd_project.store.PreprocessData;

public class TestDifferentiatorModel {

	@Test
	public void testTF_IDF_DIFF() {
		String genre1 = "Thriller";
		String genre2 = "Romance";
		
		PreprocessData pData = new PreprocessData();
		pData.setMovieTagsList();
		pData.setUserMoviesList();
		DifferentiatorModel diffModel = new DifferentiatorModel();
		diffModel.differentiateGenreModel(genre1, genre2);
		
	}
	
	@Test
	public void testPDIFF_1() {
		String genre1 = "Thriller";
		String genre2 = "Romance";
		
		DifferentiatorModel diffModel = new DifferentiatorModel();
		LinkedHashMap<String, Double> tagVector = diffModel.PDIFF_1_Generator(genre1, genre2);
		ModelHelper mHelper = new ModelHelper();
		mHelper.displayTagVector(tagVector);
	}
	
	@Test
	public void testPDIFF_2() {
		String genre1 = "Thriller";
		String genre2 = "Romance";
		
		DifferentiatorModel diffModel = new DifferentiatorModel();
		LinkedHashMap<String, Double> tagVector = diffModel.PDIFF_2_Generator(genre1, genre2);
		ModelHelper mHelper = new ModelHelper();
		mHelper.displayTagVector(tagVector);
	}
}
