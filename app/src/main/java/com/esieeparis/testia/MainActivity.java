package com.esieeparis.testia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
//import org.tensorflow.lite.support.image;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView predictedTextView;
    private ImageView imageView;
    private ImageClassifier imageClassifier = new ImageClassifier(this);
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        predictedTextView = findViewById(R.id.predicted_text);
        imageView = findViewById(R.id.image);

        try {
            imageClassifier.initialize();
        }
        catch(Exception e) {
            Log.d("DigitClassifier", "Error to setting up digit classifier");
        }

        System.out.println("JUI LA");

        /*try {
            classifyImage();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        AssetManager assetManager = getAssets();
        InputStream image = null;
        try {
            image = assetManager.open("exemple.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(image);
        imageView.setImageBitmap(bitmap);

        System.out.println("JUI LA 2");

        if(bitmap != null && imageClassifier.isInitialized()) {
            imageClassifier.classifyAsync(bitmap);
            text = imageClassifier.getPrediction();
            System.out.println("coucou " + text);
            predictedTextView.setText(text);
        }

        System.out.println("JUI LA 4");
    }

    @Override
    protected void onDestroy() {
        imageClassifier.close();
        super.onDestroy();
    }

    /*protected void classifyImage() throws IOException {
        AssetManager assetManager = getAssets();
        InputStream image = assetManager.open("exemple.png");
        Bitmap bitmap = BitmapFactory.decodeStream(image);
        imageView.setImageBitmap(bitmap);

        System.out.println("JUI LA 3");

        if(bitmap != null && digitClassifier.isInitialized()) {
            digitClassifier.classifyAsync(bitmap);
            text = digitClassifier.getPrediction();
            System.out.println("coucou " + text);
            predictedTextView.setText(text);
        }
    }*/
}
