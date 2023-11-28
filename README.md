# PaymentApplication

I have built a Web Application that allows the users to perform multiple actions using REST API and persists various forms of data into a relational database. To begin with, the application supports creating new user accounts and logging into existing accounts. Once a user logs in, the username is saved in the session for future operations. All registered users are stored in the User table in the MySQL database and their passwords are encoded for data privacy.

This application supports a user to have multiple credit cards, each having a different available credit and payable balance value. A user can perform three kinds of operations broadly: Transact, Pay off his/her bills and Manage All Transactions. While doing a new transaction or paying off the bills, the user has two options: He/She can either add new card details or save the card and use it later without entering all details again. Card details include card number, expiry month and year and name on the card for demo purposes. Once a card is saved, it is persisted in the "saved_cards" table.

Now, when the user wants to transact or pay off, he/she enters new/saved card and the amount. In the backend, first the amount is authorized to be valid, then available credit and payable balance are updated following the transaction or payment lifecycle. The table credit_limits shows current values of the available credit and payable balance for a given combination of username and card number and the transaction table shows all pending or settled transactions and payments.

Once a transaction or payment is initiated, it waits for 5 minutes and automatically changes the status to settled or posted. This window has been kept to simulate processing of transactions by a real bank.

During this window, the application supports the options to modify the transaction amount and cancel the transaction or the payment. In the end, the user can see the summary of all transactions ordered from newest to oldest also highlighting the status, timestamp, transaction ID of each entry.
