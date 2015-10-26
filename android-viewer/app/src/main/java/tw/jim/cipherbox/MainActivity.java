package tw.jim.cipherbox;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "cipherbox";
    private GoogleApiClient mGoogleApiClient;

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.i(TAG, "Problem while retrieving results.");
                        return;
                    }
                    Log.i(TAG, "result: " + result.getMetadataBuffer().get(0).getTitle());
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create application home ".cipherbox" if doesn't exist, using internal storage
        File apphome = new File(this.getApplicationContext().getFilesDir(), ".cipherbox");
        if (apphome.exists()) {
            Log.i(TAG, "Application home .cipherbox exists.");
        }
        else {
            Log.i(TAG, "Application home .cipherbox does not exist.");

            Log.i(TAG, "Trying to create application home .cipherbox ...");
            if (apphome.mkdir()) {
                Log.i(TAG, "Application home .cipherbox is created.");
            }
            else {
                Log.i(TAG, "Application home .cipherbox can not be created.");
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                // Decrypt cipher text and then display plain text
                // TODO get InputStream of the specified file from Google Drive
                // TODO decrypt the content of InputStream using the specified RSA key
                TextView plaintext = (TextView) findViewById(R.id.textView4);
                plaintext.setText("This is hello.");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "API client connected.");

        // Download cipherbox/rsa-key-pairs.tgz to local application home ".cipherbox" if does not exist
        File apphome = new File(this.getApplicationContext().getFilesDir(), ".cipherbox");
        File tgzfile = new File(apphome, "rsa-key-pairs.tgz");
        if (tgzfile.exists()) {
            Log.i(TAG, "File .cipherbox/rsa-key-pairs.tgz exists.");
        }
        else {
            Log.i(TAG, "File .cipherbox/rsa-key-pairs.tgz does not exist.");

            Log.i(TAG, "Trying to download file rsa-key-pairs.tgz from Google Drive.");
            Query query = new Query.Builder()
                    .addFilter(Filters.contains(SearchableField.TITLE, "rsa-key-pairs.tgz"))
                    .build();
            Drive.DriveApi.query(mGoogleApiClient, query)
                    .setResultCallback(metadataCallback);

            // TODO download file rsa-key-pairs.tgz from Google Drive
            // TODO untar file rsa-key-pairs.tgz
        }

        // Download cipherbox/metadata to local application home ".cipherbox" if does not exist
        File metadatafile = new File(apphome, "metadata");
        if (metadatafile.exists()) {
            Log.i(TAG, "File .cipherbox/metadata exists.");
        }
        else {
            Log.i(TAG, "File .cipherbox/metadata does not exist.");

            Log.i(TAG, "Trying to download file metadata from Google Drive.");
            Query query = new Query.Builder()
                    .addFilter(Filters.contains(SearchableField.TITLE, "metadata"))
                    .build();
            Drive.DriveApi.query(mGoogleApiClient, query)
                    .setResultCallback(metadataCallback);

            // TODO download file metadata from Google Drive
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
    }
}
