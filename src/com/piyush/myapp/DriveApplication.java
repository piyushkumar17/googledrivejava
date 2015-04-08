package com.piyush.myapp;

/**
 * Created by Piyush Kumar on 4/5/2015.
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.*;

class CUD {
    Drive service;

    CUD(Drive par_service) {
        service = par_service;
    }

    /**
     * Creates a new file in drive
     *
     * @param filename
     * @throws IOException
     */
    public void create(String filename) throws IOException {

        File body = new File();

        //File metadata starts here
        body.setTitle(filename);
        body.setDescription("Test Document");
        body.setMimeType("text/plain");

        //Inserting the file
        java.io.File fileContent = new java.io.File(filename);
        FileContent content = new FileContent("text/plain", fileContent);

        File file = service.files().insert(body, content).execute();
        System.out.println("File ID: " + file.getId());

    }

    /**
     * Updates an existing file's content
     *
     * @param filename
     * @throws IOException
     */
    public void update(String filename) throws IOException {

        /*
         * List all the files in the drive
         */
        List<File> result = new ArrayList<File>();
        Drive.Files.List request = service.files().list();
        FileList files = request.execute();
        result.addAll(files.getItems());
        request.setPageToken(files.getNextPageToken());

        /*
         * Using JSON to parse through the result
         */
        String string = result.toString();
        JSONArray jsonArray = new JSONArray(string);
        JSONObject jsonObject;
        String originalFileName;
        String id = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            originalFileName = (String) jsonObject.get("originalFilename");
            if (filename.equals(originalFileName)) {
                id = (String) jsonObject.get("id");

            }
        }

        //Getting the file using id from the API
        File file = service.files().get(id).execute();

        // File's new content.
        java.io.File fileContent = new java.io.File(filename);
        FileContent mediaContent = new FileContent("text/plain", fileContent);

        // Send the request to the API.
        service.files().update(id, file, mediaContent).execute();
    }

    /**
     * Permanently Deletes an existing file in the drive
     *
     * @param filename
     * @throws IOException
     */
    public void delete(String filename) throws IOException {

        /*
         * Getting the list of all the files
         * Using JSON parser to parse through the result
         */
        List<File> result = new ArrayList<File>();
        Drive.Files.List request = service.files().list();
        FileList files = request.execute();
        result.addAll(files.getItems());
        request.setPageToken(files.getNextPageToken());
        String string = result.toString();
        JSONArray jsonArray = new JSONArray(string);
        JSONObject jsonObject;
        String originalFileName;
        String id = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            originalFileName = (String) jsonObject.get("originalFilename");
            if (filename.equals(originalFileName)) {
                id = (String) jsonObject.get("id");
            }
        }
        //Send the request for deleting to the API
        if (id != null) {
            service.files().delete(String.valueOf(id)).execute();
        }
    }
}


public class DriveApplication {

    static Drive service;

    public DriveApplication() {

        try {
        /*
         * Specify the client id, secret and redirect uri generated from developers console
         */
            String CLIENT_ID = "459527256541-83uehvn5l7r893o8jr22kunagqoflh80.apps.googleusercontent.com";
            String CLIENT_SECRET = "uG0kSI8fzVfMyb4WbFcuUuJw";
            String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
                    .setAccessType("online")
                    .setApprovalPrompt("auto").build();
            String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();

            System.out.println("Please open the following URL in your browser then type the authorization code:");
            System.out.println("  " + url);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String code = br.readLine();
            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
            GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

            //Create a new authorized API client
            this.service = new Drive.Builder(httpTransport, jsonFactory, credential).build();
        } catch (IOException e) {
            System.out.println("Error occured " + e);
        }
    }

    private static final String ACTION = "-a";
    private static final String FILE_NAME = "-f";

    public static void main(String[] args) throws IOException {
        DriveApplication drive = new DriveApplication();
        CUD cud = new CUD(service);


        /*args[0] = ACTION;
        args[2] = FILE_NAME;

        if (args[1]=="\"create\""){
            cud.create(args[3]);
        }
        if (args[1]=="\"update\""){
            cud.update(args[3]);
        }
        if (args[1]=="\"delete\""){
            cud.delete(args[3]);
        }*/
    }
}


