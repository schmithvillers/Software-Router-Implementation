# Java Software Router — Master Scrum Plan

**Document purpose:** This is the source of truth for the project's scope, sprint sequence, acceptance criteria, dashboard, testing, and GitHub workflow. Use it to track progress across separate sprint chats.

**Project approach:** Build an advanced Java software router using vertical slices. Every sprint should leave the repository in a buildable, testable state and end with a demonstrable capability.

**Important project decisions**
- **Language/runtime:** Java 21 LTS.
- **Build:** Maven, using the Maven Wrapper (`./mvnw`), not Gradle.
- **IDE:** IntelliJ IDEA.
- **Version control:** Git and GitHub.
- **Testing:** JUnit 5, unit tests, packet-level tests, integration tests, and Linux network-namespace tests where applicable.
- **Logging:** SLF4J with Logback.
- **Dashboard:** Web UI built with React + TypeScript; topology visualization with a graph library such as React Flow; charts with a charting library.
- **Management backend:** Java REST API and a live event stream (WebSocket or Server-Sent Events, selected when implementing management).
- **Virtual networking:** Linux network namespaces and veth pairs; TUN/TAP as appropriate to the chosen packet-I/O design.
- **Scope principle:** BGP is an advanced stretch milestone, not a blocker for the first usable router. Do not remove correctness, testing, security, or integration work merely to shorten the schedule.

The original rough estimates of 3–5 focused days per sprint are optimistic for a first end-to-end systems project. Use them as planning units, not deadlines. The actual pace depends on debugging, learning, platform setup, and the amount of existing code that can safely be reused.

---

## 1. Product vision

Build a Java-based software router that can run as a middle hop between virtual networks, make real forwarding decisions, and expose what it is doing through a visual dashboard.

A successful final demo should show traffic entering one interface, being parsed and validated, passing through applicable firewall/NAT/routing/QoS logic, and either being transmitted through an egress interface or dropped with an explainable reason. The dashboard must show the router's actual state and events—not a separate simulation of what the router might have done.

### Target architecture

```text
                         Management Plane
              CLI + Java REST API + Web Dashboard
                         |             ^
             commands / queries       | telemetry
                         v             |
                   Management Services
                         |
             +-----------+------------+
             |                        |
        Control Plane              Telemetry
   Static routes, RIP/OSPF,      Counters, events,
   neighbor state, DHCP/DNS      traces, snapshots
             |                        ^
             v                        |
   +------------------------------------------+
   |             Java Router Core             |
   |                                          |
   | Receive -> Parse -> Validate -> Policy   |
   |          -> Conntrack/NAT -> Route       |
   |          -> Neighbor resolution -> QoS   |
   |          -> Forward or Drop              |
   +------------------------------------------+
                         |
                Packet I/O abstraction
                         |
           Linux TUN/TAP, veth, namespaces
```

The exact processing order varies by packet direction and feature. Implement a documented pipeline with direction-aware NAT and ingress/egress policy stages; do not assume one simplistic order applies to every packet.

### Product planes

- **Data plane:** receives, validates, processes, and forwards packets.
- **Control plane:** maintains routes and neighbor state and runs routing protocols.
- **Management plane:** CLI, REST API, configuration, and operational commands.
- **Observability plane:** metrics, bounded event streams, packet traces, and dashboard data.

### Dashboard product requirements

The dashboard is a core product capability. Its first real-data views should be delivered incrementally, not postponed until the very end.

1. **Overview:** router health, interface status, packet/byte rates, forwarded/dropped counts, active connections, recent errors.
2. **Live topology:** interfaces, networks, router nodes, links, link state, traffic direction, throughput, and packet movement.
3. **Packet explorer:** searchable/filterable event stream with timestamp, ingress/egress interface, source/destination, protocol, ports when available, length, verdict, and drop reason.
4. **Packet trace:** selected packet/flow's processing history, including parser result, policy matches, NAT changes, route selection, neighbor resolution, queueing, and final transmit/drop result where those stages apply.
5. **Routing:** route table, prefix, next hop, egress interface, metric, route source, and longest-prefix-match explanation.
6. **NAT/connection tracking:** original and translated tuples, protocol, connection state, age, timeout, and related flow identifiers.
7. **MAC/neighbor view:** ARP and IPv6 neighbor cache. A separate MAC forwarding database is only meaningful if Layer 2 bridging/switching is implemented; do not present an ARP cache as a MAC switch table.
8. **Firewall:** rules, priority/order, counters, last match, verdicts, and recent drops.
9. **QoS:** classifications, queue depths, rates, drops, rate limits, and scheduling behavior.
10. **Protocols and services:** RIP/OSPF/BGP state as implemented, DHCP leases, DNS cache statistics.
11. **Diagnostics:** logs, configuration status, errors, health, and exportable diagnostic snapshots.

### Observability design rules

- Dashboard data must come from the router's real state and real processing events.
- Do not synchronously block packet forwarding on browser/network delivery.
- Use bounded queues, sampling/filtering, bounded retention, and explicit telemetry-drop counters.
- Keep aggregate counters inexpensive; enable detailed packet tracing selectively.
- Do not store full payloads by default. Payload capture is sensitive and expensive; default to header metadata and explicit opt-in tracing.
- Include a flow/packet correlation identifier where practical so users can connect events across stages.
- Distinguish “packet observed,” “forwarding decision made,” and “packet successfully written to an interface.” These are not equivalent.
- The dashboard must show when data is sampled, delayed, incomplete, or dropped.

---

## 2. Development workflow

Each sprint chat follows this sequence:

