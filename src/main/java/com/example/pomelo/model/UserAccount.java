package com.example.pomelo.model;

import com.example.pomelo.config.DataSourceConfig;
import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.config.PasswordEncryptionService;
import com.example.pomelo.utils.User;
import com.example.pomelo.utils.UserCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class UserAccount {

    public void addNewUser(User user) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        String query = "INSERT INTO USER (USERNAME, FULL_NAME, PASSWORD) VALUES (?,?,?)";

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);

            log.info("Connected to DB");

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getFullName());

            String encodedPassword = PasswordEncryptionService.encrypt(user.getPassword());
            preparedStatement.setString(3, encodedPassword);

            preparedStatement.executeUpdate();
            connection.close();
        } catch (Exception e) {
            log.error("Error", e);
            throw new CustomException("Error creating new user. " + e.getMessage());
        }
    }

    public boolean userLogin(String username, String password) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        String query = "SELECT PASSWORD FROM USER WHERE USERNAME=?";

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);

            log.info("Connected to DB");

            preparedStatement.setString(1, username);

            String encryptedPassword = PasswordEncryptionService.encrypt(password);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String correctPass = resultSet.getString("PASSWORD");
            if(correctPass.equals(encryptedPassword)) {
                return true;
            }
            log.info("Passwords mismatch");
            connection.close();
            throw new CustomException("Bad credentials");
        } catch (Exception e) {
            log.error("Error", e);
            throw new CustomException("Error logging in");
        }
    }

    public UserCard getUserCardDetails(String username, String cardNumber) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        UserCard userCard;
        String query = "SELECT AVAILABLE_CREDIT, PAYABLE_BALANCE FROM CARD_LIMITS WHERE USERNAME=? AND CARD_NUMBER=?";
        String addCardQuery = "INSERT INTO CARD_LIMITS (USERNAME, CARD_NUMBER, AVAILABLE_CREDIT, PAYABLE_BALANCE) VALUES (?,?,?,?)";

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, cardNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()) {
                preparedStatement = connection.prepareStatement(addCardQuery);

                preparedStatement.setString(1, username);
                preparedStatement.setString(2, cardNumber);
                preparedStatement.setInt(3, 1000);
                preparedStatement.setInt(4, 0);
                preparedStatement.executeUpdate();
                return new UserCard(username, cardNumber, 1000, 0);
            }
            int availableCredit = resultSet.getInt("AVAILABLE_CREDIT");
            int payableBalance = resultSet.getInt("PAYABLE_BALANCE");
            userCard = new UserCard(username, cardNumber, availableCredit, payableBalance);
            connection.close();
        } catch (SQLException e) {
            log.error("Error", e);
            throw new CustomException("Error getting user details");
        }
        return userCard;
    }
}
