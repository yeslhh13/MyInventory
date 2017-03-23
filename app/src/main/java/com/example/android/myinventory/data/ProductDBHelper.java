package com.example.android.myinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kat on 2017-03-22.
 */

/**
 * DB helper for inventory app
 * Manages db creation and version management
 */
public class ProductDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ProductDBHelper.class.getSimpleName();
    /**
     * Name of the db file
     */
    public static final String DATABASE_NAME = "inventory.db";
    /**
     * DB version
     * To change the db schema, you must increment the db version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ProductDBHelper}
     *
     * @param context of the app
     */
    public ProductDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when db is create for the first time
     *
     * @param db to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * Create a String that contains the SQL statement to create the products table
         */
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME
                + " (" + ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE + " BLOB NOT NULL);";

        /**
         * execute the SQL statement
         */
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    /**
     * Called when db needs to be upgraded
     * As the db is still at version 1, there's nothing to do be done here
     *
     * @param db to be upgraded
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
