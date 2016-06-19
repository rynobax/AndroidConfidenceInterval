package statistics.p4combined;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
/**
 * Created by Ryan on 4/2/2016.
 */
public class ConfidenceIntervalChartView extends View {
    Context context;
    ArrayList<Sample> samples;

    /* Shapes drawn to make graph */
    ShapeDrawable bg;
    ShapeDrawable xAxis;
    ShapeDrawable yAxis;
    ShapeDrawable meanLine;
    Pair<Integer, Integer> meanLineText;
    ArrayList<ShapeDrawable> tickMarks;
    ArrayList<Pair<Integer, Integer>> tickMarksText;
    ArrayList<ShapeDrawable> CILines;
    ArrayList<ShapeDrawable> CIDots;
    Pair<Integer, Integer> CITextLeft;
    double CITextLeftMean;
    Pair<Integer, Integer> CITextCenter;
    double CITextCenterMean;
    Pair<Integer, Integer> CITextRight;
    double CITextRightMean;

    /* Globals */
    double tickMinNum;
    double tickMaxNum;
    double populationMean;
    double populationStdDev;
    int distBetweenCILines;
    int graphTop;
    int graphBottom;
    int graphLeft;
    int graphRight;
    int previousSelectedCI;
    Rect previousSelectedRect;
    double[] CIValues;
    int repeatedTouchCount;
    double tickLabelStart;
    double tickIncNum;

    /* Keeps track of if we are graphic proportion or mean */
    boolean proportion;

