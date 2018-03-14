package facade;


public class Task1Measurements
{
    public Integer tagid, tagFrequency = null;
    public Double weightedRank, weightedTimestamp;
 
    public Task1Measurements(Integer tagid, Double weightedRank, Double weightedTimestamp)
    {
        this.tagid = tagid;
        this.weightedRank = weightedRank;
        this.weightedTimestamp = weightedTimestamp;
    }
}