1. **Prerequisite learning:** explain the concepts, terminology, and design choices needed before implementation.
2. **Sprint goal and backlog:** break work into small tasks with acceptance criteria.
3. **One task at a time:** provide commands and code when needed; do not jump ahead to all tasks.
4. **Implementation:** developer writes and runs the code.
5. **Review:** share code, tests, screenshots, or command output for review.
6. **Verification:** fix issues until acceptance criteria pass.
7. **Git checkpoint:** inspect the diff, run tests, commit, and push.
8. **Sprint review:** demonstrate the capability.
9. **Retrospective:** record what worked, what failed, and what should change.
10. **Update this plan's progress tracker** before starting the next sprint.

### Status labels

Use these consistently:
- `[ ]` Not started
- `[-]` In progress
- `[x]` Done and verified
- `[!]` Blocked
- `[~]` Deferred by explicit decision

Do not mark a task done merely because its classes exist. Acceptance criteria and tests must pass.

### GitHub workflow

- Keep `main` buildable.
- For each sprint, create a branch such as `sprint-00-foundation` or `sprint-04-nat`.
- Make small commits with meaningful messages, for example:
  - `chore: initialize Maven project`
  - `test: add IPv4 parser tests`
  - `feat: implement longest-prefix route lookup`
  - `feat: publish forwarding telemetry events`
  - `docs: document Linux namespace lab`
- Before pushing, run `./mvnw clean verify` (and frontend checks once the frontend exists).
- Push the sprint branch and open a pull request into `main`, even if working alone. Review the diff and merge when the sprint's DoD passes.
- Tag important milestones, for example `v0.1-foundation`, `v0.2-packet-parser`, `v1.0-mvp`.
- Never commit secrets, tokens, personal environment files, generated `target/`, frontend `node_modules/`, or local IDE state unless a specific file is intentionally shared.
- Keep a `CHANGELOG.md` or release notes for notable milestones and maintain `docs/KNOWN_LIMITATIONS.md`.

### Standard Java/Maven commands

```bash
./mvnw clean test       # unit and configured test suites
./mvnw clean verify     # full Maven verification lifecycle
./mvnw compile          # compile Java sources
./mvnw exec:java        # launch application if exec plugin is configured
git status
git diff
git add <files>
git commit -m "type: concise description"
git push -u origin <branch>
```

Once the React frontend exists, add documented commands for installing dependencies, running the development server, linting, and building the frontend. Do not assume Maven automatically builds the frontend unless we deliberately configure that integration.

---

## 3. Sprint roadmap

## Sprint 0 — Engineering Foundation

**Priority:** P0 — mandatory  
**Goal:** Create a reproducible Java/Maven project and establish a safe development and review workflow before implementing packet logic.

### Learning prerequisites
- JDK vs JVM and Java compiler.
- Maven lifecycle, `pom.xml`, dependencies, scopes, and Maven Wrapper.
- Java packages and Maven standard source layout.
- Git working tree, staging, commits, branches, remotes, and `.gitignore`.
- Unit vs integration tests.
- Logging vs configuration.
- Difference between macOS and Linux networking capabilities.

### Tasks
- [ ] Inspect any existing code before creating or replacing project files.
- [ ] Configure Java 21 in IntelliJ: Project SDK, language level, and Maven runner JRE.
- [ ] Configure Maven and generate Maven Wrapper; use `./mvnw` for reproducible builds.
- [ ] Create/verify `pom.xml`, project coordinates, compiler plugin, Surefire, and `exec-maven-plugin`.
- [ ] Add JUnit 5, SLF4J, and Logback.
- [ ] Create `router.RouterApplication` with a minimal startup log.
- [ ] Create initial package boundaries: `packet`, `network`, `routing`, `forwarding`, `arp`, `nat`, `firewall`, `qos`, `protocols`, `dns`, `dhcp`, `management`, `telemetry`.
- [ ] Create `config/`, `scripts/`, `docs/`, `docker/`, `src/test/java/`, and README.
- [ ] Add `.gitignore` and initial documentation.
- [ ] Add one passing JUnit test and verify test discovery.
- [ ] Add a minimal configuration file and a documented approach for validation; implement full config loading in this sprint if it remains small.
- [ ] Initialize Git, create GitHub repository, commit, push, and verify the remote contains the expected files.
- [ ] Add GitHub Actions CI that checks out the repository, installs Java 21, and runs `./mvnw --batch-mode clean verify`.
- [ ] Document the planned Linux networking environment. On macOS, plan to use a Linux VM or Linux host for network namespaces and veth-based integration tests.
- [ ] Decide the frontend directory and API contract approach; do not build a full dashboard yet.

### Acceptance criteria
- [ ] `./mvnw clean verify` succeeds from a clean checkout.
- [ ] `./mvnw exec:java` starts the application and emits logs.
- [ ] At least one JUnit test runs and passes.
- [ ] Java/Maven setup is documented.
- [ ] GitHub remote is configured and initial commit is pushed.
- [ ] CI runs successfully on GitHub.
- [ ] No packet implementation begins until the foundation is reproducible.

### Demo
Clone the repository into a fresh directory, run the Maven Wrapper build and launch commands, and see the application start.

### Git checkpoint
Branch: `sprint-00-foundation`  
Suggested commit: `chore: establish Maven Java 21 project foundation`

---

## Sprint 1 — Packet Models, Parsing, and Serialization

**Priority:** P0  
**Goal:** Safely parse and serialize the initial packet formats from byte arrays without needing live network interfaces.

### Learning prerequisites
- Bytes, unsigned values, bit masks, byte order/network byte order.
- Ethernet II, IPv4, ICMP, UDP, TCP headers.
- Length fields, checksums, malformed input, and defensive parsing.
- Immutable value objects and separation of parsing from I/O.

### Scope
- Ethernet II frame: source/destination MAC, EtherType, payload.
- IPv4: version, IHL, DSCP/ECN, total length, identification, flags/fragment offset, TTL, protocol, checksum, addresses, options, payload.
- ICMPv4: initial types needed for echo, destination unreachable, and time exceeded.
- UDP header and checksum handling.
- TCP header fields and options boundaries needed for safe inspection; do not attempt a full TCP stack.
- Explicit handling of unsupported EtherTypes/protocols.
- IPv6 base-header parsing may be included if the parser design supports it cleanly, but full IPv6 forwarding belongs in a later sprint.

