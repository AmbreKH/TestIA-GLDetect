package com.esieeparis.testia;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageClassifier {

    private boolean isInitialized;
    private final ExecutorService executorService;
    private int inputImageWidth;
    private int inputImageHeight;
    private int modelInputSize;
    private final Context context;
    private static final String TAG = "DigitClassifier";
    private static final int FLOAT_TYPE_SIZE = 4;
    private static final int PIXEL_SIZE = 1;
    private static final int OUTPUT_CLASSES_COUNT = 10;
    public static final ImageClassifier.Companion Companion = new ImageClassifier.Companion(null);
    private String prediction;

    private Interpreter interpreter;

    public final boolean isInitialized() {
        return this.isInitialized;
    }

    public String getPrediction() {
        return this.prediction;
    }

    @NonNull
    public final Task initialize() {
        Task var10000 = Tasks.call((Executor)this.executorService, (Callable)(new Callable() {
            public final Void call() throws IOException {
                ImageClassifier.this.initializeInterpreter();
                return null;
            }
        }));
        assert(var10000 != null);
        return var10000;
    }

    private final void initializeInterpreter() throws IOException {
        this.isInitialized = true;
        Log.d("DigitClassifier", "Initialized TFLite interpreter.");

        AssetManager assetManager = context.getAssets();
        ByteBuffer model = loadModelFile(assetManager, "model.tflite");
        System.out.println("Model loaded");
        /*int[] inputShape = interpreter.getInputTensor(0).shape();
        System.out.println("inputImageWidth " + inputShape[1] + " inputImageHeight " + inputShape[2]);
        int inputImageWidth = inputShape[1];
        int inputImageHeight = inputShape[2];*/
        inputImageWidth = 224;
        inputImageHeight = 224;
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE;

        this.interpreter = interpreter;
    }

    public final void close() {
        //Tasks.call((Executor)this.executorService, interpreter.close());
        interpreter.close();
        Log.d(TAG, "Closed TFLite interpreter.");
    }

    private final ByteBuffer loadModelFile(AssetManager assetManager, String filename) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(filename);
        assert(fileDescriptor != null);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer var10000 = fileChannel.map(MapMode.READ_ONLY, startOffset, declaredLength);
        assert(var10000 != null);
        return (ByteBuffer)var10000;
    }

    private final String classify(Bitmap bitmap) throws Throwable {
        boolean var2 = this.isInitialized;
        if (!var2) {
            boolean var5 = false;
            String var6 = "TF Lite Interpreter is not initialized yet.";
            throw (Throwable)(new IllegalStateException(var6));
        } else {
            Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true);
            ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedImage);
            System.out.println("Image conversion done");

            float[][] output = {};
            System.out.println(byteBuffer);
            interpreter.run(byteBuffer, output);

            float[] result = output[0];
            int maxIndex = MaxIndex(result, 0, result.length);
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            prediction = "Prediction Result: " + maxIndex + "\nConfidence: " + df.format(result[maxIndex]);
            return prediction;
        }
    }

    private int MaxIndex(float[] values, int begin, int end) {
        float max = Float.MIN_VALUE;
        int index = -1;
        for(int i = begin; i < end; i++) {
            if(values[i] > max) {
                max = values[i];
                index = i;
            }
        }
        return index;
    }

    @NonNull
    public final Task classifyAsync(@NonNull final Bitmap bitmap) {
        assert bitmap != null;
        Task var10000 = Tasks.call((Executor)this.executorService, (Callable)(new Callable() {
            public final String call() {
                try {
                    return ImageClassifier.this.classify(bitmap);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                return null;
            }
        }));
        assert var10000 != null;
        return var10000;
    }

    private final ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(this.modelInputSize);
        System.out.println("SIZE " + modelInputSize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[this.inputImageWidth * this.inputImageHeight];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int[] var6 = pixels;
        int var7 = pixels.length;

        for(int var5 = 0; var5 < var7; ++var5) {
            int pixelValue = var6[var5];
            int r = pixelValue >> 16 & 255;
            int g = pixelValue >> 8 & 255;
            int b = pixelValue & 255;
            float normalizedPixelValue = (float)(r + g + b) / 3.0F / 255.0F;
            //System.out.println("normalizedPixelValue " + normalizedPixelValue);
            byteBuffer.putFloat(normalizedPixelValue);
        }

        assert byteBuffer != null;
        return byteBuffer;
    }

    public ImageClassifier(@NonNull Context context) {
        super();
        assert context != null;
        this.context = context;
        ExecutorService var10001 = Executors.newCachedThreadPool();
        assert var10001 != null;
        this.executorService = var10001;
    }

    public static final class Companion {
        private Companion() {
        }

        // $FF: synthetic method
        public Companion(DefaultConstructorMarker $constructor_marker) {
            this();
        }
    }
}