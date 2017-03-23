package com.example.android.myinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.myinventory.data.ProductContract;

/**
 * Displays list of products that were entered and stored in the app
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the product data loader
     */
    private static final int PRODUCT_LOADER = 0;
    /**
     * Adapter for the ListView
     */
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Setup FAB to open {@link EditorActivity}
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Find the ListView which will be populated with the product data
         */
        ListView productListView = (ListView) findViewById(R.id.list);

        /**
         * Find and set empty view on the ListView so that it only shows when the list has 0 items
         */
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        /**
         * Setup an adapter to create a list item for each row of product data in the Cursor
         * There is no product data yet so pass in null for the Cursor
         */
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        /**
         * Setup the item click listener
         */
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * Create new intent to go to {@link EditorActivity}
                 */
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                /**
                 * Form the content URI that represents the specific product that was clicked on by appending the id
                 */
                Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);

                /**
                 * Set the URI on the data field of the intent
                 */
                intent.setData(currentProductUri);

                /**
                 * Launch the {@link EditorActivity} to display the data for the current product
                 */
                startActivity(intent);
            }
        });

        /**
         * Kick off the loader
         */
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    /**
     * Helper method to delete all products in the db
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from product db");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_all_entries) {
            deleteAllProducts();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /**
         * Define a projection that specifies the columns from the table we care about
         */
        String[] projection = {ProductContract.ProductEntry._ID, ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY};

        /**
         * This loader will execute the ContentProvider's query method on a background thread
         */
        return new CursorLoader(this, ProductContract.ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /**
         * Update {@link ProductCursorAdapter} with this new cursor containing updated pet data
         */
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /**
         * Callback called when the data needs to be deleted
         */
        mCursorAdapter.swapCursor(null);
    }
}
