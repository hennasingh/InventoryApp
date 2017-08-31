package artist.web.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by User on 8/30/2017.
 */

public class CraftDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = CraftDbHelper.class.getSimpleName();
    //If you change the database schema, you must increment the database version

    public static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="crafts.db";

    private static final String TEXT_TYPE=" TEXT";
    private static final String INTEGER_TYPE=" INTEGER";
    private static final String FLOAT_TYPE =" REAL";
    private static final String NOT_NULL = " NOT NULL";
    private static final String COMMA_SEP =",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CraftsContract.CraftEntry.TABLE_NAME + " (" +
                    CraftsContract.CraftEntry._ID + INTEGER_TYPE+ " PRIMARY KEY AUTOINCREMENT, "+
                    CraftsContract.CraftEntry.COLUMN_CRAFT_NAME + TEXT_TYPE + NOT_NULL+COMMA_SEP +
                    CraftsContract.CraftEntry.COLUMN_CRAFT_PRICE + FLOAT_TYPE + NOT_NULL+COMMA_SEP +
                    CraftsContract.CraftEntry.COLUMN_CRAFT_QUANTITY + INTEGER_TYPE + NOT_NULL+COMMA_SEP+
                    CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER + TEXT_TYPE + NOT_NULL + COMMA_SEP +
                    CraftsContract.CraftEntry.COLUMN_CRAFT_SUPPLIER_CONTACT +TEXT_TYPE +NOT_NULL +COMMA_SEP+
                    CraftsContract.CraftEntry.COLUMN_CRAFT_PICTURE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES=
            "DROP TABLE IF EXISTS " + CraftsContract.CraftEntry.TABLE_NAME;

    public CraftDbHelper(Context context){
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        Log.i("SYNTAX: ",SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}



