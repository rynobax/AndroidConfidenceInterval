package statistics.p4combined;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class CISettingsActivity extends AppCompatActivity {
    private CIType ciType;
    private SamplingDistribution samplingDistribution;
    private int n;
    private int intervals;
    private double fieldOne;
    private double fieldTwo;

    //TODO: Add limits on inputs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cisettings);
        Intent intent = getIntent();

        String ciTypeString = intent.getStringExtra("ciType");
        if(ciTypeString.equals("PROPORTION")) ciType = CIType.PROPORTION;
        if(ciTypeString.equals("MEAN")) ciType = CIType.MEAN;

        String samplingDistributionString = intent.getStringExtra("samplingDistribution");
        Log.v("TAG", "samplingDistribution: " + samplingDistributionString);
        if(samplingDistributionString.equals("UNIFORM")) samplingDistribution = SamplingDistribution.UNIFORM;
        if(samplingDistributionString.equals("NORMAL")) samplingDistribution = SamplingDistribution.NORMAL;
        if(samplingDistributionString.equals("EXPONENTIAL")) samplingDistribution = SamplingDistribution.EXPONENTIAL;
        if(samplingDistributionString.equals("BINOMIAL")) samplingDistribution = SamplingDistribution.BINOMIAL;

        n = intent.getIntExtra("n", 1);
        intervals = intent.getIntExtra("intervals", 1);
        fieldOne = intent.getDoubleExtra("fieldOne", 1);
        fieldTwo = intent.getDoubleExtra("fieldTwo", 1);

        populateSettings();
    }

    private void populateSettings(){
        LinearLayout SettingsLinearLayout = (LinearLayout) findViewById(R.id.ci_settings_vertical_layout);
        SettingsLinearLayout.removeAllViews();

        /* Mean or Proportion Spinner */
        Spinner CITypeSpinner = new Spinner(this, Spinner.MODE_DIALOG);
        final ArrayList<String> CITypeSpinnerArray =  new ArrayList(0);
        CITypeSpinnerArray.add("Proportion");
        CITypeSpinnerArray.add("Mean");
        ArrayAdapter<String> CITypeSpinnerAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, CITypeSpinnerArray);
        CITypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CITypeSpinner.setAdapter(CITypeSpinnerAdapter);
        if(ciType == CIType.PROPORTION) CITypeSpinner.setSelection(0, false); // This fixes infinite loop
        else CITypeSpinner.setSelection(1, false);
        CITypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(CITypeSpinnerArray.get(position).equals("Mean")){
                    ciType = CIType.MEAN;
                    samplingDistribution = SamplingDistribution.NORMAL;
                }
                else ciType = CIType.PROPORTION;
                populateSettings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        SettingsLinearLayout.addView(CITypeSpinner);

        final EditText fieldOneEditText;
        final EditText fieldTwoEditText;

        if(ciType == CIType.PROPORTION){
            /* Mean */
            LinearLayout meanInputLayout = new LinearLayout(this);
            meanInputLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView meanLabelText = new TextView(this);
            meanLabelText.setText("Mean: ");
            meanInputLayout.addView(meanLabelText);

            fieldOneEditText = new EditText(this);
            fieldOneEditText.setText(String.valueOf(fieldOne));
            fieldOneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            meanInputLayout.addView(fieldOneEditText);

            /* Prevents null later */
            fieldTwoEditText = new EditText(this);
            fieldTwoEditText.setText("0");

            SettingsLinearLayout.addView(meanInputLayout);
        }else{
            Spinner CIDistributionSpinner = new Spinner(this, Spinner.MODE_DIALOG);
            final ArrayList<String> distributionSpinnerArray =  new ArrayList(0);
            distributionSpinnerArray.add("Normal");
            distributionSpinnerArray.add("Uniform");
            distributionSpinnerArray.add("Exponential");
            ArrayAdapter<String> CIDistributionSpinnerAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, distributionSpinnerArray);
            CIDistributionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            CIDistributionSpinner.setAdapter(CIDistributionSpinnerAdapter);
            if(samplingDistribution == SamplingDistribution.NORMAL) CIDistributionSpinner.setSelection(0, false); // This fixes infinite loop
            else if(samplingDistribution == SamplingDistribution.UNIFORM) CIDistributionSpinner.setSelection(1, false);
            else CIDistributionSpinner.setSelection(2, false);
            CIDistributionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(distributionSpinnerArray.get(position).equals("Normal")){
                        samplingDistribution = SamplingDistribution.NORMAL;
                        fieldOne = 0;
                        fieldTwo = 10;
                    }
                    else if(distributionSpinnerArray.get(position).equals("Uniform")){
                        samplingDistribution = SamplingDistribution.UNIFORM;
                        fieldOne = 5;
                        fieldTwo = 10;
                    }
                    else{
                        samplingDistribution = SamplingDistribution.EXPONENTIAL;
                        fieldOne = 1;
                    }
                    populateSettings();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            SettingsLinearLayout.addView(CIDistributionSpinner);

            if(samplingDistribution == SamplingDistribution.NORMAL){
                /* Mean */
                LinearLayout meanInputLayout = new LinearLayout(this);
                meanInputLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView meanLabelText = new TextView(this);
                meanLabelText.setText("Mean: ");
                meanInputLayout.addView(meanLabelText);

                fieldOneEditText = new EditText(this);
                fieldOneEditText.setText(String.valueOf(fieldOne));
                fieldOneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                meanInputLayout.addView(fieldOneEditText);

                SettingsLinearLayout.addView(meanInputLayout);

                /* StdDev */
                LinearLayout stdDevInputLayout = new LinearLayout(this);
                stdDevInputLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView stdDevLabelText = new TextView(this);
                stdDevLabelText.setText("Standard Deviation: ");
                stdDevInputLayout.addView(stdDevLabelText);

                fieldTwoEditText = new EditText(this);
                fieldTwoEditText.setText(String.valueOf(fieldOne));
                fieldTwoEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                stdDevInputLayout.addView(fieldTwoEditText);

                SettingsLinearLayout.addView(stdDevInputLayout);
            }
            else if(samplingDistribution == SamplingDistribution.UNIFORM){
                /* a */
                LinearLayout aInputLayout = new LinearLayout(this);
                aInputLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView aLabelText = new TextView(this);
                aLabelText.setText("a: ");
                aInputLayout.addView(aLabelText);

                fieldOneEditText = new EditText(this);
                fieldOneEditText.setText(String.valueOf(fieldOne));
                fieldOneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                aInputLayout.addView(fieldOneEditText);

                SettingsLinearLayout.addView(aInputLayout);

                /* b */
                LinearLayout bInputLayout = new LinearLayout(this);
                bInputLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView bLabelText = new TextView(this);
                bLabelText.setText("b: ");
                bInputLayout.addView(bLabelText);

                fieldTwoEditText = new EditText(this);
                fieldTwoEditText.setText(String.valueOf(fieldTwo));
                fieldTwoEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                bInputLayout.addView(fieldTwoEditText);

                SettingsLinearLayout.addView(bInputLayout);
            }
            else{
                /* Mean */
                LinearLayout meanInputLayout = new LinearLayout(this);
                meanInputLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView meanLabelText = new TextView(this);
                meanLabelText.setText("Mean: ");
                meanInputLayout.addView(meanLabelText);

                fieldOneEditText = new EditText(this);
                fieldOneEditText.setText(String.valueOf(fieldOne));
                fieldOneEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                meanInputLayout.addView(fieldOneEditText);

                SettingsLinearLayout.addView(meanInputLayout);

                /* Prevents null later */
                fieldTwoEditText = new EditText(this);
                fieldTwoEditText.setText("0");
            }
        }

        /* n */
        LinearLayout nInputLayout = new LinearLayout(this);
        nInputLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView nLabelText = new TextView(this);
        nLabelText.setText("n: ");
        nInputLayout.addView(nLabelText);

        final EditText nEditText = new EditText(this);
        nEditText.setText(String.valueOf(n));
        nEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        nInputLayout.addView(nEditText);

        SettingsLinearLayout.addView(nInputLayout);

        /* intervals */
        LinearLayout intervalsInputLayout = new LinearLayout(this);
        intervalsInputLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView intervalsLabelText = new TextView(this);
        intervalsLabelText.setText("Intervals: ");
        intervalsInputLayout.addView(intervalsLabelText);

        final EditText intervalsEditText = new EditText(this);
        intervalsEditText.setText(String.valueOf(intervals));
        intervalsEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        intervalsInputLayout.addView(intervalsEditText);

        SettingsLinearLayout.addView(intervalsInputLayout);

        /* save */
        Button saveButton = new Button(this);
        saveButton.setText("Save");
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("ciType", ciType.toString());
                intent.putExtra("samplingDistribution", samplingDistribution.toString());
                int newN = Integer.valueOf(nEditText.getText().toString());
                intent.putExtra("n", newN);
                int newIntervals = Integer.valueOf(intervalsEditText.getText().toString());
                intent.putExtra("intervals", newIntervals);
                double newFieldOne = Double.valueOf(fieldOneEditText.getText().toString());
                intent.putExtra("fieldOne", newFieldOne);
                double newFieldTwo = Double.valueOf(fieldTwoEditText.getText().toString());
                intent.putExtra("fieldTwo", newFieldTwo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        SettingsLinearLayout.addView(saveButton);
    }

}
