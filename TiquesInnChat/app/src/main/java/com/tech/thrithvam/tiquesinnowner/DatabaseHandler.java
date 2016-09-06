package com.tech.thrithvam.tiquesinnowner;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "TiquesInnOwner.db";
    private SQLiteDatabase db;
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    // Creating Tables
    // IMPORTANT: if you are changing anything in the below function onCreate(), DO DELETE THE DATABASE file in
    // the emulator or uninstall the application in the phone, to run the application
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_ACCOUNTS_TABLE = "CREATE TABLE IF NOT EXISTS UserAccount (LoginName TEXT,UserID TEXT,BoutiqueID TEXT,BoutiqueName TEXT);";
        db.execSQL(CREATE_USER_ACCOUNTS_TABLE);
        String CREATE_CHAT_TABLE = "CREATE TABLE IF NOT EXISTS Chat (MsgIDs TEXT PRIMARY KEY, UserID TEXT,UserName TEXT, Msg TEXT, Direction TEXT, MsgTime DATETIME, ProductID TEXT,Read BOOLEAN);";
        db.execSQL(CREATE_CHAT_TABLE);
    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME );
        // Create tables again
        onCreate(db);
    }
    //--------------------------User Accounts-----------------------------
    public void UserLogin(String LoginName,String UserID, String BoutiqueID, String BoutiqueName)
    {
        db=this.getWritableDatabase();
        db.execSQL("INSERT INTO UserAccount (LoginName,UserID,BoutiqueID,BoutiqueName) VALUES ('"+LoginName+"','"+UserID+"','"+BoutiqueID+"','"+BoutiqueName+"');");
        db.close();
    }
    public void UserLogout()
    {
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM UserAccount;");
        db.execSQL("DELETE FROM Chat;");
        db.close();
    }
    public String GetUserDetail(String detail)
    {db=this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM UserAccount;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            String result=cursor.getString(cursor.getColumnIndex(detail));
            cursor.close();
            return result;
        }
        else return null;
    }
    //------------------------------Chat table---------------------------------------
    public void insertMessage(String MsgIDs,String UserID,String UserName,String Msg,String Direction,String MsgTime,String ProductID)
    {
        db=this.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO Chat (MsgIDs,UserID,UserName,Msg,Direction,MsgTime,ProductID,Read) VALUES ('"+MsgIDs+"','"+UserID+"','"+UserName+"','"+Msg+"','"+Direction+"','"+MsgTime+"','"+ProductID+"','"+false+"');");
        }
        catch (Exception ex){
        }
        db.close();
    }
    public ArrayList<String[]> GetMsgs(String userID)
    {db=this.getReadableDatabase();
        ArrayList<String[]> msgs=new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT Msg,MsgTime,Direction,ProductID FROM Chat WHERE UserID='"+userID+"' ORDER BY MsgTime ASC;",null);
        db.execSQL("UPDATE Chat SET Read='"+true+"' WHERE UserID='"+userID+"';");
        String productID="";
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                if(!cursor.getString(cursor.getColumnIndex("ProductID")).equals("null") && !productID.equals(cursor.getString(cursor.getColumnIndex("ProductID"))))
                {
                    String[] data = new String[4];
                    data[0] = "$$NewProduct$$";
                    data[1] = "null";//cursor.getString(cursor.getColumnIndex("MsgTime"));
                    data[2] = "";
                    data[3] = cursor.getString(cursor.getColumnIndex("ProductID"));
                    msgs.add(data);
                    productID=cursor.getString(cursor.getColumnIndex("ProductID"));
                }
                String[] data = new String[4];
                data[0] = cursor.getString(cursor.getColumnIndex("Msg"));
                data[1] = cursor.getString(cursor.getColumnIndex("MsgTime"));
                data[2] = cursor.getString(cursor.getColumnIndex("Direction"));
                data[3] = cursor.getString(cursor.getColumnIndex("ProductID"));
                msgs.add(data);
            }while (cursor.moveToNext());
            cursor.close();
            return msgs;
        }
        else return msgs;//empty array list to avoid exception in custom adapter
    }
    public ArrayList<String[]> GetChatHeads()
    {
        db=this.getReadableDatabase();
        ArrayList<String[]> msgHeads=new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT UserID,UserName,Msg,MsgTime,Read FROM Chat GROUP BY UserID ORDER BY MsgTime DESC;",null);
        if (cursor.getCount()>0)
        {cursor.moveToFirst();
            do {
                String[] data = new String[5];
                data[0] = cursor.getString(cursor.getColumnIndex("UserID"));
                data[1] = cursor.getString(cursor.getColumnIndex("Msg"));
                data[2] = cursor.getString(cursor.getColumnIndex("MsgTime"));
                data[3] = cursor.getString(cursor.getColumnIndex("UserName"));
                data[4] = cursor.getString(cursor.getColumnIndex("Read"));
                msgHeads.add(data);
            }while (cursor.moveToNext());
            cursor.close();
            return msgHeads;
        }
        else return msgHeads;//empty array list to avoid exception in custom adapter
    }
    public void DeleteChatHeads(String userID){
        db=this.getWritableDatabase();
        db.execSQL("DELETE FROM Chat WHERE UserID='"+ userID +"';");
        db.close();
    }
}
