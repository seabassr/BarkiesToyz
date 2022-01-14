package com.example.barkiestoyz.Front.CustomerSide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.barkiestoyz.Front.Main;
import com.example.barkiestoyz.R;

// Customer confirmation code will be displayed here
public class CustomerConfirmation extends AppCompatActivity  {
    private String code;
    private TextView textView;

    // Setup for customer confirmation activity
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_comfirmation);
        textView = (TextView) findViewById(R.id.code);
        code = "";
        Intent userCode = getIntent();
        code = userCode.getStringExtra("codePassed");
        textView.setText(code);
    }

    // When pressed, end this activity, goes back to customer activity
    public void goBack(View v) {
        this.finish();
    }
}
