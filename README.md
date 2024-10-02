# MapReduce implementation in Java using jeroMQ(ZeroMQ)

## Table of contents
* [About](#about)
* [Features](#features)
* [Tools](#tools)
* [Getting Started](#getting-started)
* [Running the Application](#running-the-application)
* [Usage](#usage)
* [Feedback](#feedback)
* [License](#license)
* [Contacts](#contacts)

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
2. Navigate to the project directory:
   ```bash
   cd MapReduce
### Maven Configuration: The project uses a pom.xml file located in the root directory for managing dependencies and build configurations. Ensure that the pom.xml includes all necessary dependencies, including JeroMQ.

## Running the Application
### There are 3 ways to start the app;ication:
1. Start the each file manually
 * 1 masterNode.java
 * 3 workerNode.java
 * 2 reducerNode.java
 * 1 Router.java
- This is the most complicated way
2. Start the Main.java
    1. Start the masterNode1 it will start all the mappers and reducers as well as the router in separate command terminals. 
       * You can adjust the ammount of workers and reducers.
    2. Start the masterNode2, it will start all the mappers and reducers as well as the router in separate terminals in the background.
       * You can adjust the ammount of workers and reducers.
       * It will kill all the processes in the end.

## Usage
1. **Prepare Input Data**: Create a text file with content you want to analyze.
2. **Start the Application**: Run the application as described above.
3. **Follow Prompts**: Enter the number of workers and reducers, then input the path to your text file.
4. **View Results**: The program will output the count of words processed, including the option to search for specific words.

## Feedback
We welcome your feedback! If you encounter any issues or have suggestions for improvements, please feel free to reach out or submit an issue on the project repository.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

## Contacts
- **Author**: Alikhan Azanbayev
- **Email**: alihan1azanbaev@gmail.com
- **GitHub**: @KfcEnjoyer
