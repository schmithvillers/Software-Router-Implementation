# Known Limitations

## Current Status

The project is in Sprint 0, focused on establishing a reliable development foundation.

The current implementation is a startup scaffold, not a functioning network router.

## Limitations

- No packet parsing or packet forwarding is implemented.
- No routing table or route-selection algorithm is implemented.
- No real network interfaces are managed.
- The `config/router.properties` file exists, but application configuration loading has not been implemented.
- No management HTTP server listens on the configured port.
- No end-to-end network forwarding tests exist yet.

## Validation

The current foundation has been verified using:

```bash
./mvnw clean verify
./mvnw exec:java
```

These commands validate the current build, test, and startup behavior. They do not demonstrate real packet-routing functionality.
