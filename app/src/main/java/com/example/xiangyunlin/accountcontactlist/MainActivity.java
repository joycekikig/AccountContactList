package com.example.xiangyunlin.accountcontactlist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_CONTACTS;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CONTACTS = 1;
    private ListView listView;
    private List<Map<String,String>> list = new ArrayList<>();
    private MyContentObserver myObserver;
    MyAdapter myAdapter;
    private Handler handler = new Handler();
    HandlerThread mWorkerThread;
    private Handler mWorkerHandler;

    /*
    // Method 1
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, WRITE_CONTACTS}, REQUEST_CONTACTS);
        } else{
            //已有權限，可進行檔案存取
            readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得聯絡人權限，進行存取
                    readContacts();
                } else {
                    //使用者拒絕權限，顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("必須允許聯絡人權限才能顯示資料")
                            .setPositiveButton("OK", null)
                            .show();
                }
                return;
        }
    }

    private void readContacts() {

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,
                null,
                null);



        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()){
                //處理每一筆資料
                int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.d("RECORD", id+"/"+name+"/"+phone);
            }
        }

        ListView list = (ListView) findViewById(R.id.listView);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                new int[] {android.R.id.text1, android.R.id.text2},
                1);
        list.setAdapter(adapter);
    }
    */


    // Method 2
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerContentObservers();
        mWorkerThread = new HandlerThread("HandlerThread");
        mWorkerThread.start();
        mWorkerHandler = new Handler(mWorkerThread.getLooper());

        listView = (ListView)findViewById(R.id.listView);
        myAdapter = new MyAdapter(this);
        listView.setAdapter(myAdapter);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS, WRITE_CONTACTS}, REQUEST_CONTACTS);
        } else{
            mWorkerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("test", "doInBackground 3");
                    initdate();
                    //listView.setDividerHeight(50);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }, 5000);
            //已有權限，可進行檔案存取
//            AsyncTask asyncTask = new AsyncTask() {
//                @Override
//                protected Object doInBackground(Object[] objects) {
//                    initdate();
//                    Log.d("test", "doInBackground 1");
//
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            listView.setDividerHeight(50);
//                        }
//                    });
//                    return null;
//                }
//
//                @Override
//                protected void onPostExecute(Object o) {
//                    super.onPostExecute(o);
//                    Log.d("test", "onPostExecute 1");
//                    //myAdapter.notifyDataSetChanged();
//                }
//            }.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得聯絡人權限，進行存取
                    AsyncTask asyncTask = new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            initdate();
                            Log.d("test", "doInBackground 2");
                            myAdapter.notifyDataSetChanged();
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            Log.d("test", "onPostExecute 2");
                            //myAdapter.notifyDataSetChanged();
                        }
                    }.execute();
                } else {
                    //使用者拒絕權限，顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("必須允許聯絡人權限才能顯示資料")
                            .setPositiveButton("OK", null)
                            .show();
                }
                return;
        }
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public MyAdapter(Context context) {
            layoutInflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = layoutInflater.inflate(R.layout.listview_item, null);
            TextView name = convertView.findViewById(R.id.nameView);
            TextView phoneNumber = convertView.findViewById(R.id.phoneNumberView);
            name.setText(list.get(position).get("name"));
            phoneNumber.setText(list.get(position).get("phoneNumber"));
            return convertView;
        }
    } // end of class MyAdapter

    // query the data (name, phoneNumber)
    public void initdate() {
        if(list != null)
            list.clear();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                null,
                null,
                null
        );
        while(cursor.moveToNext()){
            Map<String,String> map=new HashMap<>();
            String name = cursor.getString(0);
            String phoneNumber = cursor.getString(1);
            map.put("name", name);
            map.put("phoneNumber", phoneNumber);
            list.add(map);
        }
        if (cursor != null) {
            cursor.close();
        }
    }


    private class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            initdate();
            Log.d("test", "onChange !");
        }
    } // end of class MyContentObserver

    //註冊監聽
    private void registerContentObservers() {
        myObserver = new MyContentObserver(handler);
        getContentResolver().registerContentObserver(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true, myObserver);
    }

    //取消監聽
    private void unregisterContentObservers() {
        if (myObserver != null) {
            getContentResolver().unregisterContentObserver(myObserver);
            myObserver = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterContentObservers();
        Log.d("test", "onDestroy !");
    }
}
