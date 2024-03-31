# WEB Terminal


![](demo/screenshot.PNG)

## Build on
>* Java 17, Framework Spring Boot 2.5.4
>* Angular 12.1.1, Framework Angular Material

## Feature
* SSH
* Telnet
* Local console

## Demo
![](demo/web_terminal_demo.gif)

## Run dev
> Run `WebTerminalApplication.java`

> Execute gradle Task `:web-terminal-frontend:runDevProxy`
### Open
> http://<remote_ip>:4200

## Run production
> Execute gradle Task `Task :web-terminal-backend:bootJar`

> Run command `java -jar web-terminal-backend-0.0.1-SNAPSHOT.jar`
### Open
> http://<remote_ip>:8080 (admin/admin)
