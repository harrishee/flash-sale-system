# Flash Sale System

## Project Introduction

This project is a flash sale system designed for high-concurrency environments, aimed at handling simultaneous bulk
purchases of limited-stock items. The system includes key features such as event management, product management, and
order processing. It is built on the COLA architecture, using technologies like Spring Boot, MyBatis, MySQL, Redis, and
RocketMQ.

## Launch and Testing

TODO

## Challenges and Solutions

In the high-concurrency flash sale scenario, the system faces the following challenges and their solutions:

### Performance Optimization

- **Challenge**: High volume of ineffective inventory queries with fewer successful write operations, such as inventory
  deduction and order creation.
- **Solution**: Introduce a caching system to reduce database access, and use message queues for load leveling.

### Data Consistency

- **Challenge**: Ensuring no overselling occurs while avoiding conservative designs that can cause performance issues.
- **Solution**: Use optimistic locking and versioning in the database to handle inventory deduction.

### Cache Consistency

- **Challenge**: Addressing consistency between cache and database, as well as between local cache and distributed
  cache.
- **Solution**: Maintain weak consistency between local cache and database cache, ensuring eventual consistency at the
  database layer.

### Cache Issues

- **Challenge**: Dealing with cache penetration, cache breakdown, and cache avalanche problems.
- **Solution**: Cache null objects, set expiration times, and use distributed locks to address cache issues.

## Order Placement Strategies

To accommodate different scales of flash sale events, the project offers three strategies: synchronous,
asynchronous, and bucketing strategy:

- **Synchronous Ordering**: Supports small-scale concurrent scenarios with high real-time requirements through cache
  pre-warming, pre-deduction of cache inventory, inventory deduction in the database, and order creation in the
  database.
- **Asynchronous Ordering**: Supports medium-scale concurrent scenarios with brief delays through asynchronous handling
  of cache inventory pre-deduction, inventory deduction in the database, and order creation in the database using
  message queues, requiring polling.
- **Bucketing Strategy**: TODO

This translation provides an English version of the essential information regarding the flash sale system designed for
handling high traffic sales events effectively.
