package artist.web.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import artist.web.inventoryapp.data.CraftsContract;

/**
 * Created by User on 8/30/2017.
 */

public class CraftCursorAdapter extends CursorAdapter {

    private static final String TAG = CraftCursorAdapter.class.getSimpleName();;
    private Context mContext;

    public CraftCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * This class describes the view items to create a list item
     */
    public static class ProductViewHolder {

        TextView textViewProductName;
        TextView textViewPrice;
        TextView textViewStock;
        ImageButton buttonSale;

        // Find various views within ListView and set custom typeface on them
        public ProductViewHolder(View itemView) {
            textViewProductName = (TextView) itemView.findViewById(R.id.product_name);
            textViewPrice = (TextView) itemView.findViewById(R.id.product_price);
            textViewStock = (TextView) itemView.findViewById(R.id.product_stock);
            buttonSale = (ImageButton) itemView.findViewById(R.id.button_sale);

          }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent ,false);
        ProductViewHolder holder = new ProductViewHolder(view);
        view.setTag(holder);

        return view;
    }

    /**
     * This method binds the data in the current row pointed to by cursor to the given
     * list item layout.
     * @param view - ListView
     * @param context - Activity context
     * @param cursor - Cursor containing data loaded from table
     */

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ProductViewHolder holder = (ProductViewHolder)view.getTag();

        // Set data to respective views within ListView
        final int productId = cursor.getInt(cursor.getColumnIndex(CraftsContract.CraftEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_NAME));
        Double price = cursor.getDouble(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE));
        final int stock = cursor.getInt(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY));

        holder.textViewProductName.setText(productName);
        holder.textViewPrice.setText(String.valueOf(price.doubleValue()));
        holder.textViewStock.setText(String.valueOf(stock));


        // Bind sale event to list item button so quantity is reduced with each sale
        holder.buttonSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(CraftsContract.CraftEntry.CONTENT_URI, productId);
                adjustStock(context, productUri, stock);
            }
        });

    }

    /**
     * This method reduced product stock by 1
     * @param context - Activity context
     * @param productUri - Uri used to update the stock of a specific product in the ListView
     * @param currentStock - current stock of that specific product
     */
    private void adjustStock(Context context, Uri productUri, int currentStock) {

        // Reduce stock, check if new stock is less than 0, in which case set it to 0
        int newStock = (currentStock >= 1) ? currentStock - 1 : 0;

        // Update table with new stock of the product
        ContentValues contentValues = new ContentValues();
        contentValues.put(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY, newStock);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);

        // Display error message in Log if product stock fails to update
        if (!(numRowsUpdated > 0)) {
            Log.e(TAG, "Failed to reduce product stock");
        }
    }
}
