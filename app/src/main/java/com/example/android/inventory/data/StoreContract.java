package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class StoreContract {

    /**
     * These strings are used to build Uris for each table in the database
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    // not instantiable
    public StoreContract() {
    }

    /**
     * Inventory table
     */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * Strings used in getType() function of the content provider
         */
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The Uri to address this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The name of the table
         */
        public static final String TABLE_NAME = "inventory";

        /**
         * The unique id fro each product in the table
         * type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * The picture representing the product
         * type: BLOB
         */
        public static final String COLUMN_PRODUCT_IMAGE = "image";

        /**
         * the name of each product in the table
         * type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "name";

        /**
         * The quantity of each product in the table
         * type: INTEGER
         */
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * The price of each product in the table
         * type: REAL
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * A short description of he product
         * type: TEXT
         */
        public static final String COLUMN_PRODUCT_DESCRIPTION = "description";

    }
}
