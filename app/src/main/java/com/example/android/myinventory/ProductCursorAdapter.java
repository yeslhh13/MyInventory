package com.example.android.myinventory;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.myinventory.data.ProductContract;

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
    public void bindView(View view, Context context, Cursor cursor) {
        /**
         * Find individual views that we want to modify in the list item layout
         */
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.summary);

        /**
         * Find the column of pet attributes that we're interested in
         */
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);

        String priceText = "$ " + String.valueOf(cursor.getInt(priceColumnIndex));
        /**
         * Update the TextViews with the attributes for the current product
         */
        nameTextView.setText(cursor.getString(nameColumnIndex));
        priceTextView.setText(priceText);
    }
}
