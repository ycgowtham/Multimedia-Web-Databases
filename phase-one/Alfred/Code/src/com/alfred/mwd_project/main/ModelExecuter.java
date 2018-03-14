package com.alfred.mwd_project.main;

import java.util.LinkedHashMap;
import java.util.Scanner;

import com.alfred.mwd_project.engine.TFModelGenerator;
import com.alfred.mwd_project.store.DatabaseCreation;
import com.alfred.mwd_project.store.PreprocessData;
import com.alfred.mwd_project.engine.DifferentiatorModel;
import com.alfred.mwd_project.engine.ModelHelper;
import com.alfred.mwd_project.engine.TFIDFModelGenerator;

public class ModelExecuter {

	public static void main(String[] args) {
//		DatabaseCreation dbCreation = new DatabaseCreation();
//		dbCreation.createTables();
//		dbCreation.addDataForTables();

		ModelHelper mHelper = new ModelHelper();
		mHelper.setMinMaxTimestamp();
		PreprocessData pData = new PreprocessData();
		pData.setMovieTagsList();
		pData.setUserMoviesList();
		
		TFModelGenerator tfModel = new TFModelGenerator();
		TFIDFModelGenerator tfidfModel = new TFIDFModelGenerator(); 
	
		while(true) {
			System.out.println("Enter your command. Enter exit to quit.");
			Scanner sc = new Scanner(System.in);
			String line = sc.nextLine();
			if(line.equals("exit"))
				System.exit(0);
			String[] info = line.split(" ");
			if(info.length < 3)
				System.exit(1);
			LinkedHashMap<String, Double> tagVector = null;
			switch(info[0]) {
			case "print_actor_vector":
				int actorID = Integer.parseInt(info[1]);
				switch(info[2]) {
				case "tf":
				case "TF":
					tagVector = tfModel.generateActorTagVector(actorID);
					break;
				case "tf-idf":
				case "TF-IDF":
					tagVector = tfidfModel.generateActorTagModel(actorID);
					break;
				}
				break;
			case "print_genre_vector":
				String genre = info[1];
				switch(info[2]) {
				case "tf":
				case "TF":
					tagVector = tfModel.generateGenreTagVector(genre);
					break;
				case "tf-idf":
				case "TF-IDF":
					tagVector = tfidfModel.generateGenreTagModel(genre);
					break;
				}
				break;
			case "print_user_vector":
				int userID = Integer.parseInt(info[1]);
				switch(info[2]) {
				case "tf":
				case "TF":
					tagVector = tfModel.generateUserTagVector(userID);
					break;
				case "tf-idf":
				case "TF-IDF":
					tagVector = tfidfModel.generateUserTagModel(userID);
					break;
				}
				break;
			case "differentiate_genre":
				String genre1 = info[1];
				String genre2 = info[2];
				DifferentiatorModel diffModel = new DifferentiatorModel();
				switch(info[3]) {
				case "tf-idf-diff":
				case "TF-IDF-DIFF":
					double output = diffModel.differentiateGenreModel(genre1, genre2);
					System.out.println(output);
					break;
				case "p-diff1":
				case "P-DIFF1":
					tagVector = diffModel.PDIFF_1_Generator(genre1, genre2);
					break;
				case "p-diff2":
				case "P-DIFF2":
					tagVector = diffModel.PDIFF_2_Generator(genre1, genre2);
					break;
				}
				break;
				
			}
			if(tagVector != null)
				mHelper.displayTagVector(tagVector);
		}
	}
}
