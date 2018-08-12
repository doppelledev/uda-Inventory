package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.StoreContract.InventoryEntry;

public class DetailActivity extends AppCompatActivity {

    public int quantity;
    public String name;
    public Button minus;
    public Uri mUri;
    TextView quantity_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mUri = intent.getData();
        String[] projection = {InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION};
        Cursor cursor = getContentResolver().query(mUri, projection, null, null, null);
        cursor.moveToFirst();
        String title = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
        setTitle(title);
        quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        Button plus = findViewById(R.id.plus);
        minus = findViewById(R.id.minus);

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, ++quantity);
                getContentResolver().update(mUri, values, null, null);
                minus.setEnabled(quantity > 0);
                quantity_tv.setText(String.valueOf(quantity));
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, --quantity);
                getContentResolver().update(mUri, values, null, null);
                minus.setEnabled(quantity > 0);
                quantity_tv.setText(String.valueOf(quantity));
            }
        });

        populateViews();
    }

    @Override
    protected void onResume() {
        populateViews();
        minus.setEnabled(quantity > 0);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_edit:
                Intent intent = new Intent(this, EditorActivity.class);
                intent.setData(mUri);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateViews() {
        ImageView product_image = findViewById(R.id.detail_image);
        TextView name_tv = findViewById(R.id.detail_name);
        TextView price_tv = findViewById(R.id.detail_price);
        quantity_tv = findViewById(R.id.detail_quantity);
        TextView description_tv = findViewById(R.id.detail_description);

        // query the database for the specific item
        String[] projection = {
                InventoryEntry.COLUMN_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION};
        Cursor cursor = getContentResolver().query(mUri, projection, null, null, null);
        cursor.moveToFirst();

        // get item info from the returned cursor
        byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_IMAGE));
        Bitmap bitmap = BitmapUtils.getBitmap(bytes);
        product_image.setImageBitmap(bitmap);
        quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
        name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_PRICE)) + "$";
        String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION));

        // display item info in the corresponding TextViews
        name_tv.setText(name);
        price_tv.setText(price);
        quantity_tv.setText(String.valueOf(quantity));
        description_tv.setText(description);

        // set a button to search amazon for the item name
        Button order_button = findViewById(R.id.order);
        order_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.amazon.com/s?url=search-alias%3Daps&field-keywords=" + name);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }
}
