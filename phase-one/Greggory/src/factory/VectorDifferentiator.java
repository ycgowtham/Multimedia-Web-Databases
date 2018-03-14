package factory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;

public class VectorDifferentiator
{
    private ArrayList<TaskVector> v1, v2;
    private boolean isDebug;
    
    public VectorDifferentiator(ArrayList<TaskVector> v1, ArrayList<TaskVector> v2, boolean isDebug)
    {
        sortVectorByTag(v1);
        sortVectorByTag(v2);
        
        this.v1 = v1;
        this.v2 = v2;        
        this.isDebug = isDebug;
    }
    
    public VectorDifferentiator(ArrayList<TaskVector> v1, ArrayList<TaskVector> v2)
    {
        this(v1, v2, false);
    }
    
    public ArrayList<TaskVector> simpleDiff()
    {
        ArrayList<TaskVector> diffVector = differentiate(SimpleDiff.class);
        
        return diffVector;
    }
    
    public Double differentiateInfinityNorm()
    {
        ArrayList<TaskVector> diffVector = differentiate(InfinityNorm.class);
        
        printDebugOutput(diffVector);
        
        return new Max(diffVector).aggregate();
    }
    
    public Double differentiatePNorm(Integer Lvalue)
    {
        ArrayList<TaskVector> diffVector = differentiate(PNorm.class, Lvalue);
        
        printDebugOutput(diffVector);
        
        return Math.pow(new Sum(diffVector).aggregate(), (1.0d / Lvalue.doubleValue()));
    }
    
    public Double differentiateDotProduct()
    {
        ArrayList<TaskVector> diffVector = differentiate(DotProduct.class);
        
        printDebugOutput(diffVector);
        
        return new Sum(diffVector).aggregate();
    }
    
    public Double differentiateCosine()
    {
        ArrayList<TaskVector> diffVector = differentiate(DotProduct.class);
        
        printDebugOutput(diffVector);
        
        Double dotProduct = new Sum(diffVector).aggregate();

        Double v1Magnitude = new Magnitude(v1).aggregate();
        Double v2Magnitude = new Magnitude(v2).aggregate();
        
        return dotProduct / (v1Magnitude * v2Magnitude);
    }
    
    public Double differentiateAngle()
    {
        Double cosine = differentiateCosine();
        
        return Math.acos(cosine) * (180.0d/Math.PI);
    }
    
    private void printDebugOutput(ArrayList<TaskVector> weights)
    {
        if (!isDebug)
        {
            return;
        }
        
        weights.sort(new Comparator<TaskVector>() {

            @Override
            public int compare(TaskVector o1, TaskVector o2)
            {
                // -1 = less than; 1 = greater than; 0 = equals
                // reversed
                return o1.weight > o2.weight ? -1 : (o1.weight < o2.weight ? 1 : 0);
            }

        });
        
        for (TaskVector v : weights)
        {
            System.out.println("<" + v.toString() + ", " + v.weight.toString() + ">");
        }
        
        System.out.println("Total tags: " + weights.size());
    }
    
    private ArrayList<TaskVector> differentiate(Class<?> diffFunc, Object...args)
    {
        ArrayList<TaskVector> diff = new ArrayList<TaskVector>();
        
        Iterator<TaskVector> v1Iterator = v1.iterator();
        Iterator<TaskVector> v2Iterator = v2.iterator();
        
        TaskVector v1v = null;
        TaskVector v2v = null;
        
        while(v1Iterator.hasNext() || v2Iterator.hasNext())
        {
            if(v1v == null && v1Iterator.hasNext())
            {
                v1v = v1Iterator.next();
            }
            
            if(v2v == null && v2Iterator.hasNext())
            {
                v2v = v2Iterator.next();
            }
            
            if (v1v == null && v2v != null)
            {
                diff.add(new TaskVector(v2v.toString(), createFunction(diffFunc, 0.0d, args).apply(v2v.weight), v2v.tagid));
                v2v = null;
            }
            else if (v2v == null && v1v != null)
            {
                diff.add(new TaskVector(v1v.toString(), createFunction(diffFunc, v1v.weight, args).apply(0.0d), v1v.tagid));
                v1v = null;
            }
            else if (v1v != null && v2v != null)
            {
                if (v1v.tagid.equals(v2v.tagid))
                {
                    diff.add(new TaskVector(v1v.toString(), createFunction(diffFunc, v1v.weight, args).apply(v2v.weight), v1v.tagid));
                    v1v = null;
                    v2v = null;
                }
                else if (v1v.tagid.intValue() < v2v.tagid.intValue())
                {
                    diff.add(new TaskVector(v1v.toString(), createFunction(diffFunc, v1v.weight, args).apply(0.0d), v1v.tagid));
                    v1v = null;
                }
                else
                {
                    diff.add(new TaskVector(v2v.toString(), createFunction(diffFunc, 0.0d, args).apply(v2v.weight), v2v.tagid));
                    v2v = null;
                }
            }
        }
        
        return diff;
    }
    
