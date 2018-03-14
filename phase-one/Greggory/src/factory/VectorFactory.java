package factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

import facade.DBFacade;
import facade.QueryHandler;
import facade.QuerySplitter;
import facade.Task1ActorsWithTagFacade;
import facade.Task1Measurements;
import facade.Task1TagFrequencyFacade;
import facade.Task1TagGenomeFacade;
import facade.Task1TagsByMovieActorFacade;
import facade.Task1TotalTagsFacade;
import facade.Task2GenreTagsFacade;
import facade.Task2TagsByGenreFacade;
import facade.Task3TagsByUserFacade;
import facade.Task3UsersPerTagFacade;
import facade.Task4MoviesPerTagFacade;
import facade.Task4TotalMoviesForGenreFacade;

public class VectorFactory
{

    private Connection connection;
    private boolean isDebug;

    public VectorFactory(boolean isDebug) throws SQLException
    {
        Properties props = new Properties();
        props.put("user", "greggoryscherer");

        this.isDebug = isDebug;

        connection = DriverManager.getConnection("jdbc:postgresql:greggoryscherer", props);
    }

    public VectorFactory() throws SQLException
    {
        this(false);
    }

    private Function<Double, Double> getIDFFunction(Double idf)
    {
        return new Function<Double, Double>() {

            @Override
            public Double apply(Double t)
            {
                return t * idf;
            }

        };
    }

