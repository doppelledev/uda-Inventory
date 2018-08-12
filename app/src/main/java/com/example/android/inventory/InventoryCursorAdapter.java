package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.StoreContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);

        // set up the 'sell' button in side each item_list
        // and add logic so the quantity is never negative
        Button button_sell = rootView.findViewById(R.id.sell);
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) - 1;
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryEntry._ID));
        final Context context = rootView.getContext();
        if (quantity == -1)
            button_sell.setEnabled(false);
        else
            button_sell.setEnabled(true);
        button_sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                int rowsUpdated = context.getContentResolver().update(uri, values, null, null);
                if (rowsUpdated > 0)
                    Toast.makeText(context, context.getString(R.string.sold), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getString(R.string.not_sold), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ImageView imageView = view.findViewById(R.id.product_image);
        TextView name_tv = view.findViewById(R.id.product_name);
        TextView price_tv = view.findViewById(R.id.product_price);
        TextView quantity_tv = view.findViewById(R.id.product_quantity);
        byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_IMAGE));
        Bitmap bitmap = BitmapUtils.getBitmap(bytes);
        imageView.setImageBitmap(bitmap);
        name_tv.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_NAME)));
        price_tv.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_PRICE)) + "$");
        quantity_tv.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRODUCT_QUANTITY)));
    }

}
