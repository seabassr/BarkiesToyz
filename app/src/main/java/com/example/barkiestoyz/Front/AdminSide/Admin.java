package com.example.barkiestoyz.Front.AdminSide;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

// Admin page, to change toys in database
public class Admin extends AppCompatActivity {
    private int count;
    private int mode;
    private int buttonWidth;
    private ScrollView scrollView;
    public final DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
    private List<String> product = new ArrayList<>();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference inventoryDB = database.child("Inventory");

    // Setup of admin page
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
        scrollView = (ScrollView) findViewById(R.id.scrollView2);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        buttonWidth = size.x/2;
        mode = 1;
        updateDB();
    }

    // Get data from database, grab what's in inventory
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
                // Depending on mode, go to add or delete
                if (mode == 1) {
                    addMode();
                }
                else {
                    deleteMode();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    // Press Add, to start the add mode
    public void goAdd(View v) {
        updateDB();
        mode = 1;
        Toast.makeText(Admin.this,  "You're now in ADD MODE", Toast.LENGTH_SHORT).show();
    }

    // Press Delete, to start the delete mode
    public void goDelete(View v) {
        updateDB();
        mode = 2;
        Toast.makeText(Admin.this,  "You're now in DELETE MODE", Toast.LENGTH_SHORT).show();
    }

    // Press Update, to start the update activity
    public void goUpdate(View v) {
        Intent intent = new Intent(this, Update.class);
        this.startActivity(intent);
    }

    // Press Sign Out, to go back to the login screen
    public void goBack(View v) {
        this.finish();
    }

    // Display what's in database
    public void addMode() {
        // Have all items in separate buttons
        if(count > 0) {
            scrollView.removeAllViewsInLayout();
            GridLayout grid = new GridLayout(this);
            grid.setRowCount(count);
            grid.setColumnCount(2);
            Button[] buttons = new Button[count];
            int i = 0;
            String toyInfo;

            // Split lines, and split lines to texts and buttons
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

                grid.addView(buttons[i], buttonWidth, GridLayout.LayoutParams.WRAP_CONTENT);
                i++;
            }
            // Add button, to add product
            Button add = new Button(this);
            toyInfo = "+\n";
            add.setText(toyInfo);

            grid.addView(add, buttonWidth, GridLayout.LayoutParams.WRAP_CONTENT);

            // If button clicked, go to add activity
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Admin.this, Add.class);
                    Admin.this.startActivity(intent);
                }
            });
            // Setup up scollview
            scrollView.addView(grid);
        }
        // If no product is present, just add the add button
        else {
            scrollView.removeAllViewsInLayout();
            GridLayout grid = new GridLayout(this);
            grid.setRowCount(1);
            grid.setColumnCount(2);
            String toyInfo;

            Button add = new Button(this);
            toyInfo = "+\n";
            add.setText(toyInfo);

            grid.addView(add, buttonWidth, GridLayout.LayoutParams.WRAP_CONTENT);

            // If button clicked, go to add activity
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Admin.this, Add.class);
                    Admin.this.startActivity(intent);
                }
            });
            // Setup up scollview
            scrollView.addView(grid);
        }
    }

    // Display what's in database
    public void deleteMode() {
        // Separate the items in buttons
        if(count > 0) {
            scrollView.removeAllViewsInLayout();
            GridLayout grid = new GridLayout(this);
            grid.setRowCount(count);
            grid.setColumnCount(2);
            Button[] buttons = new Button[count];
            ButtonHandler bh = new ButtonHandler();
            int i = 0;
            String toyInfo;

            // Split lines, into texts and buttons
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

    // In delete mode, if user presses button, then remove product
    private class ButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            // Get button string
            String text = (String) ((Button)v).getText().toString().toLowerCase();
            String[] separate = text.split("\n");

            // Go to inventory
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref = db.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Inventory/" + separate[0]);

            // Remove product
            ref.removeValue();
            // Update screen
            updateDB();
        }
    }
}

