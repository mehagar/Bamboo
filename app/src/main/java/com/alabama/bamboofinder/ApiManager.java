package com.alabama.bamboofinder;

import android.net.Uri;
import android.util.Log;
import com.google.android.gms.maps.model.LatLngBounds;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Michael H. on 3/22/2015.
 */
public class ApiManager {
    private static final String TAG = "ApiManager";

    private static final String URL_SWLAT = "swlat";
    private static final String URL_SWLNG = "swlng";
    private static final String URL_NELAT = "nelat";
    private static final String URL_NELNG = "nelng";
    private static final String URL_EXTRA = "extra";
    private static final String URL_LATITUDE = "observation[latitude]";
    private static final String URL_LONGITUDE = "observation[longitude]";
    private static final String URL_DATE = "observation[observed_on_string]";
    public static final String URL_DESCRIPTION = "observation[description]";
    private static final String URL_SPECIES_GUESS = "observation[species_guess]";
    private static final String URL_PHOTO = "observation_photo[observation_id]";
    private static final String URL_PROJECT_OBSERVATION = "project_observation[observation_id]";
    private static final String URL_PROJECT_ID = "project_observation[project_id]";
    private static final String JSON_PROJECT = "project_observations";
    private static final String JSON_PROJECT_ID = "project_id";
    private static final String JSON_OBSERVATION_ID = "id";

    private static final String PROJECT_ID = "3846"; // The id of the BambooFinder project on iNaturalist.org

    private static final String BASE_URL = "www.inaturalist.org";
    private static final String BASE_OBSERVATIONS_URL = "https://" + BASE_URL + "/observations.json";
    private static final String BASE_PROJECT_OBSERVATION_URL = "https://" + BASE_URL + "/project_observations.json";

    /* Gets observations from iNaturalist's api within a specified range */
    public static ArrayList<Observation> getObservationsFromNetwork(LatLngBounds bounds) {
        Uri.Builder builder = new Uri.Builder();
        // appendQueryParameter() encodes values
        builder.scheme("https")
                .authority(BASE_URL)
                .appendPath("observations")
                .appendPath("project")
                .appendPath(PROJECT_ID + ".json")
                .appendQueryParameter(URL_SWLAT, String.valueOf(bounds.southwest.latitude))
                .appendQueryParameter(URL_SWLNG, String.valueOf(bounds.southwest.longitude))
                .appendQueryParameter(URL_NELAT, String.valueOf(bounds.northeast.latitude))
                .appendQueryParameter(URL_NELNG, String.valueOf(bounds.northeast.longitude))
                .appendQueryParameter(URL_EXTRA, "projects,observation_photos")
                .build();
        String response;
        try {
            response = sendGet(builder.toString());
        } catch(IOException e) {
            Log.e(TAG, "HTTP GET Failed: " + e.getMessage());
            response = "";
        }

        return JSONDataToObservations(response);
    }

