package com.codepath.something2read.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.codepath.something2read.R;
import com.codepath.something2read.fragments.DatePickerFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    @BindView(R.id.save_button) Button mSaveButton;
    @BindView(R.id.begin_date_picker) EditText mDatePicker;
    @BindView(R.id.sort_order_picker) Spinner mSpinner;
    @BindView(R.id.checkBox) CheckBox mArtsCheckBox;
    @BindView(R.id.checkBox2) CheckBox mFashionCheckBox;
    @BindView(R.id.checkBox3) CheckBox mSportsCheckBox;

    final static String PREFS_NAME = "FilterSettingsFile";

    final static String STARTING_DATE = "starting_date";
    final static String SORT_ORDER = "sort_order";
    final static String NEWS_DESK = "news_desk";

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_settings);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        setSavedSettingsToUI();
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = parseDate(mDatePicker.getText().toString());
                String order = mSpinner.getSelectedItem().toString();
                StringBuilder newsDeskQuery = new StringBuilder("news_desk:(");
                List<String> listOfNewsDesk = new ArrayList<>();
                if (mArtsCheckBox.isChecked()) {
                    listOfNewsDesk.add("\"Arts\"");
                }
                if (mFashionCheckBox.isChecked()) {
                    listOfNewsDesk.add("\"Fashion & Style\"");
                }
                if (mSportsCheckBox.isChecked()) {
                    listOfNewsDesk.add("\"Sports\"");
                }
                for(String x : listOfNewsDesk ) {
                    newsDeskQuery.append(x).append(" ");
                }
                newsDeskQuery.append(")");
                mSharedPreferences.edit()
                    .putString(STARTING_DATE, date)
                    .putString(SORT_ORDER, order.toLowerCase())
                    .putString(NEWS_DESK, newsDeskQuery.toString())
                    .apply();
                finish();
            }
        });
    }

    private String parseDate(String date) {
        Pattern pt = Pattern.compile("[^0-9]");
        Matcher match= pt.matcher(date);
        while(match.find()){
            date=date.replace(Character.toString(date.charAt(match.start())),"");
        }
        return date;
    }

    // attach to an onclick handler to show the date picker
    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // handle the date selected
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // store the values selected into a Calendar instance
        String month = String.valueOf(monthOfYear);
        String day = String.valueOf(dayOfMonth);
        monthOfYear = monthOfYear + 1;
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        if (String.valueOf(monthOfYear).length() == 1 ) {
            month = "0" + String.valueOf(monthOfYear);
        }
        if (String.valueOf(dayOfMonth).length() == 1) {
            day = "0" + dayOfMonth;
        }
        String date = "" + year + "/" + month + "/" + day;
        mDatePicker.setText(date);
    }

    // TODO check SharedPrefs before rendering UI
    private void setSavedSettingsToUI() {
//        String date = mSharedPreferences.getString(STARTING_DATE, null);
//        if (date != null) {
//            mDatePicker.setText(date);
//        }
//        String order = mSharedPreferences.getString(SORT_ORDER, null);
//        if (order != null) {
//            mSpinner.setId(order.equals("oldest") ? 0 : 1);
//        }
//        mSharedPreferences.getString(NEWS_DESK, null);
    }
}
