# Java Software Router

An advanced software router implemented in Java. The project is being developed incrementally using Scrum, with a focus on modular architecture, correctness, testing, observability, and network engineering principles.

## Project Goals

- Build a modular software routing engine.
- Implement packet processing and routing capabilities incrementally.
- Develop automated tests for core networking behavior.
- Add structured logging and operational observability.
- Document architectural decisions, limitations, and implementation progress.

## Technology Stack

- **Language:** Java 21
- **Build system:** Maven
- **Build reproducibility:** Maven Wrapper
- **Testing:** JUnit 5
- **Logging:** SLF4J and Logback
- **CI:** GitHub Actions

## Prerequisites

- Java Development Kit (JDK) 21
- Git

Maven is not required separately when using the Maven Wrapper.

## Getting Started

Clone the repository and enter its directory:

```bash
git clone https://github.com/schmithvillers/Software-Router-Implementation.git
cd Software-Router-Implementation
```

Build and run the tests:

```bash
./mvnw clean verify
```

Start the application:

```bash
./mvnw exec:java
```

## Current Implementation Status

The initial foundation includes:

- Java application entry point
- Maven build configuration
- Maven Wrapper
- JUnit test setup
- Console logging configuration

**Important:** The current application is a foundation scaffold. Packet forwarding, routing-table algorithms, and real network-interface integration have not yet been implemented.

## Development Workflow

1. Work on the branch for the current sprint.
2. Make small, focused commits.
3. Run `./mvnw clean verify` before pushing changes.
4. Use pull requests to review and merge completed sprint work.

## Project Documentation

- [Master Scrum Plan](Software_Router_Master_Scrum_Plan.md)
- [Architecture](docs/architecture.md)
- [Known Limitations](docs/KNOWN_LIMITATIONS.md)
- [Changelog](CHANGELOG.md)

## License

License selection and publication are pending.
