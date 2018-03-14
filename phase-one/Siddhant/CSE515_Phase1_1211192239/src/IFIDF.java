


import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.DateUtil;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



class MovieIdAndRank{
    String movie_id, actor_movie_rank;
    MovieIdAndRank(String mv_id, String amr){
        movie_id = mv_id;
        actor_movie_rank = amr;
    }

    public String getMovie_id(){
        return movie_id;
    }

    public String getActor_movie_rank(){
        return actor_movie_rank;
    }
}


class TagIdwithDate{
    int tag_id;
    double date_in_excel_format;
    TagIdwithDate(int tag_id, double date_in_excel_format){
        this.tag_id = tag_id;
        this.date_in_excel_format = date_in_excel_format;
    }

    public int getTagId(){
        return tag_id;
    }

    public double getDate_in_excel_format(){
        return date_in_excel_format;
    }
}

public class IFIDF {

    static Set<Integer> actorIdSet = new TreeSet<>();
    static Set<String> genresSet = new TreeSet<>();
    static double minTS = Double.MAX_VALUE , maxTS = Double.MIN_VALUE;
    static HashMap<Integer, Integer> hmMoviesWithMaxRank = new HashMap<Integer,Integer>();

    public static  List<List<String>> readCSVToGet_movieactor(){

        String filePath = new File("").getAbsolutePath();
        //String csvFile = "C:\\Users\\tanpu\\Downloads\\phase1_dataset\\phase1_dataset\\movie-actor.csv";
        filePath += "\\phase1_dataset\\movie-actor.csv";
        //System.out.println("File path: " + filePath);
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<List<String>> lines = new ArrayList<>();

        int count =0;
        try {

            br = new BufferedReader(new FileReader(filePath));
            line = br.readLine();
            while ((line = br.readLine()) != null) {

                // use comma as se  parator
                String[] movie_actor = line.split(cvsSplitBy);
                if(hmMoviesWithMaxRank.containsKey(Integer.parseInt(movie_actor[0]))){
                    hmMoviesWithMaxRank.put(Integer.parseInt(movie_actor[0]),Math.max(hmMoviesWithMaxRank.get(Integer.parseInt(movie_actor[0])),Integer.parseInt(movie_actor[2])));
                }else{
                    hmMoviesWithMaxRank.put(Integer.parseInt(movie_actor[0]),Integer.parseInt(movie_actor[2]));
                }
                actorIdSet.add(Integer.parseInt(movie_actor[1]));
                lines.add(Arrays.asList(movie_actor));
                count++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

    public static  List<List<String>> readCSVToGet_moviegenre(){
        //String csvFile = "C:\\Users\\tanpu\\Downloads\\phase1_dataset\\phase1_dataset\\mlmovies.csv";
        String filePath = new File("").getAbsolutePath();
        filePath += "\\phase1_dataset\\mlmovies.csv";
        List<List<String>> lines = new ArrayList<>();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(filePath));
            String[] line;
            line = reader.readNext();
            while ((line = reader.readNext()) != null) {
                lines.add(Arrays.asList(line));
                if(line[2].contains("|")){
                    String[] movie_geners = line[2].split("\\|");
                    for(String movie_genre : movie_geners){
                        genresSet.add(movie_genre.trim());
                    }
                }else{
                    genresSet.add(line[2]);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static  List<List<String>> readCSV(String fileName){
        //String csvFile = "C:\\Users\\tanpu\\Downloads\\phase1_dataset\\phase1_dataset\\"+ fileName ;
        String filePath = new File("").getAbsolutePath();
        filePath += "\\phase1_dataset\\"+ fileName;
        //System.out.println("Filepath: "+ filePath );
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<List<String>> lines = new ArrayList<>();

        int count =0;
        try {

            br = new BufferedReader(new FileReader(filePath));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] movie_actor = line.split(cvsSplitBy);
                lines.add(Arrays.asList(movie_actor));
                count++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }

    public static void printCSV(List<List<String>> lines){

        int lineNo = 1;
        for(List<String> line: lines) {
            int columnNo = 1;
            for (String value: line) {
                System.out.println("Line " + lineNo + " Column " + columnNo + ": " + value);
                columnNo++;
            }
            lineNo++;
        }
    }

    public static HashMap<Integer, List<MovieIdAndRank>> findActorWirhMovies(List<List<String>> movie_actor){
        HashMap<Integer, List<MovieIdAndRank>> actorMovieAndRank = new HashMap<Integer, List<MovieIdAndRank>>();
        String[] lineValue = new String[3];
        int lineNo =0;
        for(List<String> line: movie_actor) {
            lineValue = line.toArray(lineValue);
            if(actorMovieAndRank.containsKey(Integer.parseInt(lineValue[1]))){
                List<MovieIdAndRank> ls = actorMovieAndRank.get(Integer.parseInt(lineValue[1]));
                MovieIdAndRank mv = new MovieIdAndRank(lineValue[0],lineValue[2]);
                ls.add(mv);
                actorMovieAndRank.put(Integer.parseInt(lineValue[1]),ls);
            }else{
                List<MovieIdAndRank> ls = new ArrayList<MovieIdAndRank>();
                MovieIdAndRank mv = new MovieIdAndRank(lineValue[0],lineValue[2]);
                ls.add(mv);
                actorMovieAndRank.put(Integer.parseInt(lineValue[1]),ls);
            }
            lineNo++;
        }
        return actorMovieAndRank ;
    }

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        int count =1;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("tag_id = "+ pair.getKey()+" tag = "+hmTagIdwithTag.get(pair.getKey()) + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            count++;
        }
    }

    public static HashMap<String,List<Integer>> getMoviesOfGivenGenre(List<List<String>> mlmovies){
        HashMap<String,List<Integer>> hmForGenreAndMovieId = new HashMap<String, List<Integer>>();
        String[] lineValue = new String[3];
        for(List<String> mlmovie: mlmovies){
            lineValue = mlmovie.toArray(lineValue);
            String[] genres = lineValue[2].split("\\|");
            for(String genre:genres){
                if(hmForGenreAndMovieId.containsKey(genre)){
                    List<Integer> ls = hmForGenreAndMovieId.get(genre);
                    ls.add(Integer.parseInt(lineValue[0]));
                    hmForGenreAndMovieId.put(genre,ls);
                }else{
                    List<Integer> ls = new ArrayList<Integer>();
                    ls.add(Integer.parseInt(lineValue[0]));
                    hmForGenreAndMovieId.put(genre,ls);
                }
            }

        }
        return hmForGenreAndMovieId;

    }

    public static HashMap<Integer, List<TagIdwithDate>> get_TagId_With_Dates_For_Movies(List<List<String>> mltags) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<Integer, List<TagIdwithDate>> hmForMovieWithTagId = new HashMap<Integer, List<TagIdwithDate>>();
        String[] lineValue = new String[3];
        for(List<String> mltag_temp : mltags){
                lineValue = mltag_temp.toArray(lineValue);
                Date date = sdf.parse(lineValue[3].substring(1,lineValue[3].length()-1));
                double temp_val = DateUtil.getExcelDate(date);
                minTS = Math.min(minTS, temp_val);
                maxTS = Math.max(maxTS, temp_val);

            if(hmForMovieWithTagId.containsKey(Integer.parseInt(lineValue[1]))){
                    List<TagIdwithDate> ls = hmForMovieWithTagId.get(Integer.parseInt(lineValue[1]));
                    TagIdwithDate td = new TagIdwithDate(Integer.parseInt(lineValue[2]),temp_val);
                    ls.add(td);
                    hmForMovieWithTagId.put(Integer.parseInt(lineValue[1]),ls);
                }else{
                    List<TagIdwithDate> ls = new ArrayList<TagIdwithDate>();
                    TagIdwithDate td = new TagIdwithDate(Integer.parseInt(lineValue[2]),temp_val);
                    ls.add(td);
                    hmForMovieWithTagId.put(Integer.parseInt(lineValue[1]),ls);
                }
        }
        return hmForMovieWithTagId;
    }

    public static double calculateTS(double currTS){
        double top = currTS-minTS+1;
        double bottom = maxTS - minTS+1;
        double result = (top/bottom);
        return result;
    }

    public static double calculateRankValue(int currRank, int movieId){
        int maxRankValue = hmMoviesWithMaxRank.get(movieId);
        int minRankValue = 1;
        double top =   maxRankValue - minRankValue ;
        double result = (top/currRank);
        return result;
    }

    public static Map<Integer, Double> calculateTF(Map<Integer, Double> hmResultTagIdwithWeight, double total_wt){
        Map<Integer, Double> hmResultTagIdwithWeight_with_tf = new HashMap<>();
        Iterator it = hmResultTagIdwithWeight.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            double wt = (double) pair.getValue();
            hmResultTagIdwithWeight_with_tf.put((Integer)pair.getKey(), wt/total_wt );
            it.remove(); // avoids a ConcurrentModificationException
        }
        return hmResultTagIdwithWeight_with_tf;
    }

    public static Map<Integer, Double> firstTaskTF(int actorid, HashMap<Integer, List<MovieIdAndRank>> hmActorWithMovies, HashMap<Integer, List<TagIdwithDate>> hmForMovieWithTagId ){
            Map<Integer, Double> hmResultTagIdwithWeight = new HashMap<Integer, Double>();
            if(hmActorWithMovies.containsKey(actorid)) {
                List<MovieIdAndRank> lsMR = hmActorWithMovies.get(actorid);
                double totat_wt = 0;
                if(lsMR==null){
                    return null;
                }
                for (MovieIdAndRank mar : lsMR) {
                    int mv_id = Integer.parseInt(mar.getMovie_id());
                    int mv_rank = Integer.parseInt(mar.getActor_movie_rank());
                    List<TagIdwithDate> lsTD = hmForMovieWithTagId.get(mv_id);
                    if(lsTD==null){
                        return null;
                    }
                    for (TagIdwithDate td : lsTD) {
                        if (hmResultTagIdwithWeight.containsKey(td.tag_id)) {
                            double wt = 1.0 / mv_rank * calculateTS(td.date_in_excel_format);
                            totat_wt = totat_wt + wt;
                            hmResultTagIdwithWeight.put(td.tag_id, hmResultTagIdwithWeight.get(td.tag_id) + wt);
                        } else {
                            double wt = 1.0 / mv_rank * calculateTS(td.date_in_excel_format);
                            totat_wt = totat_wt + wt;
                            hmResultTagIdwithWeight.put(td.tag_id, wt);
                        }
                    }

                }
                Map<Integer, Double> hmResultTagIdwithWeight_with_tf = calculateTF(hmResultTagIdwithWeight, totat_wt);
                hmResultTagIdwithWeight_with_tf = MapUtil.sortByValue(hmResultTagIdwithWeight_with_tf);
                return hmResultTagIdwithWeight_with_tf;
            }
            return null;
    }

    public static Map<Integer, Double> secondTaskTF(String genre, HashMap<Integer, List<TagIdwithDate>> hmForMovieWithTagId, HashMap<String, List<Integer>> hmforGenreWithMovieId){
        Map<Integer, Double> hmResultTagIdwithWeight = new HashMap<Integer, Double>();
        double totat_wt = 0;
        if(hmforGenreWithMovieId.containsKey(genre)){
            List<Integer> lsMovies = hmforGenreWithMovieId.get(genre);
            if(lsMovies==null){
                return null;
            }
            for(int movieId : lsMovies){
                List<TagIdwithDate> lsTD = hmForMovieWithTagId.get(movieId);
                if(lsTD!=null) {
                    for (TagIdwithDate td : lsTD) {
                        if (hmResultTagIdwithWeight.containsKey(td.tag_id)) {
                            double wt = calculateTS(td.date_in_excel_format);
                            totat_wt = totat_wt + wt;
                            hmResultTagIdwithWeight.put(td.tag_id, hmResultTagIdwithWeight.get(td.tag_id) + wt);
                        } else {
                            double wt = calculateTS(td.date_in_excel_format);
                            totat_wt = totat_wt + wt;
                            hmResultTagIdwithWeight.put(td.tag_id, wt);
                        }
                    }
                }
            }
            Map<Integer, Double> hmResultTagIdwithWeight_with_tf = calculateTF(hmResultTagIdwithWeight, totat_wt);
            hmResultTagIdwithWeight_with_tf = MapUtil.sortByValue(hmResultTagIdwithWeight_with_tf);
            return hmResultTagIdwithWeight_with_tf;
        }
        return null;
    }

    public static HashMap<Integer, List<TagIdwithDate>> get_TagId_With_Dates_For_Users(List<List<String>> mltags) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        HashMap<Integer, List<TagIdwithDate>> hmForUserIdWithTagId = new HashMap<Integer, List<TagIdwithDate>>();
        String[] lineValue = new String[3];
        for(List<String> mltag_temp : mltags){
            lineValue = mltag_temp.toArray(lineValue);
            Date date = sdf.parse(lineValue[3].substring(1,lineValue[3].length()-1));
            double temp_val = DateUtil.getExcelDate(date);
            minTS = Math.min(minTS, temp_val);
            maxTS = Math.max(maxTS, temp_val);

            if(hmForUserIdWithTagId.containsKey(Integer.parseInt(lineValue[0]))){
                List<TagIdwithDate> ls = hmForUserIdWithTagId.get(Integer.parseInt(lineValue[0]));
                TagIdwithDate td = new TagIdwithDate(Integer.parseInt(lineValue[2]),temp_val);
                ls.add(td);
                hmForUserIdWithTagId.put(Integer.parseInt(lineValue[0]),ls);
            }else{
                List<TagIdwithDate> ls = new ArrayList<TagIdwithDate>();
                TagIdwithDate td = new TagIdwithDate(Integer.parseInt(lineValue[2]),temp_val);
                ls.add(td);
                hmForUserIdWithTagId.put(Integer.parseInt(lineValue[0]),ls);
            }
        }
        return hmForUserIdWithTagId;
    }

    public static Map<Integer, Double> thirdTaskTF(int user_id, HashMap<Integer, Set<Integer>> hmForUserIdwithMovieId, HashMap<Integer, List<TagIdwithDate>> hmForMovieWithTagId ){
        Map<Integer, Double> hmResultTagIdwithWeight = new HashMap<Integer, Double>();
        double totat_wt = 0;
        if(hmForUserIdwithMovieId.containsKey(user_id)){
                Set<Integer> movieIds = hmForUserIdwithMovieId.get(user_id);
                if(movieIds==null){
                    return null;
                }
                for (int mv_id: movieIds){
                    List<TagIdwithDate> lsTD = hmForMovieWithTagId.get(mv_id);
                    if(lsTD!=null) {
                        for (TagIdwithDate td : lsTD) {
                            if (hmResultTagIdwithWeight.containsKey(td.tag_id)) {
                                double wt = calculateTS(td.date_in_excel_format);
                                totat_wt = totat_wt + wt;
                                hmResultTagIdwithWeight.put(td.tag_id, hmResultTagIdwithWeight.get(td.tag_id) + wt);
                            } else {
                                double wt = calculateTS(td.date_in_excel_format);
                                totat_wt = totat_wt + wt;
                                hmResultTagIdwithWeight.put(td.tag_id, wt);
                            }
                        }
                    }
                }
            Map<Integer, Double> hmResultTagIdwithWeight_with_tf = calculateTF(hmResultTagIdwithWeight, totat_wt);
            hmResultTagIdwithWeight_with_tf = MapUtil.sortByValue(hmResultTagIdwithWeight_with_tf);
            return hmResultTagIdwithWeight_with_tf;
        }
        return null;
    }

    public static int getTagIdwithActorIdCount(int tagId, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, HashMap<Integer, Set<Integer>> hmForMovieIdwithActorId){
        Set<Integer> movieId = hmForTagIdWithMovieId.get(tagId);
        Set<Integer> uniqueActorId = new TreeSet<>();
        for(int mv_id: movieId){
            Set<Integer> actorId = hmForMovieIdwithActorId.get(mv_id);
            for(int actor_id: actorId){
                uniqueActorId.add(actor_id);
            }
        }
        return uniqueActorId.size();
    }

    public static int getTagIdwithGenresCount(int tagId, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, HashMap<Integer, Set<String>> hmForMovieIdwithGenres){
        Set<Integer> movieId = hmForTagIdWithMovieId.get(tagId);
        Set<String> uniqueGenres = new TreeSet<>();
        for(int mv_id: movieId){
            Set<String> genres = hmForMovieIdwithGenres.get(mv_id);
            for(String genre: genres){
                uniqueGenres.add(genre);
            }
        }
        return uniqueGenres.size();
    }

    public static HashMap<Integer, Double> firstTaskTFIDF(Map mp, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, HashMap<Integer, Set<Integer>> hmForMovieIdwithActorId){
        Iterator it = mp.entrySet().iterator();
        HashMap<Integer, Double> firstTaskTFIDFValue = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int tagIdFrequency = getTagIdwithActorIdCount((int) pair.getKey(), hmForTagIdWithMovieId, hmForMovieIdwithActorId);
            double result = (double)pair.getValue() * (Math.log(actorIdSet.size()/tagIdFrequency));
            firstTaskTFIDFValue.put((int)pair.getKey(), result);
            it.remove(); // avoids a ConcurrentModificationException
        }
        return firstTaskTFIDFValue;
    }

