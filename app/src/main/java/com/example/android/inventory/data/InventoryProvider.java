package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.inventory.data.StoreContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int WHOLE_TABLE = 0; // Uri match for table
    public static final int SPECIFIC_ROW = 1; // Uri match for row

    // match the constants with their corresponding uris
    static {
        uriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_INVENTORY, WHOLE_TABLE);
        uriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_INVENTORY + "/#", SPECIFIC_ROW);
    }

    private StoreDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case WHOLE_TABLE:
                // if the whole table is being queried, don't modify selection
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SPECIFIC_ROW:
                // if a specific row is being queried, select only the row with the id present in the uri
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Invalid query Uri: " + uri);
        }

        // before returning the cursor returned from the database, setting a notification will notify us
        // if the row has changed in the future
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        switch (uriMatcher.match(uri)) {
            case WHOLE_TABLE:
                // we only insert if the uri is a table uri, we can't insert in a row
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Can't insert row for uri: " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        // Sanity checks
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null)
            throw new IllegalArgumentException("Item needs a name");
        Float price = values.getAsFloat(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price <= 0)
            throw new IllegalArgumentException("Item needs a valid price");
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0)
            throw new IllegalArgumentException("Item needs a valid quantity");
        byte[] image = values.getAsByteArray(InventoryEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null)
            throw new IllegalArgumentException("Item needs a valid quantity");

        // the actual insertion
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1)
            // insertion failed
            return null;

        // setting a notifier
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deletedRows;

        switch (uriMatcher.match(uri)) {
            case WHOLE_TABLE:
                // if the uri matches a table, don't modify the selection
                deletedRows = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SPECIFIC_ROW:
                // if the uri matches a specific row, select only the row with the id from the uri
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRows = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Can't delete row for Uri: " + uri);
        }
        if (deletedRows > 0)
            // notify that this row has been deleted
            getContext().getContentResolver().notifyChange(uri, null);
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        switch (uriMatcher.match(uri)) {
            case WHOLE_TABLE:
                // if the uri matches a table, don't modify the selection
                return updateItems(uri, contentValues, selection, selectionArgs);
            case SPECIFIC_ROW:
                // if the uri matches a specific row, select only the row with the id from the uri
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItems(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Can't delete row for Uri: " + uri);
        }
    }

    private int updateItems(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //if the contentValues is empty, just return already
        if (values.size() == 0)
            return 0;

        // sanity checks
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null)
                throw new IllegalArgumentException("Invalid name");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Float price = values.getAsFloat(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price <= 0)
                throw new IllegalArgumentException("Invalid price");
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || quantity < 0)
                throw new IllegalArgumentException("Invalid quantity");
        }

        // the actual update
        int rowsUpdated;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated > 0)
            // if any rows has been updated, notify
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case WHOLE_TABLE:
                return InventoryEntry.CONTENT_DIR_TYPE;
            case SPECIFIC_ROW:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Invalid Uri: " + uri);
        }
    }
}
