package com.aman.olx.Activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.aman.olx.R;

public class FilterActivity extends AppCompatActivity {

    ImageView back_imageview;
    Button save_button;
    EditText mCity, mStateProvince, mCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        back_imageview=(ImageView)findViewById(R.id.backArrow);
        save_button=(Button)findViewById(R.id.btnSave);
        mCity=(EditText)findViewById(R.id.input_city);
        mCountry=(EditText)findViewById(R.id.input_country);
        mStateProvince=(EditText)findViewById(R.id.input_state_province);

        init();
    }

    private void init() {
        getFilterPreferences();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(FilterActivity.this);
                preferences.edit().putString(getString(R.string.preferences_city),mCity.getText().toString()).apply();
                preferences.edit().putString(getString(R.string.preferences_country),mCountry.getText().toString()).apply();
                preferences.edit().putString(getString(R.string.preferences_state_province),mStateProvince.getText().toString()).apply();
            }
        });

        back_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void getFilterPreferences() {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String country= preferences.getString(getString(R.string.preferences_country),"");
        String state=preferences.getString(getString(R.string.preferences_state_province),"");
        String city=preferences.getString(getString(R.string.preferences_city),"");

        mStateProvince.setText(state);
        mCountry.setText(country);
        mCity.setText(city);

    }

}
