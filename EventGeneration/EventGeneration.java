package com.company;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class EventGeneration {
    private int EventID;
    public EventGeneration() {
        EventID = 1;
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

    public void generate_data(int AutoID, int driver_persona, int road_length, PrintWriter printWriter) {
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
                road_speed_limit = speed_lim(40, 130);
            }
            seg_array[i] = new Segment(road_speed_limit);
        }

        /**  timestamp  calculation **/
        int seconds_between_events = 10;
        Timestamp original = new Timestamp(System.currentTimeMillis());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(original.getTime());

        /** EVENTS **/
        String JourneyID = UUID.randomUUID().toString().toUpperCase(); // randomized string, uses java.util.UUID;
        int engine_speed, transmission_gear_position;
        int vehicle_speed = seg_array[0].getSpeed_limit();
        int seg_speed_limit = seg_array[0].getSpeed_limit();
        transmission_gear_position = get_recommended_gear(seg_speed_limit);
        int beam_status = 0;
        double distance = 0;
        double tire_rad = 2.25;
        // The position is taken up to the 6th decimal
        double latitude = (Math.round( Math.random() * 180 * 1000000.0))/1000000.0;
        double longitude = (Math.round( Math.random() * 180 * 1000000.0))/1000000.0;

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

            int recommended_gear = get_recommended_gear(vehicle_speed);
            transmission_gear_position += random_int(-1,1); // maybe change later, it's only for greater variance
            if(transmission_gear_position == 0) {transmission_gear_position = 1;}
            else if(transmission_gear_position > 5) transmission_gear_position = 5;
            if(transmission_gear_position != recommended_gear){
                if(driver_persona == 0 && Math.random() < 0.75)  { // Careful driver
                    transmission_gear_position = recommended_gear;
                } else if (driver_persona == 1 && Math.random() < 0.55) { // Normal driver
                    transmission_gear_position = recommended_gear;
                } else if (driver_persona == 2 && Math.random() < 0.35){ // Fast driver
                    transmission_gear_position = recommended_gear;
                }
            }
            engine_speed = calc_engine_speed(vehicle_speed, transmission_gear_position, tire_rad);

            latitude = update_angle(latitude); longitude = update_angle(longitude); // update location
            Timestamp timestamp = new Timestamp(cal.getTime().getTime()); // timestamp object
            beam_status = get_beam_status(driver_persona, beam_status, timestamp);

            printWriter.printf("%d\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%.6f\t%.6f\t%s", EventID , JourneyID, AutoID, engine_speed, vehicle_speed,
                    seg_speed_limit, transmission_gear_position, beam_status, latitude, longitude ,timestamp.toString());
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
    }
    /*
     * returns a random number between min and max included
     * */
    public int random_int(int min, int max) {
        double rand = Math.random();
        double distance = (double)(max - min);
        return (int)Math.round( rand*distance + (double) min );
    }
    /*
     * returns a new speed based on the old one, speed limit and driver persona
     * */
    public int changed_speed(int old_speed, int max_sub, int max_add, int min_limit, int max_limit) {
        int temp_speed = old_speed + random_int(max_sub, max_add);
        if (temp_speed < min_limit || temp_speed > max_limit)
            return old_speed;
        return temp_speed;
    }
    /*
     * returns a random speed limit between min and max multiplied by 10
     * */
    public int speed_lim(int min, int max) { // Assumes min and max divide by 10
        return random_int(min/10, max/10) * 10;
    }

    /*
     * returns a an updated angle based on the old one (used with latitude/longitude)
     * */
    public double update_angle(double angle){
        double rand = ((double)random_int(-100, 100)) / 1000000 ;
        angle += rand;
        if(angle < 0.0 ){ angle = 0.0;}
        else if(angle>180.0){ angle = 180.0; }
        return angle;
    }
    public int get_beam_status(int driver_persona, int beam_status, Timestamp timestamp) {
        String time = timestamp.toString().substring(11,12);
        int hour = Integer.parseInt(time);
        if( (hour >= 17 || hour <= 6) && beam_status == 0){
            double prob = Math.random();
            if(driver_persona == 0 && prob < 0.5)
                return 1;
            else if(driver_persona == 1 && prob < 0.3)
                return 1;
            else if(driver_persona == 2 && prob < 0.15)
                return 1;
        }
        return beam_status;
    }

    public int get_recommended_gear(int vehicle_speed){
        if(vehicle_speed < 15)
            return 1;
        else if(vehicle_speed < 30)
            return 2;
        else if(vehicle_speed < 50)
            return 3;
        else if(vehicle_speed < 75)
            return 4;
        else return 5;
    }

    public double get_gear_ratio(int gear){
        if(gear == 1)
            return 3.25;
        else if(gear == 2)
            return 1.8;
        else if(gear == 3)
            return 1.4;
        else if(gear == 4)
            return 1;
        else return 0.9;
    }

    public int calc_engine_speed(int vehicle_speed, int gear, double tire_rad){
        return (int) Math.round(60 * 2.45 * vehicle_speed * get_gear_ratio(gear) / tire_rad);
    }
}
