# technion-project-2018
The Event simulation output file has the following fields:

| EventID        | JourneyID           | AutoID  |engine_speed|vehicle_speed|road_speed_limit|transmission_gear_position|beam_status|timastamp|
| ------------- |:-------------:| -----:| -----:| -----:| -----:| -----:| -----:| -----:|

Currently the class *EventGeneration* writes locally, so you'll need to change the path in in the method **generate_data**:
```JAVA
public void generate_data(...){
    File file = new File ("PATH/TO/events.tsv");
    ...
}
```
The class *Main* runs **main**.
When you run it, it'll ask for 4 parameters:
* AutoID - the vehicles ID.
* driver_persona - 1 of 3 existing driver personas - will impact the driver's style.
* road_length - Short, Medium, Long - will impact road's speed limit and length.
* total_journyes - number of continues drives.