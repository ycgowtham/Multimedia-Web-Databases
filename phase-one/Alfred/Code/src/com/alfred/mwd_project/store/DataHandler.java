package com.alfred.mwd_project.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DataHandler {
	
	public static double minTimestamp = 9999999;
	public static double maxTimestamp = 0;
	
	public static ArrayList<Integer> ActorsList = new ArrayList<>();
	public static ArrayList<String> GenresList = new ArrayList<>();
	public static ArrayList<Integer> UsersList = new ArrayList<>();
	
	public static HashMap<Integer, HashSet<Integer>> MovieTags = new HashMap<>();
	public static HashMap<Integer, HashSet<Integer>> UserMovies = new HashMap<>();
}
