package org.paumard.flightmonitoring.business;

import org.paumard.flightmonitoring.business.model.Flight;
import org.paumard.flightmonitoring.business.model.FlightID;
import org.paumard.flightmonitoring.business.service.DBService;
import org.paumard.flightmonitoring.business.service.FlightConsumer;
import org.paumard.flightmonitoring.business.service.FlightGUIService;
import org.paumard.flightmonitoring.business.service.PriceMonitoringService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlightMonitoring {

    private static final Map<FlightID, Flight> monitoredFlights = new ConcurrentHashMap<>();

    private final DBService dbService;
    private final PriceMonitoringService priceMonitoringService;
    private final FlightGUIService flightGUIService;

    public FlightMonitoring(DBService dbService, FlightGUIService guiService, PriceMonitoringService monitoringService) {
        this.dbService = dbService;
        this.flightGUIService = guiService;
        this.priceMonitoringService = monitoringService;
    }

    public void followFlight(FlightID flightID) {
        var flight = dbService.fetchFlight(flightID);
        FlightConsumer flightConsumer = price -> flight.updatePrice(price);
        priceMonitoringService.followPrice(flightID, flightConsumer);
    }

    public void monitorFlight(FlightID flightID) {
        var flight = dbService.fetchFlight(flightID);
        monitoredFlights.put(flightID, flight);
    }

    public Future<?> launchDisplay(ScheduledExecutorService executor) {
        Runnable task = () -> {
            System.out.println("Displaying " + monitoredFlights.size() + " flight");
            for (var flight : monitoredFlights.values()) {
                flightGUIService.displayFlight(flight);
            }
        };
        return executor.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
    }
}