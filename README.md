## SpringBoot RabbitMQ Producer / Consumer code example


A simple project based on SpringBoot and RabbitMQ meant to showcase manual tracing and 
context propagation continuation on the consumer end.<br> 
It is based on the following project: <br>
[opentracing/java-spring-rabbitmq](https://github.com/opentracing-contrib/java-spring-rabbitmq)

Some additional changes were also introduced to fix decoration issues. 
 It is used along with the RabbitMQ Spring API (RabbitTemplate/RabbitListener)

### _Preliminary tasks and first time steps_

##### _If you have already RabbitMQ installed, you may skip this section and go straight to the DD agent section_

**Install RabbitMQ on mac OSX (tested on High Sierra)**

````
COMP10619:RabbitMQ pejman.tabassomi$ brew install rabbitmq
````

**Start RabbitMQ**

````
COMP10619:RabbitMQ pejman.tabassomi$ brew services start rabbitmq
````

Another option is to use the official docker image and run it locally.
Below an example of docker-compose file.   


##### _docker-compose.yml_
````
rabbitmq:
  image: rabbitmq:management
  ports:
    - "5672:5672"
    - "15672:15672"
````   

**Spin up the container**

````
COMP10619:RabbitMQ pejman.tabassomi$ docker-compose up -d
````

### _Administration tasks_

**Start RabbitMQ**

````
COMP10619:RabbitMQ pejman.tabassomi$ brew services start rabbitmq
````

**Stop RabbitMQ**

````
COMP10619:RabbitMQ pejman.tabassomi$ brew services stop rabbitmq
````

**RabbitMQ UI**

````
http://localhost:15672
````

### _Spin up the Datadog Agent (Provide your API key  to the  belown command_


````
docker run -d --name datadog_agent -v /var/run/docker.sock:/var/run/docker.sock:ro -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro -p 127.0.0.1:8126:8126/tcp -e DD_API_KEY=<Your API key> -e DD_APM_ENABLED=true -e DD_APM_IGNORE_RESOURCES="GET /api/random" datadog/agent:latest
````

### _Clone the repository and build the application_

The project is structured as a main gradle project with two submodules (one for the consumer, the other for the producer)

````
COMP10619:RabbitMQ pejman.tabassomi$ git clone https://github.com/ptabasso2/prodconsrbmq.git
COMP10619:RabbitMQ pejman.tabassomi$ cd prodconsrbmq
COMP10619:prodconsrbmq pejman.tabassomi$ ./gradlew build
````


### _Start the app_

Open three terminal windows, one for the producer. The second for the consumer and the third for testing.

#### Consumer

````
COMP10619:prodconsrmq pejman.tabassomi$ java -Ddd.trace.global.tags=env:datadoghq.com -jar consumer/build/libs/consumer-0.0.1-SNAPSHOT.jar --server.port=8082
````
and then 

#### Producer

````
COMP10619:prodconsrmq pejman.tabassomi$ java -Ddd.trace.http.client.split-by-domain=true  -Ddd.trace.global.tags=env:datadoghq.com  -jar producer/build/libs/producer-0.0.1-SNAPSHOT.jar --server.port=8081
````

### _Test the app_

Note: Run the following command several times

````
COMP10619:prodconsrbmq pejman.tabassomi$ curl localhost:8081/test
````



[]: https://github.com/opentracing-contrib/java-spring-rabbitmq