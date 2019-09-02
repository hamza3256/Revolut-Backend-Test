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
      "id":1,
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
      "id":2,
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
   "currency":"USD"
}
```

##### Examples:
Create a USD  Account for Vlad Yatsenko:  
`curl --request POST
   --url http://localhost:7000/customers/1/accounts
   --header 'content-type: application/json'
   --data '{
 	"currency": "USD"
 }'`

```
{  
   "account":{  
      "id":3,
      "customer":{  
         "id":1,
         "name":"Vlad",
         "surname":"Yatsenko"
      },
      "currency":"USD"
   }
}
```
 
Create a USD Account for Nikolay Yatsenko:
 
 `curl --request POST 
    --url http://localhost:7000/customers/2/accounts 
    --header 'content-type: application/json' 
    --data '{
  	"currency": "USD"
  }'`

  ```
{  
   "account":{  
      "id":4,
      "customer":{  
         "id":2,
         "name":"Nikolay",
         "surname":"Storonsky"
      },
      "currency":"USD"
   }
}
  ```
  
#### Get state of an Account:
`GET` to `/accounts/{account_id}/state`

##### Example:

Get account status for Vlad Yatsenko's account:  
`curl --request GET --url http://localhost:7000/accounts/3/state`

```
{  
   "accountState":{  
      "account":{  
         "id":3,
         "customer":{  
            "id":1,
            "name":"Vlad",
            "surname":"Yatsenko"
         },
         "currency":"USD"
      },
      "money":{  
         "amount":0,
         "currency":"USD"
      }
   }
}
```

  
### Transfer
#### Transfer money between accounts:  
`POST` to `/transfers` with body of form:
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

###### Note: in this demo, a 'Bank' is automatically added at startup as a customer with 3 Accounts (EUR, GBP, USD) with 1 million in each.
###### Therefore we can transfer $1000 each to Nikolay & Vlad:  
`curl --request POST 
   --url http://localhost:7000/transfers 
   --header 'content-type: application/json' 
   --data '{
 	"fromAccountId": 0,
 	"toAccountId": 3,
 	"money":{
 		"amount"	: "1000",
 		"currency": "USD"
 	}
 }'`  
 
`curl --request POST 
   --url http://localhost:7000/transfers 
   --header 'content-type: application/json' 
   --data '{
 	"fromAccountId": 0,
 	"toAccountId": 4,
 	"money":{
 		"amount"	: "1000",
 		"currency": "USD"
 	}
 }'`

---

Transfer $10 from Vlad Yatsenko's account to Nikolay Storonsky's account:    
`curl --request POST 
   --url http://localhost:7000/transfers 
   --header 'content-type: application/json' 
   --data '{
 	"fromAccountId": 3,
 	"toAccountId": 4,
 	"money":{
 		"amount"	: "10",
 		"currency": "USD"
 	}
 }'`

 ```
{  
   "transaction":{  
      "id":4,
      "mirrorTransactionId":5,
      "account":{  
         "id":3,
         "customer":{  
            "id":1,
            "name":"Vlad",
            "surname":"Yatsenko"
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
  	"fromAccountId": 4,
  	"toAccountId": 3,
  	"money":{
  		"amount"	: "10",
  		"currency": "USD"
  	}
  }'`

  ```
{  
   "transaction":{  
      "id":6,
      "mirrorTransactionId":7,
      "account":{  
         "id":4,
         "customer":{  
            "id":2,
            "name":"Nikolay",
            "surname":"Storonsky"
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