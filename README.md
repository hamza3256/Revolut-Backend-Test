# Money Transfers API - Revolut

### Problem:
Design and implement a RESTful API (including data model and the backing implementation)
for money transfers between accounts.

---

### Building/Running:
Run tests: `./gradlew clean test --info`  
Build: `./gradlew build`  
Run: `java -jar build/libs/revolut-backend-1.0-SNAPSHOT.jar`
---

### Clients:
Creat a client: `POST` to `/clients` with body of form:
```
{
    "name": "The name of the client",
    "surname": "The surname of the client"
}
```


#### Example:

##### Create Vlad Yatsenko:

`curl --request POST
   --url http://localhost:7000/clients
   --header 'content-type: application/json'
   --data '{
 	"name": "Vlad",
 	"surname": "Yatsenko"
 }'`

```
{
  "client": {
    "id": 0,
    "name": "Vlad",
    "surname": "Yatsenko"
  }
}
```

##### Create Nikolay Storonsky
`curl --request POST
   --url http://localhost:7000/clients
   --header 'content-type: application/json'
   --data '{
 	"name": "Nikolay",
 	"surname": "Storonsky"
 }'`
 
 ```
 {
   "client": {
     "id": 1,
     "name": "Nikolay",
     "surname": "Storonsky"
   }
 }
 ```




### Accounts:
Create an account for a Client: `POST` to `/accounts` with body of form:
```
{
"clientId": "0 ",
"startingMoney": {
    "amount": "1000",
    "currency": "USD"
    }
}
```

#### Example:
##### Create a USD account with $1000 for Vlad Yatsenko:

`curl --request POST
   --url http://localhost:7000/accounts
   --header 'content-type: application/json'
   --data '{
 	"clientId": "0",
 	"startingMoney": {
 		"amount": "1000",
 		"currency": "USD"
 	}
 }'`

```
{
  "account": {
    "id": 0,
    "client": {
      "id": 0,
      "name": "Vlad",
      "surname": "Yatsenko"
    },
    "startingMoney": {
      "amount": 1000,
      "currency": "USD"
    },
    "currency": "USD"
  }
}
```
 
#####  Create a USD account with $5000 for Nikolay Yatsenko:
 
 `curl --request POST 
    --url http://localhost:7000/accounts 
    --header 'content-type: application/json' 
    --data '{
  	"clientId": "1",
  	"startingMoney": {
  		"amount": "5000",
  		"currency": "USD"
  	}
  }'`

  ```
  {
    "account": {
      "id": 1,
      "client": {
        "id": 1,
        "name": "Nikolay",
        "surname": "Storonsky"
      },
      "startingMoney": {
        "amount": 5000,
        "currency": "USD"
      },
      "currency": "USD"
    }
  }
  ```
  
### Transfer
To transfer money between accounts: `POST` to `/transfer` with body of form:
```
{
    "fromAccountId": 0,
    "fromAccountId": 1,
    "money":{
        "amount": "10",
        "currency": "USD"
    }
}
```
#### Example:
##### Transfer $10 from Vlad Yatsenko's account to Nikolay Storonsky's account:

`curl --request POST 
   --url http://localhost:7000/transfers 
   --header 'content-type: application/json' 
   --data '{
 	"fromAccountId": 0,
 	"toAccountId": 1,
 	"money":{
 		"amount"	: "10",
 		"currency": "USD"
 	}
 }'`

 ```
 {
   "fromAccountState": {
     "account": {
       "id": 0,
       "client": {
         "id": 0,
         "name": "Vlad",
         "surname": "Yatsenko"
       },
       "startingMoney": {
         "amount": 1000,
         "currency": "USD"
       },
       "currency": "USD"
     },
     "money": {
       "amount": 990,
       "currency": "USD"
     }
   },
   "toAccountState": {
     "account": {
       "id": 2,
       "client": {
         "id": 1,
         "name": "Nikolay",
         "surname": "Storonsky"
       },
       "startingMoney": {
         "amount": 5000,
         "currency": "USD"
       },
       "currency": "USD"
     },
     "money": {
       "amount": 5010,
       "currency": "USD"
     }
   }
 }
 ```
 
##### Transfer $10 from Nikolay Storonsky's account to Vlad Yatsenko's account:
 
 `curl --request POST 
    --url http://localhost:7000/transfers 
    --header 'content-type: application/json' 
    --data '{
  	"fromAccountId": 1,
  	"toAccountId": 0,
  	"money":{
  		"amount"	: "10",
  		"currency": "USD"
  	}
  }'`

  ```
  {
    "fromAccountState": {
      "account": {
        "id": 2,
        "client": {
          "id": 1,
          "name": "Nikolay",
          "surname": "Storonsky"
        },
        "startingMoney": {
          "amount": 5000,
          "currency": "USD"
        },
        "currency": "USD"
      },
      "money": {
        "amount": 5000,
        "currency": "USD"
      }
    },
    "toAccountState": {
      "account": {
        "id": 0,
        "client": {
          "id": 0,
          "name": "Vlad",
          "surname": "Yatsenko"
        },
        "startingMoney": {
          "amount": 1000,
          "currency": "USD"
        },
        "currency": "USD"
      },
      "money": {
        "amount": 1000,
        "currency": "USD"
      }
    }
  }
  ```