### Tasks
- [ ] Design packet parsing result/error types.
- [ ] Implement MAC and IPv4 address value objects and formatting.
- [ ] Implement Ethernet parser/serializer.
- [ ] Implement IPv4 parser/serializer and header validation.
- [ ] Implement ICMP, UDP, and TCP header parsers.
- [ ] Handle truncated packets, invalid lengths, bad version/IHL, and options boundaries without crashes or out-of-bounds reads.
- [ ] Implement checksums or checksum verification where required by the supported path.
- [ ] Add golden byte-array tests and round-trip tests.
- [ ] Add malformed-packet and boundary-value tests.
- [ ] Add safe packet summaries for future dashboard use without storing payloads.

### Acceptance criteria
- [ ] Valid fixtures parse to expected fields.
- [ ] Supported packets serialize and parse back consistently.
- [ ] Invalid/truncated packets return controlled parse errors.
- [ ] Tests cover minimum header sizes, options, lengths, flags, and checksums.
- [ ] Parser does not mutate caller-owned byte arrays unexpectedly.
- [ ] No network I/O is required for parser tests.

### Demo
Pass byte arrays through Ethernet → IPv4 → TCP/UDP/ICMP parsing and print a structured packet summary.

### Git checkpoint
Branch: `sprint-01-packet-engine`  
Suggested commit: `feat: add tested Ethernet and IPv4 packet parsing`

---

## Sprint 2 — Addressing, Routing Table, and Longest Prefix Match

**Priority:** P0  
**Goal:** Select the correct route for a destination using a testable routing table before connecting it to real packet I/O.

### Learning prerequisites
- IPv4 address representation and CIDR notation.
- Network prefix and subnet mask.
- Route, next hop, metric, egress interface, and route source.
- Longest Prefix Match (LPM) and deterministic tie-breaking.

### Tasks
- [ ] Implement CIDR/prefix parsing and validation.
- [ ] Implement route model: destination prefix, optional gateway/next hop, interface, metric, route source, and administrative preference if used.
- [ ] Implement routing table add/remove/list operations.
- [ ] Implement LPM correctly.
- [ ] Define deterministic behavior for equal-prefix/equal-preference routes.
- [ ] Handle default route and no-route cases.
- [ ] Add route-table snapshots suitable for REST/dashboard queries.
- [ ] Emit structured route-lookup events with destination, selected route, and outcome.
- [ ] Test overlaps, `/0`, `/32`, invalid prefixes, route removal, and no-match cases.

### Acceptance criteria
- [ ] `10.20.30.42` selects `10.20.30.0/24` over `10.20.0.0/16` and `10.0.0.0/8`.
- [ ] Default route is used only when no more-specific route matches.
- [ ] No route is reported explicitly rather than returning a fabricated route.
- [ ] Route lookup is deterministic and unit tested.
- [ ] Route decision events are defined independently of the dashboard UI.

### Demo
Load sample routes, query several destinations, and show the selected prefix and reason.

### Git checkpoint
Branch: `sprint-02-routing-table`  
Suggested commit: `feat: implement route table and longest-prefix match`

---

## Sprint 3 — First End-to-End IPv4 Router in a Linux Lab

**Priority:** P0 — first real router milestone  
**Goal:** Forward IPv4 traffic between two isolated networks through the Java router.

### Learning prerequisites
- Linux network namespaces and veth pairs.
- TUN vs TAP and Layer 3 vs Layer 2 interfaces.
- IP forwarding, routes, MTU, permissions, and packet-I/O boundaries.
- TTL decrement and IPv4 header checksum update.
- Difference between packet processing in a test harness and forwarding a real packet.

### Platform note
A macOS host does not provide Linux network namespaces directly. Use a Linux VM or Linux host for the namespace lab. Docker Desktop alone should not be assumed to expose every required host networking feature. Keep unit tests runnable on macOS; make Linux integration tests explicit and reproducible.

### Tasks
- [ ] Define a packet-I/O interface so routing logic is not coupled to one OS mechanism.
- [ ] Implement the selected first interface strategy (for example, a Linux TUN interface for Layer 3 IP packets).
- [ ] Add scripts to create and tear down two Linux namespaces and veth links.
- [ ] Configure interface addresses and namespace routes in the lab.
- [ ] Connect received IP packets to parser, route lookup, and forwarding logic.
- [ ] Decrement IPv4 TTL and update the header checksum correctly.
- [ ] Define drop reasons for malformed packets, no route, expired TTL, unsupported protocol, and transmit failure.
- [ ] Add safe shutdown and resource cleanup.
- [ ] Add packet counters and initial ingress/route/egress/drop telemetry.
- [ ] Add integration tests that run only in the supported Linux lab.

### Acceptance criteria
- [ ] Two namespaces can communicate through the Java router without relying on the host kernel to do the router's forwarding on its behalf.
- [ ] A packet's TTL is decremented and checksum remains valid.
- [ ] No-route and TTL-expired packets are not silently forwarded.
- [ ] Received, forwarded, transmitted, and dropped counters are consistent.
- [ ] Scripts clean up namespaces/interfaces even after a failed test.
- [ ] macOS unit-test workflow remains usable.

### Demo
Ping across two Linux namespaces through the Java router and inspect logs/events proving the Java forwarding path was used.

### Git checkpoint
Branch: `sprint-03-ipv4-forwarding`  
Suggested commit: `feat: forward IPv4 packets through Linux virtual interfaces`

---

## Sprint 4 — Neighbor Resolution, ARP, and ICMP Diagnostics

**Priority:** P0  
**Goal:** Resolve next-hop link-layer addresses and provide basic router diagnostics.

