# Flash Sales

## Project Introduction
This project is a high-concurrency flash sales system designed to simulate real-world product flash sales events. 
Flash sales, as a highly competitive sales model, involve a large number of user requests competing for limited stock resources. 
In such cases, issues like excessive database load and data inconsistency can arise.

### Key Features
- `High Concurrent Read`: Supports a large number of users simultaneously accessing the system to obtain real-time product information.
- `High Concurrent Write`: Handles a sudden influx of flash sale requests to ensure the equitable allocation of resources.
- `Stock Competition`: Simulates product flash sale events where users compete for limited stock resources.

### Challenges
1. `Database Load`: A large number of instant requests can easily lead to an overloaded database, affecting system stability and performance.
2. `Data Consistency`: Concurrent requests can lead to data inconsistencies, such as overselling of stock.
3. `Anti-Fraud Strategies`: Preventing malicious users from affecting normal flash sale activities through activities like order brushing.

### Solutions
1. `Cache Optimization`: Use Redis to cache hot product information, reducing the database load.
2. `Peak Shifting`: Use message queues to smooth and stabilize the handling of instant requests, preventing database crashes.
3. `Distributed Locks`: Introduce distributed lock mechanisms to ensure the atomicity of flash sale operations and avoid race conditions.
4. `Rate Limiting Measures`: Implement rate limiting strategies to restrict the request frequency of each user and prevent malicious order brushing.

<br>

## Project Evolution

### 1. Overselling Issue

#### 1.1 feat: basic sell
Test API: `/sale/processSaleNoLock`, Jmeter script: `fs-basic-sell`

Implemented basic order placement functionality but faced the following issues:
1. Low QPS (~1100): No optimizations were made, and each request queried the database for stock and then updated it, resulting in significant database load.
2. Concurrency Issue: In high-concurrency scenarios, multiple requests read the same stock quantity simultaneously, leading to overselling.

#### 1.2 feat: sql optimistic lock
Test API: `/sale/processSaleOptimisticLock`, Jmeter script: `fs-lock-sell`

Building on the basic sell functionality, introduced SQL optimistic lock to address overselling during high concurrency. 
It checks if `available_stock` is greater than 0 while updating and locking stock in SQL. 
Multiple requests attempting to execute this SQL update statement simultaneously will result in only one successful update, preventing overselling.

However, issues persisted:
1. Overhead: Optimistic locking typically involves additional comparisons and validations when updating data, potentially impacting database performance.
2. Database Overload: Each flash sale request still queried and updated the database, leading to excessive database load.

This method aims to ensure data consistency by utilizing optimistic lock mechanisms, but does not address excessive database load.

#### 1.3 feat: Redis Lua Script
Test API: `/sale/processSaleCache`, Jmeter script: `fs-cache-sell`

First, load commodity stock information into Redis. Then, when a flash sale request comes in, check the stock in Redis and decrement it in a single atomic operation.

To resolve the issues in the database optimistic lock approach, Redis with its high-speed in-memory read/write capabilities and Lua scripting was introduced for atomic stock updates in the cache.
1. Cache Stock Information: Most data read requests were intercepted by Redis, reducing load on MySQL.
2. Atomic Operations: The Lua script combined checking and decrementing Redis stock into a single atomic operation.
3. Additional Database Check: Even if Redis allowed passage, stock was still checked before creating an order.

```
Check and deduct stock by with Lua -> fail -> attempt ends
                                         \
                                          -> success -> lock mysql stock, create order -> pay -> deduct mysql stock
```

#### 1.4 feat: process order by mq
Test API: `/sale/processSaleCacheMq`, Jmeter script: `fs-cache-mq-sell`

In this update, after creating an order, synchronous messages are sent via MQ to handle order creation, while asynchronous messages are sent to deduct or revert the final inventory.

During moments of sudden traffic spikes, it's crucial to manage the flow of orders to prevent overwhelming the database. To achieve this, asynchronous processing using message queues (MQ) helps reduce the database's instantaneous load.

Here's how it works:

1. after creating an order, a message is immediately sent via MQ to initiate order creation:
```java
messageSender.sendMessage("new_order", JSON.toJSONString(order));
```
2. a delayed message for order payment status validation is sent:
```java
messageSender.sendDelayMessage("pay_check", JSON.toJSONString(order), delayTimeLevel);
```

I choose send immediately message as it provides a better user experience.

Regarding stock deduction, three scenarios are considered:

A. Deducting During Order Creation
- Pros: Clear and simple logic, prevents overselling.
- Cons: Handling unpaid or failed payments is challenging, and unpaid orders occupy inventory.

B. Deducting During Payment
- Pros: Avoids order creation without payment, which could lead to stock issues.
- Cons: Successful order creation may lead to inventory shortages during payment.

C. Separating: Locking During Order Creation and Deducting During Payment
1. Locking stock during order creation (createOrder)
2. Deducting stock upon successful payment (PayDoneListener)
3. Reverting locked stock in case of order closure (PayCheckListener)

#### 1.5 feat: in-memory marking
`EmptyStockMap` is utilized to mark whether the stock of activities is empty in memory. 
This helps avoid unnecessary access to the Redis database to check the stock, further enhancing performance.

#### 1.6 feat: distributed lock
Distributed Lock:
- When a thread enters, it initially acquires a lock. When another thread attempts to operate and finds that the lock is already acquired, it either gives up or tries again later. After the thread's operation is complete, it needs to call the del command to release the lock.
- To prevent deadlock caused by exceptions or system crashes during the business execution, a timeout can be added.

However, if the business process is very time-consuming, it can lead to disorder. 

