package com.company;

import java.io.*;
import java.util.Scanner;

public class Main {

    //Names/locations for the airport and routes files
    public static String airportFileName = "airports.csv";
    public static String routesFileName = "routes.csv";

    //Using a recursive algorithm, we can get some pretty long routes.  For times sake, using this variable to
    //disregard any flight plan longer than this number of trips
    public static int maxNumberOfTrips = 5;

    public static airport[] airports = ReadInAirports();
    public static route[] routes = ReadInRoutes();

    //Method for reading in all the airports.  Currently not used
    public static airport[] ReadInAirports()
    {
        int lineCount = 0;

        //chunk to get the length of the file, so we can create the right sized array
        try {
            //first run through is to get the length of the file
            BufferedReader readerForLength = new BufferedReader(new FileReader(airportFileName));
            while (readerForLength.readLine() != null) lineCount++;
            readerForLength.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //now we can use the correct size for our array of airports
        airport[] airports = new airport[lineCount-1];

        int airportCounter = 0;
        String line = null;

        try {

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(new FileReader(airportFileName));

            //read first line, which should have headers
            bufferedReader.readLine();

            while((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(",");
                airport currentAirport = new airport();
                currentAirport.name = parts[0];
                currentAirport.city = parts[1];
                currentAirport.country = parts[2];
                currentAirport.iata3 = parts[3];
                currentAirport.latitude = parts[4];
                currentAirport.longitude = parts[5];
                airports[airportCounter] = currentAirport;
                airportCounter++;
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            airportFileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + airportFileName + "'");
        }
        return airports;
    }

    //Method for reading in all routes
    public static route[] ReadInRoutes()
    {
        int lineCount = 0;

        //chunk to get the length of the file, so we can create the right sized array
        try {
            //first run through is to get the length of the file
            BufferedReader readerForLength = new BufferedReader(new FileReader(routesFileName));
            while (readerForLength.readLine() != null) lineCount++;
            readerForLength.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //now we can use the correct size for our array of airports
        route[] routes = new route[lineCount-1];

        String line = null;
        int routeCounter = 0;

        try {
            FileReader fileReader = new FileReader(routesFileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //read first line, which should have headers
            bufferedReader.readLine();

            while((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(",");
                route currentRoute = new route();
                currentRoute.airlineId = parts[0];
                currentRoute.origin = parts[1];
                currentRoute.destination = parts[2];
                routes[routeCounter] = currentRoute;
                routeCounter++;
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            routesFileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + routesFileName + "'");
        }
        return routes;
    }

    //Setup classes
    static class route
    {
        public String airlineId;
        public String origin;
        public String destination;

    }

    static class airport
    {
        public String name;
        public String city;
        public String country;
        public String iata3;
        public String latitude;
        public String longitude;
    }

    private static String currentTrip;
    private static String bestTrip;
    private static String originalOrigin;
    private static String finalDestination;
    private static int currentLengthOfTrip;
    private static int bestLengthOfTrip;
    private static int bailCounter;

    //This is our main recursive function we use for finding the shortest route
    //It goes through the array of routes passed in.  If it find the final destination we're looking for, it checks if
    //that is the shortest trip, and if so, saves it.
    //If it has the next "leg" of our trip, it will recurse through the function again, with the routes with the place
    //we just came from removed
    public static void ourLoop(String origin, route[] routes)
    {
        for (int i = 0; i < routes.length; i++) {

            //current route we're looking at has our origin.  The extra clause is to save looking through flight
            //plans that are really long
            if (routes[i].origin.equals(origin) && currentLengthOfTrip < maxNumberOfTrips && currentLengthOfTrip < bestLengthOfTrip && bailCounter < 150000) {
                //if route has our destination
                if (routes[i].destination.equals(finalDestination)) {
                    //check if the route we just found is the best
                    if (currentLengthOfTrip < bestLengthOfTrip) {
                        //current length we just found is better than the previous best
                        bestTrip = currentTrip + " -> "  + routes[i].origin + " -> " + routes[i].destination;
                        bestLengthOfTrip = currentLengthOfTrip;
                    }
                }
                //here, we've got the origin we want, but not the destination; we want to recurse on the routes with the current one removed
                else {
                        bailCounter++;
                        currentTrip = currentTrip + " -> " + routes[i].origin;
                        currentLengthOfTrip++;
                        //need to create new array of routes with the route we just used removed
                        route[] tempRoutes = airportRemover(routes, routes[i].origin);
                        ourLoop(routes[i].destination, tempRoutes);
                        //this removes the last trip we just added
                        currentTrip = currentTrip.substring(0, currentTrip.length() - 7);
                        currentLengthOfTrip--;
                }
            }
        }
    }

    //Once we've visited an airport, we don't want any more routes that go to that airport
    public static route[] airportRemover(route[] passedInRoutes, String airportToRemove){
        int counter = 0;
        //this first loop is just used to see how big our new array should be
        //by seeing how many routes no longer have airportToRemove in them as destinations
        for (int i = 0; i < passedInRoutes.length; i++){
            if(passedInRoutes[i].destination.equals(airportToRemove))
                counter++;
        }

        route[] newRoutes = new route[passedInRoutes.length - counter];

        int intToDealWithRemovedElement = 0;
        for (int i = 0; i < passedInRoutes.length; i++){
            if (passedInRoutes[i].destination.equals(airportToRemove))
            {
               intToDealWithRemovedElement++;
            }
            else
            {
                newRoutes[i-intToDealWithRemovedElement] = passedInRoutes[i];
            }
    }
        return newRoutes;
    }

    public static void main(String[] args){

        Scanner input = new Scanner(System.in);

        while(true == true) {
            System.out.print("\nType request formatted as follows:  GET ORG DST");
            System.out.print("\nwere ORG is the three letter call sign if the origin");
            System.out.print("\nand DST is the three letter call sign if the destination" + "\n");

            String userInput = input.nextLine();

            if (userInput.length() != 11)
            {
                System.out.print("\nIncorrect length/format in request");
            }
            else if (!(userInput.substring(0,3).equals("get")||userInput.substring(0,3).equals("GET")))
            {
                System.out.print("\nRequest needs to begin with GET");
            }
            else
            {

                String ourOrigin = userInput.substring(4, 7);//"YYZ";
                String ourDestination = userInput.substring(8,11);//"YVR";

                currentTrip = "";
                bestTrip = "";
                originalOrigin = ourOrigin;
                finalDestination = ourDestination;
                currentLengthOfTrip = 0;
                bestLengthOfTrip = routes.length + 1;
                bailCounter = 0;

                ourLoop(ourOrigin, routes);

                if(bestTrip == "")
                {
                    System.out.print("No Route" + "\n");
                }
                else {
                    System.out.print("\n " + bestTrip.substring(4) + "\n");
                }
            }

        }
    }
}
