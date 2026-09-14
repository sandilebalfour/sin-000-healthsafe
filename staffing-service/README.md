# StaffingServiceApp

## Overview

Provides on-call schedules for doctors based on ward and status.

Part of the [HealthSafe](../README.md) project. Independent Maven module, no
parent pom.

MQ: this service publishes to the ActiveMQ topic `staffing-events-topic` — see [`../common/`](../common). Broker URL and topic name come from the common `co.wethinkcode.healthsafe.mq.MqConfig` class alongside it in this module.

REST: calls `ward-service` (`../ward-service`) to validate the ward and
`alert-level-service` (`../alert-level-service`) to read the current Emergency
Status before computing a schedule — see [Integration contracts](../README.md#integration-contracts)
in the root README for the endpoint shapes.

## Project structure

```
staffing-service/
├── pom.xml
└── src/main/java/co/wethinkcode/healthsafe/
    ├── StaffingServiceApp.java
    └── mq/
        └── MqConfig.java
```

## Build

```
mvn package
```

## Run

```
java -jar target/staffing-service.jar
```

Listens on port `7033`.

## Test

No automated tests yet. Manually verify it's up:

```
curl http://localhost:7033/health   # -> OK

checks current emergency level and then set staff based on the level.
curl http;//localhost:7033/staffing/W-02 # -> e.g 
{
    "wardId": "W-02",
    "emergencyLevel": 5,
    "code": "AMBER",
    "doctorsOnCall": 4
}
```

To add real tests, add JUnit 5 + the Surefire plugin to `pom.xml`, put tests under
`src/test/java/co/wethinkcode/healthsafe/`, and run `mvn test`.
