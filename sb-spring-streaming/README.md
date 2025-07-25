# Spring File Streaming

## Description

**file(video) streaming app by SpringBoot only**

## Architecture
```mermaid
flowchart LR
    A("User") -.->|Request Get List| B
    B("Controller<br>(dto)") --> C("Service<br>(FileList)")
    C --> D("DTO")
    B -.->|FileList| A
    A("User") -.->|Request with filename| B
    B -.->|play| A 
```
## Transition
* HW
  * Local -> Cloud Database
  * Local File -> Storage -> CDN
* SW
  * local file -> cms -> cloud cms

## Getting Started

## Testing

## Run

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.3/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.3/gradle-plugin/packaging-oci-image.html)

## Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

