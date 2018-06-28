# technion-project-2018
### *Trusted Processing of Sensitive Data in the Cloud*
---
#### Event Generation
The Event simulation output file has the following fields (filled pseudo-randomly):

| EventID        | JourneyID           | AutoID  |engine_speed|vehicle_speed|road_speed_limit|transmission_gear_position|beam_status|latitude|longitude|timastamp|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

Change the path for the file under **main**:
```JAVA
public static void main(String[] args)...{
    File file = new File ("PATH/TO/events.tsv");
    ...
}
```
Offers 2 menus: Automated and Manual.
- Automated Menu: Will ask for number of vehicles to generate (starting from ID 1 to given parameter)
- Manual Menu: Will ask for 4 parameters:
>  AutoID - the vehicles ID. <br />
>  driver_persona - 1 of 3 existing driver personas - will impact the driver's style. <br />
>  road_length - Short, Medium, Long - will impact road's speed limit and length. <br />
>  total_journyes - number of continues drives.
---

#### SparkApp - Using Unmodified Spark
Interactive Application for insurance calculation and statistics running locally using *Apache Spark SQL*.
Offers 2 menus: General and Client specific.
There are 3 database files used (found in SparkApp/main/resources/): 
- Client information - *sample_people.tsv*

PersonID|	FullName|	PreferredName|	Gender|	DateOfBirth|	PhoneNumber|	FaxNumber|	EmailAddress|	Photo|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

- Vehicle information - *sample_auto.tsv*

| AutoID	| OwnerID	|VIN	|Make	|Model	|Year	|DriveTrain	|EngineType	|ExteriorColor	|InteriorColor	|Transmission|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|

- Event Data - *sample_events.tsv* (can be generated using Event Generation)

| EventID| JourneyID|AutoID|engine_speed|vehicle_speed|road_speed_limit|transmission_gear_position|beam_status|latitude|longitude|timastamp|
|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|
> There is a java version and a scala equivalent.
---

#### EncryptedSpark
Adding the Encryption option to Apache Spark (using modified Parquet format). <br />
We've added ***HashiCorp Vault*** to Spark, which will hold the keys for encryption/decryption of user files on the cloud,
working locally, with one token under dev mode, it can be updated to serve several clients. <br />
Make sure to have altered parquet-format and parquet-mr folders under folder named 182-231. (then add  our altercations)
##### Instructions:
Open a vault server (```vault server -dev```) and copy the root token provided.
Using spark shell: in order to write an encrypted file:
```SCALA
import spark.implicits._
import org.apache.spark.sql.types._
//------ Writing a file (csv in this example)
val tc = spark.read.schema(
                            StructType(Seq(StructField("c0", IntegerType),
                            StructField("c1", StringType),))).csv("/PATH/TO/sample.csv") 
                            // just an example for a csv file with 2 columns
tc.write.option("parquet.encryption.user.token", "<root token>").
    option("parquet.encryption.key.id", "<Encryption key ID#>"). 
	option("parquet.vault.address", "<VAULT_ADDR>").
    parquet("/PATH/TO/sample_enc.parquet.encrypted")
    // Make sure to add the key to vault prior with an integer identifier, for instance:
	// export VAULT_ADDR='http://127.0.0.1:8200'
	// vault write "secret/keys" <Encryption key ID#>="AAECAwQFBgcICQoLDA0ODw==" 
//------ Reading the encrypted file
sc.hadoopConfiguration.set("parquet.decryption.user.token", "<root token>", "parquet.vault.address", "<VAULT_ADDR>")
val tpe = spark.read.parquet("/PATH/TO/sample_enc.parquet.encrypted")
// tpe now holds the original plaintext
```

##### Update the following files:
>*ParquetFileReader.java* & *ParquetOutputFormat.java* under /182-231/parquet-mr/parquet-hadoop/src/main/java/org/apache/parquet/hadoop <br />
>*VaultKeyRetriever.java* under /182-231/parquet-mr/parquet-hadoop/src/main/java/org/apache/parquet/crypto<br />
>*pom.xml* under /182-231/parquet-mr/parquet-hadoop<br />

Build parquet-format and then build parquet-mr **separately** both using ```mvn clean install -DskipTests```, and then build Spark (2.2.0) using ```./build/mvn -DskipTests clean package```

This version requires the following software (*notice the versions*):
* Apache Spark 2.2.0
* Scala 
* Maven 
* JDK 1.8
* Thrift 0.7.0
* protocol buffers 2.5.0 <br />
There is a file called helper.sh, it shows which commands to run in order to get these versions, DO NOT run this script, view it in order to complete installations of prerequisites (it is not complete, just assists). 
---
#### EncryptedSparkApp
Same as *SparkApp* but using *EncryptedSpark*.
to build do the following:
```Bash
	# Run Master and slave, see https://spark.apache.org/docs/latest/spark-standalone.html
	./sbin/start-master.sh
	./sbin/start-slave.sh <master-spark-URL>
	# Then go to EncryptedSparkApp directory to create a jar
	java -jar cloudapp.java
	mvn build
```

