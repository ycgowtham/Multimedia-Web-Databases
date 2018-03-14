package factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

public class PDiffCalculator
{
    private HashMap<Integer, Integer> v1;
    private HashMap<Integer, Integer> v2;
    private HashMap<Integer, String> tagGenome;
    private Boolean isPDiff2;
    
    public PDiffCalculator(HashMap<Integer, Integer> v1, HashMap<Integer, Integer> v2, HashMap<Integer, String> tagGenome, Boolean isPDiff2)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.tagGenome = tagGenome;
        this.isPDiff2 = isPDiff2;
    }
    
    public ArrayList<TaskVector> computeWeights(Integer R, Integer M, Set<Integer> allTags)
    {
        ArrayList<TaskVector> weights = new ArrayList<TaskVector>();
        
        allTags.forEach(new Consumer<Integer> () {

            @Override
            public void accept(Integer tag)
            {
                Integer r = v1.containsKey(tag) ? v1.get(tag) : 0;
                Integer m = v2.containsKey(tag) ? v2.get(tag) : 0;
                
                if (isPDiff2.booleanValue())
                {
                    r = R - r;
                    m = M - m;
                }
                
                weights.add(new TaskVector(tagGenome.get(tag), computeWeight(r, m, R, M), tag));
            }
            
        });
        
        return weights;
    }
    
    private Double computeWeight(Integer r, Integer m, Integer R, Integer M)
    {
        Double numeratorOffset = 0.0d;
        Double denomenatorOffset = 0.0d;
        
        if (r.equals(0) || (m - r) == 0 || m.equals(r) || (M - m - R + r) == 0 || R.equals(r) || M.equals(R))
        {
            numeratorOffset = 0.5;
            denomenatorOffset = 1.0d;
        }
        
        Double result = Math.log(((r.doubleValue() + numeratorOffset.doubleValue()) / (R.doubleValue() - r.doubleValue() + numeratorOffset.doubleValue()))
                / ((m.doubleValue() - r.doubleValue() + numeratorOffset.doubleValue()) 
                        / (M.doubleValue() - m.doubleValue() - R.doubleValue() + r.doubleValue() + numeratorOffset.doubleValue())))
            * Math.abs(((r.doubleValue() + numeratorOffset.doubleValue()) / (R.doubleValue() + denomenatorOffset.doubleValue()) 
                    - ((m.doubleValue() - r.doubleValue() + numeratorOffset.doubleValue()) / (M.doubleValue() - R.doubleValue() + denomenatorOffset.doubleValue()))));
        
        
        if (result.isInfinite() || result.isNaN())
        {
            result = approximate(r, m, R, M);
        }
        
        return result;
    }
    
    private Double approximate(Integer r, Integer m, Integer R, Integer M)
    {
        Double p_fk_given_R = r.doubleValue() + ((m.doubleValue() / M.doubleValue())/(M.doubleValue() + 1.0d));
        Double p_fk_given_I = (m.doubleValue() - r.doubleValue()) + ((m.doubleValue() / M.doubleValue())/((M.doubleValue() - R.doubleValue()) + 1.0d));
        
        Double one_minus_p_fk_given_R = (1.0d - p_fk_given_R);
        one_minus_p_fk_given_R = one_minus_p_fk_given_R.isNaN() ? 1 : one_minus_p_fk_given_R;
        one_minus_p_fk_given_R = one_minus_p_fk_given_R.doubleValue() < 0.0d ? 1E-10d : one_minus_p_fk_given_R;
        
        Double one_minus_p_Fk_given_I = (1.0d - p_fk_given_I);
        one_minus_p_Fk_given_I = one_minus_p_Fk_given_I.isNaN() ? 1 : one_minus_p_Fk_given_I;
        one_minus_p_Fk_given_I = one_minus_p_Fk_given_I.doubleValue() < 0.0d ? 1E-10d : one_minus_p_Fk_given_I;
        
        Double p_fk_given_R_times_one_minus_P_fk_given_I = (p_fk_given_R * one_minus_p_Fk_given_I);
        Double p_fk_given_I_times_one_minus_p_fk_given_R = (p_fk_given_I * one_minus_p_fk_given_R);
        
        Double term1_Before_log = p_fk_given_R_times_one_minus_P_fk_given_I / p_fk_given_I_times_one_minus_p_fk_given_R;
        
        Double term1 = Math.log(term1_Before_log);
        Double term2 = Math.abs(p_fk_given_R - p_fk_given_I);
        
        Double result = term1 * term2;
        
        return result;
    }
}
