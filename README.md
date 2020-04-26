# Data Challenge

### Assumptions on the test
The data has a timestamp, but you also mention latency, ordering etc.
I am assuming the timestamp is the authoritative source of truth rather than the event-time or similar.
Therefore I am assuming you do not require me to implement a Timestamp Extractor.    
As such I am assuming the data source is reliable so will discard any record that do not have a parsable timestamp.

## Build Requirements

This project has been built using:

    openjdk 11.0.6
    sbt 1.3.8
    scala 2.12.11
    kafka and zookeeper running locally
    server properties with listeners=PLAINTEXT://localhost:9092 included

## Testing Locally

    create input topic:
    kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic log-frame-topic

    Download file and run:
    gzcat stream.jsonl.gz | kafka-console-producer --broker-list localhost:9092 --topic log-frame-topic

## Running and Testing

At this point in time you require local kafka and zookeeper instances

On importing please run `sbt compile test`
To run locally use `sbt compile run`

## Benchmarking

The following can be run to obtain states for benchmarking

    kafka-consumer-perf-test --topic results-topic --bootstrap-server localhost:9092 --messages 2

Sample Output will be:

start.time, end.time, data.consumed.in.MB, MB.sec, data.consumed.in.nMsg, nMsg.sec, rebalance.time.ms, fetch.time.ms, fetch.MB.sec, fetch.nMsg.sec
2020-04-26 18:15:45:143, 2020-04-26 18:15:45:420, 0.0835, 0.3016, 2, 7.2202, 1587921345388, -1587921345111, -0.0000, -0.0000

With: 
    
    MB.sec: shows how much data transferred in megabytes per second (Throughput on size)
    data.consumed.in.nMsg: shows the count of the total messages consumed during this test
    nMsg.sec: shows how many messages were consumed in a second (Throughput on the count of messages) 


## Next Steps

The next steps I like to complete are:

    Complete the tests so there are IT (integration tests) run to ensure the application behaves as expected and to ensure no accidental breaking changes are created.
    Wrap the application in a docker image which can be published
    Automate data being loaded using the generator using python scripts to allow this to be run locally with 'live' data.
    It is a good idea to have a visualisation of the stream, at the moment this is output in to the console logs.  This should be saved to file to allow it to be run through a kafka visualisation tool.  This will also aid debugging vis KSQL as the aggregate name will be stored and thus queried.
