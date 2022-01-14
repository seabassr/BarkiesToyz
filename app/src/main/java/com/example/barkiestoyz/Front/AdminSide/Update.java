package com.example.barkiestoyz.Front.AdminSide;

import android.graphics.Point;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.barkiestoyz.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// Admin will be able to update name and price of toy, in database
public class Update extends AppCompatActivity {
    private int count;
    private ScrollView scrollView;
    public final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private List<String> product = new ArrayList<>();
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference inventoryDB = database.child("Inventory");

    // Setup for update activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);
        scrollView = (ScrollView) findViewById(R.id.scrollView4);
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
                updateView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    // Split the data into edit texts
    public void updateView() {
        // Have all items in separate lines
        if(count > 0) {
            scrollView.removeAllViewsInLayout();
            GridLayout grid = new GridLayout(this);
            grid.setRowCount(count);
            grid.setColumnCount(3);
            EditText[][] setup = new EditText[count][2];
            Button[] buttons = new Button[count];
            ButtonHandler bh = new ButtonHandler();
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            int width = size.x;
            int i = 0;

            // Split lines to texts and buttons
            for (String display : product) {
                String[] separate;
                separate = display.split(",");
                separate[1] = separate[1].replace("{Price=", "");
                separate[1] = separate[1].replace("}", "");
                Double number = Double.parseDouble(separate[1]);
                separate[1] = MONEY.format(number);

                setup[i][0] = new EditText(this);
                setup[i][1] = new EditText(this);
                setup[i][0].setText(separate[0]);
                setup[i][1].setText(separate[1]);
                setup[i][1].setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                setup[i][0].setId(10 * i);
                setup[i][1].setId(10 * i + 1);

                buttons[i] = new Button(this);
                buttons[i].setText("Update");
                buttons[i].setId(i);
                buttons[i].setOnClickListener(bh);

                grid.addView(setup[i][0], (int) (width * .5), ViewGroup.LayoutParams.WRAP_CONTENT);
                grid.addView(setup[i][1], (int) (width * .3), ViewGroup.LayoutParams.WRAP_CONTENT);
                grid.addView(buttons[i], (int) (width * .2), ViewGroup.LayoutParams.WRAP_CONTENT);

                i++;
            }
            // Setup scrollview
            scrollView.addView(grid);
        }
    }

    // Update toy in database
    private class ButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            // Get ID of button
            int ID = v.getId();
            // Get information of product
            String text = product.get(ID);
            String[] separate;
            separate = text.split(",");

            // Get name edit text
            EditText productET = (EditText) findViewById(10 * ID);
            String newName = productET.getText().toString().toLowerCase();

            // Get price edit text
            EditText priceET = (EditText) findViewById(10 * ID + 1);
            String newPrice = priceET.getText().toString();

            // Go to inventory, to product
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference nameDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Inventory/" + separate[0].toLowerCase());
            DatabaseReference priceDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Inventory/" + newName + "/Price");
            // Remove product
            nameDB.removeValue();
            // Set price
            priceDB.setValue(newPrice);

            // Display message
            Toast.makeText(Update.this, separate[0] + " is updated", Toast.LENGTH_SHORT).show();
            // Update screen
            updateDB();
        }
    }

    // Go back to admin page
    public void goBack(View v) {
        this.finish();
    }
}