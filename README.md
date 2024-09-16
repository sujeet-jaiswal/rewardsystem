# Retail Reward Service
This service provide a reward programme to the customers, awarding points to the customers based on each transaction for a given time.

A customer receives 2 points for every dollar spent over $100 in each transaction, plus 1 point for every dollar spent between $50 and $100 in each transaction.
(e.g. a $120 purchase = 2*$20 + 1*$50 = 90 Points).

## Technology used:
+ Java 21
+ Springboot 3.3.3
+ Maven
+ H2 in memory DB

## Setup:
1. Insert Customer deatils using data.sql at http://localhost:8080/h2-console` as tables are already created while deployment and ruuning app.
4. Pull the repository and Run with various provided apis .



## API Documentation:
1. Get Rewards:
    + URI: "api/reward/customer/[customerId]?months=[months]"
    + ** Here request parameter 'months' is optional. Default value 3, if not specified.
    + Calculate reward points for a customer for each transaction for given amount of time and prepare a report.
    + Response Body:
   ```json
        {
        "customer": {
            "id": "123e4567-e89b-12d3-a456-426614174001",
            "name": "Jane Smith",
            "email": "jane.smith@example.com",
            "phoneNumber": "555-5678"
        },
        "totalPoints": 382,
        "rewards": [
            {
                "transactionId": "d44b2f18-6e5f-4815-9892-909ea9033ecd",
                "transactionAmount": 120.00,
                "points": 90,
                "awardedDate": "2024-08-03T08:49:41.859753"
            },
            {
                "transactionId": "60b4d9c9-7cb2-4820-8314-dda773fc2c93",
                "transactionAmount": 170.00,
                "points": 190,
                "awardedDate": "2024-08-05T15:29:36.533237"
            },
            {
                "transactionId": "5e2bf6c9-2ae0-4b2d-b620-76c7be4f3a26",
                "transactionAmount": 100.00,
                "points": 50,
                "awardedDate": "2024-08-05T15:31:12.742221"
            },
            {
                "transactionId": "295df37c-9176-4eb3-b6eb-da90a9e85701",
                "transactionAmount": 101.00,
                "points": 52,
                "awardedDate": "2024-08-07T14:09:25.8593"
            }
        ]
    }
    ```
2. Get Rewards for all customers:
    + URI: "api/reward?months=[months]"
    + ** Here request parameter 'months' is optional. Default value 3, if not specified.
    + Calculate reward points for a customer for each transaction for given amount of time and prepare a report.
    + Response Body:
    ```json
      [
    {
        "customer": {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "John Doe",
            "email": "john.doe@example.com",
            "phoneNumber": "555-1234"
        },
        "totalPoints": 330,
        "rewards": [
            {
                "transactionId": "0c3f963f-1169-4980-9034-fdca7de73ee0",
                "transactionAmount": 150.00,
                "points": 150,
                "awardedDate": "2024-08-01T16:31:35.469614"
            },
            {
                "transactionId": "a6464c93-d665-4701-831b-d86bf57c1849",
                "transactionAmount": 120.00,
                "points": 90,
                "awardedDate": "2024-08-01T16:35:22.170311"
            },
            {
                "transactionId": "c66a127d-3e13-4b67-8574-b166f1aa327c",
                "transactionAmount": 120.50,
                "points": 90,
                "awardedDate": "2024-08-01T20:20:14.970539"
            },
            {
                "transactionId": "592d1ba4-ccde-4533-99fc-f86b466fbe0c",
                "transactionAmount": 12.00,
                "points": 0,
                "awardedDate": "2024-08-02T13:00:46.099846"
            }
        ]
    },
    {
        "customer": {
            "id": "123e4567-e89b-12d3-a456-426614174001",
            "name": "Jane Smith",
            "email": "jane.smith@example.com",
            "phoneNumber": "555-5678"
        },
        "totalPoints": 280,
        "rewards": [
            {
                "transactionId": "d44b2f18-6e5f-4815-9892-909ea9033ecd",
                "transactionAmount": 120.00,
                "points": 90,
                "awardedDate": "2024-08-03T08:49:41.859753"
            },
            {
                "transactionId": "60b4d9c9-7cb2-4820-8314-dda773fc2c93",
                "transactionAmount": 170.00,
                "points": 190,
                "awardedDate": "2024-08-05T15:29:36.533237"
            }
         
        ]
    },
    {
        "customer": {
            "id": "123e4567-e89b-12d3-a456-426614174002",
            "name": "Alice Johnson",
            "email": "alice.johnson@example.com",
            "phoneNumber": "555-8765"
        },
        "totalPoints": 280,
        "rewards": [
            {
                "transactionId": "de5cb005-d7e4-4caf-a97f-639533061e73",
                "transactionAmount": 140.00,
                "points": 130,
                "awardedDate": "2024-07-12T10:00:00"
            },
            {
                "transactionId": "21bb243f-0e94-441c-9b5f-c026426644d5",
                "transactionAmount": 150.00,
                "points": 150,
                "awardedDate": "2024-08-03T10:00:00"
            }
        ]
    }
   ]
    ```
3. Bulk Purchase:
    + URI: "api/transaction/createBulkTransaction"
    + Method: POST
    + Process multiple transaction for a customer
    + Req Body:
    ```json
   {
      "customerId": "123e4567-e89b-12d3-a456-426614174000",
      "transactions":[
          {
              "amount":130,
              "transactionDate":"2024-04-12"
          },
          {
              "amount":140,
              "transactionDate":"2024-02-12"
          },
          {
              "amount":-10,
              "transactionDate":"2024-08-03"
          }
      ]
    }
    ```
   + Response Body:
   ```json
    {
    "customerId": "123e4567-e89b-12d3-a456-426614174000",
    "transactions": [
        {
            "amount": 130,
            "transactionDate": "2024-04-12"
        },
        {
            "amount": 140,
            "transactionDate": "2024-02-12"
        }
     ]
    }

    ```
4. Purchase:
   + URI: "/api/transaction/createTransaction"
   + Method: POST
   + Process single transaction.
   + Req Body:
   ```json
   {
    "customerId": "123e4567-e89b-12d3-a456-426614174001",
    "amount": 101
   }
   ```
   + Response Body:
   ```
   Transaction processed and rewards awarded
   ```
