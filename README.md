Developed a load balancer in Scala using Akka HTTP and Akka Actors. The load balancer efficiently distributes incoming requests across multiple backend servers to ensure high availability and reliability. Key features include:

 • # Round-Robin Load Balancing: Implements round-robin algorithm to distribute requests evenly across available servers.
 • # Health Checks: Periodically checks the health of backend servers and adjusts the list of active servers accordingly.
 • # Resilience: Automatically removes failed servers from the active list and redistributes requests to available servers.

### Key Technologies Used:
• # Scala: Programming language used for implementing the load balancer.
• # Akka HTTP: Library used for handling HTTP requests and responses.
• # Akka Actors: Framework used for building concurrent, distributed, and fault-tolerant applications.

This project demonstrates the use of Akka HTTP and Akka Actors to build a robust load balancer capable of handling high traffic and ensuring service availability.
