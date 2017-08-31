package artist.web.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import artist.web.inventoryapp.data.CraftsContract;

public class EditorActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mStockEditText;
    private EditText mSupplierEditText;
    private EditText mSupplierContactEditText;
    private ImageView mImageView;

    private Boolean mCraftHasChanged = false;

    private static final int CRAFT_LOADER=0;

    private Uri imageUri;
    private Uri currentCraftUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Get Intent Data
        currentCraftUri = getIntent().getData();
        if(currentCraftUri==null){
            setTitle(getString(R.string.editor_activity_add_craft_title));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }else{
            setTitle(getString(R.string.editor_activity_edit_craft_title));
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_craft_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_craft_price);
        mStockEditText = (EditText) findViewById(R.id.edit_craft_stock);
        mSupplierEditText = (EditText) findViewById(R.id.edit_craft_supplier);
        mSupplierContactEditText = (EditText) findViewById(R.id.edit_craft_supplier_contact);
        Button mAddImageButton = (Button)findViewById(R.id.button_image);
        mImageView = (ImageView)findViewById(R.id.craft_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mStockEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierContactEditText.setOnTouchListener(mTouchListener);

        mAddImageButton.setOnTouchListener(mTouchListener);

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performFileSearch();
            }
        });

        getLoaderManager().initLoader(CRAFT_LOADER,null,this);

    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, IMAGE_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            try{
                imageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                mImageView.setImageBitmap(getBitmapFromUri(imageUri));


            }catch (Exception e){
                Log.e(LOG_TAG,"Error resolving image"+e);
            }
        }
    }

    /**
     * Method to display the image
     * @param uri - image path
     * @return Bitmap
     */
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, getString(R.string.exception_image_load_failed), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, getString(R.string.exception_image_load_failed), e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCraftHasChanged = true;
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentCraftUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save craft to database
                saveCraft();
                //exit Activity
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // show confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCraftHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mCraftHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteCraft();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteCraft() {

        int rowsDeleted = getContentResolver().delete(currentCraftUri,null,null);

        if (rowsDeleted > 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this,getString(R.string.editor_delete_craft_successful),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_craft_failed),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void saveCraft() {

        String nameText = mNameEditText.getText().toString().trim();
        String priceText = mPriceEditText.getText().toString().trim();
        String stockText = mStockEditText.getText().toString().trim();
        String supplierText = mSupplierEditText.getText().toString().trim();
        String supplierContactText = mSupplierContactEditText.getText().toString().trim();

        if(TextUtils.isEmpty(nameText)){
            Toast.makeText(getApplicationContext(), "Please add a valid Name",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(priceText)|| Double.parseDouble(priceText)<0 ){

            Toast.makeText(getApplicationContext(), "Please add a valid Price",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(stockText)|| Double.parseDouble(stockText)<=0 ){
            Toast.makeText(getApplicationContext(), "Please add a valid Stock",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(supplierText)){

            Toast.makeText(getApplicationContext(), "Please add a valid Supplier",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(supplierContactText)|| Integer.parseInt(supplierContactText)<0
                ||supplierContactText.length()<10){

            Toast.makeText(getApplicationContext(), "Please add a valid 10 digit Phone Number",Toast.LENGTH_SHORT).show();

        }else if(imageUri==null){

            Toast.makeText(getApplicationContext(), "Please add a valid Image", Toast.LENGTH_SHORT).show();

        }else{
           double price = Double.parseDouble(priceText);
           int  stock = Integer.parseInt(stockText);
           String imagePath = imageUri.toString();

        // Gets the data repository in write mode
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
            values.put(CraftsContract.CraftEntry.COLUMN_CRAFT_NAME,nameText );
            values.put(CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE, price);
            values.put(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY, stock);
            values.put(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER, supplierText);
            values.put(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT, supplierContactText);
            values.put(CraftsContract.CraftEntry.COLUMN_CRAFT_PICTURE,imagePath);

        if (currentCraftUri == null) {
            Uri mUri = getContentResolver().insert(CraftsContract.CraftEntry.CONTENT_URI, values);

            if (mUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_craft_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_craft_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsUpdated = getContentResolver().update(currentCraftUri, values, null, null);
            if (rowsUpdated > 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_craft_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_craft_failed),
                        Toast.LENGTH_SHORT).show();
            }

        }
      }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table

        if (currentCraftUri != null) {
            String[] projection = {
                    CraftsContract.CraftEntry._ID,
                    CraftsContract.CraftEntry.COLUMN_CRAFT_NAME,
                    CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE,
                    CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY,
                    CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER,
                    CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT,
                    CraftsContract.CraftEntry.COLUMN_CRAFT_PICTURE
            };

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,       // Parent activity context
                    currentCraftUri,         // Query the content URI for the current pet
                    projection,                 // Columns to include in the resulting Cursor
                    null,                       // No selection clause
                    null,                       // No selection arguments
                    null);                      // Default sort order
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_NAME));
            double price = cursor.getDouble(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY));
            String supplier = cursor.getString(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER));
            String supplierContact = cursor.getString(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT));
            String imageIcon = cursor.getString(cursor.getColumnIndex(CraftsContract.CraftEntry.COLUMN_CRAFT_PICTURE));


            //populate fields
            if(imageIcon!=null){
                mImageView.setImageBitmap(getBitmapFromUri(imageUri));
            }else{
                mImageView.setImageResource(R.drawable.placeholder);
            }
            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            mStockEditText.setText(String.valueOf(stock));
            mSupplierEditText.setText(supplier);
            mSupplierContactEditText.setText(supplierContact);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mStockEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierContactEditText.setText("");
        mImageView.setImageResource(R.drawable.placeholder);
    }
}
