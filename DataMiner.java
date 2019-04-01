package com.brkybrs.TrajectoryProcessing;

import com.vaadin.tapio.googlemaps.client.LatLon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataMiner {

    public ArrayList<LatLon> mainArray = new ArrayList<LatLon>();
    private ArrayList<LatLon> resultArray = new ArrayList<LatLon>();

    private static double Radiance(double distance) {
        return distance * Math.PI / 180.0;
    }

    public double Distance(double lat1, double lat2,  double lon1, double lon2) {
        double radLat1 = Radiance(lat1);
        double radLat2 = Radiance(lat2);
        double delta_lon = Radiance(lon2 - lon1);
        double top_1 = Math.cos(radLat2) * Math.sin(delta_lon);
        double top_2 = Math.cos(radLat1) * Math.sin(radLat2)
                - Math.sin(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double top = Math.sqrt(top_1 * top_1 + top_2 * top_2);
        double bottom = Math.sin(radLat1) * Math.sin(radLat2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(delta_lon);
        double delta_sigma = Math.atan2(top, bottom);
        double distance = delta_sigma * 6378137.0;
        return distance;
    }

    public double Distance(int start, int end, int current) {
        double start_end = Distance(mainArray.get(start).getLat(), mainArray.get(start).getLon(),
                mainArray.get(end).getLat(), mainArray.get(end).getLon());
        double start_current = Distance(mainArray.get(start).getLat(), mainArray.get(start).getLon(),
                mainArray.get(current).getLat(), mainArray.get(current).getLon());
        double current_current = Distance(mainArray.get(current).getLat(), mainArray.get(current).getLon(),
                mainArray.get(end).getLat(), mainArray.get(end).getLon());
        double p = (start_end + start_current + current_current) / 2;
        double square = Math.sqrt(p * (p - start_end) * (p - start_current) * (p - current_current));
        double distance = 2 * square / start_end;
        return distance;
    }

    public ArrayList<LatLon> DataReducer(int startIndex, int endIndex, double threshold) {

        double maxDistance = 0;
        int index = 0;

        if (endIndex <= startIndex + 1) {
            return resultArray;
        }

        for (int i = startIndex + 1; i < endIndex; ++i) {
            double distance = Distance(startIndex, endIndex, i);
            if ( distance > maxDistance) {
                maxDistance = distance;
                index = i;
            }
        }

        if (maxDistance >= threshold) {
            resultArray.add(mainArray.get(index));
            resultArray = DataReducer(startIndex, index, threshold);
            resultArray = DataReducer(index, endIndex, threshold);
        }

        return resultArray;
    }
}