### Learning prerequisites
- Why an IP route does not provide a destination MAC address.
- ARP request/reply and cache expiration.
- ICMP echo, destination unreachable, and time exceeded.
- IPv4 forwarding versus traffic addressed to the router itself.

### Tasks
- [ ] Implement ARP packet parsing/creation and validation.
- [ ] Implement ARP/neighbor cache with aging and states.
- [ ] Implement request/retry/timeout behavior with bounded retries.
- [ ] Resolve the next-hop MAC before egress transmission where required.
- [ ] Generate appropriate ICMP errors for supported conditions and suppress errors where protocol rules require it.
- [ ] Distinguish forwarded traffic from packets addressed to the router.
- [ ] Publish ARP/neighbor events and cache snapshots.
- [ ] Add tests for cache hit, cache miss, timeout, retry, malformed ARP, and ICMP generation.

### Acceptance criteria
- [ ] Router resolves directly connected next hops.
- [ ] Cache entries age out correctly.
- [ ] Unresolved neighbors fail with an observable reason rather than hanging forever.
- [ ] Ping/diagnostic behavior works for the supported topology.
- [ ] Dashboard data model can represent neighbor state.

### Demo
Clear the neighbor cache, send traffic, observe ARP resolution, and inspect the resulting neighbor entry and packet events.

### Git checkpoint
Branch: `sprint-04-arp-icmp`  
Suggested commit: `feat: add ARP neighbor resolution and ICMP diagnostics`

---

## Sprint 5 — Telemetry Foundation and First Live Dashboard Slice

**Priority:** P0 for the revised product goal  
**Goal:** Deliver the first real dashboard connected to the running router, not mock data.

### Learning prerequisites
- REST APIs and JSON DTOs.
- Polling versus server-pushed updates.
- Event queues, backpressure, and concurrency.
- React/TypeScript fundamentals and frontend development commands.
- Difference between an aggregate metric and a per-packet event.

### Tasks
- [ ] Define stable telemetry DTOs for packet received, parse result, route decision, neighbor resolution, forwarded, and dropped events.
- [ ] Add flow/correlation IDs where practical.
- [ ] Add bounded event delivery so dashboard clients cannot block packet forwarding.
- [ ] Track telemetry queue overflow/dropped events.
- [ ] Expose read-only REST endpoints for health, interfaces, counters, routes, neighbors, and recent events as available.
- [ ] Add a live stream using WebSocket or Server-Sent Events.
- [ ] Create a React + TypeScript frontend in a clearly documented directory.
- [ ] Build overview cards and a recent packet-event list.
- [ ] Build a simple topology view showing actual configured interfaces/networks and directional traffic indicators.
- [ ] Clearly label unavailable/unsupported fields; never fabricate live values.
- [ ] Add frontend error, disconnected, stale-data, and reconnect states.

### Acceptance criteria
- [ ] Dashboard values come from the Java router.
- [ ] A test packet causes a visible event without manual refresh.
- [ ] Topology displays real configured interfaces and link state.
- [ ] A disconnected dashboard does not stop packet forwarding.
- [ ] Event queue capacity is bounded and telemetry drops are visible.
- [ ] No payload data is captured by default.
- [ ] Frontend build/lint checks are documented and reproducible.

### Demo
Send packets through the router and watch packet counts, recent events, and topology indicators update.

### Git checkpoint
Branch: `sprint-05-live-dashboard-foundation`  
Suggested commit: `feat: stream router telemetry to live dashboard`

---

## Sprint 6 — NAT/PAT and Connection Tracking

**Priority:** P0 for MVP  
**Goal:** Allow private-side traffic to use a translated outside address and maintain return-flow mappings.

### Learning prerequisites
- SNAT, DNAT, PAT, five-tuples, port allocation, and checksum updates.
- Connection tracking and TCP/UDP/ICMP differences.
- NAT interaction with routing and firewall policy.
- Expiration, collisions, and concurrent table access.

### Tasks
- [ ] Define connection and translation key models.
- [ ] Implement SNAT/PAT for the supported IPv4 topology.
- [ ] Implement reverse translation for return traffic.
- [ ] Implement port allocation and collision handling.
- [ ] Track TCP state at a clearly documented supported level, UDP idle timeout, and supported ICMP mapping.
- [ ] Expire stale entries and provide safe table cleanup.
- [ ] Correctly update IPv4 and transport checksums when fields change.
- [ ] Expose NAT table and counters through the API.
- [ ] Add dashboard NAT/connection table with original/translated tuples and state.
- [ ] Add tests for forward and reverse flows, collision, expiration, and malformed traffic.

### Acceptance criteria
- [ ] Private namespace traffic reaches an external test namespace through NAT.
- [ ] Return traffic is mapped back to the correct private host.
- [ ] Distinct flows do not accidentally share a conflicting mapping.
- [ ] Entries expire according to configured timeout policy.
- [ ] NAT changes appear in packet traces and the dashboard.
- [ ] Tests validate checksums after translation.

### Demo
Show a packet before and after translation and inspect the live NAT table.

### Git checkpoint
Branch: `sprint-06-nat-conntrack`  
Suggested commit: `feat: implement IPv4 NAT and connection tracking`

---

## Sprint 7 — Firewall / ACL and Security Policy

**Priority:** P0 for MVP  
**Goal:** Enforce explicit packet policy and explain allow/drop decisions.

### Learning prerequisites
- Stateless ACL vs stateful firewall.
- Rule ordering, default policy, ingress/egress direction.
- Connection tracking interactions with NAT.
- Secure defaults and audit counters.

### Tasks
- [ ] Define rule model: action, protocol, source/destination prefix, ports when applicable, interface, direction, priority, and state conditions.
- [ ] Implement deterministic rule evaluation and default policy.
- [ ] Implement ingress and egress policy hooks at documented points in the pipeline.
- [ ] Integrate supported established/related return traffic with connection tracking.
- [ ] Add rule counters, byte counts, last-match time, and drop reasons.
- [ ] Log policy decisions without dumping sensitive payloads.
- [ ] Expose rule configuration and read-only statistics via API.
- [ ] Build dashboard firewall table and recent-drop view.
- [ ] Test rule ordering, default deny/allow, stateful return traffic, NAT interaction, and invalid rules.

