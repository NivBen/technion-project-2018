import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;
import org.apache.spark.sql.sources.In;
import scala.tools.cmd.gen.AnyVals;

import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

public class CloudApp {

    static SparkSession spark;
    static Dataset<Row> userDf;
    static Dataset<Row> vehicleDf;
    static Dataset<Row> eventDf;
    static Scanner scanner;

    public static void main(String[] args){
        System.setProperty("hadoop.home.dir", "c:\\winutil\\");
        scanner = new Scanner(System.in);
        spark = SparkSession
                .builder()
                .appName("CloudApp")
                .config("spark.master", "local")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");
        userDf = spark.read()
                .option("sep", "\t")
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("src/main/resources/sample_people.tsv");
        userDf.createOrReplaceTempView("userView");
        vehicleDf = spark.read()
                .option("sep", "\t")
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("src/main/resources/sample_auto.tsv");
        vehicleDf.createOrReplaceTempView("vehicleView");
        eventDf = spark.read()
                .option("sep", "\t")
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("src/main/resources/sample_events.tsv");
        eventDf.createOrReplaceTempView("eventView");

        while(appMenu())
            System.out.println("Returning to main menu...");

        return;

        /*Dataset<Row> sqldf = spark.sql("SELECT EventID, vehicle_speed, road_speed_limit FROM eventview");
        sqldf.show();
        sqldf.coalesce(1).write().format("com.databricks.spark.csv").option("inferSchema", "false").option("header", "true")
                .option("charset", "UTF-8").mode(SaveMode.Overwrite).save("src/main/resources/events_sim_out.tsv");*/

        /*sqldf.write()
                .option("sep", "\t")
                .option("header", "true")
                //.option("inferSchema", "true")
                .csv("src/main/resources/events_sim_out.tsv");*/

        /*JavaRDD<Event> eventRDD = spark.read()
                .textFile("src/main/resources/events.txt")
                .javaRDD()
                .map(line -> {
                    String[] parts = line.split(" ");
                    Event event = new Event();
                    event.setEventID(Integer.parseInt(parts[0]));
                    event.setJourneyID(parts[1]);
                    event.setAutoID(Integer.parseInt(parts[2]));
                    event.setEngineSpeed(Integer.parseInt(parts[3]));
                    event.setVehicleSpeed(Integer.parseInt(parts[4]));
                    event.setRoadSpeedLimit(Integer.parseInt(parts[5]));
                    event.setTransmissionGear(Integer.parseInt(parts[6]));
                    event.setBeamStatus(1);//Integer.parseInt(parts[7]));
                    event.setTimestamp(parts[8]);
                    return event;
                    }
                );

        Dataset<Row> eventDF = spark.createDataFrame(eventRDD, Event.class);
        eventDF.show();*/
        /*eventDF.createOrReplaceTempView("events");
        Dataset<Row> slowDF = spark.sql("SELECT * FROM events WHERE vehicleSpeed <= 60");
        slowDF.show();*/

        /*Dataset<Row> sqlDF =
                spark.sql("SELECT * FROM csv.`src/main/resources/sample_events.tsv`");
        sqlDF.show();*/
    }

    public static boolean appMenu(){
        System.out.println("Welcome to SomeCorp's Insurance App. What would you like to do?");
        System.out.println("1 - Client-specific queries.");
        System.out.println("2 - General queries and statistics.");
        System.out.println("3 - Exit.");
        int option = scanner.nextInt();
        switch(option){
            case 1: clientMenu(); break;
            case 2: GeneralMenu(); break;
            case 3: return false;
            default: System.out.println("Invalid option.");
        }
        return true;
    }

    public static void clientMenu(){
        System.out.println("Please enter client ID. (Enter -1 to return to main menu)");
        int person_id = scanner.nextInt();
        if(person_id == -1)
            return;
        Row[] users = (Row[]) spark.sql("SELECT * FROM userView WHERE PersonID=" + person_id).collect();

        if(users.length == 0){
            System.out.println("Invalid user ID.");
            return;
        }
        Row user = users[0];
        String fullName = user.getString(1);
        String nickName = user.getString(2);
        String gender = user.getString(3);
        String email = user.getString(7);
        System.out.println("- Client menu for user " + person_id + ".");
        System.out.println(fullName + " AKA \"" + nickName + "\", " + gender + ". eMail address: " + email);

        Row[] vehicles = (Row[]) spark.sql("SELECT * FROM vehicleView WHERE OwnerID=" + person_id).collect();

        if(vehicles.length == 0){
            System.out.println(nickName + " doesn't have any registered vehicles.");
            return;
        }
        System.out.println(nickName + "'s registered vehicles:");
        for (int i = 0; i < vehicles.length; i++) {
            Row vehicle = vehicles[i];
            System.out.println(i+1 + " - " + vehicle.getString(8) + " " + vehicle.getString(3) + " " +
                    vehicle.getString(4) + " " + vehicle.getInt(5) + ". (" + vehicle.getString(10) + ")");
        }

        System.out.println("Enter vehicle ID to query.");
        int vehicle_num = scanner.nextInt();
        if(vehicle_num > vehicles.length || vehicle_num < 0) {
            System.out.println("Invalid vehicle num.");
            return;
        }
        Row chosen_vehicle = vehicles[vehicle_num-1];
        print_vehicle_stats(chosen_vehicle.getInt(0));
    }

