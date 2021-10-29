package com.example.qr;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.zxing.MultiFormatWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;


public class MainActivity extends AppCompatActivity {
    //Initialize variable
    Context mContext;
    EditText etInput;
    Button btGenerate,BarcodeButton;
    ImageButton btScan;
    ImageView ivOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);//申请权限
        //Assign variable
        etInput = findViewById(R.id.et_input);
        btGenerate = findViewById(R.id.bt_generate);
        btScan = findViewById(R.id.bt_scan);
        ivOutput = findViewById(R.id.iv_output);
        BarcodeButton = findViewById(R.id.bt_generate_pro);
        mContext = this;

        btGenerate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                //从编辑框获取文本内容
                String sText=etInput.getText().toString().trim();
                //防止误触
                if(sText.length() == 0){
                    return ;
                }
                //Initialize multi format writer
                MultiFormatWriter writer = new MultiFormatWriter();
                try {
                    //Initialize bit matrix
                    BitMatrix matrix=writer.encode(sText,BarcodeFormat.QR_CODE,ivOutput.getWidth(),ivOutput.getHeight());
                    //Initialize barcode encoder
                    BarcodeEncoder encoder=new BarcodeEncoder();
                    //Initialize bitmap
                    Bitmap bitmap=encoder.createBitmap(matrix);
                    //Initialize logoBitmap
                    Bitmap logoBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.njupt);
                    //Initialize Bitmap
                    Bitmap Bitmap=EncodingUtils.createQRCode(etInput.getText().toString(),ivOutput.getWidth(),ivOutput.getHeight(),logoBitmap);
                    //Set bitmap on image view
                    ivOutput.setImageBitmap(Bitmap);


                    //Initialize input manager
                    InputMethodManager manager=(InputMethodManager) getSystemService(
                            Context.INPUT_METHOD_SERVICE
                    );
                    //收起键盘
                    manager.hideSoftInputFromWindow(etInput.getApplicationWindowToken(),0);
                }catch(WriterException e){
                    e.printStackTrace();
                }
            }
        });

        BarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取文本信息或网址内容
                String Text_Website = etInput.getText().toString().trim();
                //防止勿喷
                if(Text_Website.length() == 0){
                    return;
                }
                //生成一维码


                try {
                    Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
                    hintMap.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.L);
                    Writer writer = new Code128Writer();
                    BitMatrix bitMatrix = writer.encode(Text_Website,BarcodeFormat.CODE_128,ivOutput.getWidth(),450,hintMap);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bitmap =Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
                    for(int i = 0; i < width; i++){
                        for(int j = 0; j < height; j++){
                            bitmap.setPixel(i,j,bitMatrix.get(i,j)? Color.BLACK:Color.WHITE);
                        }
                        ivOutput.setImageBitmap(bitmap);

                    }

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(intent);
            }
        });


    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




class EncodingUtils {

    public static Bitmap createQRCode(String content, int widthPix, int heightPix, Bitmap logoBm) {
        try {
            if (content == null || "".equals(content)) {
                return null;
            }
            // 配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix,
                    heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }
            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }
}

