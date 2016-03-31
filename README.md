# LDIF to CSV

Converts LDIF files named on the command line to CSV on standard out.

## Usage

`java -jar ldif-to-csv.jar input.ldif > output.csv`

## Build

This is a simple maven project.

* Create the jar file: `mvn package`

* Install the jar file: `cp target/ldif-to-csv-${VERSION}.jar ${WHERE_EVER_YOU_WANT_IT}`

## Known Limitations

* No classes
* No tests (see 'no classes...')
* Doesn't handle multi-line values
* Doesn't handle '~' expansion to the user's home drive