    public static void GeneralMenu() {
        System.out.println("Please enter client ID. (Enter -1 to return to main menu)");
        Row[] users = (Row[]) spark.sql("SELECT * FROM userView").collect();
        Row[] vehicles = (Row[]) spark.sql("SELECT * FROM vehicleView").collect();
        Integer num_users = users.length;
        Integer vehicles_users = vehicles.length;
        System.out.println("There are " + num_users.toString() + " clients and " + vehicles_users.toString() + " vehicles registered to the system.");

        double sum_insurance_prices = 0;
        int count_vehicles_with_stats = 0;
        VehicleStats vehicle_results = new VehicleStats();
        for (Row vehicle : vehicles) {
            int vehicleID = vehicle.getInt(0);
            boolean has_recorded_events = gather_vehicle_stats(vehicleID, vehicle_results);
            if (has_recorded_events == false) break; /* TODO: change to continue, use break only for illustration*/
            count_vehicles_with_stats++;
            Integer vehicle_year = vehicle.getInt(5);
            String vehicle_model = vehicle.getString(3) + " " + vehicle.getString(4) + " " + vehicle_year.toString();
            Row owner = getOwner(vehicle.getInt(1));
            if (owner == null) {
                System.out.printf("Vehicle number %d, model %s, has the following:\n", vehicle.getInt(0), vehicle_model, owner.getString(1));
            } else {
                System.out.printf("Vehicle number %d, model %s, owned by %s, has the following:\n", vehicle.getInt(0), vehicle_model, owner.getString(1));
            }
            System.out.printf("* Insurance price is: %.2f₪\n", vehicle_results.getInsurance_price());
            System.out.printf("* Percentage of %.2f%% over the speed limit.\n", vehicle_results.getAvg_speed_deviation());
            System.out.printf("* Percentage of %.3f%% wrong transmission gear position.\n", vehicle_results.getAvg_wrong_gears());
            System.out.printf("* Average delay of %.2f seconds until beams are lit.\n", vehicle_results.getAvg_beam_reaction());
            sum_insurance_prices += vehicle_results.getInsurance_price();
        }
        if (count_vehicles_with_stats > 0){
            Double avg_insurance_prices = sum_insurance_prices / ((double) count_vehicles_with_stats);
            System.out.printf("-----------------------Average insurance price is %.2f₪-----------------------", avg_insurance_prices);
        }
    }

    private static Row getOwner(int OwnerID){
        Row[] users = (Row[]) spark.sql("SELECT * FROM userView WHERE PersonID=" + OwnerID).collect();
        return users[0];
    }

    public static void print_vehicle_stats(int AutoID) {
        VehicleStats vehicle_results = new VehicleStats();
        boolean has_recorded_events = gather_vehicle_stats(AutoID, vehicle_results);
        if(has_recorded_events) {
            System.out.printf("* This vehicle insurance price is: %.2f₪\n", vehicle_results.getInsurance_price());
            System.out.printf("* This vehicle has a percentage of %.2f%% over the speed limit.\n", vehicle_results.getAvg_speed_deviation());
            System.out.printf("* This vehicle has a percentage of %.3f%% wrong transmission gear position.\n", vehicle_results.getAvg_wrong_gears());
            System.out.printf("* This vehicle has an average delay of %.2f seconds until beams are lit.\n", vehicle_results.getAvg_beam_reaction());
        } else {
            System.out.println("No events recorded for this vehicle.");
        }
    }

