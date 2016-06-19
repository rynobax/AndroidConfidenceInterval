# Android Confidence Interval Activity

This is the activity files for the portion of a Statistics Application I developed with a group at Clemson University.

The activity generates samples based on user input, then calculates and displays confidence intervals based on the user input, which is displayed on a chart.

## Technical Details
ProportionSample and MeanSample are classes that contains information about a generated sample.  The implementations for generating the samples as well as calculating the confidence intervals from the samples is contained in them.
The chart of confidence intervals is generated using a Canvas and ShapeDrawables in a custom View, named ConfidenceIntervalChartView.  A variety of settings, including coloring, margins, and line thickness, can be modified in it’s constructor.
The other charts are generated using aFreeChart, and the code for them is in the main ConfidenceIntervalActivity class.
