package com.example.android.myinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Kat on 2017-03-22.
 */

/**
 * API contract for the inventory app
 */
public final class ProductContract {
    /**
     * To prevent from accidentally instantiating the contract class
     */
    private ProductContract() {
    }

    /**
     * Name for the entire content provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.myinventory";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which will use to contract the content provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path appended to base content URI for possible URI's
     */
    public static final String PATH_PRODUCTS = "products";

    /**
     * Inner class that defines constant values for the products db table
     * Each entry in the table represents a single product
     */
    public static final class ProductEntry implements BaseColumns {
        /**
         * The content URI to access the product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        /**
         * Name of db table for products
         */
        public static final String TABLE_NAME = "products";

        /**
         * Unique ID number for the product
         * Only for use in the db table
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;
        /**
         * Name of the product
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME = "name";
        /**
         * Price of the product
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";
        /**
         * Quantity of the product
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        /**
         * Picture of the product
         * <p>
         * Type: BLOB
         */
        public final static String COLUMN_PRODUCT_PICTURE = "picture";
    }
}
