package artist.web.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by User on 8/30/2017.
 */

public class CraftProvider extends ContentProvider {

    //Tag for the log messages
    public static final String LOG_TAG = CraftProvider.class.getSimpleName();


    //URI matcher code for the content URI for the crafts table
     private static final int CRAFTS = 100;


     //URI matcher code for the content URI for a single pet in the pets table
      private static final int CRAFT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        sUriMatcher.addURI(CraftsContract.CONTENT_AUTHORITY, CraftsContract.PATH_CRAFTS, CRAFTS);
        sUriMatcher.addURI(CraftsContract.CONTENT_AUTHORITY, CraftsContract.PATH_CRAFTS + "/#", CRAFT_ID);
    }

    //database helper object
    private CraftDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new CraftDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection,
     * selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CRAFTS:

                cursor = database.query(CraftsContract.CraftEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null);

                break;
            case CRAFT_ID:

                selection = CraftsContract.CraftEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the crafts table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(CraftsContract.CraftEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CRAFTS:
                return insertCraft(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a Craft into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertCraft(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(CraftsContract.CraftEntry.COLUMN_CRAFT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Craft requires a name");
        }
        double price = values.getAsDouble(CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE);
        if (price <=0) {
            throw new IllegalArgumentException("Craft requires a valid price");
        }
        Integer quantity = values.getAsInteger(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Craft requires a valid quantity");
        }
        String supplier = values.getAsString(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER);
        if(supplier==null){
            throw new IllegalArgumentException("Craft requires a supplier");
        }
        String supplierContact = values.getAsString(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT);
        if(supplierContact==null|| supplierContact.length()<10 || Integer.parseInt(supplierContact)<0){
            throw new IllegalArgumentException("Craft requires supplier contact");
        }


        //get Writable database

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        //Insert a new pet with  the given values
        long rowId = database.insert(CraftsContract.CraftEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (rowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, rowId);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CRAFTS:
                return updateCraft(uri, contentValues, selection, selectionArgs);
            case CRAFT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.

                selection = CraftsContract.CraftEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateCraft(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateCraft(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Check that the name is not null
        if (values.containsKey(CraftsContract.CraftEntry.COLUMN_CRAFT_NAME)) {
            String name = values.getAsString(CraftsContract.CraftEntry.COLUMN_CRAFT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Craft requires a name");
            }
        }
        if (values.containsKey(CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE)) {
            double price = values.getAsDouble(CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE);
            if (price<=0) {
                throw new IllegalArgumentException("Craft requires a valid price");
            }
        }
        if (values.containsKey(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY)) {
            Integer quantity = values.getAsInteger(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Pet requires a valid weight value");
            }
        }
        if(values.containsKey(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER)) {
            String supplier = values.getAsString(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Craft requires a supplier");
            }
        }
        if(values.containsKey(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT)) {
            String supplierContact = values.getAsString(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT);
            if (supplierContact == null || supplierContact.length() < 10 || Integer.parseInt(supplierContact) < 0) {
                throw new IllegalArgumentException("Craft requires supplier contact");
            }
        }

        //get Writable database

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(CraftsContract.CraftEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CRAFTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CraftsContract.CraftEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CRAFT_ID:
                // Delete a single row given by the ID in the URI
                selection = CraftsContract.CraftEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(CraftsContract.CraftEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CRAFTS:
                return CraftsContract.CraftEntry.CONTENT_LIST_TYPE;
            case CRAFT_ID:
                return CraftsContract.CraftEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}
