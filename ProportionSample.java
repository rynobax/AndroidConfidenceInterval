package statistics.p4combined;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * Created by Ryan on 4/2/2016.
 */
public class ProportionSample implements Sample{
    private ArrayList<Boolean> values;
    private double sampleMean;
    private double sampleStdDev;
    private double populationMean;
    private double populationStdDev;
    private int n;
    private int successCount;
    private int failCount;

    /*
    Generates normally distributed numbers between 0-1 with a  provided mean (pi)
    */
    public ProportionSample(double pi, int n){
        populationMean = pi;
        this.n = n;
        values = new ArrayList<>(0);
        for(int j=0;j<n;j++){
            Boolean success = false;
            if(Math.random() < pi) success = true;
            values.add(success);
        }

        int success = 0;
        double sumSq = 0.0;
        for(Boolean value : values){
            if(value == true){
                ++success;
                sumSq = sumSq + 1.0;
            }
        }
        successCount = success;
        failCount = n - successCount;
        sampleMean = (double)success / (double)n;
        if (n > 1) {
            sampleStdDev = Math.pow((n * sumSq - Math.pow(sumSq,2)) / (n*(n-1)), 0.5);
        }else{
            sampleStdDev = 0.0;
        }
    }

    public double[] calculateCI(double confLevel){
        // function that returns confidence interval (left, midpoint, right) for interval
        // From http://en.wikipedia.org/wiki/Binomial_proportion_confidence_interval
        // Section: "Normal approximation interval"
        // Wald

        double phat = sampleMean;
        double alpha = 1.0 - confLevel / 100.0;
        double z = Formulas.inverseNorm(1.0 - alpha/2);
        Log.v("LOG", "z: " + z);
        double[] results = new double[3];
        double halfWidth = z * Math.sqrt(phat * (1.0-phat) / (double)n);
        results[0] = phat - halfWidth;
        results[1] = phat;
        results[2] = phat + halfWidth;
        return results;
    }

    public ArrayList<Boolean> getValues() {
        return values;
    }

    @Override
    public double getSampleMean() {
        return sampleMean;
    }

    @Override
    public double getSampleStdDev() {
        return sampleStdDev;
    }

    @Override
    public double getPopulationMean() {
        return populationMean;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    @Override
    public double getPopulationStdDev() {
        return populationStdDev;
    }

    @Override
    public SamplingDistribution getSamplingDistribution() {
        return SamplingDistribution.BINOMIAL;
    }
}