    public static int getUserIdFrequency(int tagId, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, HashMap<Integer, Set<Integer>> hmForMovieIdWithUserId ){
        Set<Integer> movie_ids = hmForTagIdWithMovieId.get(tagId);
        Set<Integer> uniqueUserIds = new TreeSet<Integer>();
        for(int mv_id : movie_ids){
            uniqueUserIds.addAll(hmForMovieIdWithUserId.get(mv_id));
        }
        return uniqueUserIds.size();
    }

    public static HashMap<Integer, Double> thirdTaskTFIDF(Map mp, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, HashMap<Integer, Set<Integer>> hmForMovieIdWithUserId ){
        Iterator it = mp.entrySet().iterator();
        HashMap<Integer, Double> thirdTaskTFIDFValue = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int userIdFrequency = getUserIdFrequency((int)pair.getKey(),  hmForTagIdWithMovieId, hmForMovieIdWithUserId );
            //System.out.println("Key " + pair.getKey() +" User freq: "+ userIdFrequency);
            double result = (double)pair.getValue() * (Math.log(30001/((1.0)*userIdFrequency)));
            thirdTaskTFIDFValue.put((int)pair.getKey(), result);
        }
        return thirdTaskTFIDFValue;
    }

    public static HashMap<Integer, Double> secondTaskTFIDF(Map mp, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, HashMap<Integer, Set<String>> hmForMovieIdwithGenres){
        Iterator it = mp.entrySet().iterator();
        HashMap<Integer, Double> secondTaskTFIDFValue = new HashMap<>();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int genreFrequency = getTagIdwithGenresCount((int) pair.getKey(), hmForTagIdWithMovieId, hmForMovieIdwithGenres);
            double result = (double)pair.getValue() * (Math.log(genresSet.size()*1.0/genreFrequency));
            secondTaskTFIDFValue.put((int)pair.getKey(), result);
        }
        return secondTaskTFIDFValue;
    }

    public static HashMap<Integer, Set<Integer>> getTagIdwithMovieId(List<List<String>> mltags){
        HashMap<Integer, Set<Integer>> hmForTagIdwithMovieId = new HashMap<>();
        String[] lineValue = new String[3];
        for(List<String> mltag: mltags){
            lineValue = mltag.toArray(lineValue);
            if(hmForTagIdwithMovieId.containsKey(Integer.parseInt(lineValue[2]))){
                Set<Integer> movieId = hmForTagIdwithMovieId.get(Integer.parseInt(lineValue[2]));
                movieId.add(Integer.parseInt(lineValue[1]));
                hmForTagIdwithMovieId.put(Integer.parseInt(lineValue[2]),movieId);
            }else{
                Set<Integer> movieId = new TreeSet<>();
                movieId.add(Integer.parseInt(lineValue[1]));
                hmForTagIdwithMovieId.put(Integer.parseInt(lineValue[2]),movieId);
            }

        }
        return hmForTagIdwithMovieId;
    }

    public static HashMap<Integer, Set<Integer>> getTagIdwithUserId(List<List<String>> mltags){
        HashMap<Integer, Set<Integer>> hmForTagIdwithUserId = new HashMap<>();
        String[] lineValue = new String[3];
        for(List<String> mltag: mltags){
            lineValue = mltag.toArray(lineValue);
            if(hmForTagIdwithUserId.containsKey(Integer.parseInt(lineValue[2]))){
                Set<Integer> userId = hmForTagIdwithUserId.get(Integer.parseInt(lineValue[2]));
                userId.add(Integer.parseInt(lineValue[0]));
                hmForTagIdwithUserId.put(Integer.parseInt(lineValue[2]),userId);
            }else{
                Set<Integer> userId = new TreeSet<>();
                userId.add(Integer.parseInt(lineValue[0]));
                hmForTagIdwithUserId.put(Integer.parseInt(lineValue[2]),userId);
            }

        }
        return hmForTagIdwithUserId;
    }

    public static HashMap<Integer, Set<Integer>> getMovieIdWithUserId(List<List<String>> mltags, List<List<String>> mlratings){
        HashMap<Integer, Set<Integer>> hmMovieIdWithUserId = new HashMap<>();
        String[] lineValue = new String[3];
        for(List<String> mltag: mltags){
            lineValue = mltag.toArray(lineValue);
            if(hmMovieIdWithUserId.containsKey(Integer.parseInt(lineValue[1]))){
                Set<Integer> userId = hmMovieIdWithUserId.get(Integer.parseInt(lineValue[1]));
                userId.add(Integer.parseInt(lineValue[0]));
                hmMovieIdWithUserId.put(Integer.parseInt(lineValue[1]),userId);
            }else{
                Set<Integer> userId = new TreeSet<>();
                userId.add(Integer.parseInt(lineValue[0]));
                hmMovieIdWithUserId.put(Integer.parseInt(lineValue[1]),userId);
            }

        }

        lineValue = new String[4];
        for(List<String> mlrating: mlratings){
            lineValue = mlrating.toArray(lineValue);
            if(hmMovieIdWithUserId.containsKey(Integer.parseInt(lineValue[0]))){
                Set<Integer> movieId = hmMovieIdWithUserId.get(Integer.parseInt(lineValue[0]));
                movieId.add(Integer.parseInt(lineValue[1]));
                hmMovieIdWithUserId.put(Integer.parseInt(lineValue[0]),movieId);
            }else{
                Set<Integer> movieId = new TreeSet<>();
                movieId.add(Integer.parseInt(lineValue[1]));
                hmMovieIdWithUserId.put(Integer.parseInt(lineValue[0]),movieId);
            }

        }
        return hmMovieIdWithUserId;
    }

    public static HashMap<Integer, Set<Integer>> getMovieIdwithActorId(List<List<String>> movie_actor){
        HashMap<Integer, Set<Integer>> hmForMovieIdwithActorId = new HashMap<>();
        String[] lineValue = new String[3];
        for(List<String> mv_actor: movie_actor){
            lineValue = mv_actor.toArray(lineValue);
            if(hmForMovieIdwithActorId.containsKey(Integer.parseInt(lineValue[0]))){
                Set<Integer> actorId = hmForMovieIdwithActorId.get(Integer.parseInt(lineValue[0]));
                actorId.add(Integer.parseInt(lineValue[1]));
                hmForMovieIdwithActorId.put(Integer.parseInt(lineValue[0]),actorId);
            }else{
                Set<Integer> actorId = new TreeSet<>();
                actorId.add(Integer.parseInt(lineValue[1]));
                hmForMovieIdwithActorId.put(Integer.parseInt(lineValue[0]),actorId);
            }

        }
        return hmForMovieIdwithActorId;
    }

    public static HashMap<Integer, Set<String>> getMovieIdwithGenres(List<List<String>> mlmovies){
        HashMap<Integer, Set<String>> hmForMovieIdwithwithGenres = new HashMap<>();
        String[] lineValue = new String[2];
        for(List<String> mlmovie: mlmovies){
            lineValue = mlmovie.toArray(lineValue);
            String[] genres = lineValue[2].split("\\|");
            if(hmForMovieIdwithwithGenres.containsKey(Integer.parseInt(lineValue[0]))){
                Set<String> genres_set = hmForMovieIdwithwithGenres.get(Integer.parseInt(lineValue[0]));
                for(String genre : genres){
                    genres_set.add(genre);
                }
                hmForMovieIdwithwithGenres.put(Integer.parseInt(lineValue[0]),genres_set);
            }else{
                Set<String> genres_set = new TreeSet<>();
                for(String genre : genres){
                    genres_set.add(genre);
                }
                hmForMovieIdwithwithGenres.put(Integer.parseInt(lineValue[0]),genres_set);
            }

        }
        return hmForMovieIdwithwithGenres;
    }

    public static HashMap<Integer, Set<Integer>> getUserIDwithMovieId(List<List<String>> mltags, List<List<String>> mlratings){
        HashMap<Integer, Set<Integer>> hmForUserIdwithMovieId = new HashMap<>();
        String[] lineValue = new String[3];
        for(List<String> mltag: mltags){
            lineValue = mltag.toArray(lineValue);
            if(hmForUserIdwithMovieId.containsKey(Integer.parseInt(lineValue[0]))){
                Set<Integer> movieId = hmForUserIdwithMovieId.get(Integer.parseInt(lineValue[0]));
                movieId.add(Integer.parseInt(lineValue[1]));
                hmForUserIdwithMovieId.put(Integer.parseInt(lineValue[0]),movieId);
            }else{
                Set<Integer> movieId = new TreeSet<>();
                movieId.add(Integer.parseInt(lineValue[1]));
                hmForUserIdwithMovieId.put(Integer.parseInt(lineValue[0]),movieId);
            }

        }
        lineValue = new String[4];
        for(List<String> mlrating: mlratings){
            lineValue = mlrating.toArray(lineValue);
            if(hmForUserIdwithMovieId.containsKey(Integer.parseInt(lineValue[1]))){
                Set<Integer> movieId = hmForUserIdwithMovieId.get(Integer.parseInt(lineValue[1]));
                movieId.add(Integer.parseInt(lineValue[0]));
                hmForUserIdwithMovieId.put(Integer.parseInt(lineValue[1]),movieId);
            }else{
                Set<Integer> movieId = new TreeSet<>();
                movieId.add(Integer.parseInt(lineValue[0]));
                hmForUserIdwithMovieId.put(Integer.parseInt(lineValue[1]),movieId);
            }

        }

        return hmForUserIdwithMovieId;
    }

    static HashMap<Integer, String> hmTagIdwithTag = new HashMap<>();
    public static void getTagIdwithTag(List<List<String>> genome_tags){
        String[] lineValue = new String[3];
        for(List<String> genome_tag: genome_tags){
            lineValue = genome_tag.toArray(lineValue);
            if(!hmTagIdwithTag.containsKey(Integer.parseInt(lineValue[0]))){
                hmTagIdwithTag.put(Integer.parseInt(lineValue[0]),lineValue[1]);
            }
        }
    }

    public static int getSizeTagsPresentMovieId(Set<Integer> g1Ug2MovieIds, Set<Integer> genreMovieId ){
        Set<Integer> intersection = new HashSet<Integer>(genreMovieId);
        intersection.retainAll(g1Ug2MovieIds);
        return intersection.size();
    }

    public static HashMap<Integer, Double> calculateNewTFIDFforGenre(Set<Integer> g1Ug2MovieIds,Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1,HashMap<Integer, Set<Integer>>  hmForTagIdWithMovieId ){
        HashMap<Integer, Double> newTFIDFforGenreTask4 = new HashMap<>();
        for(int key : hmFourthTaskTFTagIdwithWeightForG1.keySet()){
            double TagPresentIng1Ug2 = (double) getSizeTagsPresentMovieId(g1Ug2MovieIds, hmForTagIdWithMovieId.get(key));
            //System.out.println("key: "+ key + " freq " + TagPresentIng1Ug2 );
            double newIFIDFforTagId = (double)hmFourthTaskTFTagIdwithWeightForG1.get(key)* (Math.log(((1.0)*g1Ug2MovieIds.size())/((1.0)*TagPresentIng1Ug2)));
            newTFIDFforGenreTask4.put(key, newIFIDFforTagId);
        }
        return newTFIDFforGenreTask4;
    }

    public static double fourthTaskTFIDFDIFF(Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1, Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG2,  HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId, Set<Integer> g1Ug2MovieIds){
        HashMap<Integer, Double> newTFIDFForg1 = calculateNewTFIDFforGenre(g1Ug2MovieIds, hmFourthTaskTFTagIdwithWeightForG1, hmForTagIdWithMovieId);
        HashMap<Integer, Double> newTFIDFForg2 = calculateNewTFIDFforGenre(g1Ug2MovieIds, hmFourthTaskTFTagIdwithWeightForG2, hmForTagIdWithMovieId);
        Set<Integer> keysfromg1Ug2 = new HashSet<>();
        for(int key : newTFIDFForg1.keySet()){
            keysfromg1Ug2.add(key);
        }
        for(int key: newTFIDFForg2.keySet()){
            keysfromg1Ug2.add(key);
        }
        double euclidian_distance_squred=0.0;
        for(int key_G1_G2: keysfromg1Ug2){
            if(newTFIDFForg1.containsKey(key_G1_G2)&&newTFIDFForg2.containsKey(key_G1_G2)){
                euclidian_distance_squred += Math.pow((newTFIDFForg1.get(key_G1_G2) - newTFIDFForg2.get(key_G1_G2)), 2);
            }else if(newTFIDFForg1.containsKey(key_G1_G2)){
                euclidian_distance_squred += Math.pow(newTFIDFForg1.get(key_G1_G2), 2);
            }else if(newTFIDFForg2.containsKey(key_G1_G2)){
                euclidian_distance_squred += Math.pow(newTFIDFForg2.get(key_G1_G2), 2);
            }
        }
        return Math.sqrt(euclidian_distance_squred);
    }


    public static Set<Integer> getAllMoviesFromG1UG2(String genre1, String genre2, HashMap<String, List<Integer>> hmforGenreWithMovieId){
        List<Integer> g1Movies = hmforGenreWithMovieId.get(genre1);
        List<Integer> g2Movies = hmforGenreWithMovieId.get(genre2);
        Set<Integer> s1_g1 = new HashSet<>(g1Movies);
        Set<Integer> s2_g2 = new HashSet<>(g2Movies);
        s1_g1.addAll(s2_g2);
        return s1_g1;
    }

    public static int getG1UG2(String genre1, String genre2, HashMap<String, List<Integer>> hmforGenreWithMovieId){
        List<Integer> l1 =  hmforGenreWithMovieId.get(genre1);
        List<Integer> l2 =  hmforGenreWithMovieId.get(genre2);
        Set<Integer> s1 = new HashSet<>(l1);
        Set<Integer> s2 = new HashSet<>(l2);
        s1.addAll(s2);
        return s1.size();
    }

    public int getR1J(int key, List<Integer> listGenreWithMovieId, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId){

        Set<Integer> setofMovieIdforTagId = hmForTagIdWithMovieId.get(key);
        int count=0;
        for(int mv_id: setofMovieIdforTagId){
            if(listGenreWithMovieId.contains(mv_id)){
                count++;
            }
        }
        return count;
    }

    public int getM1JS2(int key, String genre1, String genre2, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId,HashMap<String, List<Integer>> hmforGenreWithMovieId){

        Set<Integer> setofMovieIdforTagId = hmForTagIdWithMovieId.get(key);
        Set<Integer> g1ug2 = getAllMoviesFromG1UG2(genre1,genre2,hmforGenreWithMovieId);
        int count=0;
        for(int mv_id: setofMovieIdforTagId){
            if(g1ug2.contains(mv_id)){
                count++;
            }
        }
        return count;
    }

    public HashMap<Integer, Double> calculateTask4subtask2(Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1,
                                       Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG2,
                                       HashMap<String, List<Integer>> hmforGenreWithMovieId,
                                       HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId,
                                       String genre1, String genre2 ){
        HashMap<Integer, Double> task4subtask2result = new HashMap<>();
        List<Integer> l1 =  hmforGenreWithMovieId.get(genre1);
        int R_value = hmforGenreWithMovieId.get(genre1).size();
        int M_value = getG1UG2(genre1,genre2, hmforGenreWithMovieId);
        Set<Integer> keySetforS2 = new HashSet<>(hmFourthTaskTFTagIdwithWeightForG1.keySet());
        for(int key_g2: hmFourthTaskTFTagIdwithWeightForG2.keySet()){
            keySetforS2.add(key_g2);
        }

        for(int key : keySetforS2){
            int r1j_value = getR1J(key, l1 , hmForTagIdWithMovieId);
            int m1j_value = getM1JS2(key, genre1, genre2, hmForTagIdWithMovieId, hmforGenreWithMovieId );
            double val1 = (double)  M_value-m1j_value-R_value + r1j_value;
            double val2 = (double) m1j_value-r1j_value;
            if(r1j_value==0||(val1==0)||(val2==0)||m1j_value==r1j_value||R_value==M_value||R_value==r1j_value){
                double n_1 = (double)(r1j_value+0.5);
                double n_2 = (double)R_value-r1j_value+0.5;
                double n11 = (double)n_1/n_2;
                double d_1 = (double)val2+0.5;
                double d_2 = (double)(val1+0.5);
                double d11 = (double)d_1/d_2;
                double v11 = (double)Math.log(n11/d11);
                double v2_1 = (double)(r1j_value+0.5)/(double)(R_value+1);
                double v2_2 = (double)(m1j_value-r1j_value+0.5)/(double)(M_value-R_value+1);
                double v22 = (double)Math.abs(v2_1-v2_2);
                double result_1 = (double)v11*v22;
                task4subtask2result.put(key, result_1);
            }else{
                double n1 = (((double)(r1j_value))/((double)(R_value-r1j_value)));
                double n2 = ((double)(m1j_value-r1j_value));
                double n3 = ((double) (M_value-m1j_value-R_value+r1j_value));
                double n7 = (n1/((n2)/(n3)));
                double n8 = Math.log(n7);
                double n4 =  (double)((double)r1j_value/(double)R_value);
                double n5 =  ((double)(m1j_value-r1j_value))/((double)(M_value-R_value));
                double n6 = Math.abs(n4-n5);

                double result = (double) n8*n6;

                task4subtask2result.put(key, result);
            }
        }
        return task4subtask2result;
    }

    public int getR1J_noTag(int key, List<Integer> listGenreWithMovieId, HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId){

        Set<Integer> setofMovieIdforTagId = hmForTagIdWithMovieId.get(key);
        int count =0;
        Set<Integer> setofGenrewithMov = new HashSet<>(listGenreWithMovieId);
        for(int mv_g2: setofGenrewithMov){
            if(!setofMovieIdforTagId.contains(mv_g2)){
                count++;
            }
        }
        return count;
    }

    public static int getG1UG2_noTag(String genre1, String genre2, HashMap<String, List<Integer>> hmforGenreWithMovieId, Set<Integer> listForTagIdWithMovieId){
        List<Integer> l1 =  hmforGenreWithMovieId.get(genre1);
        List<Integer> l2 =  hmforGenreWithMovieId.get(genre2);
        Set<Integer> s1 = new HashSet<>(l1);
        Set<Integer> s2 = new HashSet<>(l2);
        s1.addAll(s2);
        int count =0;
        for(int genre: s1){
            if(!listForTagIdWithMovieId.contains(genre)){
                count++;
            }
        }
        return count;
    }

    public HashMap<Integer, Double> calculateTask4subtask3(Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1,
                                                           Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG2,
                                                           HashMap<String, List<Integer>> hmforGenreWithMovieId,
                                                           HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId,
                                                           String genre1, String genre2 ){
        HashMap<Integer, Double> task4subtask3result = new HashMap<>();
        List<Integer> l2 = hmforGenreWithMovieId.get(genre2);
        int R_value = hmforGenreWithMovieId.get(genre2).size();
        int M_value = getG1UG2(genre1,genre2, hmforGenreWithMovieId);
        Set<Integer> keySetforS3 = new HashSet<>(hmFourthTaskTFTagIdwithWeightForG1.keySet());
        for(int key_g2: hmFourthTaskTFTagIdwithWeightForG2.keySet()){
            keySetforS3.add(key_g2);
        }

        for(int key : keySetforS3){
            int r1j_value = getR1J_noTag(key, l2 , hmForTagIdWithMovieId);
            int m1j_value = getG1UG2_noTag(genre1, genre2, hmforGenreWithMovieId, hmForTagIdWithMovieId.get(key) );
            double val1 = (double)  M_value-m1j_value-R_value + r1j_value;
            double val2 = (double) m1j_value-r1j_value;
            if(r1j_value==0||(val1==0)||(val2==0)||m1j_value==r1j_value||R_value==M_value||R_value==r1j_value){
                double n_1 = (double)(r1j_value+0.5);
                double n_2 = (double)R_value-r1j_value+0.5;
                double n11 = (double)n_1/n_2;
                double d_1 = (double)val2+0.5;
                double d_2 = (double)(val1+0.5);
                double d11 = (double)d_1/d_2;
                double v11 = (double)Math.log(n11/d11);
                double v2_1 = (double)(r1j_value+0.5)/(double)(R_value+1);
                double v2_2 = (double)(m1j_value-r1j_value+0.5)/(double)(M_value-R_value+1);
                double v22 = (double)Math.abs(v2_1-v2_2);
                double result_1 = (double)v11*v22;
                task4subtask3result.put(key, result_1);
            }else{
                double n1 = (((double)(r1j_value))/((double)(R_value-r1j_value)));
                double n2 = ((double)(m1j_value-r1j_value));
                double n3 = ((double) (M_value-m1j_value-R_value+r1j_value));
                double n7 = (n1/((n2)/(n3)));
                double n8 = Math.log(n7);
                double n4 =  (double)((double)r1j_value/(double)R_value);
                double n5 =  ((double)(m1j_value-r1j_value))/((double)(M_value-R_value));
                double n6 = Math.abs(n4-n5);

                double result = (double) n8*n6;

                task4subtask3result.put(key, result);
            }
        }
        return task4subtask3result;
    }

    public static void main(String[] args) throws ParseException, IOException {

        List<List<String>> movie_actor = readCSVToGet_movieactor(); // movie_actor table
        List<List<String>> mltags = readCSV("mltags.csv");
        List<List<String>> genome_tags = readCSV("genome-tags.csv");
        List<List<String>> mlratings = readCSV("mlratings.csv");
        List<List<String>> mlmovies = readCSVToGet_moviegenre();
        HashMap<Integer, List<MovieIdAndRank>> hmActorWithMovies = findActorWirhMovies(movie_actor); // Hashmap <actorId, <movieId, rank>>
        HashMap<String, List<Integer>> hmforGenreWithMovieId = getMoviesOfGivenGenre(mlmovies);
        HashMap<Integer, List<TagIdwithDate>> hmForMovieWithTagId = get_TagId_With_Dates_For_Movies(mltags);
        HashMap<Integer, List<TagIdwithDate>> hmUserIdwithTagId = get_TagId_With_Dates_For_Users(mltags);
        HashMap<Integer, Set<Integer>> hmForTagIdWithMovieId = getTagIdwithMovieId(mltags);
        HashMap<Integer, Set<Integer>> hmForMovieIdwithActorId = getMovieIdwithActorId(movie_actor);
        HashMap<Integer, Set<String>> hmForMovieIdwithGenres = getMovieIdwithGenres(mlmovies);
        HashMap<Integer, Set<Integer>> hmForTagIdwithUserId = getTagIdwithUserId(mltags);
        HashMap<Integer, Set<Integer>> hmForUserIdwithMovieId = getUserIDwithMovieId(mltags, mlratings );
        HashMap<Integer, Set<Integer>> hmForMovieIdWithUserId = getMovieIdWithUserId(mltags, mlratings);
        getTagIdwithTag(genome_tags);
        Scanner sc = new Scanner(System.in);
        String in ;
        while(true){
            System.out.println("Please enter command or exit to close> ");
            in = sc.nextLine();
            if(in.equals("exit")){
                System.out.println("Thank You! Bye!");
                System.exit(0);
            }
            String[] strArray = in.split(" ");
            if(strArray.length >= 3 && strArray.length <= 4){
                if(strArray[0].equals("print_actor_vector")) {
                    if (strArray[2].equals("TF")) {
                        int actorId = Integer.parseInt(strArray[1]);
                        Map<Integer, Double> hmFirstTaskResultTagIdwithWeight = firstTaskTF(actorId, hmActorWithMovies, hmForMovieWithTagId);
                        if(hmFirstTaskResultTagIdwithWeight==null){
                            System.out.println("Please enter valid actor id or actor record not present.");
                        }else{
                            printMap(hmFirstTaskResultTagIdwithWeight);
                        }
                    }else if (strArray[2].equals("TF-IDF")) {
                        int actorId = Integer.parseInt(strArray[1]);
                        Map<Integer, Double> hmFirstTaskResultTagIdwithWeight = firstTaskTF(actorId, hmActorWithMovies, hmForMovieWithTagId);
                        if(hmFirstTaskResultTagIdwithWeight==null){
                            System.out.println("Please enter valid actor id command or actor record not present.");
                        }else{
                            HashMap<Integer, Double> firstTaskTFIDFValue = firstTaskTFIDF(hmFirstTaskResultTagIdwithWeight, hmForTagIdWithMovieId, hmForMovieIdwithActorId);
                            Map<Integer, Double> firstTaskTFIDFValue_sorted = MapUtil.sortByValue(firstTaskTFIDFValue);
                            printMap(firstTaskTFIDFValue_sorted);
                        }
                    }else {
                        System.out.println("Please enter valid command.");
                    }
                }else if(strArray[0].equals("print_genre_vector")){
                    if (strArray[2].equals("TF")) {
                        String genre_val = strArray[1];
                        Map<Integer, Double> hmSecondTaskResultTagIdwithWeight = secondTaskTF(genre_val, hmForMovieWithTagId, hmforGenreWithMovieId);
                        if(hmSecondTaskResultTagIdwithWeight==null){
                            System.out.println("Please enter valid genre id or actor record not present.");
                        }else{
                            printMap(hmSecondTaskResultTagIdwithWeight);
                        }
                    }else if (strArray[2].equals("TF-IDF")) {
                        String genre_val = strArray[1];
                        Map<Integer, Double> hmSecondTaskResultTagIdwithWeight = secondTaskTF(genre_val, hmForMovieWithTagId, hmforGenreWithMovieId);
                        if(hmSecondTaskResultTagIdwithWeight==null){
                            System.out.println("Please enter valid genre or genre record not present.");
                        }else{
                            HashMap<Integer,Double> secondTaskTFIDFValues = secondTaskTFIDF(hmSecondTaskResultTagIdwithWeight, hmForTagIdWithMovieId, hmForMovieIdwithGenres);
                            Map<Integer,Double> secondTaskTFIDFValue_sorted = MapUtil.sortByValue(secondTaskTFIDFValues);
                            printMap(secondTaskTFIDFValue_sorted);
                        }
                    }else {
                        System.out.println("Please enter valid command.");
                    }
                }else if(strArray[0].equals("print_user_vector")){
                    if (strArray[2].equals("TF")) {
                        int u_id = Integer.parseInt(strArray[1]);
                        Map<Integer, Double> hmThirdTaskResultTagIdwithWeight = thirdTaskTF(u_id , hmForUserIdwithMovieId, hmForMovieWithTagId);
                        if(hmThirdTaskResultTagIdwithWeight==null){
                            System.out.println("Please enter valid user id or user id record not present.");
                        }else{
                            printMap(hmThirdTaskResultTagIdwithWeight);
                        }
                    }else if (strArray[2].equals("TF-IDF")) {
                        int u_id = Integer.parseInt(strArray[1]);
                        Map<Integer, Double> hmThirdTaskResultTagIdwithWeight = thirdTaskTF(u_id , hmForUserIdwithMovieId, hmForMovieWithTagId);
                        if(hmThirdTaskResultTagIdwithWeight==null){
                            System.out.println("Please enter valid user id or user id record not present.");
                        }else{
                            HashMap<Integer,Double> thirdTaskTFIDFValue = thirdTaskTFIDF(hmThirdTaskResultTagIdwithWeight, hmForTagIdWithMovieId, hmForMovieIdWithUserId );
                            Map<Integer,Double> thirdTaskTFIDFValue_sorted = MapUtil.sortByValue(thirdTaskTFIDFValue);
                            printMap(thirdTaskTFIDFValue_sorted);
                        }
                    }else {
                        System.out.println("Please enter valid command.");
                    }
                }else if(strArray[0].equals("differentiate_genre")){
                    String gen1 = strArray[1];
                    String gen2 = strArray[2];
                    if (strArray[3].equals("TF-IDF-DIFF")) {
                        Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1 = secondTaskTF(gen1, hmForMovieWithTagId, hmforGenreWithMovieId);
                        Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG2 = secondTaskTF(gen2, hmForMovieWithTagId, hmforGenreWithMovieId);
                        if(hmFourthTaskTFTagIdwithWeightForG1==null||hmFourthTaskTFTagIdwithWeightForG2==null){
                            System.out.println("Please enter valid genre or genre record not present.");
                        }else{
                           Set<Integer> g1Ug2MovieIds = getAllMoviesFromG1UG2(gen1, gen2, hmforGenreWithMovieId);
                           double result_Task4_s1 = fourthTaskTFIDFDIFF(hmFourthTaskTFTagIdwithWeightForG1, hmFourthTaskTFTagIdwithWeightForG2, hmForTagIdWithMovieId, g1Ug2MovieIds);
                           System.out.println("p-norm/Euclidean Distance: = " + result_Task4_s1);
                        }
                    }else if (strArray[3].equals("P-DIFF1")) {
                        Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1 = secondTaskTF(gen1, hmForMovieWithTagId, hmforGenreWithMovieId);
                        Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG2 = secondTaskTF(gen2, hmForMovieWithTagId, hmforGenreWithMovieId);
                        if(hmFourthTaskTFTagIdwithWeightForG1==null||hmFourthTaskTFTagIdwithWeightForG2==null){
                            System.out.println("Please enter valid genre or genre record not present.");
                        }else{
                            IFIDF ifidf = new IFIDF();
                            HashMap<Integer, Double> hmResultForSubtask2= ifidf.calculateTask4subtask2(hmFourthTaskTFTagIdwithWeightForG1, hmFourthTaskTFTagIdwithWeightForG2, hmforGenreWithMovieId, hmForTagIdWithMovieId, gen1, gen2);
                            Map<Integer,Double> hmResultForSubtask2_sorted = MapUtil.sortByValue(hmResultForSubtask2);
                            printMap(hmResultForSubtask2_sorted);
                        }
                    }else if (strArray[3].equals("P-DIFF2")) {
                        Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG1 = secondTaskTF(gen1, hmForMovieWithTagId, hmforGenreWithMovieId);
                        Map<Integer, Double> hmFourthTaskTFTagIdwithWeightForG2 = secondTaskTF(gen2, hmForMovieWithTagId, hmforGenreWithMovieId);
                        if(hmFourthTaskTFTagIdwithWeightForG1==null||hmFourthTaskTFTagIdwithWeightForG2==null){
                            System.out.println("Please enter valid genre or genre record not present.");
                        }else{
                            IFIDF ifidf = new IFIDF();
                            HashMap<Integer, Double> hmResultForSubtask3= ifidf.calculateTask4subtask3(hmFourthTaskTFTagIdwithWeightForG1, hmFourthTaskTFTagIdwithWeightForG2, hmforGenreWithMovieId, hmForTagIdWithMovieId, gen1, gen2);
                            Map<Integer,Double> hmResultForSubtask3_sorted = MapUtil.sortByValue(hmResultForSubtask3);
                            printMap(hmResultForSubtask3_sorted);

                        }
                    }else {
                        System.out.println("Please enter valid command.");
                    }
                }else{
                    System.out.println("Please enter valid command.");
                }
            }else{
                System.out.println("Please enter valid command.");
            }
        }
    }
}

