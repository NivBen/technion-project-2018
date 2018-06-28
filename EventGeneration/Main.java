package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException , java.util.InputMismatchException{
        File file = new File ("PATH/TO/events.tsv"); // Change path to your wanted .tsv file location
        FileWriter fileWriter = new FileWriter(file, false); // not appending
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.printf("EventID\t"+"JourneyID\tAutoID\tengine_speed\tvehicle_speed\troad_speed_limit\ttransmission_gear_position\tbeam_status\tlatitude\tlongitude\ttimastamp");
        printWriter.println();
        Scanner scan = new Scanner(System.in);
        int AutoID;
        int driver_persona;
        int road_length;
        int total_journeys;
        int active = 1;
        EventGeneration generate = new EventGeneration();

        System.out.println("Welcome to the Event simulation program. What would you like to do?");
        System.out.println("1 - Automated input up to a given AutoID.");
        System.out.println("2 - Manual input for each vehicle.");
        int option = scan.nextInt();
        switch(option){
            case 1:
                try {
                    System.out.println("Enter maximal AutoID (Positive Integer).");
                    int max_AutoID = scan.nextInt();
                    for (AutoID = 1; AutoID <= max_AutoID; AutoID++) {
                        driver_persona = generate.random_int(0, 2);
                        road_length = generate.random_int(0, 2);
                        total_journeys = generate.random_int(1, 5); // change this for longer journeys per vehicle
                        for (int j = 0; j < total_journeys; j++) {
                            generate.generate_data(AutoID, driver_persona, road_length, printWriter);
                        }
                    }
                } catch (java.util.InputMismatchException e) { System.out.println("Invalid input!"); }
                break;
            case 2:
                try {
                    while(active == 1){
                        System.out.println("Please enter your details below:");
                        System.out.println("AutoID {integer}, driver_persona {0 - Careful, 1 - Normal, 2 - Fast}, road_length {0 - Short, 1 - Medium, 2 - Long}, total_journeys {Positive Integer}" );
                        AutoID = scan.nextInt();
                        if(AutoID < 0) { throw new java.io.IOException(); }
                        System.out.println("AutoID: " + AutoID);
                        driver_persona = scan.nextInt();
                        if(driver_persona<0 || driver_persona>2) { throw new java.io.IOException(); }
                        System.out.println("driver_persona: " + driver_persona);
                        road_length = scan.nextInt();
                        if(road_length<0 || road_length>2) { throw new java.io.IOException(); }
                        System.out.println("road_length: " + road_length);
                        total_journeys = scan.nextInt();
                        if(total_journeys < 0) { throw new java.io.IOException(); }
                        System.out.println("total_journeys: " + total_journeys);
                        for (int i = 0; i < total_journeys; i++) {
                            generate.generate_data(AutoID, driver_persona, road_length, printWriter);
                        }
                        System.out.println("Would you like to continue? (Enter 1 for Yes/ 0 for No)");
                        active = scan.nextInt();
                        if(active != 0 && active != 1) {active = 1;}
                    }
                } catch(java.io.IOException e) { System.out.println("IO Exception"); }
                  catch (java.util.InputMismatchException e) { System.out.println("Invalid input!"); }
                break;
            default: System.out.println("Invalid option.");
                     printWriter.close();
                     scan.close();
                     return;
        }
        printWriter.close();
        scan.close();
    }
}