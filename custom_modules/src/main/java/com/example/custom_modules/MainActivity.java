package com.example.custom_modules;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.io.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button btnAdd;
    EditText editTextItem;
    RecyclerView rvItems;
    itemsAdapter iAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("ToDo App");

        btnAdd = findViewById(R.id.btnAdd);
        editTextItem = findViewById(R.id.editTextItem);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        itemsAdapter.OnLongClickListener onLongClickListener = new itemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position) {
                //delete the item from the model
                items.remove(position);
                //notify the adapter;
                iAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };

        itemsAdapter.OnClickListener  onClickListener = new itemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single clicked at position" + position);
                // create the new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);

                //pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);

                //display the activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };

        iAdapter = new itemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(iAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = editTextItem.getText().toString();

                //add item to the model
                items.add(todoItem);

                //notify adapter that an item is inserted
                iAdapter.notifyItemInserted(items.size()-1);
                editTextItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            //retreive the updated text value
            String itemText = data.getStringExtra((KEY_ITEM_TEXT));

            //extract the original position of the edited item from the position key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            //update the model at the right positon with new item text
            items.set(position, itemText);

            //notify the adapter
            iAdapter.notifyItemChanged(position);

            //presit the changes
            saveItems();
            Toast.makeText(getApplicationContext(), "item updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile(){
        return new File(getFilesDir(), "data.txt");
    }

    //to read and load the data in the file
    private void loadItems(){
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e){
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }

    //to write to the file
    private void saveItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e){
            Log.e("MainActivity", "Error writing items", e);
        }
    }
}