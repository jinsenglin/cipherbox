package tw.jim.cipherbox;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;

import tw.jim.cipherbox.model.MetaData;;

public class InitGoogleDriveSecureStore {
    /** Application name. */
    private static final String APPLICATION_NAME =
        "Cipherbox";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".cipherbox/google");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart. */
    private static final List<String> SCOPES =
        Arrays.asList(DriveScopes.DRIVE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(System.getProperty("user.home") + "/.cipherbox/google/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        
        // Display OAuth Access Token
        System.out.println(credential.getAccessToken());
        
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Drive service = getDriveService();
        
        // create a folder named “cipherbox” on Google Drive
        File root = createFolder(service, "cipherbox");
        
        // create a folder named “ciphertext-files” on Google Drive, under the folder “cipherbox”
        File ciphertext_files_dir = createFolder(service, "ciphertext-files", root.getId());
        
        // upload file ~/.cipherbox/rsa-key-pairs.tgz to Google Drive, under the folder “cipherbox”
        File rsa_key_pairs_tgz = uploadFile(service, "rsa-key-pairs.tgz", "application/x-gzip", System.getProperty("user.home") + "/.cipherbox/rsa-key-pairs.tgz", root.getId());
        
        // serialize “‘metatdata’ object” and save it to file ~/.cipherbox/metadata
        MetaData metadata_obj = new MetaData();
        metadata_obj.ciphertext_files_folder_id = ciphertext_files_dir.getId();
        FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home") + "/.cipherbox/metadata");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(metadata_obj);
        out.close();
        fileOut.close();
        System.out.println("Serialized data is saved in " + System.getProperty("user.home") + "/.cipherbox/metadata");
        
        // upload file ~/.cipherbox/metadata to Google Drive, under the folder “cipherbox”
        File metadata_file = uploadFile(service, "metadata", "application/x-java-serialized-object ", System.getProperty("user.home") + "/.cipherbox/metadata", root.getId());
    }
    
    public static File createFolder(Drive service, String title) throws IOException {
    	File body = new File();
    	body.setTitle(title);
    	body.setMimeType("application/vnd.google-apps.folder");
    	File file = service.files().insert(body).execute();
    	System.out.println("Folder '" + title + "' created.");
    	System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
    	return file;
    }
    public static File createFolder(Drive service, String title, String parentID) throws IOException {
    	File body = new File();
    	body.setTitle(title);
    	body.setMimeType("application/vnd.google-apps.folder");
    	body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
    	File file = service.files().insert(body).execute();
    	System.out.println("Folder '" + title + "' created.");
    	System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
    	return file;
    }
    public static File uploadFile(Drive service, String title, String mimeType, String path, String parentID) throws IOException {
    	File body = new File();
    	body.setTitle(title);
    	body.setMimeType(mimeType);
    	body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
    	java.io.File fileContent = new java.io.File(path);
    	FileContent mediaContent = new FileContent(mimeType, fileContent);
    	File file = service.files().insert(body, mediaContent).execute();
    	System.out.println("File '" + path + "' uploaded.");
    	System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
    	return file;
    }
}