### Acceptance criteria
- [ ] Allowed traffic forwards and denied traffic does not.
- [ ] Return traffic behaves according to documented stateful policy.
- [ ] Every drop has an actionable reason where the router can determine one.
- [ ] Rule counters match test traffic.
- [ ] Dashboard shows the matched rule and verdict.
- [ ] Invalid or unsafe configuration is rejected rather than partially applied.

### Demo
Allow HTTPS-like test traffic, deny a selected flow, and inspect the matching rule and packet trace.

### Git checkpoint
Branch: `sprint-07-firewall-acl`  
Suggested commit: `feat: add ACL engine and stateful firewall policy`

---

## Sprint 8 — IPv6 Forwarding and Neighbor Discovery

**Priority:** P1  
**Goal:** Extend the data plane to basic IPv6 routing and diagnostics.

### Learning prerequisites
- IPv6 header, address notation, extension headers, hop limit.
- ICMPv6 and Neighbor Discovery.
- IPv6 checksum behavior (notably no IPv6 header checksum).
- IPv4/IPv6 differences in routing and NAT assumptions.

### Tasks
- [ ] Complete IPv6 parsing and validation.
- [ ] Implement IPv6 address and prefix handling.
- [ ] Add IPv6 route lookup.
- [ ] Implement Neighbor Discovery for supported scenarios.
- [ ] Decrement Hop Limit and generate supported ICMPv6 errors.
- [ ] Forward IPv6 packets across the Linux lab.
- [ ] Add IPv6 packet events and route/neighbor dashboard support.
- [ ] Add malformed and boundary tests.

### Acceptance criteria
- [ ] Supported IPv6 traffic forwards between namespaces.
- [ ] Hop Limit handling is correct.
- [ ] Neighbor Discovery succeeds for the test topology.
- [ ] Unsupported extension-header behavior is documented and safe.
- [ ] IPv4 behavior remains passing.

### Demo
Ping across the IPv6 lab and inspect the selected IPv6 route and neighbor entry.

### Git checkpoint
Branch: `sprint-08-ipv6`  
Suggested commit: `feat: add IPv6 routing and neighbor discovery`

---

## Sprint 9 — QoS and Traffic Management

**Priority:** P1  
**Goal:** Classify traffic and apply observable queueing/rate policies.

### Learning prerequisites
- DSCP and classification.
- Queueing and scheduling.
- Token bucket rate limiting.
- Queue saturation, drops, and fairness.

### Tasks
- [ ] Define classifiers using source/destination, protocol, ports when available, DSCP, and interface.
- [ ] Implement bounded priority queues.
- [ ] Implement token-bucket rate limiting.
- [ ] Define queue-full and rate-limit drop behavior.
- [ ] Add queue depth, enqueue/dequeue, rate, and drop metrics.
- [ ] Add dashboard QoS view and per-class counters.
- [ ] Add a reproducible traffic-generation script and competing-flow test.
- [ ] Document fairness limitations; schedule Weighted Fair Queuing as an extension if priority queues are stable.

### Acceptance criteria
- [ ] Configured traffic classes receive the intended policy.
- [ ] Rate limits are measurable and within documented tolerance.
- [ ] Queue memory is bounded.
- [ ] Queue drops and delays are observable.
- [ ] Dashboard metrics reflect generated traffic.

### Demo
Generate competing traffic and show a configured class receiving priority or rate limiting.

### Git checkpoint
Branch: `sprint-09-qos`  
Suggested commit: `feat: add traffic classification and token-bucket QoS`

---

## Sprint 10 — Management API, Configuration, and CLI

**Priority:** P1  
**Goal:** Operate and inspect the router without editing source code or attaching a debugger.

### Learning prerequisites
- REST conventions, DTOs, HTTP status codes, validation, and API versioning.
- Configuration lifecycle and safe runtime changes.
- CLI parsing and command design.
- Authentication and management-plane isolation.

### Tasks
- [ ] Define validated router configuration schema and defaults.
- [ ] Support config loading at startup and clear validation errors.
- [ ] Define API endpoints for health, interfaces, routes, neighbors, NAT, firewall, QoS, metrics, and events as implemented.
- [ ] Add route and firewall management operations with validation and safe failure behavior.
- [ ] Add CLI commands such as `show interfaces`, `show routes`, `show arp`, `show nat`, `show firewall`, `show qos`, and `show statistics`.
- [ ] Ensure CLI and REST API use the same underlying service layer.
- [ ] Protect management endpoints; bind to loopback by default for development and document access controls.
- [ ] Add API tests, error-response tests, and configuration tests.
- [ ] Connect dashboard filters and detail panels to the APIs.

### Acceptance criteria
- [ ] Key router state can be queried through documented APIs and CLI commands.
- [ ] Invalid configuration is rejected with useful messages.
- [ ] CLI and API report consistent state.
- [ ] Management access is not unintentionally exposed to untrusted networks.
- [ ] API tests run in CI.

### Demo
Start the router with a config file, inspect routes and NAT state via CLI/API, and view the same state in the dashboard.

### Git checkpoint
Branch: `sprint-10-management-plane`  
Suggested commit: `feat: add router management API and CLI`

---

## Sprint 11 — DHCP and DNS Services

**Priority:** P1  
**Goal:** Provide basic address assignment and DNS caching services for lab clients.

### Learning prerequisites
- DHCPv4 message exchange, leases, pools, and reservations.
- DNS request/response structure, TTL, caching, and upstream forwarding.
- UDP sockets and service binding.
- Safe input validation and resource limits.

