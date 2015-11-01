package tw.jim.cipherbox;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.sample.demo.ApiClientAsyncTask;
import com.google.android.gms.drive.sample.demo.BaseDemoActivity;
import com.google.android.gms.drive.sample.demo.ResultsAdapter;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tw.jim.cipherbox.model.MetaData;

public class MainActivity extends BaseDemoActivity {

    private static final String TAG = "cipherbox";

    private ListView mResultsListView;
    private ResultsAdapter mResultsAdapter;

    final private ResultCallback<DriveApi.MetadataBufferResult> ciphertext_metadataCallback =
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
                    new ciphertext_RetrieveDriveFileContentsAsyncTask(MainActivity.this).execute(result.getMetadataBuffer().get(0).getDriveId());

                }
            };

    final private class ciphertext_RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public ciphertext_RetrieveDriveFileContentsAsyncTask(Context context) {
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

            // TODO Decrypting ciphertext from the stream
            Log.i(TAG, "Decrypting ciphertext from the stream");
            driveContents.getInputStream();

            EditText keyname = (EditText) findViewById(R.id.editText2);
            EditText keyphrase = (EditText) findViewById(R.id.editText3);

            // Error: Only the original thread that created a view hierarchy can touch its views.
            Log.i(TAG, "Displaying plaintext from the stream");
            TextView plaintext = (TextView) findViewById(R.id.textView4);
            // plaintext.setText("This is hello.");

            driveContents.discard(getGoogleApiClient());
            return contents;
        }
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> rsakeypairstgz_metadataCallback =
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
                    new rsakeypairstgz_RetrieveDriveFileContentsAsyncTask(MainActivity.this).execute(result.getMetadataBuffer().get(0).getDriveId());

                }
            };

    final private class rsakeypairstgz_RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public rsakeypairstgz_RetrieveDriveFileContentsAsyncTask(Context context) {
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
                Log.i(TAG, "Saving rsa-key-pairs.tgz from the stream");
                FileOutputStream fos = openFileOutput("rsa-key-pairs.tgz", Context.MODE_PRIVATE);
                org.apache.commons.io.IOUtils.copy(driveContents.getInputStream(),fos);
                fos.close();

                Log.i(TAG, "Untaring rsa-key-pairs.tgz");
                final int BUFFER = 2048;
                /** create a TarArchiveInputStream object. **/
                FileInputStream fin = openFileInput("rsa-key-pairs.tgz");
                BufferedInputStream in = new BufferedInputStream(fin);
                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
                TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);
                TarArchiveEntry entry = null;
                /** Read the tar entries using the getNextEntry method **/
                while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                    Log.i(TAG, "Extracting: " + entry.getName());
                    /** If the entry is a directory, create the directory. **/
                    if (entry.isDirectory()) {
                        File f = new File(getFilesDir(), entry.getName());
                        f.mkdirs();
                        Log.i(TAG, "Created dir absolute path: " + f.getAbsolutePath());
                    }
                    /**
                     * If the entry is a file,write the decompressed file to the disk
                     * and close destination stream.
                     **/
                    else {
                        int count;
                        byte data[] = new byte[BUFFER];
                        fos = new FileOutputStream(getFilesDir().getAbsolutePath() + "/" + entry.getName());
                        BufferedOutputStream dest = new BufferedOutputStream(fos,BUFFER);
                        while ((count = tarIn.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.close();
                        Log.i(TAG, "Saved file absolute path: " + getFilesDir().getAbsolutePath() + "/" + entry.getName());
                    }
                }
                /** Close the input stream **/
                tarIn.close();
                Log.i(TAG, "Untar completed successfully!!");

            } catch (IOException e) {
                Log.e(TAG, "IOException while saving rsa-key-pairs.tgz from the stream", e);
            }

            driveContents.discard(getGoogleApiClient());
            return contents;
        }
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadata_metadataCallback =
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
                    new metadata_RetrieveDriveFileContentsAsyncTask(MainActivity.this).execute(result.getMetadataBuffer().get(0).getDriveId());

                }
            };

    final private class metadata_RetrieveDriveFileContentsAsyncTask
            extends ApiClientAsyncTask<DriveId, Boolean, String> {

        public metadata_RetrieveDriveFileContentsAsyncTask(Context context) {
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

            driveContents.discard(getGoogleApiClient());
            return contents;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                // Decrypt cipher text and then display plain text
                EditText filename = (EditText) findViewById(R.id.editText);
                Log.i(TAG, "Reading " + filename.getText().toString() + " file from Google Drive");
                Query metadata_query = new Query.Builder()
                    .addFilter(Filters.contains(SearchableField.TITLE, filename.getText().toString()))
                    .build();
                Drive.DriveApi.query(getGoogleApiClient(), metadata_query)
                    .setResultCallback(ciphertext_metadataCallback);
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
        File metadata_file = new File(this.getFilesDir(), "metadata");
        if ( ! metadata_file.exists() ) {
            // Download metadata file from Google Drive
            Log.i(TAG, "Downloading metadata file from Google Drive");

            Query metadata_query = new Query.Builder()
                    .addFilter(Filters.contains(SearchableField.TITLE, "metadata"))
                    .build();
            Drive.DriveApi.query(getGoogleApiClient(), metadata_query)
                    .setResultCallback(metadata_metadataCallback);
        }

        //
        File rsa_key_pairs_tgz_file = new File(this.getFilesDir(), "rsa-key-pairs.tgz");
        if ( ! rsa_key_pairs_tgz_file.exists() ) {
            // Download rsa-key-pairs.tgz file from Google Drive
            Log.i(TAG, "Downloading rsa-key-pairs.tgz file from Google Drive");

            Query rsakeypairstgz_query = new Query.Builder()
                    .addFilter(Filters.contains(SearchableField.TITLE, "rsa-key-pairs.tgz"))
                    .build();
            Drive.DriveApi.query(getGoogleApiClient(), rsakeypairstgz_query)
                    .setResultCallback(rsakeypairstgz_metadataCallback);
        }

    }
}
