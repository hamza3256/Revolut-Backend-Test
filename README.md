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

### Customers:
#### Creat a customer:  
`POST` to `/customers` with body of form:
```
{  
   "name":"The name of the customer",
   "surname":"The surname of the customer"
}
```


##### Examples:

Create Vlad Yatsenko:  
`curl --request POST
   --url http://localhost:7000/customers
   --header 'content-type: application/json'
   --data '{
 	"name": "Vlad",
 	"surname": "Yatsenko"
 }'`

```
{  
   "customer":{  
      "id":0,
      "name":"Vlad",
      "surname":"Yatsenko"
   }
}
```

Create Nikolay Storonsky:  
`curl --request POST
   --url http://localhost:7000/customers
   --header 'content-type: application/json'
   --data '{
 	"name": "Nikolay",
 	"surname": "Storonsky"
 }'`
 
 ```
{  
   "customer":{  
      "id":1,
      "name":"Nikolay",
      "surname":"Storonsky"
   }
}
 ```




### Accounts:
#### Create an account for a Customer:  
`POST` to `/customers/{customer_id}/accounts` with body of form:
```
{  
   "startingMoney":{  
      "amount":"1000",
      "currency":"USD"
   }
}
```

##### Examples:
Create an Account with $1000 for Vlad Yatsenko:  
`curl --request POST
   --url http://localhost:7000/customers/0/accounts
   --header 'content-type: application/json'
   --data '{
 	"startingMoney": {
 		"amount": "1000",
 		"currency": "USD"
 	}
 }'`

```
{  
   "account":{  
      "id":0,
      "customer":{  
         "id":0,
         "name":"Vlad",
         "surname":"Yatsenko"
      },
      "startingMoney":{  
         "amount":1000,
         "currency":"USD"
      },
      "currency":"USD"
   }
}
```
 
Create an Account with $5000 for Nikolay Yatsenko:
 
 `curl --request POST 
    --url http://localhost:7000/customers/1/accounts 
    --header 'content-type: application/json' 
    --data '{
  	"startingMoney": {
  		"amount": "5000",
  		"currency": "USD"
  	}
  }'`

  ```
{  
   "account":{  
      "id":1,
      "customer":{  
         "id":1,
         "name":"Nikolay",
         "surname":"Storonsky"
      },
      "startingMoney":{  
         "amount":5000,
         "currency":"USD"
      },
      "currency":"USD"
   }
}
  ```
  
#### Get state of an Account:
`GET` to `/accounts/{account_id}/state`

##### Example:

Get account status for Vlad Yatsenko's account:  
`curl --request GET --url http://localhost:7000/accounts/0/state`

```
{  
   "accountState":{  
      "account":{  
         "id":0,
         "customer":{  
            "id":0,
            "name":"Vlad",
            "surname":"Yatsenko"
         },
         "startingMoney":{  
            "amount":1000,
            "currency":"USD"
         },
         "currency":"USD"
      },
      "money":{  
         "amount":1000,
         "currency":"USD"
      }
   }
}
```

  
### Transfer
#### Transfer money between accounts:  
`POST` to `/transfer` with body of form:
```
{  
   "fromAccountId":0,
   "toAccountId":1,
   "money":{  
      "amount":"10",
      "currency":"USD"
   }
}
```
##### Examples:
Transfer $10 from Vlad Yatsenko's account to Nikolay Storonsky's account:    
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
   "transaction":{  
      "id":0,
      "mirrorTransactionId":1,
      "account":{  
         "id":0,
         "customer":{  
            "id":0,
            "name":"Vlad",
            "surname":"Yatsenko"
         },
         "startingMoney":{  
            "amount":1000,
            "currency":"USD"
         },
         "currency":"USD"
      },
      "money":{  
         "amount":-10,
         "currency":"USD"
      }
   }
}
 ```
 
Transfer $10 from Nikolay Storonsky's account to Vlad Yatsenko's account:   
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
   "transaction":{  
      "id":2,
      "mirrorTransactionId":3,
      "account":{  
         "id":1,
         "customer":{  
            "id":1,
            "name":"Nikolay",
            "surname":"Storonsky"
         },
         "startingMoney":{  
            "amount":5000,
            "currency":"USD"
         },
         "currency":"USD"
      },
      "money":{  
         "amount":-10,
         "currency":"USD"
      }
   }
}
  ```