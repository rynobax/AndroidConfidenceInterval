package statistics.p4combined;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

public class ConfidenceIntervals extends AppCompatActivity {
    private FrameLayout layout;
    private ConfidenceIntervalChartView mConfidenceIntervalChartView;
    private ArrayList<Sample> samples;

    private CIType ciType;
    private SamplingDistribution samplingDistribution;
    private int n;
    private int intervals;
    private double fieldOne;
    private double fieldTwo;
    private double confidenceLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confidence);
        layout = (FrameLayout) findViewById(R.id.graphingArea);

        ciType = CIType.PROPORTION;
        samplingDistribution = SamplingDistribution.BINOMIAL;
        n = 31;
        intervals = 12;
        fieldOne = 0.5;
        fieldTwo = 0.0;
        confidenceLevel = 99;


        layout.addView(new View(this));

        // TODO: Make this actaully work
        drawIntervals();

        launchSettings();
    }

    public void newData(){
        // mean, n, intervals
        samples = new ArrayList<>(0);
        Log.v("LOG", "Intervals: " + intervals);
        for(int i=0;i<intervals;i++){
            if(ciType == CIType.PROPORTION){
                samples.add(new ProportionSample(fieldOne, n));
            }
            else samples.add(new MeanSample(fieldOne, fieldTwo, samplingDistribution, n));
        }
    }

    static final int SETTINGS_REQUEST = 1;
    public void launchSettings(View view){
        launchSettings();
    }

    public void launchSettings(){
        Intent intent = new Intent(this, CISettingsActivity.class);
        Log.v("TAG", "ciType: " + ciType.toString());
        intent.putExtra("ciType", ciType.toString());
        intent.putExtra("samplingDistribution", samplingDistribution.toString());
        intent.putExtra("n", n);
        intent.putExtra("intervals", intervals);
        intent.putExtra("fieldOne", fieldOne);
        intent.putExtra("fieldTwo", fieldTwo);
        startActivityForResult(intent, SETTINGS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SETTINGS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.v("TAG", "got new stuff");
                String ciTypeString = intent.getStringExtra("ciType");
                if(ciTypeString.equals("PROPORTION")) ciType = CIType.PROPORTION;
                if(ciTypeString.equals("MEAN")) ciType = CIType.MEAN;

                String samplingDistributionString = intent.getStringExtra("samplingDistribution");
                if(samplingDistributionString.equals("UNIFORM")) samplingDistribution = SamplingDistribution.UNIFORM;
                if(samplingDistributionString.equals("NORMAL")) samplingDistribution = SamplingDistribution.NORMAL;
                if(samplingDistributionString.equals("EXPONENTIAL")) samplingDistribution = SamplingDistribution.EXPONENTIAL;
                if(samplingDistributionString.equals("BINOMIAL")) samplingDistribution = SamplingDistribution.BINOMIAL;

                n = intent.getIntExtra("n", 1);
                intervals = intent.getIntExtra("intervals", 1);
                fieldOne = intent.getDoubleExtra("fieldOne", 1);
                fieldTwo = intent.getDoubleExtra("fieldTwo", 1);

                //TODO: Make this actually work
                drawIntervals();
            }
        }
    }

    public void drawIntervals(final View view){
        drawIntervals();
    }
    public void drawIntervals(){
        layout.removeViewAt(0);

        newData();

        double confidenceInterval = new Double(((EditText) findViewById(R.id.confidence_interval_input)).getText().toString());
        Log.v("TAG", "Confidence Interval: " + confidenceInterval);

        mConfidenceIntervalChartView = new ConfidenceIntervalChartView(this, layout.getWidth(), layout.getHeight(),
                samples, confidenceInterval);
        mConfidenceIntervalChartView.setOnLongPressListener(new ConfidenceIntervalChartView.OnLongPressListener() {
            @Override
            public void onLongPress(Sample sample) {
                if(sample.getClass() == ProportionSample.class)
                    drawSingleSampleStatistics((ProportionSample) sample);
                else drawSingleSampleStatistics((MeanSample) sample);
            }
        });
        layout.addView(mConfidenceIntervalChartView,0);
    }

    public void drawSingleSampleStatistics(ProportionSample sample){
        layout.removeViewAt(0);

        XYSeries series = new XYSeries("Proportion series");
        series.add(0, sample.getFailCount());
        series.add(1, sample.getSuccessCount());
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setDisplayBoundingPoints(true);
        renderer.setFillPoints(true);

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // We want to avoid black border
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true); // we show the grid
        mRenderer.setBarWidth(12);

        GraphicalView chartView = ChartFactory.getBarChartView(this, dataset, mRenderer, BarChart.Type.DEFAULT);

        layout.addView(chartView, 0);
    }

    public void drawSingleSampleStatistics(MeanSample sample){
        layout.removeViewAt(0);

        double graphMin = sample.getSampleGraphMin();
        double graphMax = sample.getSampleGraphMax();

        XYSeries series = new XYSeries("Mean series");

        int binCount = 11;
        double binCutoff = graphMin;
        double binInterval = (graphMax - graphMin) / binCount;
        ArrayList<BigDecimal> valuesCopy = new ArrayList(sample.getValues());
        int[] bins = new int[binCount];
        for(int i=0; i<binCount; i++){
            binCutoff = binCutoff + binInterval;
            Log.v("TAG", "Bincutoff: " + binCutoff);
            for (Iterator<BigDecimal> iterator = valuesCopy.iterator(); iterator.hasNext();) {
                BigDecimal value = iterator.next();
                if(value.doubleValue() < binCutoff){
                    ++bins[i];
                    iterator.remove();
                }
            }
            Log.v("LOG", "bins[" + i + "]: " + bins[i]);
            series.add(binCutoff-(binInterval/2), bins[i]);
        }
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setDisplayBoundingPoints(true);
        renderer.setFillPoints(true);

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // We want to avoid black border
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true);
        //mRenderer.setBarWidth(layout.getWidth()/(binCount*2));
        mRenderer.setShowLegend(false);

        GraphicalView chartView = ChartFactory.getBarChartView(this, dataset, mRenderer, BarChart.Type.DEFAULT);

        layout.addView(chartView, 0);
    }

    public void drawSamplesStatistics(View view){
        drawSamplesStatistics();
    }
    public void drawSamplesStatistics(){
        layout.removeViewAt(0);

        XYSeries series = new XYSeries("Samples series");


        double graphMin;
        double graphMax;
        if(samples.get(0).getClass().equals(ProportionSample.class)){
            /* Proportion */
            graphMin = 0;
            graphMax = 1;
        }else{
            /* Mean */
            if(samplingDistribution == SamplingDistribution.UNIFORM){
                graphMin = samples.get(0).getPopulationMean() - (samples.get(0).getPopulationStdDev());
                graphMax = samples.get(0).getPopulationMean() + (samples.get(0).getPopulationStdDev());
            }else if(samplingDistribution == SamplingDistribution.NORMAL){
                graphMin = samples.get(0).getPopulationMean() - (samples.get(0).getPopulationStdDev() * 3);
                graphMax = samples.get(0).getPopulationMean() + (samples.get(0).getPopulationStdDev() * 3);
            }else{
                graphMin = 0;
                graphMax = samples.get(0).getPopulationMean() * 2;
            }
        }

        int binCount = 11;
        double binCutoff = graphMin;
        double binInterval = (graphMax - graphMin) / binCount;
        ArrayList<Sample> samplesCopy = new ArrayList(samples);
        int[] bins = new int[binCount];
        for(int i=0; i<binCount; i++){
            binCutoff = binCutoff + binInterval;
            for (Iterator<Sample> iterator = samplesCopy.iterator(); iterator.hasNext();) {
                Sample sample = iterator.next();
                if(sample.getSampleMean() < binCutoff){
                    ++bins[i];
                    iterator.remove();
                }
            }
            Log.v("LOG", "bins[" + i + "]: " + bins[i]);
            // Add everything past our last bin to the last bin
            if(i == binCount-1) bins[i] += samplesCopy.size();
            series.add(binCutoff-(binInterval/2), bins[i]);
        }

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setDisplayBoundingPoints(true);
        renderer.setFillPoints(true);

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);
        // We want to avoid black border
        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
        // Disable Pan on two axis
        mRenderer.setPanEnabled(false, false);
        mRenderer.setShowGrid(false);
        mRenderer.setBarWidth(50);
        mRenderer.setYAxisMin(0);
        /* TODO: Make axis labels stand out more */

        GraphicalView chartView = ChartFactory.getBarChartView(this, dataset, mRenderer, BarChart.Type.DEFAULT);

        layout.addView(chartView, 0);
    }
}