    public ConfidenceIntervalChartView(Context context, int layoutWidth, int layoutHeight, ArrayList<Sample> samples, double confidenceLevel){
        super(context);
        this.context = context;
        previousSelectedCI = -1;
        repeatedTouchCount = 0;
        this.samples = samples;
        populationMean = samples.get(0).getPopulationMean();
        populationStdDev = samples.get(0).getPopulationStdDev();

        if(samples.get(0).getClass().equals(ProportionSample.class)) proportion = true;

        int frameXStart = 0;
        int frameYStart = 0;
        int frameXSize = layoutWidth;
        int frameYSize = layoutHeight;

        /* Set background color */
        bg = new ShapeDrawable(new RectShape());
        bg.setBounds(frameXStart, frameYStart, frameXStart + frameXSize, frameYStart + frameYSize);
        int bgColor = Color.WHITE;
        bg.getPaint().setColor(bgColor);

        /* Draw Axises */
        int axisThickness = 6;
        int axisLeftOffset = 10;
        int axisRightOffset = 16;
        int axisTopOffset = 40;
        int axisBottomOffset = 20;

        int xAxisLeft = frameXStart + axisLeftOffset;
        int xAxisRight = frameXSize - axisRightOffset;
        int xAxisBottom = frameYSize - axisBottomOffset;
        int xAxisTop = xAxisBottom - axisThickness;
        xAxis = new ShapeDrawable(new RectShape());
        xAxis.setBounds(xAxisLeft, xAxisTop, xAxisRight, xAxisBottom);
        int xAxisColor = ContextCompat.getColor(context, R.color.graphLine);
        xAxis.getPaint().setColor(xAxisColor);
        Log.v("LOG", "xaxis: left:" + xAxisLeft + ", right:" + xAxisRight + ", top:" + xAxisTop + ", bot:" + xAxisBottom);

        int yAxisLeft = axisLeftOffset;
        int yAxisRight = yAxisLeft + axisThickness;
        int yAxisTop = frameYStart + axisTopOffset;
        int yAxisBottom = frameYSize - axisBottomOffset;
        yAxis = new ShapeDrawable(new RectShape());
        yAxis.setBounds(yAxisLeft, yAxisTop, yAxisRight, yAxisBottom);
        int yAxisColor = ContextCompat.getColor(context, R.color.graphLine);
        yAxis.getPaint().setColor(yAxisColor);
        Log.v("LOG", "yaxis: left:" + yAxisLeft + ", right:" + yAxisRight + ", top:" + yAxisTop + ", bottom:" + yAxisBottom);

        /* Set some globals */
        graphTop = yAxisTop;
        graphBottom = xAxisTop;
        graphLeft = yAxisRight;
        graphRight = xAxisRight;

        /* Calculate drawable area */
        int graphDrawableWidth = xAxisRight - (xAxisLeft + axisThickness);
        int graphDrawableHeight = yAxisTop - (yAxisBottom + axisThickness);

        /* Draw mean line */
        int meanLineThickness = 2;
        int meanLineCenter;
        if(proportion) meanLineCenter = (int)((double)graphDrawableWidth * populationMean) + (axisLeftOffset + axisThickness);
        else meanLineCenter = (int)((double)graphDrawableWidth * .5) + (axisLeftOffset + axisThickness);
        int meanLineLeft = meanLineCenter - (meanLineThickness / 2);
        int meanLineRight = meanLineCenter + (meanLineThickness / 2);
        int meanLineTop = yAxisTop;
        int meanLineBottom = yAxisBottom;
        meanLine = new ShapeDrawable(new RectShape());
        meanLine.setBounds(meanLineLeft, meanLineTop, meanLineRight, meanLineBottom);
        int meanLineColor = ContextCompat.getColor(context, R.color.graphLine);
        meanLine.getPaint().setColor(meanLineColor);
        Log.v("LOG", "meanLine: left:" + meanLineLeft + ", right:" + meanLineRight + ", top:" + meanLineTop + ", bottom:" + meanLineBottom);

        /* Label Mean line */
        int meanLineOffset = 12;
        meanLineText = new Pair<>(meanLineCenter, meanLineTop - meanLineOffset);

        /* Draw Axis Tick Marks and Text*/
        tickMarks = new ArrayList<>(0);
        tickMarksText = new ArrayList<>(0);

        int tickMarkThickness = 2;
        int tickMarkHeight = 8 + axisThickness;

        int tickCount;

        if(proportion){
            tickMinNum = 0.0;
            tickMaxNum = 1.0;
            tickCount = 6;
            tickIncNum = (tickMaxNum - tickMinNum) / (tickCount - 1);
        }else{
            tickCount = 5;
            Log.v("TAG", "dist: " + samples.get(0).getSamplingDistribution());
            if(samples.get(0).getSamplingDistribution() == SamplingDistribution.EXPONENTIAL){
                Log.v("TAG", "Exponential");
                tickMinNum = 0;
                tickMaxNum = populationMean * 2;
            }else {
                tickMinNum = populationMean - (2 * populationStdDev);
                tickMaxNum = populationMean + (2 * populationStdDev);
            }
            tickIncNum = (tickMaxNum - tickMinNum) / (tickCount - 1);
        }
        tickLabelStart = tickMinNum;
        Log.v("TAG", "min: " + tickMinNum);
        Log.v("TAG", "max: " + tickMaxNum);
        Log.v("TAG", "IncNum: " + tickIncNum);

        for(double i=0.0;i<=1.0;i+=(1.0 / (tickCount-1))){
            int tickMarkXPos = (int)((double)graphDrawableWidth * i) + (axisLeftOffset + axisThickness);
            int tickMarkYPos = yAxisBottom - (axisThickness / 2);
            int tickMarkLeft = tickMarkXPos - (tickMarkThickness / 2);
            int tickMarkRight = tickMarkXPos + (tickMarkThickness / 2);
            int tickMarkTop = tickMarkYPos - (tickMarkHeight / 2);
            int tickMarkBottom = tickMarkYPos + (tickMarkHeight / 2);
            ShapeDrawable tickMark = new ShapeDrawable(new RectShape());
            tickMark.setBounds(tickMarkLeft, tickMarkTop, tickMarkRight, tickMarkBottom);
            int tickMarkColor = ContextCompat.getColor(context, R.color.graphLine);
            tickMark.getPaint().setColor(tickMarkColor);
            tickMarks.add(tickMark);
            Log.v("LOG", "tickMark " + i + ": left:" + tickMarkLeft + ", right:" + tickMarkRight + ", top:" + tickMarkTop + ", bottom:" + tickMarkBottom);

            int tickMarkTextOffset = 16;
            tickMarksText.add(new Pair<>(tickMarkXPos, tickMarkBottom + tickMarkTextOffset));
        }

        /* Draw Intervals */
        int sampleCount = samples.size();
        distBetweenCILines = (xAxisTop - yAxisTop) / (sampleCount + 1);
        int CILineYPos = yAxisTop;
        CILines = new ArrayList<>(0);
        CIDots = new ArrayList<>(0);
        CIValues = new double[sampleCount * 3];

        int i = 0;
        for(Sample s : samples){
            CILineYPos += distBetweenCILines;
            double CI[] = s.calculateCI(confidenceLevel);
            double CILeft = CI[0];
            double CIMid = CI[1];
            double CIRight = CI[2];
            Log.v("TAG", "CILeft: " + CILeft + ", CIMid: " + CIMid + ", CIRight: " + CIRight);

            CIValues[i++] = CILeft;
            CIValues[i++] = CIMid;
            CIValues[i++] = CIRight;

            double numRange = tickMaxNum - tickMinNum;
            double numRangeOffset = 0 - tickMinNum;

            int CILineThickness = 4;

            int CILineLeft = (int)(((CILeft + numRangeOffset) / numRange) * graphDrawableWidth) + graphLeft;
            int CILineMid = (int)(((CIMid + numRangeOffset) / numRange) * graphDrawableWidth) + graphLeft;
            int CILineRight = (int)(((CIRight + numRangeOffset) / numRange) * graphDrawableWidth) + graphLeft;
            int CILineTop = CILineYPos - (CILineThickness / 2);
            int CILineBottom = CILineYPos + (CILineThickness / 2);

            ShapeDrawable CILine = new ShapeDrawable(new RectShape());
            CILine.setBounds(CILineLeft, CILineTop, CILineRight, CILineBottom);
            if(CILineLeft > meanLineCenter || CILineRight < meanLineCenter){
                int CILineColor = ContextCompat.getColor(context, R.color.CINotContain);
                CILine.getPaint().setColor(CILineColor);
            }else{
                int CILineColor = ContextCompat.getColor(context, R.color.CIContain);
                CILine.getPaint().setColor(CILineColor);
            }
            Log.v("LOG", "CILine: left:" + CILineLeft + ", right:" + CILineRight + ", top:" + CILineTop + ", bottom:" + CILineBottom);

            CILines.add(CILine);

            ShapeDrawable CIDot = new ShapeDrawable(new RectShape());
            CIDot.setBounds(CILineMid - CILineThickness, CILineTop - (CILineThickness/2), CILineMid + CILineThickness, CILineBottom + (CILineThickness/2));
            int CIDotColor = ContextCompat.getColor(context, R.color.graphLine);
            CIDot.getPaint().setColor(CIDotColor);
            CIDots.add(CIDot);
        }

        /* Initialize the text that shows when you select a CI */
        CITextLeft = new Pair<>(-1, -1);
        CITextCenter = new Pair<>(-1, -1);
        CITextRight = new Pair<>(-1, -1);
        // TODO: Dynamically change text size
    }

