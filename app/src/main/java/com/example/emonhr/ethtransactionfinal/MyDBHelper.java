package com.example.emonhr.ethtransactionfinal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


import java.util.ArrayList;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME      = "account.db";

    private static final String CURRENT_TABLE_NAME = "accounttable";

    private static final int    DATABASE_VERSION   = 1;

    private static final String SQL_CREATE_STUDENT_TABLE =  "CREATE TABLE " + CURRENT_TABLE_NAME + " ("
            + "_ID" + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "password" + " TEXT NOT NULL, "
            + "file_name" + " TEXT NOT NULL);";

    private static final String Drop_Table="DROP TABLE IF EXISTS "+CURRENT_TABLE_NAME;

    private Context context;

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {


        try {

            db.execSQL(SQL_CREATE_STUDENT_TABLE);
            Toast.makeText(context,"database created ",Toast.LENGTH_SHORT).show();

        }catch (Exception e){

            Toast.makeText(context,"Error on creating table",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            sqLiteDatabase.execSQL(Drop_Table);
            Toast.makeText(context,"on upgrade ",Toast.LENGTH_SHORT).show();
            onCreate(sqLiteDatabase);

        }catch (Exception e){

            Toast.makeText(context,"failed to catch",Toast.LENGTH_SHORT).show();
        }
    }
    public ArrayList<MyAccountData> loadData()
    {
        Cursor cursor=showAccounts();
         ArrayList<MyAccountData> contents=new ArrayList<>();
        if(cursor.getCount()>0)
        {
            while (cursor.moveToNext())
            {
                MyAccountData studentHelper=new MyAccountData(cursor.getInt(0),
                        cursor.getString(1),cursor.getString(2));

                contents.add(studentHelper);
            }
        }
        return contents;
    }
    public Cursor showAccounts() {

        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor;
        String SelectDb="SELECT * FROM "+CURRENT_TABLE_NAME;

        cursor=db.rawQuery(SelectDb,null);

        return cursor;
    }

    public void insertData(String password,String fileName) {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor;
        ContentValues values=new ContentValues();
        values.put("password",password);
        values.put("file_name",fileName);
        long rowId=db.insert(CURRENT_TABLE_NAME,null, values);

    }
    public void updateData(String password,String fileName){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor;
        String strFilter = "_id=" + 1;
        ContentValues values=new ContentValues();
        values.put("file_name",fileName);
        values.put("password",password);

        long rowId=db.update(CURRENT_TABLE_NAME,values,strFilter,null);
    }
    public void deleteData(int id){

            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + CURRENT_TABLE_NAME+ " WHERE "+"_ID"+"='"+id+"'");
            db.close();

    }
}