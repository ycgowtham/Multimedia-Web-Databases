package factory;


public class TaskVector
{
    private String tag;
    public Integer tagid;
    public Double weight;
    
    public TaskVector(String tag, Double weight)
    {
        this.tag = tag;
        this.weight = weight;
    }
    
    public TaskVector(String tag, Double weight, Integer tagid)
    {
        this(tag, weight);
        this.tagid = tagid;
    }
    
    public String toString()
    {
        return tag;
    }
}
