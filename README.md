# Starling
[![build](https://github.com/LukeDS-it/starling-migrate/actions/workflows/push-on-master.yml/badge.svg?branch=master)](https://github.com/LukeDS-it/starling-migrate/actions/workflows/push-on-master.yml)

Starling is an ETL tool made as an exercise with technologies such as Scala and
the Akka framework.

It aims to implement the most important functions of an ETL tool but keeping
the runtime configuration simple and lightweight, trying to make it generic
enough to cover many use cases, but also keeping the design easy to extend
to create custom processors.

## Architecture
The architecture is fairly simple and is composed by two main components:
Extractor and Consumer.

### Extractors
An extractor takes the role of both E and T in the ETL process:
An extractor will extract data from a data source, but can also be used to
transform the data coming downstream using input data to interpolate it from
another datasource. Think of it as a `flatMap` operation where each data coming
from the previous step is used to configure a new extractor which in turn will
generate another sequence of data.

### Consumers
Consumers are the L phase of the ETL process. They represent terminal operations
and are usually used to load data into another place.
A process can have any nymber of consumers set, and they will all run in parallel
taking data from the last extractor.

## Building and using
In this section you'll find all you need to get started with Starling.

### Building
To build the project you need to be able to build projects with `sbt`.
You can use either sbt via command line or IntelliJ IDEA (suggested).

### Running
Starling provides the following modes

## Roadmap
