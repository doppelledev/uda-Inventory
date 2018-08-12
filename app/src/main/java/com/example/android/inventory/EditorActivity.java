package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.StoreContract.InventoryEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity {

    public static final int IMAGE_REQUEST_CODE = 0;
    private Bitmap old_image, new_image;
    private EditText name_et, price_et, quantity_et, description_et;
    private ImageView image_edit;
    private Uri mUri;

    private String old_name, old_price, old_quantity, old_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        name_et = findViewById(R.id.edit_name);
        price_et = findViewById(R.id.edit_price);
        quantity_et = findViewById(R.id.edit_quantity);
        description_et = findViewById(R.id.edit_description);
        image_edit = findViewById(R.id.edit_image);

        FloatingActionButton fab = findViewById(R.id.editor_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        Intent intent = getIntent();
        mUri = intent.getData();
        if (mUri == null) {
            // if the received uri is null, then the user is adding a new item
            setTitle(getString(R.string.title_add));
            fab.setImageResource(R.drawable.plus);
            old_image = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
        } else {
            // if the received uri is not null, then the user is editing an existing item
            setTitle(getString(R.string.title_edit));
            fab.setImageResource(R.drawable.ic_mode_edit);

            // query the item
            String[] projection = {
                    InventoryEntry.COLUMN_PRODUCT_IMAGE,
                    InventoryEntry.COLUMN_PRODUCT_NAME,
                    InventoryEntry.COLUMN_PRODUCT_PRICE,
                    InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                    InventoryEntry.COLUMN_PRODUCT_DESCRIPTION};
            Cursor cursor = getContentResolver().query(mUri, projection, null, null, null);
            cursor.moveToFirst();

            // get the item info from the cursor
            old_name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME));
            old_price = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_PRICE));
            old_quantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY));
            old_description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION));
            byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_IMAGE));
            old_image = BitmapUtils.getBitmap(bytes);

            // display the item info in the corresponding Views
            image_edit.setImageBitmap(old_image);
            name_et.setText(old_name);
            price_et.setText(old_price);
            quantity_et.setText(old_quantity);
            description_et.setText(old_description);
        }

    }

    /**
     * this method responds to the user selecting an image from the gallery
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                Uri imageUri = data.getData();
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    new_image = BitmapFactory.decodeStream(inputStream);
                    updateImage();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        if (inputStream != null)
                            inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // when the user clicks the fab to change the item picture
    // this method is called to let him chose from gallery
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String picDirPath = picDir.getPath();
        Uri data = Uri.parse(picDirPath);
        intent.setDataAndType(data, "image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    // this method is called when the user chooses an image from the gallery,
    // it displays the image in the ImageView
    private void updateImage() {
        image_edit.setImageBitmap(new_image);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveItem();
                return true;
            case R.id.delete:
                deleteItem();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // saves all items info into the database
    private void saveItem() {
        // sanity checks
        String name = name_et.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.no_name), Toast.LENGTH_SHORT).show();
            return;
        }
        String price_str = price_et.getText().toString();
        if (TextUtils.isEmpty(price_str)) {
            Toast.makeText(this, getString(R.string.no_price), Toast.LENGTH_SHORT).show();
            return;
        }
        Float price = Float.parseFloat(price_str);
        if (price == 0) {
            Toast.makeText(this, getString(R.string.no_price), Toast.LENGTH_SHORT).show();
            return;
        }
        String quantity_str = quantity_et.getText().toString();
        if (TextUtils.isEmpty(quantity_str)) {
            Toast.makeText(this, getString(R.string.no_quantity), Toast.LENGTH_SHORT).show();
            return;
        }
        Integer quantity = Integer.parseInt(quantity_str);
        String description = description_et.getText().toString();
        if (TextUtils.isEmpty(description))
            description = getString(R.string.no_description);
        if (new_image == null)
            new_image = old_image;

        ContentValues values = new ContentValues();
        byte[] bytes = BitmapUtils.getBytes(new_image);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, bytes);
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, description);

        if (mUri == null) {
            // if the user is adding a new item
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null)
                Toast.makeText(this, getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
        } else {
            // if the user is editing an existing item
            int savedRows = getContentResolver().update(mUri, values, null, null);
            if (savedRows > 0)
                Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.not_saved), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void deleteItem() {
        // the user wants to delete an item
        // show an alert dialog first
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want tp delete this item?")
                // if the user confirms, delete
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int rowsDeleted = getContentResolver().delete(mUri, null, null);
                        if (rowsDeleted == 1) {
                            Toast.makeText(getApplicationContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), CatalogActivity.class);
                            startActivity(intent);
                        } else
                            Toast.makeText(getApplicationContext(), getString(R.string.not_deleted), Toast.LENGTH_SHORT).show();
                    }
                })
                // if the user doesn't confirm, just dismiss the alert dialog
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null)
                            dialogInterface.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // prevent accidentally discarding data when the user clicks the back button
    @Override
    public void onBackPressed() {
        Log.d("woops", "pressed");
        if (mUri != null) {
            if (editorChanged()) {
                dialogHelper();
            } else
                finish();
        } else {
            if (adderChanged()) {
                dialogHelper();
            } else
                finish();
        }
    }

    // if the user is editing an existing item, check if some info has changed
    private boolean editorChanged() {
        image_edit.invalidate();
        if (new_image == null)
            new_image = old_image;
        return !old_name.equals(name_et.getText().toString())
                || !old_price.equals(price_et.getText().toString())
                || !old_quantity.equals(quantity_et.getText().toString())
                || !old_description.equals(description_et.getText().toString())
                || !old_image.sameAs(new_image);
    }

    // if the user is adding a new item, check if any data has been entered
    private boolean adderChanged() {
        return new_image != null
                || !TextUtils.isEmpty(name_et.getText().toString())
                || !TextUtils.isEmpty(price_et.getText().toString())
                || !TextUtils.isEmpty(quantity_et.getText().toString())
                || !TextUtils.isEmpty(description_et.getText().toString());
    }

    // method that shows the alert dialog
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.unsaved_data))
                .setPositiveButton(getString(R.string.discard), discardButtonClickListener)
                .setNegativeButton(getString(R.string.stay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null)
                            dialogInterface.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // defines what happens when the user confirms the discard
    private void dialogHelper() {
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
