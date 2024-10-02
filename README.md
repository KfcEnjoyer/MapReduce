# MapReduce implementation in Java using jeroMQ(ZeroMQ)

## Table of contents
* [About](#About)
* [Features](#Features)
* [Tools](#Tools)
* [Getting Started](#Getting-Started)
* [Running the Application]
* [Usage]
* [Feedback]
* [License]
* [Contacts]

## About
This project is a Java-based implementation of the MapReduce programming model, designed for distributed computing. It allows users to process large datasets across a cluster of machines efficiently. The primary function of this implementation is to count the occurrences of words in a text document, showcasing how data can be processed in parallel using multiple worker nodes.

## Features
- **Distributed Word Counting**: Efficiently counts the occurrences of each word in large text files.
- **Scalability**: The system can scale to utilize multiple worker nodes to handle larger datasets.
- **Modular Architecture**: 
  - **Master Node**: Manages the overall task distribution and coordination of worker and reducer nodes.
  - **Worker Nodes**: Process data chunks assigned to them by the master node and send the results to the reducer nodes.
  - **Reducer Nodes**: Aggregate results from multiple worker nodes and provide a final count of each word.
  - **Router**: Acts as a load balancer, directing messages between workers and reducers to ensure efficient processing and communication.
- **Real-Time Processing**: Supports real-time task distribution and result aggregation.

## Tools
- **Programming Language**: Java
- **Messaging Library**: [JeroMQ](https://github.com/zeromq/jeromq) for inter-process communication
- **Build Tool**: Maven for dependency management and building the project
- **Development Environment**: IntelliJ IDEA for coding and debugging

## Getting Started
### Prerequisites
To run this project, you need:
- **Java Development Kit (JDK) 11 or higher**: Ensure it's installed and configured in your environment.
- **Maven**: This project uses Maven for dependency management. You can download Maven from [Maven's official website](https://maven.apache.org/download.cgi) and follow the installation instructions there.
- **JeroMQ Library**: Download the required JAR file and place it in the `lib` directory.

### Installation Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/KfcEnjoyer/MapReduce
