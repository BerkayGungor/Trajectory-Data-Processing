package com.brkybrs.TrajectoryProcessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

@Service
public class MapService {

    @Autowired
    SnappedPoints snappedPoints;

    public SnappedPoints getSnappedPoints(String[] trajectory) throws Exception{

        String apiKey = "AIzaSyCIcgJToC5fG8YIuBJjjP22tUVbBY4km4M";

        String pathData = "";

        for (int i = 0; i < trajectory.length; i++) {
            
            pathData += trajectory[i] + "|";
            
        }
        pathData = pathData.substring(0, pathData.length() - 1);

        String apiUrl = "https://roads.googleapis.com/v1/snapToRoads?path=" + pathData + "&interpolate=true&key=" + apiKey;

        URL url = new URL(apiUrl);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        StringBuffer response=new StringBuffer();
        while((inputLine = bufferedReader.readLine())!=null){
            response.append(inputLine);
        }
        bufferedReader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray jsonSnappedPointsData = jsonResponse.getJSONArray("snappedPoints");

        for (Object o : jsonSnappedPointsData) {
            JSONObject jsonObject = (JSONObject) o;
            JSONObject object = jsonObject.getJSONObject("location");

            snappedPoints.latitude = object.getDouble("latitude");
            snappedPoints.longitude = object.getDouble("longitude");
            snappedPoints.placeId = jsonObject.getString("placeId");
        }

        return snappedPoints;
    }
}