    private DifferentiationFunction createFunction(Class<?> diffFunc, Object... args)
    {
        if (diffFunc.equals(InfinityNorm.class))
        {
            return new InfinityNorm((Double) args[0]);
        }
        else if (diffFunc.equals(PNorm.class))
        {
            return new PNorm((Double) args[0], (Integer) ((Object []) args[1])[0]);
        }
        else if (diffFunc.equals(DotProduct.class))
        {
            return new DotProduct((Double) args[0]);
        }
        else if (diffFunc.equals(SimpleDiff.class))
        {
            return new SimpleDiff((Double) args[0]);
        }
        
        return null;
    }
    
    private abstract class DifferentiationFunction implements Function<Double, Double>
    {
        Double weight1;
        
        public DifferentiationFunction(Double weight1)
        {
            this.weight1 = weight1;
        }
    }
    
    private class InfinityNorm extends DifferentiationFunction
    {
        public InfinityNorm(Double weight1)
        {
            super(weight1);
        }

        @Override
        public Double apply(Double weight2)
        {
            return Math.abs(weight1 - weight2);
        }
    }
    
    private class PNorm extends DifferentiationFunction
    {
        private Integer Lvalue;
        
        public PNorm(Double weight1, Integer Lvalue)
        {
            super(weight1);
            this.Lvalue = Lvalue;
        }
        
        @Override
        public Double apply(Double weight2)
        {
            return Math.pow(Math.abs(weight1 - weight2), Lvalue.doubleValue());
        }
    }
    
    private class DotProduct extends DifferentiationFunction
    {
        public DotProduct(Double weight1)
        {
            super(weight1);
        }

        @Override
        public Double apply(Double weight2)
        {
            return weight1 * weight2;
        }
    }
    
    private class SimpleDiff extends DifferentiationFunction
    {

        public SimpleDiff(Double weight1)
        {
            super(weight1);
        }

        @Override
        public Double apply(Double weight2)
        {
            return weight1 - weight2;
        }
    }
    
    private abstract class Aggregator
    {
        protected ArrayList<TaskVector> weights;
        
        public Aggregator(ArrayList<TaskVector> weights)
        {
            this.weights = weights;
        }
        
        public abstract Double aggregate();
    }
    
    private class Sum extends Aggregator
    {
        public Sum(ArrayList<TaskVector> weights)
        {
            super(weights);
        }

        public Double aggregate()
        {
            Double sum = 0.0d;
            
            for (TaskVector v : weights)
            {
                sum += v.weight;
            }
            
            return sum;
        }
    }
    
    private class Max extends Aggregator
    {
        public Max(ArrayList<TaskVector> weights)
        {
            super(weights);
        }

        @Override
        public Double aggregate()
        {
            Double max = 0.0d;
            
            for (TaskVector v : weights)
            {
                max = Math.max(max, v.weight);
            }
            
            return max;
        }
    }
    
    private class Magnitude extends Aggregator
    {

        public Magnitude(ArrayList<TaskVector> weights)
        {
            super(weights);
        }

        @Override
        public Double aggregate()
        {
            Double magnitude = 0.0d;
            
            for (TaskVector v : weights)
            {
                magnitude += Math.pow(v.weight, 2.0d);
            }
            
            magnitude = Math.pow(magnitude, 0.5d);
            
            return magnitude;
        }
        
    }
    
    private void sortVectorByTag(ArrayList<TaskVector> v)
    {
        v.sort(new Comparator<TaskVector>() {

            @Override
            public int compare(TaskVector o1, TaskVector o2)
            {
                // -1 = less than; 1 = greater than; 0 = equals
                return o1.tagid < o2.tagid ? -1 : (o1.tagid > o2.tagid ? 1 : 0);
            }
            
        });
    }
}