    @SuppressWarnings("unchecked")
    public ArrayList<TaskVector> buildTask1Vector(Integer actorId, String model)
            throws SQLException, InterruptedException
    {
        Task1TagsByMovieActorFacade ma = new Task1TagsByMovieActorFacade(connection, actorId);
        Task1TagGenomeFacade tg = new Task1TagGenomeFacade(connection);

        ma.join();

        HashMap<Integer, ArrayList<Integer>> tagMovies = new HashMap<Integer, ArrayList<Integer>>();
        HashMap<Integer, Task1Measurements> tagMeasurements = new HashMap<Integer, Task1Measurements>();

        ArrayList<Integer> distinctMovies = new ArrayList<Integer>();
        ArrayList<Integer> distinctTags = new ArrayList<Integer>();

        if (!ma.hasNext())
        {
            return null;
        }

        for (Object mapping : ma)
        {
            Integer tagId = (Integer) ((ArrayList<Object>) mapping).get(2);
            Double weightedRank = (Double) ((ArrayList<Object>) mapping).get(1);
            Integer movieId = (Integer) ((ArrayList<Object>) mapping).get(0);
            Double weightedTimestamp = (Double) ((ArrayList<Object>) mapping).get(3);

            if (tagMovies.containsKey(tagId))
            {
                ArrayList<Integer> movies = tagMovies.get(tagId);

                if (!movies.contains(movieId))
                {
                    movies.add(movieId);
                }
            }
            else
            {
                ArrayList<Integer> movies = new ArrayList<Integer>();
                movies.add(movieId);

                tagMovies.put(tagId, movies);
            }

            if (!distinctMovies.contains(movieId))
            {
                distinctMovies.add(movieId);
            }

            if (!distinctTags.contains(tagId))
            {
                distinctTags.add(tagId);
            }

            if (!tagMeasurements.containsKey(tagId))
            {
                tagMeasurements.put(tagId, new Task1Measurements(tagId, weightedRank, weightedTimestamp));
            }
        }

        ArrayList<Task1TagFrequencyFacade> tfs = new ArrayList<Task1TagFrequencyFacade>();
        Task1TotalTagsFacade tt = new Task1TotalTagsFacade(connection, distinctMovies);

        tagMovies.forEach(new BiConsumer<Integer, ArrayList<Integer>>() {

            @Override
            public void accept(Integer tagid, ArrayList<Integer> movieids)
            {
                try
                {
                    tfs.add(new Task1TagFrequencyFacade(connection, tagid, movieids));
                }
                catch (SQLException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });

        tt.join();

        Double totalTags = 0.0d;

        for (Integer key : tagMeasurements.keySet())
        {
            Task1Measurements tm = tagMeasurements.get(key);

            totalTags += tm.weightedRank * tm.weightedTimestamp;
        }

        for (Task1TagFrequencyFacade tf : tfs)
        {
            tf.join();

            for (Object row : tf)
            {
                Integer tagid = (Integer) ((ArrayList<Object>) row).get(0);
                Integer tagFrequency = (Integer) ((ArrayList<Object>) row).get(1);

                tagMeasurements.get(tagid).tagFrequency = tagFrequency;
            }
        }

        ArrayList<TaskVector> t1v = new ArrayList<TaskVector>();

        HashMap<Integer, Integer> tagActorCount = null;

        tagActorCount = getActorsPerTag(distinctTags);

        tg.join();

        for (Object row : tg)
        {
            Integer tagId = (Integer) ((ArrayList<Object>) row).get(0);

            if (tagMeasurements.containsKey(tagId))
            {
                String tag = (String) ((ArrayList<Object>) row).get(1);

                Task1Measurements t1m = tagMeasurements.get(tagId);
                Double weight = 0.0;

                if (t1m.tagFrequency != null)
                {
                    Double tf = (t1m.weightedRank * t1m.weightedTimestamp) / totalTags;

                    if (model.toLowerCase().equals("tf"))
                    {
                        weight = getIDFFunction(1.0d).apply(tf);
                    }
                    else
                    {
                        final Double totalActors = 27279d;
                        final Double idf = Math.log(totalActors / (1.0d * tagActorCount.get(tagId)));
                        weight = getIDFFunction(idf).apply(tf);
                    }
                }

                t1v.add(new TaskVector(tag, weight));
            }
        }

        return sortVectorByWeight(t1v);
    }

    private ArrayList<TaskVector> sortVectorByWeight(ArrayList<TaskVector> v)
    {
        return sortVectorByWeight(v, new Function<Double, Double>() {

            @Override
            public Double apply(Double t)
            {
                return t; // no op
            }

        });
    }

    private ArrayList<TaskVector> sortVectorByWeight(ArrayList<TaskVector> v, Function<Double, Double> f)
    {
        v.sort(new Comparator<TaskVector>() {

            @Override
            public int compare(TaskVector o1, TaskVector o2)
            {
                // -1 = less than; 1 = greater than; 0 = equals
                // reversed
                Double weight1 = f.apply(o1.weight);
                Double weight2 = f.apply(o2.weight);

                return weight1.doubleValue() > weight2.doubleValue() ? -1
                        : (weight1.doubleValue() < weight2.doubleValue() ? 1 : 0);
            }

        });

        return v;
    }

    @SuppressWarnings("unchecked")
    private HashMap<Integer, Integer> getActorsPerTag(ArrayList<Integer> distinctTags)
            throws SQLException, InterruptedException
    {
        Task1ActorsWithTagFacade at = new Task1ActorsWithTagFacade(connection, distinctTags);
        at.join();

        HashMap<Integer, Integer> tagActorCount = new HashMap<Integer, Integer>();

        for (Object row : at)
        {
            Integer tagid = (Integer) ((ArrayList<Object>) row).get(0);
            Integer actorCount = (Integer) ((ArrayList<Object>) row).get(1);
            tagActorCount.put(tagid, actorCount);
        }

        return tagActorCount;
    }

    public ArrayList<TaskVector> buildTask2Vector(String genre, String model) throws SQLException, InterruptedException
    {
        Task2TagsByGenreFacade tbg = new Task2TagsByGenreFacade(connection, genre);

        return buildTask23Vector(tbg, new CountFunction() {

            @Override
            public HashMap<Integer, Integer> getCountForTags(ArrayList<Integer> tagids)
                    throws SQLException, InterruptedException
            {
                return getXPerTag(new Task2GenreTagsFacade(connection, tagids));
            }

        }, model, 18);
    }

    public ArrayList<TaskVector> buildTask3Vector(Integer userid, String model)
            throws InterruptedException, SQLException
    {
        Task3TagsByUserFacade tbu = new Task3TagsByUserFacade(connection, userid);

        return buildTask23Vector(tbu, new CountFunction() {

            @Override
            public HashMap<Integer, Integer> getCountForTags(ArrayList<Integer> tagids)
                    throws SQLException, InterruptedException
            {
                return getXPerTag(new QuerySplitter<Integer>(Task3UsersPerTagFacade.class, connection, tagids, 80));
            }

        }, model, 30001);
    }

    public ArrayList<TaskVector> buildTask4TF_IDF_DIFFVector(String genre1, String genre2, Object... args)
            throws Exception
    {
        Task2TagsByGenreFacade tbg1 = new Task2TagsByGenreFacade(connection, genre1);
        Task2TagsByGenreFacade tbg2 = new Task2TagsByGenreFacade(connection, genre2);

        ArrayList<String> genres = new ArrayList<String>();
        genres.add(genre1);
        genres.add(genre2);

        Task4MoviesPerTagFacade tpm = new Task4MoviesPerTagFacade(connection, genres);
        tpm.join();

        ArrayList<Object> rows = new ArrayList<Object>();
        for (Object row : tpm)
        {
            rows.add(row);
        }

        ArrayList<TaskVector> v1 = buildTask23Vector(tbg1, new CountFunction() {

            @Override
            public HashMap<Integer, Integer> getCountForTags(ArrayList<Integer> tagids)
                    throws SQLException, InterruptedException
            {
                return getXPerTag(rows);
            }

        }, "tfidf", tpm.getTotal());

        if (v1 == null)
        {
            return null;
        }

        ArrayList<TaskVector> v2 = buildTask23Vector(tbg2, new CountFunction() {

            @Override
            public HashMap<Integer, Integer> getCountForTags(ArrayList<Integer> tagids)
                    throws SQLException, InterruptedException
            {
                return getXPerTag(rows);
            }
        }, "tfidf", tpm.getTotal());

        if (v2 == null)
        {
            return null;
        }

        VectorDifferentiator vd = new VectorDifferentiator(v1, v2, isDebug);

        ArrayList<TaskVector> out = sortVectorByWeight(vd.simpleDiff());

        Object[] arg = { "angle" };

        out.add(0, new TaskVector("Angle", differentiateVectors(v1, v2, arg)));

        arg[0] = "cos";

        out.add(0, new TaskVector("Cosine", differentiateVectors(v1, v2, arg)));

        arg[0] = "dot";

        out.add(0, new TaskVector("Dot-Product", differentiateVectors(v1, v2, arg)));

        arg[0] = Integer.MAX_VALUE;

        out.add(0, new TaskVector("Infinity-Norm", differentiateVectors(v1, v2, arg)));

        arg[0] = (Integer) 3;

        out.add(0, new TaskVector("3-Norm", differentiateVectors(v1, v2, arg)));

        arg[0] = (Integer) 2;

        out.add(0, new TaskVector("2-Norm", differentiateVectors(v1, v2, arg)));

        arg[0] = (Integer) 1;

        out.add(0, new TaskVector("1-Norm", differentiateVectors(v1, v2, arg)));

        return out;
    }

    public ArrayList<TaskVector> buildTask4PDIFFVector(String genre1, String genre2, Object... args) throws Exception
    {
        ArrayList<String> _genre1 = new ArrayList<String>();
        _genre1.add(genre1);

        ArrayList<String> _genre2 = new ArrayList<String>();
        _genre2.add(genre2);

        ArrayList<String> bothGenres = new ArrayList<String>();
        bothGenres.add(genre1);
        bothGenres.add(genre2);

        Task4MoviesPerTagFacade mpt1, mpt2;

        Boolean isPDiff2 = (Boolean) args[1];

        if (isPDiff2.booleanValue())
        {
            mpt1 = new Task4MoviesPerTagFacade(connection, _genre2);
            mpt2 = new Task4MoviesPerTagFacade(connection, bothGenres);
        }
        else
        {
            mpt1 = new Task4MoviesPerTagFacade(connection, _genre1);
            mpt2 = new Task4MoviesPerTagFacade(connection, bothGenres);
        }

        Task4TotalMoviesForGenreFacade tm = new Task4TotalMoviesForGenreFacade(connection, bothGenres);
        Task1TagGenomeFacade tg = new Task1TagGenomeFacade(connection);

        HashMap<Integer, Integer> moviesPerGenre1 = getXPerTag(mpt1);
        HashMap<Integer, Integer> moviesPerGenre2 = getXPerTag(mpt2);

        tg.join();
        HashMap<Integer, String> tagGenome = getTagGenomeMap(tg);

        TreeSet<Integer> allTags = new TreeSet<Integer>();

        moviesPerGenre1.forEach(new BiConsumer<Integer, Integer>() {

            @Override
            public void accept(Integer key, Integer value)
            {
                allTags.add(key);
            }

        });

        moviesPerGenre2.forEach(new BiConsumer<Integer, Integer>() {

            @Override
            public void accept(Integer key, Integer value)
            {
                allTags.add(key);
            }

        });

        tm.join();

        PDiffCalculator pdiff = new PDiffCalculator(moviesPerGenre1, moviesPerGenre2, tagGenome, isPDiff2);
        ArrayList<TaskVector> v1 = pdiff.computeWeights(mpt1.getTotal(), tm.getTotal(), allTags);

        return sortVectorByWeight(v1);
    }

    private Double differentiateVectors(ArrayList<TaskVector> v1, ArrayList<TaskVector> v2, Object[] args)
    {
        VectorDifferentiator vecDiff = new VectorDifferentiator(v1, v2, isDebug);

        if (args.length == 0)
        {
            return null;
        }
        else if (args.length >= 1)
        {
            if (args[0] instanceof Integer)
            {
                Integer Lvalue = (Integer) args[0];
                if (Lvalue == Integer.MAX_VALUE)
                {
                    return vecDiff.differentiateInfinityNorm();
                }
                else
                {
                    return vecDiff.differentiatePNorm(Lvalue);
                }
            }
            else if (args[0] instanceof String)
            {
                if (((String) args[0]).toLowerCase().equals("dot"))
                {
                    return vecDiff.differentiateDotProduct();
                }
                else if (((String) args[0]).toLowerCase().equals("cos"))
                {
                    return vecDiff.differentiateCosine();
                }
                else if (((String) args[0]).toLowerCase().equals("angle"))
                {
                    return vecDiff.differentiateAngle();
                }
            }
            else
            {
                return null;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<TaskVector> buildTask23Vector(DBFacade tagsByDocument, CountFunction documentCountForTag,
            String model, Integer idfTotal) throws InterruptedException, SQLException
    {
        ArrayList<TaskVector> tvs = new ArrayList<TaskVector>();

        Task1TagGenomeFacade tg = new Task1TagGenomeFacade(connection);

        tagsByDocument.join();

        if (!tagsByDocument.hasNext())
        {
            return null;
        }

        tg.join();

        HashMap<Integer, String> tgm = getTagGenomeMap(tg);

        Double totalTags = 0.0d;

        ArrayList<Integer> distinctTags = new ArrayList<Integer>();
        for (Object row : tagsByDocument)
        {
            ArrayList<Object> _row = (ArrayList<Object>) row;
            Integer tagid = (Integer) _row.get(0);
            Double time_weight = (Double) _row.get(1);

            if (tgm.containsKey(tagid))
            {
                tvs.add(new TaskVector(tgm.get(tagid), time_weight, tagid));

                totalTags += time_weight;

                if (!distinctTags.contains(tagid) && !model.toLowerCase().equals("tf"))
                {
                    distinctTags.add(tagid);
                }
            }
        }

        HashMap<Integer, Integer> cpt = null;

        if (!model.toLowerCase().equals("tf"))
        {
            cpt = documentCountForTag.getCountForTags(distinctTags);
        }

        for (TaskVector tv : tvs)
        {
            final Double tf = tv.weight / totalTags;
            Double idf = 1.0d;

            if (cpt != null)
            {
                if (!cpt.containsKey(tv.tagid))
                {
                    return null;
                }
                idf = Math.log(idfTotal / (cpt.get(tv.tagid) * 1.0d));
            }

            tv.weight = getIDFFunction(idf).apply(tf); // tf
        }

        return sortVectorByWeight(tvs);
    }

    private HashMap<Integer, Integer> getXPerTag(Iterable<Object> queryResult)
    {
        HashMap<Integer, Integer> xpt = new HashMap<Integer, Integer>();

        for (Object _row : queryResult)
        {
            @SuppressWarnings("unchecked")
            ArrayList<Object> row = (ArrayList<Object>) _row;

            Integer tagid = (Integer) row.get(0);
            Integer x = (Integer) row.get(1);

            xpt.put(tagid, x);
        }

        return xpt;
    }

    private HashMap<Integer, Integer> getXPerTag(QueryHandler query) throws InterruptedException
    {
        query.join();

        return getXPerTag((Iterable<Object>) query);
    }

    @SuppressWarnings("unchecked")
    public HashMap<Integer, String> getTagGenomeMap(Task1TagGenomeFacade tg)
    {
        HashMap<Integer, String> tgm = new HashMap<Integer, String>();

        for (Object row : tg)
        {
            Integer tagid = (Integer) ((ArrayList<Object>) row).get(0);
            String tag = (String) ((ArrayList<Object>) row).get(1);

            if (!tgm.containsKey(tagid))
            {
                tgm.put(tagid, tag);
            }
        }

        return tgm;
    }

    private Function<Double, Double> absoluteValue = new Function<Double, Double>() {

        @Override
        public Double apply(Double t)
        {
            return Math.abs(t.doubleValue());
        }

    };
}
