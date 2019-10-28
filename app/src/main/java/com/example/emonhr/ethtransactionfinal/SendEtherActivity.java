package com.example.emonhr.ethtransactionfinal;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import static com.example.emonhr.ethtransactionfinal.MainActivity.credentials;
import static com.example.emonhr.ethtransactionfinal.MainActivity.web3;


public class SendEtherActivity extends AppCompatActivity {

    private static int camID= android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
    private static  String myAdress =null;
    private static  String toAddress=null;
    private  static String sendAmount=null;

    public static final BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(4300000);

    private AlertDialog.Builder alertDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_ether);

        loadWalletFile();

        final Activity activity=this;

        final EditText toWalletAdressEditTextview  = (EditText) findViewById(R.id.to_id);
        final EditText amounToSendEditTextView  =(EditText) findViewById(R.id.amount_to_send);

        Button toQRscanButton = (Button) findViewById(R.id.qr_scan);
        Button sendEhterButtonView = (Button) findViewById(R.id.send_ether_button);

        toQRscanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator=new IntentIntegrator(activity);
                intentIntegrator.setCameraId(camID);
                intentIntegrator.setDesiredBarcodeFormats(intentIntegrator.QR_CODE_TYPES);
                intentIntegrator.setPrompt("scan");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setBarcodeImageEnabled(true);
                intentIntegrator.initiateScan();
            }
        });

        sendEhterButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddress= String.valueOf(toWalletAdressEditTextview.getText());
                sendAmount=String.valueOf(amounToSendEditTextView.getText());
                if(toAddress.isEmpty()||sendAmount.isEmpty()){
                    Toast.makeText(activity,"Plz enter valid address and amount",Toast.LENGTH_LONG).show();
                }
                else{
                    alertDialogBuilder=new AlertDialog.Builder(activity);
                    alertDialogBuilder.setTitle("Transaction Warning??");
                    alertDialogBuilder.setMessage("are u sure about this transaction");
                    alertDialogBuilder.setPositiveButton("agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            sendEther();
                            toWalletAdressEditTextview.setText("");
                            amounToSendEditTextView.setText("");


                        }
                    });
                    alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            toWalletAdressEditTextview.setText("");
                            amounToSendEditTextView.setText("");
                        }
                    });
                    AlertDialog alertDialog=alertDialogBuilder.create();
                    alertDialog.show();

                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null){
            if(result.getContents()!=null){
                EditText setToAdrress=(EditText)findViewById(R.id.to_id);
                setToAdrress.setText(result.getContents());
                toAddress=result.getContents();

                Toast.makeText(this,result.getContents().toString(),Toast.LENGTH_LONG).show();
            }
        }
    }
    public void loadWalletFile(){

        try {

            //getting my wallet address
            myAdress =credentials.getAddress();

        }catch (Exception e){
            Toast.makeText(this,"Error hgh" ,Toast.LENGTH_LONG).show();
        }
    }

    public void sendEther(){


        EthGetTransactionCount ethGetTransactionCount = null;

        try {
            ethGetTransactionCount = web3.ethGetTransactionCount(
                    myAdress,DefaultBlockParameterName.LATEST).sendAsync().get();

            //Toast.makeText(this,"aise  : "+ethGetTransactionCount ,Toast.LENGTH_LONG).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //getting the transaction id
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //amount of ether to send

        BigInteger value = Convert.toWei(sendAmount, Convert.Unit.ETHER).toBigInteger();

        //creating the rawtrasaction

        RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                nonce, GAS_PRICE,GAS_LIMIT,toAddress, value);

        //sign the rawtransaction

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = null;

        try {

            //send the transaction

            ethSendTransaction = web3.ethSendRawTransaction(hexValue).sendAsync().get();
            Toast.makeText(this,"transaction Done",Toast.LENGTH_SHORT).show();

        } catch (ExecutionException e) {
            Toast.makeText(this,"ExecutionException",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (InterruptedException e) {
            Toast.makeText(this,"InterruptedException",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        String transactionHash = ethSendTransaction.getTransactionHash();

    }


}
