# My first Pulsar Function

Use the [Extended Pulsar Functions SDK for Java][1] to create my first Pulsar Function, which appends two dots at the end of each message. 

## Create and Run in `localrun` mode
1. Write a Pulsar Function based on a provided examples in the [tutorial][2].
2. Write a unit test according to the [tutorial][3].
3. Run the unit test with `mvn clean test`.
4. Package the function with `mvn clean package -DskipTest=True`.
5. Test the function using `localrun` after starting up a standalone cluster locally.
   ```bash
   $ bin/pulsar-daemon start standalone
   
   $ bin/pulsar-admin brokers healthcheck
   
   # prepare input and output topics
   $ bin/pulsar-admin topics create red_input
   $ bin/pulsar-admin topics create red_output
   $ bin/pulsar-admin topics list public/default
   
   $ bin/pulsar-admin functions localrun \
       --output red_output \
       --inputs red_input \
       --classname org.examples.RedFunction \
       --jar /home/tz/apache-pulsar-2.11.0/examples/tz-pulsar-functions-1.1-SNAPSHOT.jar \
       --name red-funciton
   
   # alternatively
   $ bin/pulsar-admin functions localrun \
       --inputs persistent://public/default/red_input \
       --output persistent://public/default/red_output \
       --jar /home/tz/apache-pulsar-2.11.0/examples/tz-pulsar-functions-1.1-SNAPSHOT.nar \
       --classname org.examples.RedFunction \
       --name red-function
   ```
   Alternatively, test it without a running Pulsar cluster as described [here][4].
6. In a second terminal, start listening to the output topic.
   ```shell 
   $ bin/pulsar-client consume red_output -s red_sub -n 0
   ```
7. In a third terminal, publish messages to the input topic. Observe messages with dots appended getting printed out in the second terminal.
   ```shell
   $ bin/pulsar-client produce red_input -m "test-messages-`date`" -n 10
   ```
8. Cleanup.
   ```shell
   # delete function
   $ bin/pulsar-admin functions delete red-function --tenant public --namespace default
   $ rm -rf packages-storage/function/public/default/red-function/
   # alternatively
   $ bin/pulsar-admin packages delete function://public/default/red-function@0
   
   # delete topics
   $ bin/pulsar-admin topics delete persistent://public/default/${FUNCTION_NAME}
   
   # stop standalone cluster
   $ bin/pulsar-daemon stop standalone
   ```

## Run in Docker

1. Start Docker. 
   ```shell
   # first time
   $ docker run -it -p 6650:6650 -p 8080:8080 --mount source=pulsardata,target=/pulsar/data --mount source=pulsarconf,target=/pulsar/conf apachepulsar/pulsar:2.11.1 bin/pulsar standalone

   # later times
   $ docker ps -a
   $ docker start ${CONTAINTER_ID}
   ```
2. Create function and check its running properly.
   ```shell
   # optional
   $ docker cp tz-pulsar-functions-1.1-SNAPSHOT.jar ${CONTAINTER_ID}:/pulsar
   
   $ bin/pulsar-admin functions create \
       --function-config-file examples/red-function-config.yaml \
       --jar examples/tz-pulsar-functions-1.1-SNAPSHOT.jar
   
   # alternatively
   $ bin/pulsar-admin functions create \
       --function-config-file examples/red-function-config.yaml \
       --jar examples/tz-pulsar-functions-1.1-SNAPSHOT.nar
   
   $ bin/pulsar-admin functions get --tenant public --namespace default --name red-function
   
   $ bin/pulsar-admin functions status --tenant public --namespace default --name red-function
   
   # if the function already exists
   $ bin/pulsar-admin functions list --tenant public --namespace default
   $ bin/pulsar-admin functions stop --tenant public --namespace default --name red-function
   $ bin/pulsar-admin functions start --tenant public --namespace default --name red-function
   
   $ bin/pulsar-admin functions trigger \
    --tenant public \
    --namespace default \
    --name red-function \
    --topic red_input \
    --trigger-value "hello"
   hello..
   ```
   Check out the contents of [`red-function-config.yaml`](src/main/resources/red-function-config.yaml).
3. In a second terminal, start listening to the output topic.
   ```shell 
   $ bin/pulsar-client consume red_output -s red_sub -n 0
   ```
4. In a third terminal, publish messages to the input topic. Observe messages with dots appended getting printed out in the second terminal.
   ```shell
   $ bin/pulsar-client produce red_input -m "test-messages-`date`" -n 10
   ```
5. Cleanup.
   ```shell
    # stop function
   $ bin/pulsar-admin functions stop red-function --tenant public --namespace default
   
   # delete function
   $ bin/pulsar-admin functions delete red-function --tenant public --namespace default
   
   # delete package
   $ bin/pulsar-admin packages delete function://public/default/red-function@0
   
   # delete topics
   $ bin/pulsar-admin topics delete persistent://public/default/${FUNCTION_NAME}
   
   # stop Docker container
   $ docker stop ${CONTAINER_ID}
   ```

## Package NAR

1. Add the following build plugin to the POM file in order to package NAR.
   ```xml
      <plugin>
        <groupId>org.apache.nifi</groupId>
        <artifactId>nifi-nar-maven-plugin</artifactId>
        <version>${nifi.nar.plugin.version}</version>
        <extensions>true</extensions>
        <configuration>
          <finalName>${project.artifactId}-${project.version}</finalName>
        </configuration>
        <executions>
          <execution>
            <id>default-nar</id>
            <phase>package</phase>
            <goals>
              <goal>nar</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
   ```
2. Run `mvn clean package -DskipTests=True` to obtain `/target/tz-pulsar-functions-${VERSION}-SNAPSHOT.nar`.

[1]: https://pulsar.apache.org/docs/2.11.x/functions-develop-api/#use-extended-sdk-for-java
[2]: https://pulsar.apache.org/docs/2.11.x/functions-develop-tutorial/
[3]: https://pulsar.apache.org/docs/2.11.x/functions-debug-unit-test/
[4]: https://pulsar.apache.org/docs/2.11.x/functions-debug-localrun/