### Tasks
- [ ] Implement DHCP DISCOVER/OFFER/REQUEST/ACK for the supported lab scenario.
- [ ] Add address pool, lease time, gateway, DNS option, and MAC-to-lease mapping.
- [ ] Implement lease expiration and duplicate allocation prevention.
- [ ] Implement DNS forwarding to a configured upstream resolver.
- [ ] Add positive caching with TTL; add negative caching only with correct semantics.
- [ ] Add cache size limits, timeouts, and failure handling.
- [ ] Expose DHCP leases and DNS cache statistics via API/dashboard.
- [ ] Add namespace-based integration tests and documentation.

### Acceptance criteria
- [ ] A lab client obtains an address and gateway automatically.
- [ ] Leases expire and addresses can be reused safely.
- [ ] DNS requests resolve through the configured upstream under normal conditions.
- [ ] Cache hits/misses and errors are observable.
- [ ] Timeouts and malformed requests do not hang the service.

### Demo
Create a fresh namespace client, obtain a lease, resolve a domain through the router, and inspect the lease/cache views.

### Git checkpoint
Branch: `sprint-11-dhcp-dns`  
Suggested commit: `feat: add DHCP leasing and DNS caching`

---

## Sprint 12 — Dynamic Routing: RIP

**Priority:** P1  
**Goal:** Exchange routes between router instances using a controlled RIP implementation.

### Learning prerequisites
- Control plane vs data plane.
- Distance-vector routing and hop-count metrics.
- Route advertisements, timers, split horizon, and poison reverse.
- Convergence and route loops.

### Tasks
- [ ] Implement the selected RIP version and explicitly document the supported subset.
- [ ] Implement neighbor/peer handling, advertisements, route timeout, and withdrawal.
- [ ] Add split horizon and poison reverse as appropriate to the selected mode.
- [ ] Feed learned routes into the common routing table with source/metric metadata.
- [ ] Add multi-router namespace topology and failure scripts.
- [ ] Publish protocol state and route-change events to dashboard.

### Acceptance criteria
- [ ] Multiple router instances exchange routes in the test topology.
- [ ] Learned routes are distinguishable from static routes.
- [ ] Route withdrawal/timeout occurs after a neighbor or link failure.
- [ ] Convergence behavior and known limitations are documented.
- [ ] Tests show data-plane forwarding follows updated routes.

### Demo
Disconnect a link and show route withdrawal and updated forwarding.

### Git checkpoint
Branch: `sprint-12-rip`  
Suggested commit: `feat: add RIP route exchange and withdrawal`

---

## Sprint 13 — Dynamic Routing: OSPF

**Priority:** P1 advanced  
**Goal:** Implement a documented OSPF subset with link-state database and shortest-path calculation.

### Learning prerequisites
- Link-state routing, LSAs, LSDB, neighbor state machines, SPF, and Dijkstra.
- Reliable protocol state machines and timers.
- Route installation and convergence.

### Tasks
- [ ] Define the supported OSPF version and scope; do not imply full standards compliance unless achieved.
- [ ] Implement Hello processing and neighbor state transitions for the selected topology.
- [ ] Implement supported LSA handling and link-state database.
- [ ] Implement SPF calculation and route installation.
- [ ] Add aging, withdrawal, and adjacency failure behavior.
- [ ] Build multi-router topology tests and deterministic fixtures.
- [ ] Expose neighbors, LSDB summaries, SPF runs, and route changes to management/dashboard.

### Acceptance criteria
- [ ] Routers form supported adjacencies in the lab.
- [ ] The LSDB and SPF results match the test topology.
- [ ] Link failure triggers recalculation and route updates.
- [ ] Forwarding uses the updated route.
- [ ] Unsupported OSPF features are documented.

### Demo
Use a triangle topology, disable one link, and show the alternate route being selected.

### Git checkpoint
Branch: `sprint-13-ospf`  
Suggested commit: `feat: add OSPF neighbor and SPF routing subset`

---

## Sprint 14 — BGP (Advanced Stretch)

**Priority:** P2 — not an MVP blocker  
**Goal:** Implement a limited, testable BGP subset only after core forwarding and OSPF are stable.

### Scope
- TCP session management.
- OPEN, KEEPALIVE, UPDATE, NOTIFICATION messages for the supported subset.
- Prefix advertisement and withdrawal.
- Basic path attributes and deterministic route selection.
- Peer configuration and session diagnostics.

### Acceptance criteria
- [ ] Two test peers establish a session.
- [ ] Supported prefixes are advertised and withdrawn.
- [ ] Session failure removes or invalidates routes according to documented policy.
- [ ] Route-selection behavior is covered by tests.
- [ ] Implementation limitations are explicit; no claim of full Internet-scale BGP support.

### Demo
Withdraw a prefix and show route and dashboard changes.

### Git checkpoint
Branch: `sprint-14-bgp`  
Suggested commit: `feat: implement limited BGP peer and route exchange`

---

## Sprint 15 — Complete Observability Dashboard

**Priority:** P1 product milestone; foundational slices were delivered earlier  
**Goal:** Deliver the integrated visual operations console for real router behavior.

### Learning prerequisites
- React component/state design, TypeScript DTOs, streaming updates, graph visualization.
- Efficient rendering of high-volume event lists.
- API pagination/filtering and stale-data behavior.
- Telemetry sampling and privacy.