    /**
     * Calculates a given vehicle stats and saves them in vehicleStats.
     * @param AutoID - the vehicle being queried
     * @param vehicleStats - VehicleStats instance to save results
     * @return true if There are events recorded for the vehicle
     * */
    private static boolean gather_vehicle_stats(int AutoID, VehicleStats vehicleStats) {
        Row[] events = (Row[]) spark.sql("SELECT * FROM eventView WHERE AutoID=" + AutoID).collect();
        if(events.length == 0){
            return false;
        }
        // EventID	JourneyID	AutoID	engine_speed	vehicle_speed	road_speed_limit	transmission_gear_position	beam_status latitude    longitude   timastamp
        int vehicle_speed, speed_limit, gear_position; // column values
        Timestamp timestamp;
        double sum_deviation = 0; // sum of deviation from speed limit
        double count_deviated_events = 0; // amount of events in which the driver crossed the speed limit
        int count_wrong_gear_position = 0;
        int sum_beam_reaction_time = 0, count_beam_reaction = 0, num_beam_reactions = 0, beam_status = 0;

        for (int i = 0; i < events.length; i++) {
            Row event = events[i];
            vehicle_speed = event.getInt(4);
            speed_limit = event.getInt(5);
            if(vehicle_speed > speed_limit){
                sum_deviation += (double)(vehicle_speed - speed_limit) / speed_limit;
                count_deviated_events++;
            }

            gear_position = event.getInt(6);
            if(!check_gear(vehicle_speed, gear_position)){
                count_wrong_gear_position++;
            }

            beam_status = event.getInt(7);
            timestamp = event.getTimestamp(10);
            if(!check_headlights(beam_status, timestamp))
                count_beam_reaction++;
            else {
                if(count_beam_reaction != 0){
                    sum_beam_reaction_time += count_beam_reaction;
                    count_beam_reaction=0;
                    num_beam_reactions++;
                }
            }

        }
        if(count_beam_reaction != 0){
            sum_beam_reaction_time += count_beam_reaction;
            num_beam_reactions++;
        }

        double avg_speed_deviation = (count_deviated_events == 0) ? 0 : (sum_deviation / count_deviated_events) * 100;
        double avg_wrong_gears = ((double)count_wrong_gear_position / (double)events.length) * 100;
        double avg_beam_reaction = (sum_beam_reaction_time == 0) ? 0 : ((double)sum_beam_reaction_time / (double)num_beam_reactions);
        double insurance_price = 1500 + 300*(avg_speed_deviation/100) + 150*(avg_wrong_gears/100);
        vehicleStats.setAvg_speed_deviation(avg_speed_deviation);
        vehicleStats.setAvg_wrong_gears(avg_wrong_gears);
        vehicleStats.setAvg_beam_reaction(avg_beam_reaction*10);
        vehicleStats.setInsurance_price(insurance_price);
        return true;
    }

    private static boolean check_gear(int vehicle_speed, int gear_position){
        if(vehicle_speed < 15)
            return gear_position == 1;
        else if (vehicle_speed < 30)
            return gear_position == 2;
        else if (vehicle_speed < 50)
            return gear_position == 3;
        else if (vehicle_speed < 75)
            return gear_position == 4;
        return gear_position == 5;
    }

    private static boolean is_it_night_time(Timestamp timestamp){ // checks if a given timestamp is at night hours
        int hour = Integer.parseInt(timestamp.toString().substring(11,12));
        return (hour >= 17 || hour <= 6);
    }

    private static boolean check_headlights(int beam_status, Timestamp timestamp){
        if(!is_it_night_time(timestamp)) return true; // It's day time, so whatever
        if(beam_status == 0) return true; // It's night time, need to turn lights on!
        return false; // no lights at night
    }

    /*******************************************************************************************************************
     * Holds a specific vehicle statistics
     * */
    public static class VehicleStats{
        private double avg_speed_deviation;
        private double avg_wrong_gears;
        private double avg_beam_reaction;
        private double insurance_price;

        public VehicleStats() {
            this.avg_speed_deviation = 0;
            this.avg_wrong_gears = 0;
            this.avg_beam_reaction = 0;
            this.insurance_price = 0;
        }

        public VehicleStats(double avg_speed_deviation, double avg_wrong_gears, double avg_beam_reaction, double insurance_price) {
            this.avg_speed_deviation = avg_speed_deviation;
            this.avg_wrong_gears = avg_wrong_gears;
            this.avg_beam_reaction = avg_beam_reaction;
            this.insurance_price = insurance_price;
        }

        public double getAvg_speed_deviation() {
            return avg_speed_deviation;
        }

        public double getAvg_wrong_gears() {
            return avg_wrong_gears;
        }

        public double getAvg_beam_reaction() {
            return avg_beam_reaction;
        }

        public double getInsurance_price() {
            return insurance_price;
        }

        public void setAvg_speed_deviation(double avg_speed_deviation) {
            this.avg_speed_deviation = avg_speed_deviation;
        }

        public void setAvg_wrong_gears(double avg_wrong_gears) {
            this.avg_wrong_gears = avg_wrong_gears;
        }

        public void setAvg_beam_reaction(double avg_beam_reaction) {
            this.avg_beam_reaction = avg_beam_reaction;
        }

        public void setInsurance_price(double insurance_price) {
            this.insurance_price = insurance_price;
        }
    }
}

