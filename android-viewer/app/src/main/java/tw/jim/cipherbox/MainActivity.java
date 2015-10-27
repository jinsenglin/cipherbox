package tw.jim.cipherbox;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.sample.demo.ApiClientAsyncTask;
import com.google.android.gms.drive.sample.demo.BaseDemoActivity;
import com.google.android.gms.drive.sample.demo.ResultsAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tw.jim.cipherbox.model.MetaData;

public class MainActivity extends BaseDemoActivity {

    private static final String TAG = "cipherbox";

    private ListView mResultsListView;
    private ResultsAdapter mResultsAdapter;

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataCallback =
            new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Problem while retrieving results");
                        return;
                    }
                    mResultsAdapter.clear();
                    mResultsAdapter.append(result.getMetadataBuffer());

                    //
                    new RetrieveDriveFileContentsAsyncTask(MainActivity.this).execute(result.getMetadataBuffer().get(0).getDriveId());

                }
            };

    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            new RetrieveDriveFileContentsAsyncTask(MainActivity.this).execute(result.getDriveId());
        }
    };

    final private class RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            DriveFile file = params[0].asDriveFile();
            DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();

            //
            try {
                Log.i(TAG, "Deserializing metadata from the stream");
                ObjectInputStream in = new ObjectInputStream(driveContents.getInputStream());
                MetaData metadata_obj = (MetaData) in.readObject();

                Log.i(TAG, "Saving metadata from the stream");
                FileOutputStream fos = openFileOutput("metadata", Context.MODE_PRIVATE);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(metadata_obj);
                out.close();
                fos.close();

            } catch (IOException e) {
                Log.e(TAG, "IOException while deserializing metadata from the stream", e);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "ClassNotFoundException while deserializing metadata from the stream", e);
            }

            /*
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(driveContents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
            } catch (IOException e) {
                Log.e(TAG, "IOException while reading from the stream", e);
            }*/

            driveContents.discard(getGoogleApiClient());
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                showMessage("Error while reading from the file");
                return;
            }
            showMessage("File contents: " + result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //
        mResultsListView = (ListView) findViewById(R.id.listView);
        mResultsAdapter = new ResultsAdapter(this);
        mResultsListView.setAdapter(mResultsAdapter);

        // Using internal storage as local application home

        //
        File metadata_file = new File(this.getFilesDir(), "metadata");
        if ( ! metadata_file.exists() ) {
            // Download metadata file from Google Drive
            Log.i(TAG, "Downloading metadata file from Google Drive");
        }

        //
        File rsa_key_pairs_tgz_file = new File(this.getFilesDir(), "rsa-key-pairs.tgz");
        if ( ! rsa_key_pairs_tgz_file.exists() ) {
            // Download rsa-key-pairs.tgz file from Google Drive
            Log.i(TAG, "Downloading rsa-key-pairs.tgz file from Google Drive");
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

    /**
     * Clears the result buffer to avoid memory leaks as soon as the activity is no longer
     * visible by the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mResultsAdapter.clear();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        //
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, "metadata"))
                .build();
        Drive.DriveApi.query(getGoogleApiClient(), query)
                .setResultCallback(metadataCallback);

        /*
        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), EXISTING_FILE_ID)
                .setResultCallback(idCallback);*/
    }
}
