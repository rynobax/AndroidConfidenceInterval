package statistics.p4combined;

/**
 * Created by Ryan on 4/3/2016.
 */
public interface Sample {
    double[] calculateCI(double confLevel);
    double getSampleMean();
    double getSampleStdDev();
    double getPopulationMean();
    double getPopulationStdDev();
    SamplingDistribution getSamplingDistribution();
}
