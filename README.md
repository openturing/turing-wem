# Listener WEM for Viglet Turing 
Open Text Web Experience Management (WEM) Listener to publish content to Viglet Turing


## Deploy 
### 1. WEM Jar files
Copy the following jar files to /lib directory:

* thirdparty-combined.jar
* vgn-appsvcs-cma.jar
* vgn-shared-logging.jar

### 2. Compile

Use Gradle to compile the listener jar file, without generate jar file.

```shell
$ ./gradlew build
```
