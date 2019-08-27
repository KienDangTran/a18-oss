# How to applying `The Twelve-Factor App` Concept

## 1. Codebase - One codebase tracked in revision control, many deploys
### Concept
* A twelve-factor app is always tracked in a version control system.
* There is always a one-to-one correlation between the codebase and the app.
* The codebase is the same across all deploys, although different versions may be active in each 
deploy.
### Solution
* Store all source code on `Gitlab`
* Follow `Microservice` pattern, each service is a separated app
* Create separating profile for each environment
* Use multi-modules project, each service corresponding to a module, common things (configs, constants, ...) will be put in common module

## 2. Dependencies - Explicitly declare and isolate dependencies
### Concept
* A twelve-factor app never relies on implicit existence of system-wide packages. 
  - It declares all dependencies, completely and exactly, via a dependency declaration manifest. 
  - It uses a dependency isolation tool during execution to ensure that no implicit dependencies 
  “leak in” from the surrounding system. The full and explicit dependency specification is applied 
  uniformly to both production and development.
* Twelve-factor apps also do not rely on the implicit existence of any system tools.
### Solution
* Using `gradle` to manage dependencies

## 3. Config - Store config in the environment
### Concept
* The twelve-factor app stores config in environment variables
* In a twelve-factor app, env vars are granular controls, each fully orthogonal to other env vars. 
They are never grouped together as “environments”, but instead are independently managed for each 
deploy.
### Solution
* Use Spring's `config-server` and store configurations on a separated `gitlab` repo. Those configs 
need to be encrypted.
* Or use k8s `configMaps`, `secrets`

## 4. Backing services - Treat backing services as attached resources
### Concept
* The code for a twelve-factor app makes no distinction between local and third party services. To 
the app, both are attached resources, accessed via a URL or other locator/credentials stored in the 
config. 
* Each distinct backing service is a resource , which indicates their loose coupling to the deploy 
they are attached to.
* Resources can be attached to and detached from deploys at will.
### Solution
* Using `Spring boot auto-configuration` for each resource

## 5. Build, release, run - Strictly separate build and run stages
### Concept
* The twelve-factor app uses strict separation between the build, release, and run stages.
### Solution
* Config `gitlab-ci` for each phrase `build`, `release` & `deploy`

## 6. Processes - Execute the app as one or more stateless processes
### Concept
* Twelve-factor processes are stateless and share-nothing. Any data that needs to persist must be 
stored in a stateful backing service, typically a database.
### Solution
* Dockerize app and deploy container to `docker swarm`

## 7. Port binding - Export services via port binding
### Concept
* The twelve-factor app is completely self-contained and does not rely on runtime injection of a 
web-server into the execution environment to create a web-facing service
### Solution
* `Jetty` will be embedded to each service and using different port

## 8. Concurrency - Scale out via the process model
### Concept
* In the twelve-factor app, processes are a first class citizen. Processes in the twelve-factor app 
take strong cues from the unix process model for running service daemons.
* **Note:** This does not exclude individual processes from handling their own internal multiplexing,
via threads inside the runtime VM, or the async/evented model found in tools such as EventMachine, 
Twisted, or Node.js. 
* Twelve-factor app processes should never daemonize or write PID files. Instead, rely on the 
operating system’s process manager to manage output streams, respond to crashed processes, and 
handle user-initiated restarts and shutdowns.
## Solution


## 9. Disposability - Maximize robustness with fast startup and graceful shutdown
## Concept
* The twelve-factor app’s processes can be started or stopped at a moment’s notice.
  - Processes should strive to minimize startup time.
  - Processes shut down gracefully.
  - Processes should also be robust against sudden death

## 10. Dev/prod parity - Keep development, staging, and production as similar as possible
### Concept
* The twelve-factor app is designed for continuous deployment by keeping the gap between development and production small
* The twelve-factor developer resists the urge to use different backing services between development and production

### Solution
* CI/CD
* all env. should be the same

## 11. Logs - Treat logs as event streams
### Concept
* Each running process writes its event stream, unbuffered, to `stdout`
### Solution
* `ELK` stack

## 12. Admin processes - Run admin/management tasks as one-off processes
### Concept
* Running database migrations
* Running a console
* Running one-time scripts committed into the app’s repo

### Solution
* `Flyway`
* `bash` inside docker containers
* 