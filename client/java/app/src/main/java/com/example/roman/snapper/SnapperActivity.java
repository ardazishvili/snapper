package com.example.roman.snapper;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.os.AsyncTask;
import android.app.Activity;
import android.os.Environment;
import android.Manifest;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.graphics.BitmapFactory;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.io.File;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.snapper.Chunk;
import io.grpc.examples.snapper.SnapperGrpc;
import io.grpc.examples.snapper.Resolution;

public class SnapperActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 0;

    private Button sendButton;
    private TextView resultText;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapper);
        sendButton = (Button) findViewById(R.id.send_button);
        resultText = (TextView) findViewById(R.id.grpc_response_text);

        String[] resolutions = new String[] {
                "320 × 240",
                "640 x 480",
                "960 x 720",
                "1024 x 768",
                "1600 x 1200",
                "1920 x 1440",
                "2560 x 1920",
                "2800 x 2100",
                "3200 x 2400",
                "3280 × 2464"};
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, resolutions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        resultText.setMovementMethod(new ScrollingMovementMethod());
    }

    public void sendMessage(View view) {
        sendButton.setEnabled(false);
        resultText.setText("");
        new GrpcTask(this)
                .execute();
    }

    private static class GrpcTask extends AsyncTask<String, Void, String> {
        private final WeakReference<Activity> activityReference;
        private ManagedChannel channel;

        private GrpcTask(Activity activity) {
            this.activityReference = new WeakReference<Activity>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                channel = ManagedChannelBuilder.forAddress("192.168.1.21", 50051).usePlaintext(true).build();
                SnapperGrpc.SnapperBlockingStub stub = SnapperGrpc.newBlockingStub(channel);
                Resolution resolution = Resolution.newBuilder().setWidth(320).setHeight(240).build();

                Iterator<Chunk> chunks;
                chunks = stub.snapshot(resolution);

//              obtain permissions
                if (ContextCompat.checkSelfPermission(activityReference.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activityReference.get(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    } else {
                        ActivityCompat.requestPermissions(activityReference.get(),
                                new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                PERMISSION_REQUEST_STORAGE);
                    }
                } else {
                }

                File downloadFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/" + "somefile.jpg");
                OutputStream outputStream = new FileOutputStream(downloadFile);
                while (chunks.hasNext())
                {
                    Chunk c = chunks.next();
                    outputStream.write(c.getContent().toByteArray());
                }

                return "Done";
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return String.format("Failed... : %n%s", sw);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Activity activity = activityReference.get();
            if (activity == null) {
                return;
            }
            TextView resultText = (TextView) activity.findViewById(R.id.grpc_response_text);
            Button sendButton = (Button) activity.findViewById(R.id.send_button);
            resultText.setText(result);
            sendButton.setEnabled(true);

            ImageView image = (ImageView) activityReference.get().findViewById(R.id.grpc_response_image);

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/somefile.jpg";
            image.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }
}