### Views and tasks
- [ ] Overview with health, interface status, packet/byte rates, forwarded/dropped counts, errors, and active connections.
- [ ] Topology view with actual nodes/interfaces/links and directional traffic indicators.
- [ ] Packet explorer with filters for time, interface, protocol, source/destination, verdict, and flow.
- [ ] Packet detail panel with ordered processing trace and explainable route/NAT/firewall decisions.
- [ ] Routing table with LPM explanation for a selected destination.
- [ ] NAT/connection tracking table with original/translated tuples and expiration.
- [ ] ARP/IPv6 neighbor cache view.
- [ ] MAC forwarding database view only if a bridge/switch feature actually exists.
- [ ] Firewall rules, match counters, and recent drops.
- [ ] QoS classes, queue depths, rates, and drops.
- [ ] Protocol/service pages for implemented RIP/OSPF/BGP, DHCP leases, and DNS cache.
- [ ] Filters, pagination, sorting, empty states, API errors, reconnect status, and stale-data indicators.
- [ ] Add a demo mode only if it is visibly labeled as synthetic data and cannot be confused with live telemetry.
- [ ] Add dashboard tests for DTO rendering, API failures, stream reconnects, and event-list performance.
- [ ] Document frontend setup, production build, and how it connects to the Java API.

### Acceptance criteria
- [ ] Every live view reads from real Java router state or actual events.
- [ ] Selecting a packet lets the user inspect available route, NAT, policy, and forwarding decisions.
- [ ] The topology responds to real interface/link changes.
- [ ] High event rates do not block the forwarding path.
- [ ] Sampling, event loss, and stale data are visible.
- [ ] UI does not claim that a packet was transmitted merely because it was received or scheduled.
- [ ] Dashboard remains usable when no traffic is flowing and when the router/API disconnects.

### Demo
Run traffic through the router and show the packet path, route selection, NAT mapping, firewall verdict, and egress result in the dashboard.

### Git checkpoint
Branch: `sprint-15-dashboard`  
Suggested commit: `feat: deliver live router observability dashboard`

---

## Sprint 16 — Reliability, Security, and Performance

**Priority:** P0 for declaring the project complete  
**Goal:** Verify failure handling, resource bounds, security, and performance rather than relying only on happy-path demos.

### Test categories
- [ ] Parser fuzz/property tests and malformed/oversized packets.
- [ ] Invalid checksums, fragments, unsupported protocols, and boundary lengths.
- [ ] No-route, neighbor timeout, interface down, transmit failure, route change, NAT expiration, and protocol neighbor loss.
- [ ] Concurrent traffic across interfaces.
- [ ] NAT port collision and connection-table pressure.
- [ ] Firewall rule changes during traffic.
- [ ] Telemetry queue saturation and dashboard disconnection.
- [ ] Configuration errors and management API abuse cases.
- [ ] Resource leaks: sockets, file descriptors, threads, queues, and retained packet data.
- [ ] Load tests with increasing packet counts (for example 100K, 1M, and, if feasible, 10M packet workloads).
- [ ] Measure packets/sec, throughput, CPU, memory, average latency, p50/p95/p99 latency, drops, and telemetry overhead.
- [ ] Compare performance with telemetry disabled, counters only, and detailed tracing enabled.
- [ ] Establish documented benchmark environment and repeatable scripts.

### Security requirements
- [ ] Management API binds to loopback by default in development.
- [ ] No secrets committed.
- [ ] Validate packet lengths and configuration inputs.
- [ ] Bound queues, caches, connection tables, and event retention.
- [ ] Apply safe defaults and clear failure behavior.
- [ ] Do not log full packet payloads by default.
- [ ] Document required OS privileges and minimize them where feasible.

### Acceptance criteria
- [ ] All automated tests pass in the supported environment.
- [ ] Failure scenarios terminate or recover predictably.
- [ ] Resource use remains bounded under documented stress tests.
- [ ] Performance results include environment, workload, and limitations.
- [ ] Known issues are documented; no unsupported capability is advertised as complete.

### Git checkpoint
Branch: `sprint-16-hardening`  
Suggested commit: `test: add router fault, security, and performance validation`

---

## Sprint 17 — Capstone Integration and Release

**Priority:** P0 final milestone  
**Goal:** Demonstrate the router as an integrated system with multiple networks and dashboard visibility.

### Reference topology

```text
 LAN-A / Client A
        |
      Router R1 -------- Upstream/Test Network
        |
        +------ R2 ------ LAN-B / Client B
                  \
                   R3 ------ LAN-C / Client C
```

Use only the routing protocols and features that have passed their own acceptance criteria. A simpler, reliable topology is better than a complex demo with hidden manual forwarding.

### End-to-end scenarios
- [ ] Normal IPv4 forwarding between LANs.
- [ ] IPv6 forwarding if Sprint 8 is complete.
- [ ] ARP/neighbor resolution on cold and warm cache.
- [ ] NAT outbound flow and return traffic.
- [ ] Firewall allow and deny decisions.
- [ ] QoS classification/rate limiting.
- [ ] Dynamic route change after a link failure, if the relevant protocol is complete.
- [ ] DHCP address assignment and DNS resolution, if services are complete.
- [ ] Dashboard packet trace from ingress through final action.
- [ ] Dashboard route, NAT, neighbor, firewall, and QoS state agree with CLI/API.
- [ ] Restart/reload and documented recovery scenario.

### Documentation and release
- [ ] Architecture overview and component responsibilities.
- [ ] Build/run instructions for macOS and Linux.
- [ ] Linux lab setup and cleanup instructions.
- [ ] Example router configuration.
- [ ] Network topology diagrams.
- [ ] API and CLI reference.
- [ ] Dashboard setup and screenshots/GIFs from real operation.
- [ ] Testing and performance report.
- [ ] Security model and privilege requirements.
- [ ] Known limitations and supported protocol subset.
- [ ] Release notes and version tag.

### Final acceptance criteria
- [ ] Fresh checkout can be built using the Maven Wrapper.
- [ ] The documented demo can be reproduced.
- [ ] The Java router—not a separate kernel route—is responsible for the forwarding path being demonstrated.
- [ ] Tests verify the major supported functions.
- [ ] Dashboard shows actual state and explains observed packet decisions.
- [ ] Known limitations are honest and specific.
- [ ] Final release is tagged and pushed to GitHub.

### Git checkpoint
Branch: `sprint-17-capstone-release`  
Suggested tag: `v1.0.0` only if the release acceptance criteria pass.

