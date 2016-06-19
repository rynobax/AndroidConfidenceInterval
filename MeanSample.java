package statistics.p4combined;

import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Ryan on 4/7/2016.
 */
public class MeanSample implements Sample {
    private ArrayList<BigDecimal> values;
    private double sampleMean;
    private double sampleStddev;
    private double populationMean;
    private double populationStdDev;
    private double sampleGraphMin;
    private double sampleGraphMax;
    private SamplingDistribution samplingDistribution;
    private int n;

    /*
    Generates normally distributed numbers
     */
    public MeanSample(double firstInput, double secondInput, SamplingDistribution sampleDistribution, int n){
        this.n = n;
        values = new ArrayList<>(0);
        samplingDistribution = sampleDistribution;

        if(sampleDistribution == SamplingDistribution.NORMAL) {
            populationMean = firstInput;
            populationStdDev = secondInput;

            sampleGraphMin = populationMean - (populationStdDev * 3);
            sampleGraphMax = populationMean + (populationStdDev * 3);

            /* Normal */
            for (int i = 0; i < n; i++) {
                // Algorithm from http://www.rossmanchance.com/applets/ConfSim.html
                BigDecimal value;
                double r, x, y;

                // http://www.cs.princeton.edu/courses/archive/fall12/cos126/assignments/StdGaussian.java.html
                // find a uniform random point (x, y) inside unit circle
                do {
                    x = 2.0 * Math.random() - 1.0;
                    y = 2.0 * Math.random() - 1.0;
                    r = x * x + y * y;
                } while (r > 1 || r == 0);
                // http://en.wikipedia.org/wiki/Box-Muller_transform

                // apply the Box-Muller formula to get standard Gaussian z
                double z = x * Math.sqrt(-2.0 * Math.log(r) / r);

                value = new BigDecimal((z * populationStdDev) + populationMean);
                values.add(value);
            }
        }
        if(sampleDistribution == SamplingDistribution.UNIFORM){
            double a = firstInput;
            double b = secondInput;
            sampleGraphMin = a;
            sampleGraphMax = b;
            populationMean = ((b - a)/2) + a;
            populationStdDev = (b-a)/2;
            for (int i = 0; i < n; i++) {
                values.add(new BigDecimal( (Math.random() * (b-a)) + a ));
            }
        }
        if(sampleDistribution == SamplingDistribution.EXPONENTIAL){
            populationMean = firstInput;
            sampleGraphMin = 0;
            sampleGraphMax = populationMean * 2;
            for (int i = 0; i < n; i++) {
                values.add(new BigDecimal( (-Math.log(Math.random()) * populationMean)));
            }
            for(BigDecimal v : values){
                Log.v("TAG", "Value: " + v);
            }
        }

       /* Calculate mean of sample */
        double sum = 0;
        for(BigDecimal v : values){
            sum += v.doubleValue();
        }
        sampleMean = sum / n;

        /* Calculate std dev of sample */
        sum = 0;
        for(BigDecimal v : values) {
            sum += Math.pow(v.doubleValue() - sampleMean, 2);
        }
        sampleStddev = Math.sqrt(sum/(n-1));
    }

    public double[] calculateCI(double confLevel){
        // function that returns confidence interval (left, midpoint, right) for interval
        // Using z score with sigma input by use

        double phat = sampleMean;
        double alpha = 1.0 - confLevel / 100.0;
        double z = Formulas.inverseNorm(1.0 - alpha/2);
        double sigma = sampleStddev;
        Log.v("LOG", "z: " + z);
        double[] results = new double[3];
        double halfWidth = z * sigma / Math.sqrt(n);
        results[0] = phat - halfWidth;
        results[1] = phat;
        results[2] = phat + halfWidth;
        return results;
    }

    public ArrayList<BigDecimal> getValues() {
        return values;
    }

    @Override
    public double getSampleMean() {
        return sampleMean;
    }

    @Override
    public double getSampleStdDev() {
        return sampleStddev;
    }

    @Override
    public double getPopulationMean() { return populationMean;}

    @Override
    public double getPopulationStdDev() { return populationStdDev;}

    public double getSampleGraphMin() {
        return sampleGraphMin;
    }

    public double getSampleGraphMax() {
        return sampleGraphMax;
    }

    public SamplingDistribution getSamplingDistribution() {
        return samplingDistribution;
    }
}
