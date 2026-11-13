# ATM Flow — ATM Transaction Simulator

A responsive ATM web simulator built with Java Spring Boot, HTML, CSS and JavaScript. It demonstrates card/PIN validation, balance enquiry, withdrawals, transaction limits and transaction history. The validation rules are suitable for explaining Decision Table Testing and Cause-Effect Graphing in a Software Engineering mini-project.

## Demo credentials
- Card number: `1234567890123456`
- PIN: `1234`
- Starting balance: ₹8,000
- Per-transaction limit: ₹10,000
- Daily withdrawal limit: ₹20,000

## Run locally

Requirements: Java 21+ and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open http://localhost:8080.

To create a deployable JAR:

```bash
mvn clean package
java -jar target/atm-web-simulator-1.0.0.jar
```

The application uses the `PORT` environment variable when supplied by a hosting provider, otherwise it listens on port 8080.

## Deploy

The project can be deployed to a Java-capable host such as Render using:
- Build command: `mvn clean package`
- Start command: `java -jar target/atm-web-simulator-1.0.0.jar`
- Runtime: Java 21

Alternatively, use the included Dockerfile on any Docker-compatible hosting platform.

## Validation rules
1. Card number must match the demo card.
2. PIN must match the demo PIN.
3. Withdrawal amount must be greater than zero.
4. A single withdrawal cannot exceed ₹10,000.
5. Total withdrawals in one session cannot exceed ₹20,000.
6. Withdrawal cannot exceed the available balance.
7. Balance and daily withdrawn amount change only when all checks pass.

## Important limitations
This is an educational prototype, not a banking product. It does not connect to a bank, ATM network, payment gateway or physical card reader. Demo account values are stored in the HTTP session and reset when the session ends. Credentials are hardcoded for demonstration. Do not use real card or PIN details. A production banking system would require secure identity verification, encrypted secrets, persistent transactional storage, audit logging, rate limiting, monitoring and security review.
