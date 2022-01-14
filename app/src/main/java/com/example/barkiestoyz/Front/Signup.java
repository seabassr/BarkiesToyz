package com.example.barkiestoyz.Front;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.barkiestoyz.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Customer will be able to create an account with Barkies Toyz
public class Signup extends AppCompatActivity {
    private boolean check;
    private String userString;
    private String passwordString;
    private String verifyingString;
    private EditText hidePassword;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Setup for sign up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Hide text from edit texts
        hidePassword = (EditText) findViewById(R.id.create_password);
        hidePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        hidePassword = (EditText) findViewById(R.id.check_password);
        hidePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    public void goCreate(View v) {
        check = false;

        // Grab edit text from username
        EditText userET = findViewById(R.id.create_user);
        userString = userET.getText().toString().toLowerCase();

        // Grab edit text from password
        EditText passwordET = findViewById(R.id.create_password);
        passwordString = passwordET.getText().toString();

        // Grab edit text from verifying password
        EditText verifyingET = findViewById(R.id.check_password);
        verifyingString = verifyingET.getText().toString();

        // Go to users
        DatabaseReference checkDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/");

        if (!passwordString.equals(verifyingString)) {
            // Display message
            Toast.makeText(Signup.this, "MAKE SURE PASSWORDS MATCH", Toast.LENGTH_LONG).show();
        }
        else {
            // Go to users
            DatabaseReference userDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/" + userString);
            // Go to users password
            DatabaseReference passwordDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/" + userString + "/Password");
            // Go to users type
            DatabaseReference typeDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/" + userString + "/Type");

            // Set username
            userDB.setValue(userString);
            // Set password
            passwordDB.setValue(verifyingString);
            // Set type
            typeDB.setValue("client");

            // Display message
            Toast.makeText(Signup.this, userString + " is now a customer", Toast.LENGTH_LONG).show();
        }

        // Erase text from edit texts
        userET.setText("");
        passwordET.setText("");
        verifyingET.setText("");
    }

    // When exit is pressed, leave sign up, goes back to login
    public void goBack(View v) {
        this.finish();
    }
}
