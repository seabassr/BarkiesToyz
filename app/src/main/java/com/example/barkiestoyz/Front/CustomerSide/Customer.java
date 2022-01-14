package com.example.barkiestoyz.Front.CustomerSide;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.barkiestoyz.Back.Button;
import com.example.barkiestoyz.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Customer will be able to pick and add product to list
public class Customer extends AppCompatActivity {
    private int count;
    private int buttonWidth;
    private String code = "";
    private String username;
    private ScrollView scrollView;
    public final DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
    private List<String> product = new ArrayList<>();
    private List<String> purchase = new ArrayList<>();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference inventoryDB = database.child("Inventory");
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference cartDB;

    // Setup customer page
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        buttonWidth = size.x/2;

        Random rand = new Random();
        for (int i = 0; i < 9; i++) {
            int num = rand.nextInt(10);
            code += Integer.toString(num);
        }

        // Grab user code, and access their list
        username = "";
        Intent name = getIntent();
        username = name.getStringExtra("userPassed");

        cartDB = db .getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + code);

        updateDB();
    }

    // Update page
    protected void onResume()
    {
        super.onResume();
        updateDB();
    }

    // Get data from database, get what's in inventory
    public void updateDB() {
        inventoryDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    count = 0;
                    product.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String input = snapshot.getKey() + "," + snapshot.getValue();
                        product.add(input);
                        count++;
                    }
                }
                updateView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    // Display data from database
    public void updateView() {
        // Have all items in separate buttons
        if(count > 0) {
            scrollView.removeAllViewsInLayout();
            GridLayout grid = new GridLayout(this);
            grid.setRowCount(count);
            grid.setColumnCount(2);
            Button[] buttons = new Button[count];
            ButtonHandler bh = new ButtonHandler();
            int i = 0;
            String toyInfo;

            // Split products into buttons
            for (String display : product) {
                String[] separate;
                separate = display.split(",");
                separate[1] = separate[1].replace("{Price=", "");
                separate[1] = separate[1].replace("}", "");
                Double number = Double.parseDouble(separate[1]);
                separate[1] = MONEY.format(number);

                buttons[i] = new Button(this);
                toyInfo = separate[0] + "\n" + separate[1];
                buttons[i].setText(toyInfo);
                buttons[i].setId(i);
                buttons[i].setOnClickListener(bh);

                grid.addView(buttons[i], buttonWidth, GridLayout.LayoutParams.WRAP_CONTENT);
                i++;
            }
            scrollView.addView(grid);
        }
    }

    // When button is clicked, display message, and add product to list
    private class ButtonHandler implements View.OnClickListener
    {
        public void onClick(View v)
        {
            // Split text from button
            int quantity = 1;
            int position = 0;
            int found = 0;
            boolean remove = false;
            String text = (String) ((Button)v).getText().toString().toLowerCase();
            String[] separate = text.split("\n");
            separate[0] = separate[0].toLowerCase();

            // Make sure there's no copies
            for (String check : purchase) {
                String[] name = check.split(",");
                name[0] = name[0].toLowerCase();

                if (separate[0].equals(name[0])) {
                    quantity = Integer.parseInt(name[2]);
                    quantity += 1;
                    found = position;
                    remove = true;
                    break;
                }
                position++;
            }

            // Remove old item
            if (remove == true) {
                purchase.remove(found);
            }

            // Put in the new item
            String cart = (separate[0] + "," + separate[1] + "," + quantity);
            purchase.add(cart);

            // Setting locations of cart list, status and name
            DatabaseReference cartDB = db.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + code);
            DatabaseReference statusDB = db.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + code + "/Status");
            DatabaseReference nameDB = db.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + code + "/Name");

            cartDB.setValue(purchase);
            statusDB.setValue("IN PROGRESS");
            nameDB.setValue(username);

            // Display message
            String output = (separate[0].toUpperCase() + " added to cart");
            Toast.makeText(Customer.this, output, Toast.LENGTH_LONG).show();
        }
    }

    // Setup toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // When icon is click on toolbar, start activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_map:
                Intent addIntent = new Intent(this, Map.class);
                this.startActivity(addIntent);
                return true;
            case R.id.action_signout:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // When clicked, add list to database, pass user code, and start payment activity
    public void goPay(View v) {
        Intent myIntent = new Intent(this, Payment.class);
        myIntent.putExtra("userPassed", code);
        this.startActivity(myIntent);
    }
}