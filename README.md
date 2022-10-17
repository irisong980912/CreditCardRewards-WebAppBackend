# Introduction

Technical assessment for Capital One Software Engineering, Full-Time 2023 position.

Participant name: [Xiaoqi Gao](https://drive.google.com/file/d/12F37lIn_6t7Qv3dn-f5hZFQw8FCC6Pyf/view?usp=sharing)

*Note: This is a backend web app.
I also wrote a console app version, which is simpler without http requests, please see the other email attachment.

## Skills Applied
Java Spring Boot, Unit / Integration Testing, RESTful API

## Steps to Run
### Step 1: Build a local sql database
1. Accessing sql commandline tool as the root user (Mac OS command line):
```bash
/usr/local/mysql/bin/mysql -u root -p
```

2. Build a database `reward`, a user `capitalOne`, and a table `transaction` using the following statements:

```bash
CREATE DATABASE `rewards`;

CREATE USER 'capitalOne'@'%' IDENTIFIED BY 'capitalOne2022';
GRANT ALL PRIVILEGES ON *.* TO capitalOne@'%';

USE `rewards`;
CREATE TABLE `transaction` (
 `id` int NOT NULL AUTO_INCREMENT,
`transaction_name` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `post_year` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `post_month` varchar(128) CHARACTER SET utf8mb4 DEFAULT NULL,
 `post_day` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `merchant_code` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `amount_cents` int DEFAULT NULL,
 PRIMARY KEY (`id`),
UNIQUE KEY `transaction_name` (`transaction_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
3. Similarly, for testing purposes, build a database `reward-test`, and a table `transaction` using the following statements:

```bash
CREATE DATABASE `rewards-test`;

USE `rewards-test`;
CREATE TABLE `transaction` (
 `id` int NOT NULL AUTO_INCREMENT,
`transaction_name` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `post_year` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `post_month` varchar(128) CHARACTER SET utf8mb4 DEFAULT NULL,
 `post_day` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `merchant_code` varchar(128) CHARACTER SET utf8 DEFAULT NULL,
 `amount_cents` int DEFAULT NULL,
 PRIMARY KEY (`id`),
UNIQUE KEY `transaction_name` (`transaction_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 2: Run Java WAR file
```bash
java -jar credit-card-rewards-0.0.1-SNAPSHOT.jar
```
### Step 3: Send requests in PostMan
1. Post a list of transactions

```bash
(POST) localhost:8080/transaction/post-list
```
with JSON request body

```bash
[{"transaction_name": "T01", "date": "2021-05-09", "merchant_code" : "sportcheck", "amount_cents": 2550},
{"transaction_name": "T02", "date": "2021-05-10", "merchant_code" : "tim_hortons", "amount_cents": 1050},
{"transaction_name": "T03", "date": "2021-05-10", "merchant_code" : "subway", "amount_cents": 1100}]
```

2. Get the monthly rewards info

```bash
(GET) localhost:8080/transaction/monthly-reward-report?year=2021&month=05
```

## About Reward Rules

After calculation, I found that Rule 3 and Rule 5 are less cost-efficient than a combination of Rule 6 and Rule 7.
Therefore, I did not consider them when calculating the maximum monthly reward point.

### Why Eliminate Rule 3?
Rule 3: 200 points for every $75 spend at Sport Check

which can be substituted by 3 * Rule 6 + 15 * Rule 7 = 3 * 75 + 15 = 240 points

### Why Eliminate Rule 5?
Rule 5: 75 points for every $25 spend at Sport Check, $10 spend at Tim Hortons

which can be substituted by 1 * Rule 6 + 15 * Rule 7 = 75 + 15 = 90 points

## Finally
Thank you for taking the time reviewing my submission!
