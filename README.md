# transitlog-hfp-importer

Application for importing [HFP data from compressed CSV archives](https://github.com/HSLdevcom/transitlog-hfp-csv-sink) to Transitlog database.

## Building

```bash
./gradlew shadowJar
```

JAR file will be available in `build/libs/transitlog-hfp-importer.jar`

## Usage

```bash
java -jar transitlog-hfp-importer.jar -s <blob storage connection string> -c <blob container> -d <database connection string> -f <timestamp from> -t <timestamp to> 
```

Options:
* `-s` - Blob storage connection string, available from Azure Portal (Security + networking -> Access keys -> Connection string)
* `-c` - Blob container name, e.g. `hfp-v2-test`
* `-d` - JDBC database connection string, e.g. `"jdbc:postgresql://localhost:5432/postgres?user=postgres&password=password"`
* `-f` - Minimum timestamp for data, in format `yyyy-MM-dd'T'HH:mm:ss`
* `-t` - Maximum timestamp for data, in format `yyyy-MM-dd'T'HH:mm:ss`