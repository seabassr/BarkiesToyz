package com.example.barkiestoyz.Front.AdminSide;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.barkiestoyz.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Admin will be able to add toyz into database
public class Add extends AppCompatActivity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    // Setup for add activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
    }

    // Add toys to database
    public void add(View v) {
        // Take name and price, and add into the database
        EditText nameET = findViewById(R.id.input_name);
        String name = nameET.getText().toString().toLowerCase();

        EditText priceET = findViewById(R.id.input_price);
        String priceString = priceET.getText().toString();
        double price = Double.parseDouble(priceString);

        // Go to inventory, and create product and product price
        DatabaseReference priceDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Inventory/" + name + "/Price");

        priceDB.setValue(price);

        // Display message
        Toast.makeText(this, name + " is added", Toast.LENGTH_LONG).show();

        // Erase edit texts
        nameET.setText("");
        priceET.setText("");
    }

    // Go back to the main activity
    public void goBack(View v) {
        this.finish();
    }
}