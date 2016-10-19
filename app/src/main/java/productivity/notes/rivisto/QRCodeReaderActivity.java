package productivity.notes.rivisto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.json.JSONException;
import org.json.JSONObject;

public class QRCodeReaderActivity extends Activity implements QRCodeReaderView.OnQRCodeReadListener {

    private QRCodeReaderView mydecoderview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodereader);

        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setOnQRCodeReadListener(this);

        // Use this function to enable/disable decoding
        mydecoderview.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        mydecoderview.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        mydecoderview.setTorchEnabled(true);

        // Use this function to set front camera preview
        mydecoderview.setFrontCamera();

        // Use this function to set back camera preview
        mydecoderview.setBackCamera();
    }

    @Override
    public void onQRCodeRead(String qrJSON, PointF[] points) {
        try {
            //Log.i("QR Text", qrJSON);
            JSONObject jsonObject = new JSONObject(qrJSON);

            Intent intent = new Intent();
            intent.putExtra("apiKey", jsonObject.getString("apiKey"));
            intent.putExtra("messagingSenderID", jsonObject.getString("messagingSenderId"));
            intent.putExtra("databaseURL", jsonObject.getString("databaseURL"));

            setResult(RESULT_OK, intent);
        } catch (JSONException e) {
            Toast.makeText(this, "Unable to get the details", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
        }

        mydecoderview.stopCamera();
        finish();
    }
}
