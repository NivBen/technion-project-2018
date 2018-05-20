# technion-project-2018
### *Trusted Processing of Sensitive Data in the Cloud*
---
#### Event Generation
The Event simulation output file has the following fields:

| EventID        | JourneyID           | AutoID  |engine_speed|vehicle_speed|road_speed_limit|transmission_gear_position|beam_status|latitude|longitude|timastamp|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

Currently the class *EventGeneration* writes locally, so you'll need to change the path in in the method **generate_data**:
```JAVA
public void generate_data(...){
    File file = new File ("PATH/TO/events.tsv");
    ...
}
```
The class *Main* runs **main**.
Offers 2 menus: Automated and Manual.
- Automated Menu: Will ask for number of vehicles to generate (starting from ID 1 to given parameter)
- Manual Menu: Will ask for 4 parameters:
-- AutoID - the vehicles ID.
-- driver_persona - 1 of 3 existing driver personas - will impact the driver's style.
-- road_length - Short, Medium, Long - will impact road's speed limit and length.
-- total_journyes - number of continues drives.
---

#### SparkApp
Interactive Application for insurance calculation and statistics running locally using *Apache Spark SQL*.
Offers 2 menus: General and Client specific.
There are 3 database files used (found in SparkApp/main/resources/): 
- Client information - *sample_people.tsv*

PersonID|	FullName|	PreferredName|	Gender|	DateOfBirth|	PhoneNumber|	FaxNumber|	EmailAddress|	Photo|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

- Vehicle information - *sample_auto.tsv*

| AutoID	| OwnerID	|VIN	|Make	|Model	|Year	|DriveTrain	|EngineType	|ExteriorColor	|InteriorColor	|Transmission|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

- Event Data - *sample_events.tsv*

| EventID| JourneyID|AutoID|engine_speed|vehicle_speed|road_speed_limit|transmission_gear_position|beam_status|latitude|longitude|timastamp|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

