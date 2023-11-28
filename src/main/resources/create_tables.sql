CREATE TABLE `pomelo`.`user` (
  `username` VARCHAR(50) NOT NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`username`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE);
  
  CREATE TABLE `pomelo`.`saved_cards` (
  `card_number` VARCHAR(50) NOT NULL,
  `user` VARCHAR(50) NOT NULL,
  `expiry_month` INT NOT NULL,
  `expiry_year` INT NOT NULL,
  `name_on_card` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`card_number`),
  UNIQUE INDEX `card_number_UNIQUE` (`card_number` ASC) VISIBLE,
  INDEX `username_idx` (`user` ASC) VISIBLE);
	
CREATE TABLE `pomelo`.`transactions` (
  `txn_id` INT NOT NULL AUTO_INCREMENT,
  `event_type` VARCHAR(25) NOT NULL,
  `amount` INT NOT NULL,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `username` VARCHAR(50) NOT NULL,
  `card_number` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`txn_id`),
  UNIQUE INDEX `txn_id_UNIQUE` (`txn_id` ASC) VISIBLE,
  INDEX `username_idx` (`username` ASC) VISIBLE,
  INDEX `card_number_idx` (`card_number` ASC) VISIBLE);

CREATE TABLE `pomelo`.`card_limits` (
  `card_number` VARCHAR(50) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `available_credit` INT NOT NULL,
  `payable_balance` INT NOT NULL,
  PRIMARY KEY (`card_number`, `username`),
  UNIQUE INDEX `card_number_UNIQUE` (`card_number` ASC) VISIBLE,
  INDEX `user_idx` (`username` ASC) VISIBLE);