import java.sql.Timestamp

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql._
//import spark.implicits._
import org.apache.spark.sql.types._

object Main {
  var spark : SparkSession = null
  var userDf: Dataset[Row] = null
  var vehicleDf: Dataset[Row] = null
  var eventDf: Dataset[Row] = null

  case class VehicleStats(avg_speed_deviation: Double, avg_wrong_gears: Double, avg_night_drives: Double, insurance_price: Double)

  def main(args: Array[String]) {
    //System.setProperty("hadoop.home.dir", "c:\\winutil\\")
    spark = SparkSession
      .builder()
      .appName("CloudApp")
      .config("spark.master", "local")
      .getOrCreate()
    Logger.getRootLogger.setLevel(Level.ERROR)
    //PersonID	FullName	PreferredName	Gender	DateOfBirth	PhoneNumber	FaxNumber	EmailAddress	Photo
    userDf = spark.read.schema(
      StructType(Seq(
        StructField("PersonID", IntegerType),
        StructField("FullName", StringType),
        StructField("PreferredName", StringType),
        StructField("Gender", StringType),
        StructField("DateOfBirth", StringType),
        StructField("PhoneNumber", StringType),
        StructField("FaxNumber", StringType),
        StructField("EmailAddress", StringType),
        StructField("Photo", StringType))))
      .option("sep", "\t")
      .option("header", "true")
      .csv("src/main/resources/sample_people.tsv")
    /*userDf = spark.read
      .option("sep", "\t")
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/main/resources/sample_people.tsv")*/
    userDf.createOrReplaceTempView("userView")
    vehicleDf = spark.read.schema(
      StructType(Seq(
        StructField("AutoID", IntegerType),
        StructField("OwnerID", IntegerType),
        StructField("VIN", StringType),
        StructField("Make", StringType),
        StructField("Model", StringType),
        StructField("Year", IntegerType),
        StructField("DriveTrain", StringType),
        StructField("EngineType", StringType),
        StructField("ExteriorColor", StringType),
        StructField("InteriorColor", StringType),
        StructField("Transmission", StringType))))
      .option("sep", "\t")
      .option("header", "true")
      .csv("src/main/resources/sample_auto.tsv")
    /*vehicleDf = spark.read
      .option("sep", "\t")
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/main/resources/sample_auto.tsv")*/
    vehicleDf.createOrReplaceTempView("vehicleView")
//	beam_status	latitude	longitude	timastamp
    eventDf = spark.read.schema(
      StructType(Seq(
        StructField("EventID", IntegerType),
        StructField("JourneyID", StringType),
        StructField("AutoID", IntegerType),
        StructField("engine_speed", IntegerType),
        StructField("vehicle_speed", IntegerType),
        StructField("road_speed_limit", IntegerType),
        StructField("transmission_gear_position", IntegerType),
        StructField("beam_status", IntegerType),
        StructField("latitude", FloatType),
        StructField("longitude", FloatType),
        StructField("timastamp", TimestampType))))
      .option("sep", "\t")
      .option("header", "true")
      .csv("src/main/resources/sample_events.tsv")
    /*eventDf = spark.read
      .option("sep", "\t")
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("src/main/resources/sample_events.tsv")*/
    eventDf.createOrReplaceTempView("eventView")

    println("Welcome to SomeCorp's Insurance App!")
    GeneralMenu
  }

