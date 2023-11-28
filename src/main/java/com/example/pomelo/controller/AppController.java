package com.example.pomelo.controller;

import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.model.*;
import com.example.pomelo.utils.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import com.example.pomelo.utils.CardInfo;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class AppController {

    @Autowired
    private UserAccount userAccount;
    @Autowired
    private CardManager cardManager;
    @Autowired
    private Authorizer authorizer;
    @Autowired
    private TransactionsManager transactionsManager;
    @Autowired
    private PaymentsManager paymentsManager;
    @Autowired
    private EventsManager eventsManager;

    @PostMapping("/signup")
    public HttpEntity<String> createAccount(@RequestBody User user) {
        try {
            userAccount.addNewUser(user);
        }
        catch(CustomException e) {
            log.error(e.getMessage());
            return new HttpEntity<>(e.getMessage());
        }
        return new HttpEntity<>("Success");
    }

    @PostMapping("/login")
    public HttpEntity<String> login(@RequestBody String body, HttpSession session) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> data = objectMapper.readValue(body, HashMap.class);
            String username = data.get("username");
            String password = data.get("password");
            if(userAccount.userLogin(username, password)) {
                session.setAttribute("username", username);
                return new HttpEntity<>("Logged in");
            }
        }
        catch(CustomException | JsonProcessingException e) {
            return new HttpEntity<>(e.getMessage());
        }
        return new HttpEntity<>("Unexpected error");
    }

    @PostMapping("/saveCard")
    public HttpEntity<?> saveNewCard(@RequestBody CardInfo cardInfo, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            cardManager.saveCard(username, cardInfo);
        }
        catch(CustomException e) {
            return new HttpEntity<>(e.getMessage());
        }
        return new HttpEntity<>("Success");
    }

    @PostMapping("/transact")
    public HttpEntity<String> addTransaction(@RequestBody String body, HttpSession session) throws JsonProcessingException {
        String username = (String) session.getAttribute("username");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.readValue(body, HashMap.class);
        CardInfo cardInfo;
        try {
            cardInfo = cardManager.getCard(data);
            int amount = Integer.parseInt((String) data.get("amount"));
            if (authorizer.authorizeTransactionAmount(username, cardInfo.getNumber(), amount)) {
                transactionsManager.addNewTransaction(username, cardInfo.getNumber(), amount);
                return new HttpEntity<>("Success");
            }
        }
        catch (CustomException e) {
            return new HttpEntity<>("Failed to fetch card");
        }
        return new HttpEntity<>("Unexpected error");
    }

    @PostMapping("/payoff")
    public HttpEntity<String> payOff(@RequestBody String body, HttpSession session) throws JsonProcessingException {
        String username = (String) session.getAttribute("username");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.readValue(body, HashMap.class);
        CardInfo cardInfo;
        try {
            cardInfo = cardManager.getCard(data);
            int amount = Integer.parseInt((String) data.get("amount"));
            if(authorizer.authorizePaymentAmount(username, cardInfo.getNumber(), amount)) {
                paymentsManager.payOff(username, cardInfo.getNumber(), amount);
                return new HttpEntity<>("Success");
            }
        }
        catch (CustomException e) {
            return new HttpEntity<>("Failed to fetch card");
        }
        return new HttpEntity<>("Unexpected error");
    }

    @PostMapping("/manage")
    public Map<String, Object> manage(@RequestBody CardInfo cardInfo, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            return eventsManager.getEventDetails(username, cardInfo.getNumber());
        } catch (CustomException e) {
            return new HashMap<>();
        }
    }

    @GetMapping("/modifyTransaction")
    public HttpEntity<String> modifyTransaction(@RequestParam String txnId, @RequestParam int newAmount, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            transactionsManager.modifyTransaction(username, Integer.parseInt(txnId), newAmount);
            return new HttpEntity<>("Success");
        } catch (CustomException e) {
            return new HttpEntity<>("Couldn't update the transaction");
        }
    }

    @GetMapping("/cancelTransaction")
    public HttpEntity<String> cancelTransaction(@RequestParam String txnId, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            transactionsManager.cancelTransaction(username, Integer.parseInt(txnId));
            return new HttpEntity<>("Success");
        } catch (CustomException e) {
            return new HttpEntity<>("Couldn't update the transaction");
        }
    }

    @GetMapping("/cancelPayment")
    public HttpEntity<String> cancelPayment(@RequestParam String txnId, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            paymentsManager.cancelPayment(username, Integer.parseInt(txnId));
            return new HttpEntity<>("Success");
        } catch (CustomException e) {
            return new HttpEntity<>("Couldn't update the transaction");
        }
    }
}
