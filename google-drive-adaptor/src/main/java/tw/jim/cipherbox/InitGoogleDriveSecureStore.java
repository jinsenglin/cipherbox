package tw.jim.cipherbox;

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

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
        
        // listFolder(service);
        // createFolder(service); // ID: 0ByMlSqqcEbhXZDluZXZlTHRJNEE
        // uploadFile(service);
        // uploadFile(service, "0ByMlSqqcEbhXZDluZXZlTHRJNEE"); // ID: 0ByMlSqqcEbhXQkxSaWNwNDNYQm8
        // downloadFile(service, "0ByMlSqqcEbhXQkxSaWNwNDNYQm8");
    }
    
    public static void listFolder(Drive service) throws IOException {
    	// Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
             .setMaxResults(10)
             .execute();
        List<File> files = result.getItems();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
            }
        }
    }
    public static void createFolder(Drive service) throws IOException {
    	File body = new File();
    	body.setTitle("secure store");
    	body.setMimeType("application/vnd.google-apps.folder");
    	File file = service.files().insert(body).execute();
    	System.out.println("Folder 'secure store' created.");
    	System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
    }
    public static void uploadFile(Drive service) throws IOException {
    	File body = new File();
    	body.setTitle("title");
    	body.setMimeType("application/xml");
    	java.io.File fileContent = new java.io.File("/Users/cclin/Downloads/curl-drive/pom.xml");
    	FileContent mediaContent = new FileContent("application/xml", fileContent);
    	File file = service.files().insert(body, mediaContent).execute();
    	System.out.println("File '/Users/cclin/Downloads/curl-drive/pom.xml' uploaded.");
    	System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
    }
    public static void uploadFile(Drive service, String parentID) throws IOException {
    	File body = new File();
    	body.setTitle("title");
    	body.setMimeType("application/xml");
    	body.setParents(Arrays.asList(new ParentReference().setId(parentID)));
    	java.io.File fileContent = new java.io.File("/Users/cclin/Downloads/curl-drive/pom.xml");
    	FileContent mediaContent = new FileContent("application/xml", fileContent);
    	File file = service.files().insert(body, mediaContent).execute();
    	System.out.println("File '/Users/cclin/Downloads/curl-drive/pom.xml' uploaded.");
    	System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
    }
    public static void downloadFile(Drive service, String fileID) throws IOException {
    	InputStream is = service.files().get(fileID).executeMediaAsInputStream();
    	String result = getStringFromInputStream(is);
    	System.out.println(result);
    }
 // convert InputStream to String
 	private static String getStringFromInputStream(InputStream is) {

 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();

 		String line;
 		try {

 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}

 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}

 		return sb.toString();

 	}
}