Solutions include:
1. Avoid performing time-consuming operations after acquiring the lock.
2. Set a random string as the value of the lock. When releasing the lock, compare the random string to determine whether to release it. This ensures that only the thread that acquired the lock can release it.
3. Releasing the lock involves three steps: 1. Viewing the lock value, 2. Comparing the value for correctness, and 3. Releasing the lock. These three steps are not atomic. Therefore, Lua scripts can be used to ensure the atomicity of these steps.

### 2. 页面优化

#### 2.1 feat: Page Caching
Introducing Redis as a page cache solution, reducing database queries and accelerating page loading, QPS ~3700

Technologies Used:
1. Thymeleaf template engine and Spring MVC configuration are employed for page rendering and caching.
2. Redis is chosen as the caching solution, utilizing RedisTemplate for cache read and write operations.

How it Works:
1. It first checks if the HTML content for "activityAll" is cached in Redis. If found, it serves as a cache hit, and the cached content is retrieved and returned.
2. If the content is not cached, the method generates the HTML content, caches it in Redis for future requests, and returns it to the user, acting as a cache miss.

Performance Improvement:
1. Redis' fast in-memory read/write capabilities significantly accelerate page loading.
2. By setting a cache expiration time of 60 seconds, data remains fresh while minimizing database queries.

Limitations and Solutions:
1. Transmitting entire pages to the frontend can lead to large data transfers. To mitigate this, consider only transmitting essential data.
2. AJAX-based data retrieval instead of modelandview-based page rendering.

#### 2.2 feat: Static Page for Commodity Detail
Place the frontend pages in the static resource directory, and let the backend solely provide data.

### 3. Interface Optimization

#### 3.1 feat: limit user purchase
Rate limiting strategy: Limit the number of purchases for each user to prevent malicious orders.

```java
void addLimitMember(long activityId, String userId);

boolean isInLimitMember(long activityId, String userId);

void removeLimitMember(Long activityId, String userId);
```

Record this in Redis, with a key named activity_limited_user and a set as its value, containing user IDs. 
Each time a request comes in, it checks Redis to see if the user has already made a purchase. 
If they have, it indicates that they have already made a purchase. If not, it proceeds with the flash sale logic. 
After a successful purchase, the user's ID is added to Redis.

#### 3.2 feat: sentinel flow control
Rate limiting strategy: Implement rate limiting through Sentinel to prevent excessive repeated access to the API, which can lead to excessive database load.

#### 3.3 feat: hide sale api
If the flash sale API endpoint is publicly exposed, malicious attackers can use automated tools to send a large number of requests, attempting to purchase a large quantity of products. This can lead to excessive system load and potentially cause system downtime. 

By concealing the API endpoint, the likelihood of malicious attacks and fraudulent bulk purchases can be reduced.

#### 3.4 feat: verify code
A captcha can effectively prevent malicious attackers from engaging in bulk purchases using automated tools. Users are required to enter a captcha before participating in a flash sale, ensuring that only humans can perform the purchase action.

However, it's acknowledged that this approach can negatively impact user experience, so it's not being implemented in this case.

<br>

## Project Configuration

### 1. Redis Connection Pool Selection
In this project, considering factors like performance, reliability, and applicability, I chose Lettuce as the implementation for the Redis connection pool:
1. `Performance`: Lettuce generally performs better in terms of performance compared to Jedis. Lettuce is built on the Netty library and uses an asynchronous, non-blocking approach to handle connections and command operations. This allows it to more effectively utilize resources in high-concurrency scenarios, providing better performance.
2. `Thread Model`: Jedis uses a blocking synchronous approach to handle connections and operations, which can lead to thread blocking in high-concurrency situations, affecting performance. Lettuce leverages Netty's event-driven model, avoiding thread blocking and enabling each connection to handle multiple operations. This is beneficial for handling concurrent requests more efficiently.
3. `Connection Management`: Lettuce offers more flexible connection management and connection pool configuration options, allowing fine-grained control over connection creation, reuse, and disposal.
4. `Response Modes`: Lettuce supports synchronous, asynchronous, and reactive operation modes, making it suitable for various programming styles and requirements.

### 2. Spring MVC Configuration
In the Spring MVC configuration aspect, the following settings were made for this project:
1. `Adding Custom Argument Resolver`: UserArgumentResolver is added, which retrieves the user's ticket from cookies and uses this identifier to fetch the corresponding user. In the future, this might be replaced with an interceptor.
2. `Handling Static Resources`: All request paths are mapped to the classpath:/static/ directory, making it possible to directly select database image paths like /img/xxx.jpg.

### 3. Choice of Message Queue (MQ)
RocketMQ was selected for this project:
1. `Higher Data Throughput`: RocketMQ provides higher data throughput compared to RabbitMQ. Its architecture is designed around distributed message storage and multi-replica synchronous replication mechanisms, enabling fast and highly reliable message transmission.
2. `Efficient Network Communication`: RocketMQ uses Netty as its network communication framework, allowing efficient handling of a large number of network connection requests and data transfers. In contrast, RabbitMQ relies on the Erlang VM as its underlying support, which may result in slightly lower performance compared to Netty.

<br>

## Some thoughts

### 1. How to Handle Timeout Tasks?
There are two main approaches to handling timeout tasks:
1 `Scheduled Polling`：
- `Poor Timeliness`: If polling is done, for example, every minute, the maximum error in canceling orders could be up to one minute.
- `Susceptible to Backlogs`: If there is a large volume of data to process within one minute, and it's not completed within that timeframe, the backlog may accumulate.
- `Inefficient`: Polling requires iterating through resources, which is not suitable for projects with a large amount of data.

2 `Delayed Messaging`：
- `Reliable Performance`: Exceptional exits don't result in data loss.
- `Good Timeliness`: High time precision, often in seconds.
- `Low Overhead`: No need for iteration; it consumes fewer system resources.
