package com.brkybrs.TrajectoryProcessing;

import com.vaadin.tapio.googlemaps.client.LatLon;

import java.util.ArrayList;

public class DataProcessing {

    private double DistanceToSection(LatLon pointA, LatLon pointB, LatLon pointC) {

        double triangleArea = Math.abs(
                (
                    (pointA.getLat() * (pointB.getLon() - pointC.getLon())) +
                    (pointB.getLat() * (pointC.getLon() - pointA.getLon())) +
                    (pointC.getLat() * (pointA.getLon() - pointB.getLon()))
                ) / 2
        );
        double distance = Math.sqrt(
                (
                    (pointA.getLat() - pointB.getLat()) * (pointA.getLat() - pointB.getLat())
                ) +
                (
                    (pointA.getLon() - pointB.getLon()) * (pointA.getLon() - pointB.getLon())
                )
        );

        return (2 * triangleArea) / distance;
    }

    public ArrayList<LatLon> Reduce(ArrayList<LatLon> points, double threshold) {

        ArrayList<LatLon> reducedPoints = new ArrayList<>();
        ArrayList<LatLon> listPasser = new ArrayList<>();

        double furthestDistance = 0.0;

        int indexOfFurthestDistance = 0;
        int endPointIndex = points.size() - 1;
        int currentPointIndex = 0;

        for (LatLon point : points) {
            if (currentPointIndex != 0 && currentPointIndex != endPointIndex) {
                double distance = DistanceToSection(point, points.get(0), points.get(endPointIndex));
                if (furthestDistance == 0.0 || distance > furthestDistance) {
                    indexOfFurthestDistance = currentPointIndex;
                    furthestDistance = distance;
                }
            }
            currentPointIndex++;
        }
        if (furthestDistance > threshold) {

            listPasser.addAll(points.subList(0, indexOfFurthestDistance + 1));
            ArrayList<LatLon> firstHalf = Reduce(listPasser, threshold);
            listPasser.clear();

            listPasser.addAll(points.subList(indexOfFurthestDistance, endPointIndex + 1));
            ArrayList<LatLon> secondHalf = Reduce(listPasser, threshold);
            listPasser.clear();

            reducedPoints.addAll(firstHalf);
            reducedPoints.addAll(secondHalf);
        } else {
            reducedPoints.add(points.get(0));
            reducedPoints.add(points.get(endPointIndex));
        }
        return reducedPoints;
    }
}
