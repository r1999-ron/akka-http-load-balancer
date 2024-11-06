# Scala Load Balancer using Akka HTTP and Akka Actors

Developed a load balancer in Scala using Akka HTTP and Akka Actors. This load balancer efficiently distributes incoming requests across multiple backend servers to ensure high availability and reliability.

## Key Features

- **Round-Robin Load Balancing**: Distributes requests evenly across available servers using the round-robin algorithm.
- **Health Checks**: Periodically verifies the health of backend servers and adjusts the list of active servers accordingly.
- **Resilience**: Automatically removes failed servers from the active list and redistributes requests to ensure continuous availability.

## Key Technologies Used

- **Scala**: The programming language used to implement the load balancer.
- **Akka HTTP**: Handles HTTP requests and responses for managing incoming traffic.
- **Akka Actors**: Provides the framework for building a concurrent, distributed, and fault-tolerant application.

## Project Overview

This project showcases how Akka HTTP and Akka Actors can be leveraged to build a robust load balancer that handles high traffic volumes and maintains consistent service availability.
