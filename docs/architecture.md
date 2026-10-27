# Architecture

## Current State

The project currently contains a minimal Java application foundation.

```text
src/
├── main/
│   ├── java/
│   │   └── router/
│   │       └── RouterApplication.java
│   └── resources/
│       └── logback.xml
└── test/
    └── java/
        └── router/
            └── RouterApplicationTest.java
```

## Current Components

### RouterApplication

The application entry point. It emits startup messages and reports the Java runtime version.

### Logging

SLF4J provides the logging API, while Logback supplies the logging implementation and console output configuration.

### Automated Tests

JUnit 5 is used to test the initial application behavior.

## Planned Architectural Evolution

As implementation proceeds, the architecture will be expanded to support packet representation, routing decisions, forwarding, configuration, error handling, and observability.

These are planned capabilities, not features already implemented.

## Design Principles

- Keep components modular and testable.
- Separate routing decisions from packet-processing mechanics.
- Make behavior observable through structured application logging.
- Validate networking behavior with automated tests.
- Document architectural decisions as the design evolves.