    private OnLongPressListener onLongPressListener;
    public interface OnLongPressListener {
        void onLongPress(Sample sample);
    }
    public void setOnLongPressListener(OnLongPressListener listener) {
        onLongPressListener = listener;
    }

    long startTime = System.currentTimeMillis();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        final float x = event.getX();
        final float y = event.getY();
        int selectedCI = whichLineWasSelected((int) x, (int) y);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                if(previousSelectedCI != -1){
                    /* Deflate previous CI */
                    CIDots.get(previousSelectedCI).setBounds(previousSelectedRect);

                    /* Destroy the text */
                    CITextLeft = new Pair<>(-1, -1);
                    CITextRight = new Pair<>(-1, -1);
                }

                if(selectedCI != -1){
                    startTime = System.currentTimeMillis();
                    /* Inflate CI Dot */
                    Rect dotRect = CIDots.get(selectedCI).getBounds();
                    previousSelectedRect = new Rect(dotRect);
                    int inflationAmount = 3;
                    CIDots.get(selectedCI).setBounds(dotRect.left - inflationAmount, dotRect.top - inflationAmount, dotRect.right + inflationAmount, dotRect.bottom + inflationAmount);

                    /* Display text */
                    Rect lineRect = CILines.get(selectedCI).getBounds();
                    int CITextOffset = 25;
                    CITextLeft = new Pair<>(lineRect.left - CITextOffset, lineRect.bottom + 5);
                    CITextLeftMean = CIValues[selectedCI*3];
                    CITextCenter = new Pair<>(lineRect.left + (lineRect.width() / 2), lineRect.top - 10);
                    CITextCenterMean = CIValues[(selectedCI*3)+1];
                    CITextRight = new Pair<>(lineRect.right + CITextOffset, lineRect.bottom + 5);
                    CITextRightMean = CIValues[(selectedCI*3)+2];
                }

