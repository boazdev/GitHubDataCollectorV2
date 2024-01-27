# GitHub Data Collector

The GitHub Data Collector is a Java-based application that collects information on GitHub users and their repositories. The application performs scraping and regex operations to gather relevant repository URLs and raw data, then clones the repositories to collect additional data on specific languages, such as Java, Python, Node.js, Angular, React, and .NET. It also reads the repository files to obtain data on the number of lines and matches for specified keywords.
## Running the Application - Online

Go to https://githubscanner.com/ and have fun!

## Running the Application - Local

To run the application, set up the project with an IDE and run it. Access the application via http://localhost:8080/.

## Docker Image

To run the application using Docker, build the Dockerfile and run the image.

## Technologies Used

The GitHub Data Collector uses the following technologies:
- Java
- Spring Boot
- Regex
- Web Scraping
- Docker
- PostgreSQL

## Data Collected

The GitHub Data Collector collects the following data for each user:
- User's Name
- User's URL
- Number of Public Repositories
- Number of Followers
- Number of Following
- Number of Forks
- Number of Commits
- Number of Stars
- Number of Code Lines
- Number of Keywords Apears
- Number of Tests
- Number of Java Repositories
- Number of Python Repositories
- Number of Node.js Repositories
- Number of Angular Repositories
- Number of React Repositories
- Number of .NET Repositories
