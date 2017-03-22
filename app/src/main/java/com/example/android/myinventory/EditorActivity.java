package com.example.android.myinventory;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myinventory.data.ProductContract;

/**
 * Created by Kat on 2017-03-22.
 */

/**
 * Allows user to create a new product or edit an existing one
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Content URI for the existing product(null if it's a new product)
     */
    private Uri mCurrentUri;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;
    /**
     * EditText Field to enter the product's price
     */
    private EditText mPriceEditText;
    /**
     * TextView field to show the product's quantity
     */
    private TextView mQuantityTextView;
    /**
     * Button field to increase the product's quantity
     */
    private Button mSaleButton;
    /**
     * Button field to decrease the product's quantity
     */
    private Button mOrderButton;

    /**
     * Boolean flag that keeps track of whether the product has been edited(true) or not(false)
     */
    private boolean mProductHasChanged = false;

    private static final int SALE_BUTTON = 0;
    private static final int ORDER_BUTTON = 1;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying the view
     * and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /**
         * Examine the intent that was used to launch this activity
         * in order to figure out if we're creating a new one or editing an existing one
         */
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        /**
         * If the intent does not contain a product content URI, then we know that we are creating a new one
         */
        if (mCurrentUri == null) {
            /**
             * Change the app bar title to say "Add a Product"
             */
            setTitle(getString(R.string.editor_activity_title_new_product));
            /**
             * Invalidate the options menu, so the "Delete" menu option can be hidden
             */
            invalidateOptionsMenu();
        } else {
            /**
             * Change the app bar title to say "Edit Product"
             */
            setTitle(getString(R.string.editor_activity_title_edit_product));
            /**
             * Initialize a loader to read the product data from the db and display the current values in the editor
             */
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        /**
         * Find all relevant views that we will need to read user input from
         */
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityTextView = (TextView) findViewById(R.id.show_product_quantity);
        mSaleButton = (Button) findViewById(R.id.button_sale_product);
        mOrderButton = (Button) findViewById(R.id.button_order_product);

        mSaleButton.setVisibility(View.GONE);
        mOrderButton.setVisibility(View.GONE);

        /**
         * Setup OnTouchListeners on all input field, so we can determine if the user has touched or modified them
         * This will let us know if there are unsaved changes or not, if the user tries to leave the editor without saving
         */
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        if (mCurrentUri != null) {
            /**
             * Show sale button and order button only when the product already exists
             */
            mSaleButton.setVisibility(View.VISIBLE);
            mOrderButton.setVisibility(View.VISIBLE);

            mSaleButton.setOnTouchListener(mTouchListener);
            mOrderButton.setOnTouchListener(mTouchListener);

            mSaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saleOrOrder(SALE_BUTTON);
                }
            });

            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saleOrOrder(ORDER_BUTTON);
                }
            });
        }
    }

    /**
     * Increase or decrease the quantity value of the product by knowing what button has been pressed
     */
    private void saleOrOrder(int saleOrOrder) {
        /**
         * Create and initialize the quantity value
         */
        int quantity = 0;

        /**
         * If {@link #saleOrOrder(int)} is {@link #SALE_BUTTON}, decrease the quantity value
         * If {@link #saleOrOrder(int)} is {@link #ORDER_BUTTON}, increase the quantity value
         */
        if (saleOrOrder == SALE_BUTTON) {
            if (Integer.parseInt(mQuantityTextView.getText().toString().trim()) == 0) {
                Toast.makeText(this, getString(R.string.cannot_sale), Toast.LENGTH_SHORT).show();
                return;
            } else
                quantity = Integer.parseInt(mQuantityTextView.getText().toString().trim()) - 1;
        } else if (saleOrOrder == ORDER_BUTTON)
            quantity = Integer.parseInt(mQuantityTextView.getText().toString().trim()) + 1;
        else
            Toast.makeText(this, getString(R.string.toast_invalid_button_value), Toast.LENGTH_SHORT).show();

        mQuantityTextView.setText(String.valueOf(quantity));
    }

    /**
     * Get user input from editor and save product into db
     */
    private void saveProduct() {
        /**
         * Read from input fields
         * Use trim to eliminate leading or trailing white space
         */
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        /**
         * Check if this is supposed to be a new pet and check if all fields are blank
         */
        if (mCurrentUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString))
            return;

        /**
         * Create a {@link ContentValues} object where column names are the keys and product attributes from the editor are the values
         */
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);

        /**
         * If the quantity 0(new product), don't try to parse the string into an integer value, just use 0 by default.
         */
        int quantity = 0;
        if (!mQuantityTextView.getText().toString().trim().equals(""))
            quantity = Integer.parseInt(mQuantityTextView.getText().toString().trim());

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        /**
         * Determine if this is a new or existing product by checking if {@link #mCurrentUri} is null or not
         */
        if (mCurrentUri == null) {
            /**
             * It is a new product, so insert a new product into the provider, returning the content URI for new product
             */
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null)
            /**
             * There was an error with insertion
             */
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
            else
            /**
             * Otherwise, the insertion was successful
             */
                Toast.makeText(this, getString(R.string.editor_insert_product_successful), Toast.LENGTH_SHORT).show();
        } else {
            /**
             * This is an existing product, so update the product with content URI: {@link #mCurrentUri}
             * and pass in the new {@link ContentValues}
             */
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            if (rowsAffected == 0)
            /**
             * There was an error with updating
             */
                Toast.makeText(this, getString(R.string.editor_update_product_failed), Toast.LENGTH_SHORT).show();
            else
            /**
             * Otherwise, the update was successful
             */
                Toast.makeText(this, getString(R.string.editor_update_product_successful), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /**
         * Inflate the menu options from the res/menu/menu_editor.xml file
         * This adds menu items to the app bar
         */
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after {@link #invalidateOptionsMenu()}, so that the menu can be updated
     *
     * @param menu to be updated
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        /**
         * If this is a new product, hide the "Delete" menu item
         */
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * User clicked on a menu option in the app bar overflow menu
         */
        switch (item.getItemId()) {
            case R.id.action_save:
                /**
                 * Save product to db
                 */
                saveProduct();
                /**
                 * Exit activity
                 */
                finish();
                return true;
            case R.id.action_delete:
                /**
                 * Pop up confirmation dialog for deletion
                 */
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                /**
                 * If the product hasn't changed, continue with navigating up to parent activity, which is the {@link MainActivity}
                 */
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                /**
                 * Otherwise if there are unsaved changes, setup a dialog to warn the user.
                 */
                DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                /**
                 * Show a dialog that notifies the user they have unsaved changes
                 */
                showUnsavedChangesDialog(discardButtonListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        /**
         * If the product hasn't changed, continue with handling back button press
         */
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        /**
         * Otherwise if there are unsaved changes, setup a dialog to warn the user.
         */
        DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        /**
         * Show dialog that there are unsaved changes
         */
        showUnsavedChangesDialog(discardButtonListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /**
         * Define a projection that contains all columns from the product table
         */
        String[] projection = {ProductContract.ProductEntry._ID, ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY};

        /**
         * This loader will execute the {@link android.content.ContentProvider}'s query method on a background thread
         */
        return new CursorLoader(this, mCurrentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /**
         * Bail early if the cursor is null or there is less than 1 row in the cursor
         */
        if (data == null || data.getCount() < 1)
            return;

        /**
         * Proceed with moving to the first row of the cursor and reading data from it
         */
        if (data.moveToFirst()) {
            /**
             * Find the columns of product attributes that we're interested in
             */
            int nameColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

            /**
             * Extract out the value from the Cursor and update the views on the screen with the values from the db
             */
            mNameEditText.setText(data.getString(nameColumnIndex));
            mPriceEditText.setText(data.getString(priceColumnIndex));
            mQuantityTextView.setText(data.getString(quantityColumnIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /**
         * If the loader is invalidated, clear out all the data from the input fields
         */
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("0");
    }

    /**
     * Prompt the user to confirm that they want to delete this product
     */
    private void showDeleteConfirmationDialog() {
        /**
         * Create an {@link android.support.v7.app.AlertDialog.Builder} and set the message and click listeners
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProduct();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        /**
         * Create and show the {@link AlertDialog}
         */
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the db
     */
    private void deleteProduct() {
        /**
         * Only perform the delete if this is an existing product
         */
        if (mCurrentUri != null) {
            /**
             * Call the {@link android.content.ContentResolver} to delete the product at the given content URI
             */
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            if (rowsDeleted == 0)
            /**
             * There was an error with deletion
             */
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            else
            /**
             * Otherwise, the delete was successful
             */
                Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();

            /**
             * Close the activity
             */
            finish();
        }
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost if they continue leaving the editor
     *
     * @param discardButtonListener is the click listener for what to do when the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonListener) {
        /**
         * Create an {@link android.support.v7.app.AlertDialog.Builder} and set the message and click listeners
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(R.string.unsaved_changes_dialog_message)
                .setPositiveButton(R.string.discard, discardButtonListener)
                .setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        /**
         * Create and show the {@link AlertDialog}
         */
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