  def GeneralMenu(): Unit = {
    val timeSample = System.nanoTime
    val users = spark.sql("SELECT * FROM userView").collect
    val vehicles = spark.sql("SELECT * FROM vehicleView").collect
    val num_users = users.length
    val num_vehicles = vehicles.length
    println(f"There are ${num_users}%1.0f clients and ${num_vehicles}%1.0f vehicles registered to the system.")
    var sum_insurance_prices = 0.0
    var count_vehicles_with_stats = 0
    var vehicle_year : Integer = 0; var vehicle_model = ""; var owner : Row = null
    for (vehicle <- vehicles) {
      vehicle_year = vehicle.getInt(5)
      vehicle_model = s"${vehicle.getString(3)} ${vehicle.getString(4)} ${vehicle_year.toString}"
      owner = getOwner(vehicle.getInt(1))
      if (owner == null) println(s"Vehicle number ${vehicle.getInt(0)}, model ${vehicle_model}, has the following:")
      else println(s"Vehicle number ${vehicle.getInt(0)}, model ${vehicle_model}, owned by ${owner.getString(1)}, has the following:")
      val vehicle_results = gather_vehicle_stats(vehicle.getInt(0))
      if (vehicle_results == null) println("* No recorded Events.")
      else {
        println(f"* Insurance price is: ${vehicle_results.insurance_price}%1.2f₪")
        println(f"* Percentage of ${vehicle_results.avg_speed_deviation}%1.2f%% over the speed limit.")
        println(f"* Percentage of ${vehicle_results.avg_night_drives}%1.3f%% time spent driving late at night.")
        println(f"* Percentage of ${vehicle_results.avg_wrong_gears}%1.3f%% wrong transmission gear position.")
        sum_insurance_prices += vehicle_results.insurance_price
        count_vehicles_with_stats += 1
      }
    }

    if (count_vehicles_with_stats > 0) {
      val avg_insurance_prices = sum_insurance_prices / count_vehicles_with_stats.toDouble
      println(f"-----------------------Average insurance price is ${avg_insurance_prices}.2f₪-----------------------")
    }
    println("*** Completed in: " + ((System.nanoTime - timeSample) / 1e9d) + " seconds. *")
  }


  private def getOwner(OwnerID: Int) : Row = {
    val users = spark.sql("SELECT * FROM userView WHERE PersonID=" + OwnerID).collect
    return users(0)
  }

  /**
    * @param AutoID - the vehicle being queried
    * @return case class VehicleStats
    **/
  private def gather_vehicle_stats(AutoID: Int) : VehicleStats = {
    val events = spark.sql("SELECT vehicle_speed,transmission_gear_position,timastamp FROM eventView WHERE AutoID=" + AutoID).collect

    if (events.length == 0) null
    else {
      // EventID	JourneyID	AutoID	engine_speed	vehicle_speed	road_speed_limit	transmission_gear_position	beam_status latitude    longitude   timastamp
      var vehicle_speed = 0 //; var speed_limit = 0;
      var gear_position = 0
      var timestamp : Timestamp = null
      var count_wrong_gear_position = 0; var count_late_night_drives = 0

      for (event <- events) {
        vehicle_speed = event.getInt(0)
        gear_position = event.getInt(1)
        if (!check_gear(vehicle_speed, gear_position)) count_wrong_gear_position += 1
        timestamp = event.getTimestamp(2)
        if (is_it_late_night_time(timestamp)) count_late_night_drives += 1
      }

      val speeding = spark.sql("SELECT AVG((vehicle_speed-road_speed_limit)/road_speed_limit) FROM eventView WHERE vehicle_speed > road_speed_limit AND AutoID=" + AutoID).head
      val avg_speed_deviation = speeding.getDouble(0)*100.0
      val avg_wrong_gears = (count_wrong_gear_position.toDouble / events.length.toDouble) * 100.0
      val avg_night_drives = if (events.length == 0) 0.0 else (count_late_night_drives.toDouble / events.length.toDouble) * 100.0
      val insurance_price = 1500.0 + 500.0 * (avg_speed_deviation / 100.0) + 200.0 * (avg_wrong_gears / 100.0) + 500.0 * (avg_night_drives / 100.0)

      VehicleStats(avg_speed_deviation, avg_wrong_gears, avg_night_drives, insurance_price)
    }
  }

  private def check_gear(vehicle_speed: Int, gear_position: Int) : Boolean = {
    if (vehicle_speed < 15)  gear_position == 1
    else if (vehicle_speed < 30)  gear_position == 2
    else if (vehicle_speed < 50)  gear_position == 3
    else if (vehicle_speed < 75)  gear_position == 4
    else gear_position == 5
  }

  /*private def is_it_night_time(timestamp: Timestamp): Boolean = {
    val hour = timestamp.toString.substring(11, 13).toInt
    hour >= 17 || hour <= 6
  }*/

  private def is_it_late_night_time(timestamp: Timestamp): Boolean = {
    val hour = timestamp.toString.substring(11, 13).toInt
    hour >= 23 || hour <= 6
  }


}

