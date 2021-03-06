# Money Transfers API - Revolut

### Problem:
Design and implement a RESTful API (including data model and the backing implementation)
for money transfers between accounts.

---

### Notes on my implementation:
1. 100% Kotlin 1.3.50 (targeting Java 1.8)
2. Javalin as a web framework (since Revolut is on their list of companies using it)
3. Jackson for JSON
4. JUnit4 for unit testing
5. Unirest for testing REST API calls

Since depositing money wasn't part of the requirements, the app automatically adds a 'Bank' Customer & three Accounts (USD, GBP, EUR) with 1 million in each Account.
You can use these accounts to transfer money to other, new Accounts. [Click here to see the IDs](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/b662efd2a4dfd0d847e2d497f7dd35be2ea51069/src/main/kotlin/RevolutApp.kt#L63)

Each [Customer](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/master/src/main/kotlin/customers/Customer.kt) in the bank has a set of [Accounts](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/master/src/main/kotlin/customers/accounts/Account.kt).
The Account does not store how  much money they have, all it stores are the Account features, such as currency (we could add fields for name, created time, branch name, etc).
Instead of storing the money in the Account, [we can get](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/29fd5c810f5d236b569eee181295aa6b131926d7/src/main/kotlin/customers/accounts/AccountStateQuerier.kt#L45)
the [AccountState](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/29fd5c810f5d236b569eee181295aa6b131926d7/src/main/kotlin/customers/accounts/AccountStateQuerier.kt#L13)
of an Account at any time, which exposes how much money the Account has.
This way, we can rollback [transactions](https://github.com/maciej-kaznowski/Revolut-Backend-Test/tree/master/src/main/kotlin/customers/accounts/transactions)
and the Account doesn't have to be updated.

Creators, such as [CustomerCreator](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/master/src/main/kotlin/customers/CustomerCreator.kt) are responsible for creating and storing entities into a repository for a Request. Most importantly, they provide the ID for new entities.  
Repositories, such as [TransactionRepository](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/master/src/main/kotlin/customers/accounts/transactions/TransactionRepository.kt) expose CRUD operations for entities.  
Handlers, such as [CreateAccountHandler](https://github.com/maciej-kaznowski/Revolut-Backend-Test/blob/master/src/main/kotlin/customers/accounts/CreateAccountHandler.kt)  are responsible for handling REST API calls.

DI wasn't used as I mainly use [Dagger2](https://github.com/google/dagger) which is more Android-specific

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