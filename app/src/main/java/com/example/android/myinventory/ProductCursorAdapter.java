package com.example.android.myinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myinventory.data.ProductContract;
import com.example.android.myinventory.data.ProductDBHelper;

import static com.example.android.myinventory.R.id.quantity;

/**
 * Created by Kat on 2017-03-22.
 */

public class ProductCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ProductCursorAdapter}
     *
     * @param context the context
     * @param c       the cursor to get the data
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0/* flags */);
    }

    /**
     * Makes a new blank list item view
     * No data is set or bound to the views yet
     *
     * @param context app context
     * @param cursor  to get the data
     *                already moved to the current position
     * @param parent  to attach the new view
     * @return the newly created list item view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        /**
         * Inflate a list item view using the layout specified in list_item.xml
         */
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Binds the product data in the current row pointed to by cursor to the given list item layout
     *
     * @param view    existing view
     * @param context app context
     * @param cursor  the cursor from which to get the data
     *                already moved to the correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        /**
         * Find individual views that we want to modify in the list item layout
         */
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(quantity);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        /**
         * Find the column of product attributes that we're interested in
         */
        int idIndex = cursor.getColumnIndex(ProductContract.ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        String priceText = "$ " + String.valueOf(cursor.getInt(priceColumnIndex));

        /**
         * Update the TextViews with the attributes for the current product
         */
        nameTextView.setText(cursor.getString(nameColumnIndex));
        priceTextView.setText(priceText);
        quantityTextView.setText(String.valueOf(cursor.getInt(quantityColumnIndex)));

        final int _id = cursor.getInt(idIndex);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Decrease the quantity of the product when Track Sale button is clicked
                 */
                int quantity = Integer.parseInt(quantityTextView.getText().toString().trim());
                if (quantity > 0) {
                    /**
                     * Quantity value can't be negative
                     */
                    ProductDBHelper dbHelper = new ProductDBHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);

                    db.update(ProductContract.ProductEntry.TABLE_NAME, values,
                            ProductContract.ProductEntry._ID + "=" + _id, null);

                    quantityTextView.setText(String.valueOf(quantity - 1));
                } else {
                    Toast.makeText(context, "There is no more product to sale.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
