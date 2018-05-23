package com.example.roman.snapper;

import android.widget.Button;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.os.AsyncTask;
import android.app.Activity;

import java.lang.ref.WeakReference;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.snapper.SnapperGrpc;
import io.grpc.examples.snapper.Resolution;
import io.grpc.examples.snapper.Reply;

public class SnapperActivity extends AppCompatActivity {
    private Button sendButton;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapper);
        sendButton = (Button) findViewById(R.id.send_button);
        resultText = (TextView) findViewById(R.id.grpc_response_text);
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
                Reply reply = stub.snapshot(resolution);
                return reply.getError().toString();
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
        }
    }
}