    private static String sendGet(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        String response = "";
        try {
            response = readResponseFromConnection(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
        return response;
    }

    private static String readResponseFromConnection(InputStream is) throws IOException {
        StringBuilder response = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(is));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /* Converts an JSON string to a list of observations */
    private static ArrayList<Observation> JSONDataToObservations(String data) {
        ArrayList<Observation> observations = new ArrayList<Observation>();
        try {
            JSONArray observationsData = new JSONArray(data);
            for (int i = 0; i < observationsData.length(); ++i) {
                JSONObject obs = observationsData.getJSONObject(i);
                JSONArray projects = obs.getJSONArray(JSON_PROJECT);
                for(int j = 0; j < projects.length(); ++j) {
                    String id = projects.getJSONObject(j).getString(JSON_PROJECT_ID);
                    if(id.equals(PROJECT_ID)) {
                        observations.add(new Observation(obs));
                    }
                }
            }
        } catch(JSONException e) {
            Log.e(TAG, "Error parsing json observations : " + e.getMessage());
        }
        return observations;
    }

    /* Uploads one observation to iNaturalist, with its photo stored on the device. */
    public static void uploadObservation(Observation o, String token, InputStream photoFile) {
        Log.d(TAG, "token: " + token);
        uploadObservation(o, token);
        uploadPictureForObservation(o, token, photoFile);
        uploadObservationToProject(o, token);
    }

    // uploads the observation, but does not associate it with a picture or project.
    private static void uploadObservation(Observation o, String token) {
        Uri.Builder paramsBuilder = new Uri.Builder();
        paramsBuilder
                .appendQueryParameter(URL_SPECIES_GUESS, o.getSpeciesGuess())
                .appendQueryParameter(URL_LATITUDE, String.valueOf(o.getLocation().latitude))
                .appendQueryParameter(URL_LONGITUDE, String.valueOf(o.getLocation().longitude))
                .appendQueryParameter(URL_DATE, getFormattedDateString(o.getTimeStamp()))
                .appendQueryParameter(URL_DESCRIPTION, o.getDescription())
                .build();
        try {
            // we can only know the inaturalist observation id by looking at the response from uploading it.
            String response = sendPost(BASE_OBSERVATIONS_URL, paramsBuilder.toString(), token);
            String observationId = getObservationIdFromJSON(new JSONArray(response));
            o.setId(observationId);
            Log.d(TAG, "observationId: " + observationId);
        } catch(Exception e) {
            Log.e(TAG, "HTTP POST Failed: " + e.getMessage());
        }
    }

    private static String getFormattedDateString(Date d) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(d);
    }

    private static String getObservationIdFromJSON(JSONArray jsonArray) throws JSONException {
        return jsonArray.getJSONObject(0).getString(JSON_OBSERVATION_ID);
    }

    private static void uploadPictureForObservation(Observation o, String token, InputStream photoFile) {
        SyncHttpClient client = new SyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("file", photoFile);
        params.put(URL_PHOTO, o.getId());

        Uri.Builder photoBuilder = new Uri.Builder();
        photoBuilder.scheme("https")
                .authority(BASE_URL)
                .appendPath("observation_photos.json")
                .build();
        Log.d(TAG, "Url to post photo was: " + photoBuilder.toString());

        client.addHeader("Authorization", "Bearer " + token);
        client.post(photoBuilder.toString(), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "Photo uploaded successfully");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, "Failed to upload photo");
                Log.d(TAG, "status code: " + statusCode);
            }
        });
    }

    private static void uploadObservationToProject(Observation o, String token) {
        Uri.Builder projectBuilder = new Uri.Builder();
        projectBuilder
                .appendQueryParameter(URL_PROJECT_OBSERVATION, o.getId())
                .appendQueryParameter(URL_PROJECT_ID, PROJECT_ID)
                .build();
        try {
            sendPost(BASE_PROJECT_OBSERVATION_URL, projectBuilder.toString(), token);
        } catch(IOException e) {
            Log.e(TAG, "HTTP POST Failed: " + e.getMessage());
        }
    }

    private static String sendPost(String baseUrl, String params, String token) throws IOException {
        URL url = new URL(baseUrl);

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        String response = "";
        try {
            // setDoOutput sets POST as method
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Authorization", "Bearer " + token);

            if(!params.equals("")) {
                writeParamsToConnection(con.getOutputStream(), params);
            }
            Log.d(TAG, "Response code: " + con.getResponseCode());
            try {
                response = readResponseFromConnection(con.getInputStream());
            } catch(IOException io) {
                Log.e(TAG, "Could not read response");
            }
        } catch(Exception e) {
            Log.e(TAG, "Error sending post: " + e.getMessage());
        } finally {
            con.disconnect();
        }
        return response;
    }

    private static void writeParamsToConnection(OutputStream os, String params) throws Exception {
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        // Chopping off the first character is necessary because Uri.Builder adds a "?", which isn't needed.
        Log.d(TAG, "writing params: " + params.substring(1));
        out.write(params.substring(1));
        out.close();
    }

    public static String callSendGet(String urlSpec) throws IOException {
        return sendGet(urlSpec);
    }

    public static ArrayList<Observation> callJSONDataToObservations(String data) {
        return JSONDataToObservations(data);
    }

    public static void addUserToProject(String token) {
        Uri.Builder addUserBuilder = new Uri.Builder();
        addUserBuilder
                .scheme("https")
                .authority(BASE_URL)
                .appendPath("projects")
                .appendPath(PROJECT_ID)
                .appendPath("join.json");
        try {
            sendPost(addUserBuilder.toString(), "", token);
        } catch(IOException ioe) {
            Log.e(TAG, "Error adding user to project");
        }
    }
}
