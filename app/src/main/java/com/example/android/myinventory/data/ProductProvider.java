package com.example.android.myinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Kat on 2017-03-22.
 */

/**
 * {@link ContentProvider} for inventory app
 */
public class ProductProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;
    /**
     * URI matcher code for the content URI for a single product int the products table
     */
    private static final int PRODUCT_ID = 101;

    /**
     * {@link UriMatcher} object to match a content URI to a corresponding code
     * The input passed into the constructor represents the code to return for the root URI
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Static initializer
     * Run the first time anything is called from this class
     */
    static {
        /**
         * Provide access to MULTIPLE rows of the products table
         */
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        /**
         * Provide access to ONE single row of the products table
         */
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * DB helper object
     */
    private ProductDBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        mDBHelper = new ProductDBHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        /**
         * Get readable db
         */
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        /**
         * This will hold the result of the query
         */
        Cursor cursor;

        /**
         * Figure out if the URI matcher can match the URI to a specific code
         */
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                /**
                 * Query the products table directly with the given projection, selection, selection arguments, and sort order
                 * The cursor could contain multiple rows of the products table
                 */
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                /**
                 * Extract the ID from the URI
                 * For every "?" in the selection, we need to have an element in the selection arguments that will fill in the "?"
                 * Since we have 1 question mark in the selection, we have 1 String in the selection arguments' String array
                 */
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                /**
                 * This will perform a query on the products table where the _id equals 3 to return Cursor containing that row of the table
                 */
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        /**
         * Set notification URI on the cursor
         * If the data at this URI changes, then we know we need to update the Cursor
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the db with the given content values
     *
     * @return a new content URI for the specific row in the db
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        /**
         * Check that the name is not null
         */
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null)
            throw new IllegalArgumentException("Product requires a name");

        /**
         * Check that the quantity is valid
         */
        Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0)
            throw new IllegalArgumentException("Product requires valid quantity");

        /**
         * Check that the price is valid
         */
        Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0)
            throw new IllegalArgumentException("Product requires valid price");

        /**
         * Get the writable db
         */
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        /**
         * Insert the new product with the given values
         */
        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
        /**
         * If the ID is -1, then the insertion is failed
         */
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        /**
         * Notify all listeners that the data has changed for the product content URI
         */
        getContext().getContentResolver().notifyChange(uri, null);

        /**
         * Return the new URI with the ID of the newly inserted row appended at the end
         */
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        /**
         * Get writable db
         */
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        /**
         * Track the number of rows that were deleted
         */
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                /**
                 * Delete all rows that match the selection and selection arguments
                 */
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                /**
                 * Delete a single row given by the ID in the URI
                 */
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        /**
         * If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
         */
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        /**
         * Return the number of rows deleted
         */
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                /**
                 * For the PRODUCT_ID code, extract the ID from the URI to know which row to update
                 * Selection will be "_id=?" and selection arguments will be a String array containing the actual ID
                 */
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the db with the given content values
     *
     * @return the number of rows that were successfully updated
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        /**
         * If the {@link com.example.android.myinventory.data.ProductContract.ProductEntry#COLUMN_PRODUCT_NAME} key is present,
         * check that the name value is not null
         */
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null)
                throw new IllegalArgumentException("Product requires a name");
        }

        /**
         * If the {@link com.example.android.myinventory.data.ProductContract.ProductEntry#COLUMN_PRODUCT_QUANTITY} key is present,
         * check that the quantity value is valid
         */
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0)
                throw new IllegalArgumentException("Product requires valid quantity");
        }

        /**
         * If the {@link com.example.android.myinventory.data.ProductContract.ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
         * check that the price value is valid
         */
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0)
                throw new IllegalArgumentException("Product requires valid price");
        }

        /**
         * If there are no values to update, then don't try to update the db
         */
        if (values.size() == 0)
            return 0;

        /**
         * Otherwise, get the writable db to update the data
         */
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        /**
         * Perform the update on the db and get the number of rows affected
         */
        int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        /**
         * If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
         */
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        /**
         * Return the number of rows updated
         */
        return rowsUpdated;
    }
}