                invalidate();
                previousSelectedCI = selectedCI;
                break;
            case (MotionEvent.ACTION_UP) :
                /* Ignore long presses on nothing or ones that move */
                if(selectedCI == -1 || selectedCI != previousSelectedCI) return true;

                long pushLength = System.currentTimeMillis() - startTime;
                /* 1000 is 1 second */
                if(pushLength > 500){
                    onLongPressListener.onLongPress(samples.get(selectedCI));
                }
                break;
        }
        return true;
    }

    private int whichLineWasSelected(int x, int y){
        // TODO: Adjust padding based on number of lines or limit max number of samples

        int yTouchPadding = 50;
        int xTouchPadding = 30;

        int i = 0;
        for(ShapeDrawable CI : CILines){
            if(y > (CI.getBounds().top - yTouchPadding) &&
                    y < (CI.getBounds().bottom + yTouchPadding)){
                if(x > (CI.getBounds().left - xTouchPadding) &&
                        x < (CI.getBounds().right + xTouchPadding)){
                    /* We touched this shape */
                    return i;
                }
            }
            ++i;
        }
        return -1;
    }

    protected void onDraw(Canvas canvas) {
        bg.draw(canvas);
        yAxis.draw(canvas);
        xAxis.draw(canvas);
        meanLine.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(populationMean).substring(0, 3), meanLineText.getLeft(), meanLineText.getRight(), paint);

        for(ShapeDrawable tick : tickMarks) tick.draw(canvas);
        double tickLabel = tickLabelStart;
        for(Pair<Integer, Integer> p : tickMarksText){
            //TODO: Limit text length better
            canvas.drawText(String.valueOf(tickLabel).substring(0, 3), p.getLeft(), p.getRight(), paint);
            tickLabel = tickLabel + tickIncNum;
        }

        for(ShapeDrawable CILine : CILines) CILine.draw(canvas);

        for(ShapeDrawable CIDot : CIDots) CIDot.draw(canvas);


        String LeftText = String.valueOf(CITextLeftMean);
        String CenterText = String.valueOf(CITextCenterMean);
        String RightText = String.valueOf(CITextRightMean);

        int cutoffLength = 5;
        if(LeftText.length() > cutoffLength) LeftText = LeftText.substring(0,cutoffLength);
        if(CenterText.length() > cutoffLength) CenterText = CenterText.substring(0,cutoffLength);
        if(RightText.length() > cutoffLength) RightText = RightText.substring(0,cutoffLength);


        if(CITextLeft.getLeft() > graphLeft)
            canvas.drawText(LeftText, CITextLeft.getLeft(), CITextLeft.getRight(), paint);
        if(CITextRight.getLeft() < graphRight && CITextRight.getLeft() > graphLeft)
            canvas.drawText(RightText, CITextRight.getLeft(), CITextRight.getRight(), paint);
        if(CITextCenter.getLeft() < graphRight && CITextCenter.getLeft() > graphLeft)
            canvas.drawText(CenterText, CITextCenter.getLeft(), CITextCenter.getRight(), paint);
    }

    public class Pair<L,R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() { return left; }
        public R getRight() { return right; }

        @Override
        public int hashCode() { return left.hashCode() ^ right.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair pairo = (Pair) o;
            return this.left.equals(pairo.getLeft()) &&
                    this.right.equals(pairo.getRight());
        }
    }
}
