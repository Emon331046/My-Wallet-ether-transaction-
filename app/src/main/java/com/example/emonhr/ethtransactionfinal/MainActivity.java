package com.example.emonhr.ethtransactionfinal;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener {

    private Context context;
    public static  String myAdress =null;
    public static   Web3j web3=null;
    public static Credentials credentials=null;
    public TextView myWalletBalanceView;
    private MyDBHelper myDBHelper;
    private ArrayList<MyAccountData> contents;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //write from here #EMON###zingalala

        myDBHelper=new MyDBHelper(this);
        SQLiteDatabase db=myDBHelper.getWritableDatabase();
        contents=new ArrayList<>();




        //loading data from database in array list;
        contents=myDBHelper.loadData();


        context=this;
        startMyWeb3j();

        WritePermissionRequest();

        loadWalletFile();

        final ImageView myQRCodeImageView=(ImageView) findViewById(R.id.qr_code_image);
        final TextView myIDTextView=(TextView) findViewById(R.id.my_wallet_address);
        Button sendButtonView=(Button) findViewById(R.id.for_send);
        myWalletBalanceView=(TextView) findViewById(R.id.balance_show);

        Thread t=new Thread(){
            @Override
            public void run() {
                while (!isInterrupted()){
                    try {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(myAdress.isEmpty()){

                                }else {
                                    myWalletBalanceView.setText(walletBalance());

                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();

        sendButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(context,SendEtherActivity.class);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home) {
            // Handle the camera action
        } else if (id == R.id.nav_generate_wallet) {
            Toast.makeText(this,"ksjzhxn",Toast.LENGTH_SHORT).show();
            generateDialogBox();

        } else if (id == R.id.nav_import_wallet) {
            importWalletDialogBox();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private String walletBalance(){
        EthGetBalance ethGetBalance = null;
        if(myAdress.isEmpty()){
            return null;
        }
        try {
            ethGetBalance = web3
                    .ethGetBalance(myAdress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BigInteger balanceInWei = ethGetBalance.getBalance();
        java.math.BigDecimal balanceInEther = Convert.fromWei(String.valueOf(balanceInWei), Convert.Unit.ETHER);
        String myWalletBalance = "MyWallet Balance: \n" + String.valueOf(balanceInEther) + " ETH";
        return myWalletBalance;
    }

    public void QRCodeGenerate(String generateAddress){

        final ImageView myQRCodeImageView=(ImageView) findViewById(R.id.qr_code_image);
        final TextView myIDTextView=(TextView) findViewById(R.id.my_wallet_address);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(generateAddress, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            myQRCodeImageView.setImageBitmap(bitmap);
            myIDTextView.setText( generateAddress);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void generateDialogBox(){
        final Dialog dialog;
        //craeting a dialog
        dialog=new Dialog(context);

        //set view for the dialog box
        dialog.setContentView(R.layout.dialog_generate_wallet);
        final LinearLayout dialogBox=(LinearLayout) dialog.findViewById(R.id.
                dialog_option_LinearLayout);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialogBox.setBackground(ContextCompat.getDrawable(context,
                        R.drawable.dialog_background));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            }
        }catch (Exception e){

        }

        Button generateButton=(Button) dialog.findViewById(R.id.generate_button);
        final EditText passwordEditText=(EditText) dialog.findViewById(R.id.password_edit_text_for_generate);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password=passwordEditText.getText().toString();

                generateWalletFile(password);
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public void importWalletDialogBox(){
        final Dialog dialog;
        //craeting a dialog
        dialog=new Dialog(context);

        //set view for the dialog box
        dialog.setContentView(R.layout.dialog_import_wallet);
        final LinearLayout dialogBox=(LinearLayout) dialog.findViewById(R.id.
                dialog_option_LinearLayout);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialogBox.setBackground(ContextCompat.getDrawable(context,
                        R.drawable.dialog_background));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            }
        }catch (Exception e){

        }

        Button importButton=(Button) dialog.findViewById(R.id.import_button);
        final EditText passwordEditText=(EditText) dialog.findViewById(R.id.password_edit_text_for_import);
        final EditText fileNameEditText=(EditText) dialog.findViewById(R.id.file_name_edit_text_for_import);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName=fileNameEditText.getText().toString();
                String password=passwordEditText.getText().toString();


                importWalletFile(fileName,password);

                dialog.cancel();
            }
        });
        dialog.show();
    }
    public void importWalletFile(String fileName,String password){
        try {
            //finding the directory

            File path = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            }
            if (!path.exists()) {

                //not exist then make directory
                path.mkdir();
            }
            //invoke info from the Testing.json file
            credentials =
                    WalletUtils.loadCredentials(
                            password,
                            path + "/" + fileName);
            Log.e("TAG", "generateWallet: " + path + " " + fileName );

            myDBHelper=new MyDBHelper(context);
            SQLiteDatabase db=myDBHelper.getWritableDatabase();
            myDBHelper.updateData(password,fileName);
            //getting my wallet address
            myAdress =credentials.getAddress();

            //Toast.makeText(this,"checking wallet name"+ credentials.getAddress() + credentials.getEcKeyPair() ,Toast.LENGTH_LONG).show();
            TextView textView=(TextView) findViewById(R.id.my_wallet_address);
            textView.setText(credentials.getAddress() );

            TextView myWalletBalanceView=(TextView) findViewById(R.id.balance_show);
            myWalletBalanceView.setText(walletBalance());
            QRCodeGenerate(myAdress);
        }catch (Exception e){

            Toast.makeText(this,"Error " +fileName,Toast.LENGTH_LONG).show();
        }
    }

    private void startMyWeb3j() {
        web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/v3/66115b0cc6c745e7af3dca53b691a04b"));
        Web3ClientVersion web3ClientVersion =null;
        try {
            web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
        } catch (Exception e) {
            Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
            Log.v("smdhxcbhm","sdkhxjcn");
            e.printStackTrace();
        }
        try {
            String clientVersion = web3ClientVersion.getWeb3ClientVersion();

        } catch (Exception e) {
            Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
            Log.v("smdhxcbhm","sdkhxjcn");
            e.printStackTrace();
        }

        //Toast.makeText(this,"ok "+clientVersion,Toast.LENGTH_LONG).show();


    }
    public void WritePermissionRequest() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("permission1","Permission is granted");

            } else {

                Log.v("permission2","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("permission3","Permission is granted");


        }

    }
    public void generateWalletFile(String password){
        String fileName=null;
        try {
            //finding the directory

            File path = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            }
            if (!path.exists()) {

                //not exist then make directory
                path.mkdir();
            }

            //for generate new wallet file (json file)
            fileName = WalletUtils.generateLightNewWalletFile(password, new File(String.valueOf(path)) );
            myDBHelper=new MyDBHelper(context);
            SQLiteDatabase db=myDBHelper.getWritableDatabase();
            contents=new ArrayList<>();
            contents=myDBHelper.loadData();
            if(contents.isEmpty()){

                myDBHelper.insertData(password,fileName);
            }
            else {
                myDBHelper.updateData(password,fileName);
            }


            //invoke info from the Testing.json file
            credentials =
                    WalletUtils.loadCredentials(
                            password,
                            path + "/" + fileName);
            Log.e("TAG", "generateWallet: " + path + " " + fileName );
            //getting my wallet address
            myAdress =credentials.getAddress();

            //Toast.makeText(this,"checking wallet name"+ credentials.getAddress() + credentials.getEcKeyPair() ,Toast.LENGTH_LONG).show();
            TextView textView=(TextView) findViewById(R.id.my_wallet_address);
            textView.setText(credentials.getAddress() );

            TextView myWalletBalanceView=(TextView) findViewById(R.id.balance_show);
            myWalletBalanceView.setText(walletBalance());
            QRCodeGenerate(myAdress);
        }catch (Exception e){

            Toast.makeText(this,"Error jghgvhg" ,Toast.LENGTH_LONG).show();
        }
    }
    public void loadWalletFile(){
        //file name from where will load our wallet
        String fileName="Testing.json";
        myDBHelper=new MyDBHelper(context);
        SQLiteDatabase db=myDBHelper.getWritableDatabase();
        contents=new ArrayList<>();
        if(contents.isEmpty()){
            myDBHelper.insertData("12345678",fileName);
        }
        contents=myDBHelper.loadData();
        fileName=contents.get(0).fileName;
        String passWord=contents.get(0).password;

        try {
            //finding the directory
            File path = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            }
            if (!path.exists()) {
                //not exist then make directory
                path.mkdir();
            }
            //invoke info from the Testing.json file
            credentials =
                    WalletUtils.loadCredentials(
                            passWord,
                            path + "/" + fileName);
            Log.e("TAG", "generateWallet: " + path + " " + fileName );
            //getting my wallet address
            myAdress =credentials.getAddress();

            //Toast.makeText(this,"checking wallet name"+ credentials.getAddress() + credentials.getEcKeyPair() ,Toast.LENGTH_LONG).show();
            TextView textView=(TextView) findViewById(R.id.my_wallet_address);
            textView.setText(credentials.getAddress() );
            TextView myWalletBalanceView=(TextView) findViewById(R.id.balance_show);
            myWalletBalanceView.setText(walletBalance());
            QRCodeGenerate(myAdress);

        }catch (Exception e){

            Toast.makeText(this,"Error jghgvhg" ,Toast.LENGTH_LONG).show();
        }
    }

}
