import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int AutoID;
        int driver_persona;
        int road_length;
        int total_journeys;
        System.out.println("Please enter your details below:");
        System.out.println("AutoID {integer}, driver_persona {0 - Careful, 1 - Normal, 2 - Fast}, road_length {0 - Short, 1 - Medium, 2 - Long}, total_journeys {Integer}"  );
        Scanner scan = new Scanner(System.in);
        AutoID = scan.nextInt();
        System.out.println("AutoID: " + AutoID);
        driver_persona = scan.nextInt();
        System.out.println("driver_persona: " + driver_persona);
        road_length = scan.nextInt();
        System.out.println("road_length: " + road_length);
        total_journeys = scan.nextInt();
        System.out.println("total_journeys: " + total_journeys);

        EventGeneration generate = new EventGeneration();
        try {
            generate.generate_data(AutoID, driver_persona, road_length);
        } catch(java.io.IOException e) { System.out.println("IO Exception"); }
        scan.close();
    }

}