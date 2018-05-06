
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** for timestamp **/
import java.util.*;
import java.sql.*;

public class EventGeneration {

    public EventGeneration() {
    }

    public class Segment {
        private int length;
        private int speed_limit;

        public Segment(int speed_limit) {
            this.speed_limit = speed_limit;
            if(speed_limit <= 50){
                this.length = random_int(2, 7);
            } else if(speed_limit <= 70){
                this.length = random_int(5, 10);
            } else this.length = random_int(8, 20);
        }
        public int getLength() {
            return length;
        }
        public int getSpeed_limit() {
            return speed_limit;
        }
    }

    public void generate_data(int AutoID, int driver_persona, int road_length)
            throws IOException {
        File file = new File ("C:/Users/niv/Desktop/events.txt");
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.printf("EventID\t"+"JourneyID\tAutoID\tengine_speed\tvehicle_speed\troad_speed_limit\ttransmission_gear_position\tbeam_status\ttimastamp");
        printWriter.println();

        /** Randomizing number of ride segments based on road_length parameter **/
        int num_segments;
        if (road_length == 0)  { // Short
            num_segments = random_int(1, 3);
        } else if (road_length == 1) { // Medium
            num_segments = random_int(4, 5);
        } else { // Long
            num_segments = random_int(6, 8);
        }

        /** Setting each segment's speed limit based on road_length parameter **/
        Segment[] seg_array = new Segment[num_segments];
        for (int i = 0; i < seg_array.length; i++) {
            int road_speed_limit;
            if (road_length == 0)  { // Short
                road_speed_limit = speed_lim(30, 60);
            } else if (road_length == 1) { // Medium
                road_speed_limit = speed_lim(30, 90);
            } else { // Long
                road_speed_limit = speed_lim(30, 130);
            }
            seg_array[i] = new Segment(road_speed_limit);
        }

        /**  timestamp  calculation **/
        int seconds_between_events = 10;
        Timestamp original = new Timestamp(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(original.getTime());

        /** EVENTS **/
        int EventID = 1;
        String JourneyID = UUID.randomUUID().toString().toUpperCase(); // randomized string, uses java.util.UUID;
        int engine_speed, transmission_gear_position;
        int vehicle_speed = seg_array[0].getSpeed_limit();
        int seg_speed_limit = seg_array[0].getSpeed_limit();
        double distance = 0;
        int i = 0;
        while (i < num_segments) {
            if(driver_persona == 0) { // Careful driver
                vehicle_speed = changed_speed(vehicle_speed, -3, 2,
                        (int) (seg_speed_limit * 0.75), (int) (seg_speed_limit * 1.1));
            } else if (driver_persona == 1) { // Normal driver
                vehicle_speed = changed_speed(vehicle_speed, -2, 2,
                        (int) (seg_speed_limit * 0.85), (int) (seg_speed_limit * 1.15));
            } else { // Fast driver
                vehicle_speed = changed_speed(vehicle_speed, -2, 3,
                        (int) (seg_speed_limit * 0.90), (int) (seg_speed_limit * 1.3));
            }
            distance += (double) vehicle_speed/360; // KM/(10sec) Event every 10 seconds

            engine_speed = random_int(800,4000);
            transmission_gear_position = random_int(1,6);
            String beam_status = GetBeam_status();

            Timestamp timestamp = new Timestamp(cal.getTime().getTime()); // timestamp object
            printWriter.printf("%d\t%s\t%d\t%d\t%d\t%d\t%d\t%s\t%s", EventID , JourneyID, AutoID, engine_speed, vehicle_speed,
                    seg_speed_limit, transmission_gear_position, beam_status, timestamp.toString());
            printWriter.println();
            cal.add(Calendar.SECOND, seconds_between_events); // adding 10 seconds to next event

            ++EventID;
            if( distance >= seg_array[i].getLength() ) {
                vehicle_speed = seg_array[i].getSpeed_limit();
                seg_speed_limit = seg_array[i].getSpeed_limit();
                distance = 0;
                i++;
            }
        }

        printWriter.close();

    }

    public int random_int(int min, int max) { // returns a random number between min and max included
        double rand = Math.random();
        double distance = (double)(max - min);
        return (int)Math.round( rand*distance + (double) min );
    }

    public int changed_speed(int old_speed, int max_sub, int max_add, int min_limit, int max_limit) {
        int temp_speed = old_speed + random_int(max_sub, max_add);
        if (temp_speed < min_limit || temp_speed > max_limit)
            return old_speed;
        return temp_speed;
    }

    public int speed_lim(int min, int max) { // Assumes min and max divide by 10
        return random_int(min/10, max/10) * 10;
    }

    public String GetBeam_status() {
        int temp = random_int(0,2);
        if (temp == 0) {return "On";} else if(temp == 1) {return "Off";} else {return "On-Fog";}
    }
}
