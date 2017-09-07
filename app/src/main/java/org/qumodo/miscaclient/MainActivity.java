package org.qumodo.miscaclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.qumodo.network.QSSLClientSocket;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText editText;
    ScrollView scrollView;

    private static final String LOG_TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view);
        editText = (EditText) findViewById(R.id.Message);
        scrollView = (ScrollView) findViewById(R.id.scrollview);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage(textView);
                }
                return false;
            }
        });

    }

    public void sendMessage(View v) {
        String message = editText.getText().toString();
        editText.setText("");
        textView.append(message+"\n\n");
        scrollView.fullScroll(View.FOCUS_DOWN);
        editText.requestFocus();
    }
}
