package artist.web.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by User on 8/30/2017.
 */

public final class CraftsContract {

    //Content Authority which is used to help identify the Content Provider

    public static final String CONTENT_AUTHORITY = "artist.web.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //stores the path for the table which will be appended to the base content URI.
    public static final String PATH_CRAFTS = "crafts";

    private CraftsContract(){

    }

    /** inner class that defines the table contents*/

    public static class CraftEntry implements BaseColumns {

        /** The content URI to access the crafts data in the provider */
        //full URI for the class as a constant called CONTENT_URI.
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CRAFTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of crafts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CRAFTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single craft.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CRAFTS;


        public static final String TABLE_NAME = "crafts";
        public static final String _ID= BaseColumns._ID;
        public static final String COLUMN_CRAFT_NAME = "name";
        public static final String COLUMN_CRAFT_PRICE = "price";
        public static final String COLUMN_CRAFT_QUANTITY = "quantity";
        public static final String COLUMN_CRAFT_PICTURE = "picture";
        public static final String COLUMN_CRAFT_SUPPLIER = "supplier";
        public static final String COLUMN_CRAFT_SUPPLIER_CONTACT = "supplier_contact";
    }
}
