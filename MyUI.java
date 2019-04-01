package com.brkybrs.TrajectoryProcessing;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.Iterator;

@SpringUI
public class MyUI extends UI {

    private TextField textInput0 = new TextField();

    private Button sendDataButton = new Button("Send Data");
    private Button showMainDataButton = new Button("Show Main Data");
    private Button clearMainDataButton = new Button("Clear Main Data");
    private Button showReducedDataButton = new Button("Show Reduced Data");
    private Button clearReducedDataButton = new Button("Clear Reduced Data");
    private Button startQuery = new Button("Start Query");
    private Button stopQuery = new Button("Stop Query");
    private GoogleMap googleMap;

    private GoogleMapMarker mapMarker = new GoogleMapMarker("DRAGGABLE: Kakolan vankila", new LatLon(60.44291, 22.242415), true, null);

    public String[] trajectory;

    ArrayList<LatLon> points;
    ArrayList<LatLon> reducedPoints;

    @Autowired
    MapService mapService;

    @Autowired
    SnappedPoints snappedPoints;

    //DataMiner dataMiner = new DataMiner();
    DataProcessing dataProcessor = new DataProcessing();

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyUI.class, widgetset="com.example.tester.widgetset.TesterWidgetset")
        public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {

        VerticalLayout vLayout0 = new VerticalLayout();
        CssLayout cssLayout0 = new CssLayout();
        CssLayout cssButtonLayout0 = new CssLayout();
        CssLayout cssButtonLayout1 = new CssLayout();
        CssLayout cssButtonLayout2 = new CssLayout();

        googleMap = new GoogleMap(null, null, "turkish");
        googleMap.setCenter(new LatLon(39.97598,116.3284666));
        googleMap.setSizeFull();

        cssButtonLayout0.addComponents(showMainDataButton, clearMainDataButton);

        cssButtonLayout1.addComponents(showReducedDataButton, clearReducedDataButton);

        cssButtonLayout2.addComponents(startQuery, stopQuery);

        cssLayout0.addComponents(
                googleMap,
                textInput0,
                sendDataButton,
                cssButtonLayout0,
                cssButtonLayout1,
                cssButtonLayout2
        );

        cssLayout0.setSizeFull();

        vLayout0.addComponents(cssLayout0);

        sendDataButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        textInput0.setPlaceholder("Trajectory Data");
        textInput0.setDescription("Enter trajectory data");
        textInput0.clear();

        sendDataButton.addClickListener(clickEvent -> {
            if (textInput0.getValue() == null || textInput0.getValue().equals(""))
                new Notification(
                        "Hata",
                        "Lat-Long girisi algilanmadi",
                        Notification.Type.WARNING_MESSAGE,
                        true
                ).show(Page.getCurrent());
            else
                trajectory = textInput0.getValue().split(" ");

            if (trajectory != null && trajectory.length > 0) {
                points = new ArrayList<LatLon>();

                String[] latlong;

                for (int i = 0; i < trajectory.length; i++) {
                    latlong = trajectory[i].split(","); //O inci index lat, 1 inci index long
                    points.add(new LatLon(Double.parseDouble(latlong[0]), Double.parseDouble(latlong[1])));
                }

                GoogleMapPolyline mainPath = new GoogleMapPolyline(
                        points,
                        "#0000FF",
                        0.8,
                        10
                );

                //dataMiner.mainArray = points;
                //reducedPoints = new ArrayList<LatLon>();
                //reducedPoints = dataMiner.DataReducer(0, points.size()-1, 128.00);
                reducedPoints = dataProcessor.Reduce(points, 0.0005);

                GoogleMapPolyline reducedPath = new GoogleMapPolyline(
                        reducedPoints,
                        "#FF0000",
                        0.8,
                        10
                );

                showMainDataButton.addClickListener(clickEvent1 -> {
                    googleMap.addPolyline(mainPath);
                });
                clearMainDataButton.addClickListener(clickEvent1 -> {
                    googleMap.removePolyline(mainPath);
                });
                showReducedDataButton.addClickListener(clickEvent2 -> {
                    googleMap.addPolyline(reducedPath);
                });
                clearReducedDataButton.addClickListener(clickEvent2 -> {
                    googleMap.removePolyline(reducedPath);
                });
                startQuery.addClickListener(clickEvent3 -> {
                    try {
                        googleMap.addMapClickListener(mapFirstClick -> {
                            double clickLat = mapFirstClick.getLat();
                            double clickLon = mapFirstClick.getLon();
                            LatLon firstClick = new LatLon(clickLat, clickLon);
                            googleMap.addMapClickListener(mapSecondClick ->{
                                LatLon secondClick = new LatLon(mapSecondClick.getLat(), mapSecondClick.getLon());
                                LatLon cornerBelow = new LatLon(
                                        firstClick.getLat() - (firstClick.getLat() - secondClick.getLat()),
                                        secondClick.getLon()
                                );
                                LatLon cornerUpper = new LatLon(
                                        firstClick.getLat(),
                                        firstClick.getLon() - (firstClick.getLon() - secondClick.getLon())
                                );
                                ArrayList<LatLon> query = new ArrayList();

                                query.add(firstClick);
                                query.add(cornerUpper);
                                query.add(secondClick);
                                query.add(cornerBelow);
                                query.add(firstClick);

                                if (query.size() >= 4) {
                                    GoogleMapPolyline queryPolyline = new GoogleMapPolyline(
                                            query,
                                            "#FFF000",
                                            0.8,
                                            10
                                    );
                                    googleMap.addPolyline(queryPolyline);
                                }
                            });
                        });
                    } catch (Exception exc) {
                        new Notification(
                                "Hata",
                                exc.getLocalizedMessage(),
                                Notification.Type.WARNING_MESSAGE,
                                true
                        ).show(Page.getCurrent());
                    }
                });
            } else {
                new Notification(
                        "Hata",
                        "Lat-Long girisi algilanmadi",
                        Notification.Type.WARNING_MESSAGE,
                        true
                ).show(Page.getCurrent());
            }
            textInput0.clear();
        });

        vLayout0.setSpacing(true);
        vLayout0.setSizeFull();

        setContent(vLayout0);
    }
}