---

## 4. MVP milestone

The first meaningful MVP should be considered achieved when these are working together in a Linux virtual network:

- [ ] Java router starts from a reproducible Maven build.
- [ ] Supported Ethernet/IP packet data can be parsed and validated.
- [ ] IPv4 routes use longest-prefix match.
- [ ] Packets are forwarded through the Java packet-I/O path.
- [ ] TTL and required checksums are handled correctly.
- [ ] ARP/neighbor resolution works for the lab topology.
- [ ] SNAT/PAT and return mapping work for the supported case.
- [ ] Firewall policies allow and deny traffic as configured.
- [ ] Unit and Linux integration tests cover the critical paths.
- [ ] The initial dashboard shows real packet events and core counters.

This MVP target is intentionally larger than a parser demo. Do not call it an MVP if packets are only simulated or if the dashboard is populated with mock values.

---

## 5. Cross-sprint Definition of Done

Every task/sprint must satisfy the applicable items below.

### Code quality
- [ ] Code compiles with Java 21 and Maven Wrapper.
- [ ] Responsibilities and dependencies are clear.
- [ ] Error handling is explicit; no swallowed exceptions.
- [ ] Public APIs and non-obvious behavior are documented.
- [ ] No unexplained TODOs in critical paths.

### Tests
- [ ] Unit tests cover normal, boundary, and failure cases.
- [ ] Integration tests cover interactions between components.
- [ ] Network integration tests run in the documented Linux environment.
- [ ] Tests are repeatable and do not depend on undeclared local state.

### Observability
- [ ] Relevant counters/events are emitted.
- [ ] Telemetry cannot block the packet path.
- [ ] Dashboard/API reflects actual state and handles stale/disconnected data.
- [ ] Sensitive packet payloads are not captured by default.

### Documentation and Git
- [ ] README/docs updated.
- [ ] `./mvnw clean verify` passes.
- [ ] Frontend checks pass when applicable.
- [ ] Diff reviewed; no secrets/generated artifacts committed.
- [ ] Sprint branch pushed to GitHub.
- [ ] Sprint acceptance criteria and retrospective recorded.

---

## 6. Suggested repository layout

The layout may evolve as the architecture becomes clearer. Avoid creating empty classes just to fill folders.

```text
software-router/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
├── .gitignore
├── README.md
├── CHANGELOG.md
├── config/
├── docs/
│   ├── architecture.md
│   ├── packet-pipeline.md
│   ├── linux-network-lab.md
│   ├── dashboard.md
│   ├── api.md
│   ├── testing.md
│   └── KNOWN_LIMITATIONS.md
├── scripts/
│   ├── linux-lab-up.sh
│   ├── linux-lab-down.sh
│   └── generate-traffic.sh
├── docker/
├── src/
│   ├── main/
│   │   ├── java/router/
│   │   │   ├── packet/
│   │   │   ├── network/
│   │   │   ├── routing/
│   │   │   ├── forwarding/
│   │   │   ├── arp/
│   │   │   ├── nat/
│   │   │   ├── firewall/
│   │   │   ├── qos/
│   │   │   ├── protocols/
│   │   │   ├── dns/
│   │   │   ├── dhcp/
│   │   │   ├── management/
│   │   │   ├── telemetry/
│   │   │   └── RouterApplication.java
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
├── frontend/
│   ├── package.json
│   ├── src/
│   └── README.md
└── .github/
    └── workflows/
        └── ci.yml
```

---

## 7. Progress tracker

Update this table at the end of each sprint. Keep detailed task checklists in the sprint's own working chat or issue tracker.

| Sprint | Goal | Status | GitHub branch / PR | Demo verified | Notes |
|---|---|---|---|---|---|
| 0 | Engineering foundation | [ ] Not started | | [ ] | |
| 1 | Packet parsing/serialization | [ ] Not started | | [ ] | |
| 2 | Routing table and LPM | [ ] Not started | | [ ] | |
| 3 | IPv4 forwarding in Linux lab | [ ] Not started | | [ ] | |
| 4 | ARP and ICMP | [ ] Not started | | [ ] | |
| 5 | Telemetry and first live dashboard | [ ] Not started | | [ ] | |
| 6 | NAT and connection tracking | [ ] Not started | | [ ] | |
| 7 | Firewall and ACL | [ ] Not started | | [ ] | |
| 8 | IPv6 | [ ] Not started | | [ ] | |
| 9 | QoS | [ ] Not started | | [ ] | |
| 10 | Management API and CLI | [ ] Not started | | [ ] | |
| 11 | DHCP and DNS | [ ] Not started | | [ ] | |
| 12 | RIP | [ ] Not started | | [ ] | |
| 13 | OSPF | [ ] Not started | | [ ] | |
| 14 | BGP stretch | [ ] Not started | | [ ] | |
| 15 | Complete dashboard | [ ] Not started | | [ ] | |
| 16 | Hardening/performance | [ ] Not started | | [ ] | |
| 17 | Capstone/release | [ ] Not started | | [ ] | |

---

## 8. Sprint review and retrospective template

At the end of every sprint, record:

```text
Sprint:
Goal:
Completed stories:
Acceptance criteria passed:
Tests run and results:
Demo performed:
Git branch / PR / commit:
What went well:
What blocked progress:
Bugs or technical debt:
Architecture decisions:
Known limitations:
Changes needed in the master plan:
Next sprint readiness:
```

### Working agreement for separate sprint chats

At the start of each sprint chat, state the sprint number and use this master plan as the source of truth. Begin with the concepts needed for that sprint, then provide one implementation task at a time. Review the submitted work before moving forward. At sprint completion, summarize evidence of completion and GitHub status, then update the progress tracker here or in the maintained project source.

**First action:** finish Sprint 0. Do not begin packet parsing until the Maven build, application startup, first test, GitHub push, and CI baseline are working.
