package com.example.barkiestoyz.Front;

/*
Francisco Romo
Jacob Garza
Sebastian Rodriguez

Dr. Jeong Yang
Nov 17, 2020
CSCI 4325_001

This application will allow the admin to add, delete and update dog toys from the database. While,
allowing customers to purchase toys, and locate a store. Customers will able to place orders, with
a confirmation code. For new users, they will be able to make an account with a username and
password, for admin access, that requires access to database.
*/

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.barkiestoyz.Front.AdminSide.Admin;
import com.example.barkiestoyz.Front.CustomerSide.Customer;
import com.example.barkiestoyz.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Main extends AppCompatActivity {
    private String pw;
    private String type;
    private String userString;
    private String passwordString;
    private EditText hidePassword;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Start of program
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Hide characters in password
        hidePassword = (EditText) findViewById(R.id.input_password);
        hidePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    // When login button is pressed
    public void goLogin(View v) {
        // Get username
        EditText userET = findViewById(R.id.input_user);
        userString = userET.getText().toString().toLowerCase();

        // Get password
        EditText passwordET = findViewById(R.id.input_password);
        passwordString = passwordET.getText().toString();

        // Go to users
        DatabaseReference userDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/");

        // Check if user exist
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userString)) {
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    final DatabaseReference profileDB = db.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/" + userString + "/Password");

                    // Check if user password is correct
                    profileDB.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            pw = (String) snapshot.getValue();

                            if (!passwordString.equals(pw)) {
                                Toast.makeText(Main.this,  "Username/Password is incorrect", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                final DatabaseReference typeDB = db.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Users/" + userString + "/Type");

                                // Check what type user, the user is
                                typeDB.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot shot) {
                                        type = (String) shot.getValue();

                                        if(type.equals("ADMIN")) {
                                            Intent intent = new Intent(Main.this, Admin.class);
                                            startActivity(intent);
                                        }
                                        else if(type.equals("client")) {
                                            Intent intent = new Intent(Main.this, Customer.class);
                                            intent.putExtra("userPassed", userString);
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError database) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                else {
                    Toast.makeText(Main.this,  "This user doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        // Erase text from edit texts
        userET.setText("");
        passwordET.setText("");
    }

    // When sign up button is pressed, go to sign up activity
    public void goCreate(View v) {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }
}
