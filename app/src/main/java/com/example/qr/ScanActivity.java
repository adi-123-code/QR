package com.example.qr;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qr.Model.QRTXTModel;
import com.example.qr.Model.QRURLModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{


    private ZXingScannerView scannerView;   //Zxing第三方库扫码窗口
    private TextView txtResult;             //用于存放扫码结果，主要用于测试，最终效果可隐藏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);      //保存当前Activity的状态信息
        setContentView(R.layout.activity_scan);  //设置扫描活动的显示界面

        //绑定视图
        scannerView = findViewById(R.id.zxscan);
        txtResult = findViewById(R.id.txt_result);

        //相机权限申请
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.CAMERA)
                    .withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                scannerView.setResultHandler(ScanActivity.this);
                scannerView.startCamera();     //打开相机
            }

            //拒绝许可时，弹出消息提示框
            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(ScanActivity.this, "You must accept this permission", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest
            permission, PermissionToken token) {

            }
        })
                .check();
    }

    //销毁扫描进程
    @Override
    protected void onDestroy(){
        scannerView.stopCamera();
        super.onDestroy();
    }

    //处理扫描结果，并重新开始扫描
    @Override
    public void handleResult(Result rawResult) {
        processRawResult(rawResult.getText());
        scannerView.startCamera();
    }


    //各种URL协议类型
    public String URL[] = {
                "http://",
                "https://",
                "http://",
                "https://",
                "www.",
                "ftp://",
                "mailto:",
                "LDAP://",
                "file:///",
                "gopher://"
    };

    //通过获取的文本信息进行自动判断是否是文本信息，或是网页浏览
    private void processRawResult(String text) {
        //判断是否是URL
        boolean isUrl = false;
        for (int i=0; i<URL.length; ++i){
            if (text.startsWith(URL[i]))
            {
                isUrl = true;
            }
        }
        if (isUrl)
        {
            QRURLModel qrurlModel = new QRURLModel(text);
            //网页链接，启动浏览器进行访问
            txtResult.setText(qrurlModel.getUrl());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(txtResult.getText());
            builder.setTitle("扫描结果");
            builder.setPositiveButton("访问该链接", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.parse(text);
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        //指定谷歌浏览器
                        intent.setClassName("com.android.chrome","com.google.android.apps.chrome.Main");
                        startActivity(intent);

                    }catch (Exception e){
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }

                }
            }).setNegativeButton("再扫一次", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return ;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            QRTXTModel qrtxtModel = new QRTXTModel(text);
            //文本信息，调用Android文本阅读器显示
            txtResult.setText(text);
            System.out.println("文本");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(txtResult.getText());
            builder.setTitle("扫描结果");

            builder.setPositiveButton("调用文本阅读器显示", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

                            ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            File directory = cw.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                            File file = new File(directory, "QrScanResult.txt");

//                            File file = new File(Environment.getExternalStorageDirectory(), "QrScanResult.txt");
                            FileOutputStream outStream = new FileOutputStream(file);
                            outStream.write(text.getBytes());
                            outStream.close();


                            Intent intent = new Intent("android.intent.action.VIEW");
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri;
                            //判断是否是AndroidN以及更高的版本
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24
                                uri = FileProvider.getUriForFile(ScanActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                uri = Uri.fromFile(file);
                            }

                            intent.setDataAndType(uri, "text/plain");
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }).setNegativeButton("再扫一次", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return ;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        scannerView.resumeCameraPreview(ScanActivity.this);     //
    }
    private static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//如果SDK版本>=24，即：Build.VERSION.SDK_INT >= 24
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}

