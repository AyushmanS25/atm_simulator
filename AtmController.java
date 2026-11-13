package com.atm.simulator.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/atm")
public class AtmController {
    private static final String DEMO_CARD = "1234567890123456";
    private static final String DEMO_PIN = "1234";
    private static final double INITIAL_BALANCE = 8000.00;
    private static final double TRANSACTION_LIMIT = 10000.00;
    private static final double DAILY_LIMIT = 20000.00;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpSession session) {
        String card = body.getOrDefault("cardNumber", "").replaceAll("\\s", "");
        String pin = body.getOrDefault("pin", "");
        if (!DEMO_CARD.equals(card)) return response(HttpStatus.UNAUTHORIZED, false, "Card not recognised. Please check the card number.");
        if (!DEMO_PIN.equals(pin)) return response(HttpStatus.UNAUTHORIZED, false, "Incorrect PIN. Please try again.");

        session.setAttribute("authenticated", true);
        session.setAttribute("balance", INITIAL_BALANCE);
        session.setAttribute("dailyWithdrawn", 0.0);
        session.setAttribute("transactions", new ArrayList<Map<String, Object>>());
        return ResponseEntity.ok(Map.of("success", true, "message", "Welcome! Your card has been verified.", "balance", INITIAL_BALANCE));
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> balance(HttpSession session) {
        if (!authenticated(session)) return unauthorized();
        return ResponseEntity.ok(Map.of("success", true, "balance", balanceOf(session), "dailyWithdrawn", dailyOf(session),
                "transactionLimit", TRANSACTION_LIMIT, "dailyLimit", DAILY_LIMIT));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!authenticated(session)) return unauthorized();
        double amount;
        try { amount = Double.parseDouble(String.valueOf(body.get("amount"))); }
        catch (Exception ex) { return response(HttpStatus.BAD_REQUEST, false, "Enter a valid amount."); }

        double balance = balanceOf(session);
        double daily = dailyOf(session);
        String error = null;
        if (!Double.isFinite(amount) || amount <= 0) error = "Amount must be greater than zero.";
        else if (amount > TRANSACTION_LIMIT) error = "Per-transaction limit is ₹10,000.";
        else if (daily + amount > DAILY_LIMIT) error = "Daily withdrawal limit of ₹20,000 exceeded.";
        else if (amount > balance) error = "Insufficient account balance.";
        if (error != null) return response(HttpStatus.BAD_REQUEST, false, error);

        session.setAttribute("balance", balance - amount);
        session.setAttribute("dailyWithdrawn", daily + amount);
        addTransaction(session, "Cash withdrawal", amount, "Successful");
        return ResponseEntity.ok(Map.of("success", true, "message", "Withdrawal approved. Please collect your cash.",
                "amount", amount, "balance", balance - amount, "dailyWithdrawn", daily + amount));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> transactions(HttpSession session) {
        if (!authenticated(session)) return unauthorized();
        @SuppressWarnings("unchecked") List<Map<String, Object>> items = (List<Map<String, Object>>) session.getAttribute("transactions");
        return ResponseEntity.ok(Map.of("success", true, "transactions", items == null ? List.of() : items));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true, "message", "You have been safely logged out."));
    }

    private boolean authenticated(HttpSession session) { return Boolean.TRUE.equals(session.getAttribute("authenticated")); }
    private double balanceOf(HttpSession session) { return (double) session.getAttribute("balance"); }
    private double dailyOf(HttpSession session) { return (double) session.getAttribute("dailyWithdrawn"); }
    @SuppressWarnings("unchecked")
    private void addTransaction(HttpSession session, String type, double amount, String status) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) session.getAttribute("transactions");
        if (list == null) list = new ArrayList<>();
        list.add(0, Map.of("type", type, "amount", amount, "status", status, "time", LocalDateTime.now().format(TIME_FORMAT)));
        session.setAttribute("transactions", list);
    }
    private ResponseEntity<Map<String, Object>> unauthorized() { return response(HttpStatus.UNAUTHORIZED, false, "Session expired. Please insert your card again."); }
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, boolean success, String message) {
        return ResponseEntity.status(status).body(Map.of("success", success, "message", message));
    }
}
