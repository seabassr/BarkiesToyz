package com.example.barkiestoyz.Front.CustomerSide;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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

// Create list, with customer purchase list
public class Payment extends AppCompatActivity {
    private int count;
    private String userCode;
    private String userName;
    private TextView TotalView;
    private TextView TaxView;
    private ScrollView scrollView;
    public final DecimalFormat MONEY = new DecimalFormat("$#,##0.00");
    private List<String> cart = new ArrayList<>();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference cartDB;

    // Setup for payment activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);
        scrollView = (ScrollView) findViewById(R.id.scrollView3);

        // Grab user's code, and access their list
        userCode = "";
        Intent code = getIntent();
        userCode = code.getStringExtra("userPassed");

        cartDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + userCode);
        updateDB();
    }

    // Update page
    protected void onResume()
    {
        super.onResume();
        updateDB();
    }

    // Go to database, and obtain user's list
    public void updateDB() {
        cartDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    cart.clear();
                    count = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String input = " " + snapshot.getValue();
                        cart.add(input);
                        count++;
                    }
                }
                updateView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Payment.this,  " " + error.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Display user's list
    public void updateView() {
        // Remove status from list
        cart.remove(count - 1);
        count--;

        // Grab username
        userName = cart.get(count - 1);

        // remove username from list
        cart.remove(count - 1);
        count--;

        // Have all items in separate text view
        if(count > 0) {
            TotalView = (TextView) findViewById(R.id.total);
            TaxView = (TextView) findViewById(R.id.tax);
            scrollView.removeAllViewsInLayout();
            GridLayout grid = new GridLayout(this);
            grid.setRowCount(count);
            grid.setColumnCount(5);
            TextView[][] setup = new TextView[count][3];
            Button[][] buttons = new Button[count][2];
            ButtonHandler bh = new ButtonHandler();
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            int width = size.x;
            int i = 0;
            double total = 0;
            double tax = 0;

            // Split text, and input them into Text views, include - and + buttons
            for (String display : cart) {
                String[] separate;
                separate = display.split(",");

                setup[i][0] = new TextView(this);
                setup[i][1] = new TextView(this);
                setup[i][2] = new TextView(this);
                setup[i][0].setText(separate[0].toUpperCase());
                setup[i][1].setText(separate[1]);
                setup[i][2].setText(separate[2]);
                setup[i][0].setId(10 * i);
                setup[i][1].setId(10 * i + 1);
                setup[i][2].setId(10 * i + 2);

                buttons[i][0] = new Button(this);
                buttons[i][0].setText("-");
                buttons[i][0].setId(i);
                buttons[i][0].setOnClickListener(bh);

                buttons[i][1] = new Button(this);
                buttons[i][1].setText("+");
                buttons[i][1].setId(i);
                buttons[i][1].setOnClickListener(bh);

                grid.addView(setup[i][0], (int) (width * .5), ViewGroup.LayoutParams.WRAP_CONTENT);
                grid.addView(setup[i][1], (int) (width * .2), ViewGroup.LayoutParams.WRAP_CONTENT);
                grid.addView(buttons[i][0], (int) (width * .1), ViewGroup.LayoutParams.WRAP_CONTENT);
                grid.addView(setup[i][2], (int) (width * .1), ViewGroup.LayoutParams.WRAP_CONTENT);
                grid.addView(buttons[i][1], (int) (width * .1), ViewGroup.LayoutParams.WRAP_CONTENT);

                String temp = separate[1].replace("$", "");
                total += (Double.parseDouble(temp) * Integer.parseInt(separate[2]));

                i++;
            }
            // Grab total, and calculate tax
            tax = (total * .0825);
            // Grab tax and total, and add them together
            total = total + tax;

            // Setup scrollview and text views
            scrollView.addView(grid);
            TaxView.setText(MONEY.format(tax));
            TotalView.setText(MONEY.format(total));
        }
    }

    // When button is clicked, get button text, and product name
    private class ButtonHandler implements View.OnClickListener
    {
        public void onClick(View v)
        {
            int quantity = 0;
            int position = 0;
            int found = 0;
            String text = (String) ((Button)v).getText().toString();
            int ID = (Integer) ((Button)v).getId();
            String name = cart.get(ID);
            String[] separate;
            separate = name.split(",");

            // If product exist in list, update list
            for (String check : cart) {
                String[] product = check.split(",");
                product[0] = product[0].toLowerCase();

                // If -, remove quantity, else, add quantity
                if (separate[0].equals(product[0])) {
                    quantity = Integer.parseInt(product[2]);

                    if (text.equals("-")) {
                        quantity -= 1;
                    }
                    else {
                        quantity += 1;
                    }

                    found = position;
                    break;
                }
                position++;
            }

            // Remove old listed item
            cart.remove(found);

            // If quantity isn't zero, update product list
            if (quantity > 0) {
                String input = (separate[0] + "," + separate[1] + "," + quantity);
                cart.add(input);
            }

            // Connect database to cart list, status, and username
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference cartDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + userCode);
            DatabaseReference statusDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + userCode + "/Status");
            DatabaseReference nameDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + userCode + "/Name");

            // Set value for cart list, status, and username
            cartDB.setValue(cart);
            statusDB.setValue("IN PROGRESS");
            nameDB.setValue(userName);

            // Update display
            updateDB();
        }
    }

    // When pressed, end activity
    public void goBack(View v) {
        this.finish();
    }

    // When pressed, update status, send user code, start customer confirmation activity, and end this activity
    public void goCash(View v) {
        DatabaseReference statusDB = database.getReferenceFromUrl("https://barkiestoyz-e35e7.firebaseio.com/Orders/" + userCode + "/Status");
        statusDB.setValue("Pickup");

        Intent myIntent = new Intent(Payment.this, CustomerConfirmation.class);
        myIntent.putExtra("codePassed", userCode);
        this.startActivity(myIntent);
        Payment.this.finish();
    }
}
