package com.example.pomelo.model;

import com.example.pomelo.config.DataSourceConfig;
import com.example.pomelo.exceptions.CustomException;
import com.example.pomelo.utils.CardInfo;
import com.example.pomelo.config.PasswordEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@Component
@Slf4j
public class CardManager {

    public void saveCard(String username, CardInfo cardInfo) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        String query = "INSERT INTO SAVED_CARDS (CARD_NUMBER, USER, EXPIRY_MONTH, EXPIRY_YEAR, NAME_ON_CARD) VALUES (?,?,?,?,?)";

        try {
            log.info(username);
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);

            String encodedCardNumber = PasswordEncryptionService.encrypt(cardInfo.getNumber());
            preparedStatement.setString(1, encodedCardNumber);

            preparedStatement.setString(2, username);
            preparedStatement.setInt(3, cardInfo.getExpiryMonth());
            preparedStatement.setInt(4, cardInfo.getExpiryYear());
            preparedStatement.setString(5, cardInfo.getName());

            preparedStatement.executeUpdate();
            connection.close();
        } catch (Exception e) {
            log.error("Error", e);
            throw new CustomException("Error saving new card");
        }
    }

    public CardInfo fetchSavedCard(String number) throws CustomException {
        DataSource dataSource;
        Connection connection;
        PreparedStatement preparedStatement;
        CardInfo cardInfo;
        String query = "SELECT EXPIRY_MONTH, EXPIRY_YEAR, NAME_ON_CARD FROM SAVED_CARDS WHERE CARD_NUMBER=?";

        try {
            dataSource = DataSourceConfig.source();
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);

            String encodedCardNumber = PasswordEncryptionService.encrypt(number);
            preparedStatement.setString(1, encodedCardNumber);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int expMonth = resultSet.getInt("EXPIRY_MONTH");
            int expYear = resultSet.getInt("EXPIRY_YEAR");
            String name = resultSet.getString("NAME_ON_CARD");

            cardInfo = new CardInfo(number, expMonth, expYear, name);
            connection.close();

        } catch (Exception e) {
            log.error("Error", e);
            throw new CustomException("Error fetching card");
        }
        return cardInfo;
    }

    public CardInfo getCard(Map<String, Object> data) throws CustomException {
        CardInfo cardInfo;
        String cardNumber = (String) data.get("cardNumber");
        boolean useSavedCard = (boolean) data.get("useSavedCard");
        if(useSavedCard) {
            cardInfo = fetchSavedCard(cardNumber);
        }
        else {
            String expiryMonth = (String) data.get("expiryMonth");
            String expiryYear = (String) data.get("expiryYear");
            String nameOnCard = (String) data.get("nameOnCard");
            cardInfo = new CardInfo(cardNumber, Integer.parseInt(expiryMonth), Integer.parseInt(expiryYear), nameOnCard);
        }
        return cardInfo;
    }
}
