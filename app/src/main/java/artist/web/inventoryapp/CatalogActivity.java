package artist.web.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import artist.web.inventoryapp.data.CraftsContract;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private static final int PRODUCT_LOADER = 1;
    final Context mContext = this;
    CraftCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the list data
        ListView mListViewProducts = (ListView) findViewById(R.id.list_products);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        View mEmptyStateView = findViewById(R.id.empty_view);
        mListViewProducts.setEmptyView(mEmptyStateView);

        // Set up adapter to create a list item for each row of data in the Cursor
        mCursorAdapter = new CraftCursorAdapter(this, null);

        //attach cursor adapter to the list view
        mListViewProducts.setAdapter(mCursorAdapter);

        // Setup the item click listener
        mListViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentCraftUri = ContentUris.withAppendedId(CraftsContract.CraftEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentCraftUri);

                // Launch the {@link DetailActivity} to display the data for the current item
                startActivity(intent);
            }
        });

        // Start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllCrafts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Helper method to delete all pets in the database.

    private void deleteAllCrafts() {
        int rowsDeleted = getContentResolver().delete(CraftsContract.CraftEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from craft database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.

        String[] projection = {
                CraftsContract.CraftEntry._ID,
                CraftsContract.CraftEntry.COLUMN_CRAFT_NAME,
                CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE,
                CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY
        };
        return new CursorLoader(this, CraftsContract.CraftEntry.CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCursorAdapter.swapCursor(null);
    }
}
