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

public class SettingsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    @BindView(R.id.save_button) Button btnSaveButton;
    @BindView(R.id.begin_date_picker) EditText etDatePicker;
    @BindView(R.id.sort_order_picker) Spinner spnSortOrder;
    @BindView(R.id.valueArts) CheckBox cbArtsCheckBox;
    @BindView(R.id.valueFashionStyle) CheckBox cbFashionCheckBox;
    @BindView(R.id.valueSports) CheckBox cbSportsCheckBox;

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
        applySavedSettingsToUI();

        btnSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsToSharedPreferences();
            }
        });
    }

    private String parseDate(String date) {
        date = date.replaceAll("\\D+","");
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
        etDatePicker.setText(date);
    }

    private void saveSettingsToSharedPreferences() {
        String date = parseDate(etDatePicker.getText().toString());
        String order = spnSortOrder.getSelectedItem().toString();
        StringBuilder newsDeskQuery = new StringBuilder();
        List<String> listOfNewsDesk = new ArrayList<>();
        if (cbArtsCheckBox.isChecked()) {
            listOfNewsDesk.add("\"Arts\"");
        }
        if (cbFashionCheckBox.isChecked()) {
            listOfNewsDesk.add("\"Fashion & Style\"");
        }
        if (cbSportsCheckBox.isChecked()) {
            listOfNewsDesk.add("\"Sports\"");
        }
        for(String x : listOfNewsDesk ) {
            newsDeskQuery.append(x).append(" ");
        }
        mSharedPreferences.edit()
            .putString(STARTING_DATE, date)
            .putString(SORT_ORDER, order.toLowerCase())
            .putString(NEWS_DESK, newsDeskQuery.toString())
            .apply();
        finish();
    }

    private void applySavedSettingsToUI() {
        String date = mSharedPreferences.getString(STARTING_DATE, null);
        if (date != null) {
            String formatedDate = date.substring(0,4) + "/" + date.substring(4,6) + "/" + date.substring(6);
            etDatePicker.setText(formatedDate);
        }
        String order = mSharedPreferences.getString(SORT_ORDER, null);
        if (order != null) {
            spnSortOrder.setSelection(order.equals("oldest") ? 0 : 1);
        }
        String news_desc = mSharedPreferences.getString(NEWS_DESK, null);
        if (news_desc != null) {
            if (news_desc.contains("Arts")) {
                cbArtsCheckBox.setChecked(true);
            } else {
                cbArtsCheckBox.setChecked(false);
            }
            if (news_desc.contains("Fashion")) {
                cbFashionCheckBox.setChecked(true);
            } else {
                cbFashionCheckBox.setChecked(false);
            }
            if (news_desc.contains("Sports")) {
                cbSportsCheckBox.setChecked(true);
            } else {
                cbFashionCheckBox.setChecked(false);
            }
        }

    }
}
