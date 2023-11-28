package com.example.pomelo.model;

import com.example.pomelo.config.DataSourceConfig;
import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.utils.Constants;
import com.example.pomelo.utils.UserCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EventsManager {

    @Autowired
    private UserAccount userAccount;

    public int addEvent(String username, String cardNumber, int amount, String type) throws CustomException {
        UserCard userCard = userAccount.getUserCardDetails(username, cardNumber);
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        String newTransactionQuery = "INSERT INTO TRANSACTIONS (EVENT_TYPE, AMOUNT, USERNAME, CARD_NUMBER) VALUES (?,?,?,?)";
        String updateUserCardQuery = "UPDATE CARD_LIMITS SET AVAILABLE_CREDIT=?, PAYABLE_BALANCE=? WHERE USERNAME=? AND CARD_NUMBER=?";
        String eventType = "";
        int availableCredit = userCard.getAvailableCredit();
        int payableBalance = userCard.getPayableBalance();
        int txnId;

        if ("TXN".equals(type)) {
            availableCredit = availableCredit - amount;
            eventType = Constants.NEW_TXN_EVENT;
        } else {
            payableBalance = payableBalance - amount;
            eventType = Constants.NEW_PAYMENT_EVENT;
        }
        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(newTransactionQuery);

            preparedStatement.setString(1, eventType);
            preparedStatement.setInt(2, amount);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, cardNumber);

            preparedStatement.executeUpdate();

            String getTxnId = "SELECT LAST_INSERT_ID() AS TXN_ID";
            preparedStatement = connection.prepareStatement(getTxnId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            txnId = resultSet.getInt("TXN_ID");

            preparedStatement = connection.prepareStatement(updateUserCardQuery);
            preparedStatement.setInt(1, availableCredit);
            preparedStatement.setInt(2, payableBalance);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, cardNumber);

            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            log.error("Error: ", e);
            throw new CustomException("Error adding event");
        }
        return txnId;
    }

    public void settleEvent(String username, String cardNumber, int txnId) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        UserCard userCard = userAccount.getUserCardDetails(username, cardNumber);
        int payableBalance = userCard.getPayableBalance();
        int availableCredit = userCard.getAvailableCredit();

        String getTransactionQuery = "SELECT EVENT_TYPE, AMOUNT FROM TRANSACTIONS WHERE TXN_ID=?";
        String updateTransactionQuery = "UPDATE TRANSACTIONS SET EVENT_TYPE=? WHERE TXN_ID=?";
        String updateUserCardQuery = "UPDATE CARD_LIMITS SET AVAILABLE_CREDIT=?, PAYABLE_BALANCE=? WHERE USERNAME=? AND CARD_NUMBER=?";

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(getTransactionQuery);

            preparedStatement.setInt(1, txnId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String eventType = resultSet.getString("EVENT_TYPE");
            int amount = resultSet.getInt("AMOUNT");
            String newEvent = "";

            if(eventType.startsWith("TXN")) {
                payableBalance += amount;
                newEvent = Constants.SETTLE_TXN_EVENT;
            }
            else {
                availableCredit += amount;
                newEvent = Constants.POST_PAYMENT_EVENT;
            }

            preparedStatement = connection.prepareStatement(updateUserCardQuery);
            preparedStatement.setInt(1, availableCredit);
            preparedStatement.setInt(2, payableBalance);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, cardNumber);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(updateTransactionQuery);
            preparedStatement.setString(1, newEvent);
            preparedStatement.setInt(2, txnId);

            preparedStatement.executeUpdate();
            connection.close();

        } catch (SQLException e) {
            log.error("Error", e);
            throw new CustomException("Error settling event");
        }
    }

    public void modifyEvent(String username, int newAmount, int txnId, String action) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;

        String getTransactionQuery = "SELECT EVENT_TYPE, AMOUNT, CARD_NUMBER FROM TRANSACTIONS WHERE TXN_ID=?";
        String updateUserCardQuery = "UPDATE CARD_LIMITS SET AVAILABLE_CREDIT=?, PAYABLE_BALANCE=? WHERE USERNAME=? AND CARD_NUMBER=?";
        String updateTransactionQuery = "UPDATE TRANSACTIONS SET EVENT_TYPE=?, AMOUNT=? WHERE TXN_ID=?";

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(getTransactionQuery);

            preparedStatement.setInt(1, txnId);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String eventType = resultSet.getString("EVENT_TYPE");
            int prevAmount = resultSet.getInt("AMOUNT");
            String cardNumber = resultSet.getString("CARD_NUMBER");

            String newEvent = "";
            UserCard userCard = userAccount.getUserCardDetails(username, cardNumber);
            int availableCredit = userCard.getAvailableCredit();
            int payableBalance = userCard.getPayableBalance();

            if (Constants.SETTLE_TXN_EVENT.equals(eventType) || Constants.CANCEL_TXN_EVENT.equals(eventType)) {
                throw new CustomException("Cannot modify settled/canceled event");
            }
            if (Constants.POST_PAYMENT_EVENT.equals(eventType) || Constants.CANCEL_PAYMENT_EVENT.equals(eventType)) {
                throw new CustomException("Cannot cancel settled/canceled payment");
            }

            if(Constants.MODIFY_TXN_ACTION.equals(action)) {
                availableCredit = availableCredit + prevAmount - newAmount;
                newEvent = eventType;
            }
            else if(Constants.CANCEL_TXN_ACTION.equals(action)) {
                availableCredit = availableCredit + prevAmount;
                newAmount = prevAmount;
                newEvent = Constants.CANCEL_TXN_EVENT;
            }
            else {
                payableBalance += prevAmount;
                newAmount = prevAmount;
                newEvent = Constants.CANCEL_PAYMENT_EVENT;
            }

            preparedStatement = connection.prepareStatement(updateUserCardQuery);
            preparedStatement.setInt(1, availableCredit);
            preparedStatement.setInt(2, payableBalance);
            preparedStatement.setString(3, username);
            preparedStatement.setString(4, cardNumber);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(updateTransactionQuery);
            preparedStatement.setString(1, newEvent);
            preparedStatement.setInt(2, newAmount);
            preparedStatement.setInt(3, txnId);

            preparedStatement.executeUpdate();
            connection.close();


        } catch (SQLException e) {
            log.error("Error", e);
            throw new CustomException("Error modifying event");
        }
    }

    public Map<String, Object> getEventDetails(String username, String cardNumber) throws CustomException {
        String getTransactionsQuery = "SELECT TXN_ID, TIMESTAMP, AMOUNT, EVENT_TYPE FROM TRANSACTIONS " +
                "WHERE USERNAME=? AND CARD_NUMBER=? ORDER BY TIMESTAMP DESC";
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        Map<String, Object> result = new HashMap<>();

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(getTransactionsQuery);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, cardNumber);

            ResultSet resultSet = preparedStatement.executeQuery();

            UserCard userCard = userAccount.getUserCardDetails(username, cardNumber);

            result.put("Available Credit", userCard.getAvailableCredit());
            result.put("Payable Balance", userCard.getPayableBalance());

            List<Map<String, String>> eventList = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, String> transMap = new HashMap<>();
                String txnId = resultSet.getString("TXN_ID");
                String timestamp = String.valueOf(resultSet.getTimestamp("TIMESTAMP"));
                String amount = String.valueOf(resultSet.getInt("AMOUNT"));
                String eventType = resultSet.getString("EVENT_TYPE");
                String type, status = null, possibleActions = "";
                if (eventType.startsWith("TXN")) {
                    type = "Transaction";
                } else {
                    type = "Payment";
                }
                switch (eventType) {
                    case Constants.NEW_TXN_EVENT:
                        status = "Pending";
                        possibleActions = "Modify,Cancel";
                        break;
                    case Constants.CANCEL_TXN_EVENT, Constants.CANCEL_PAYMENT_EVENT:
                        status = "Canceled";
                        break;
                    case Constants.SETTLE_TXN_EVENT:
                        status = "Settled";
                        break;
                    case Constants.NEW_PAYMENT_EVENT:
                        status = "Pending";
                        possibleActions = "Cancel";
                        break;
                    case Constants.POST_PAYMENT_EVENT:
                        status = "Posted";
                        break;
                    default:
                }
                transMap.put("TXN ID", txnId);
                transMap.put("Timestamp", timestamp);
                transMap.put("Amount", amount);
                transMap.put("Transaction/Payment", type);
                transMap.put("Status", status);
                transMap.put("Possible_Actions", possibleActions);
                eventList.add(transMap);
            }
            result.put("Events", eventList);
            connection.close();

        } catch (SQLException e) {
            log.error("Error", e);
            throw new CustomException("Error getting event details");
        }
        return result;
    }
}
