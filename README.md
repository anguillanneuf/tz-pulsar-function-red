# My first Pulsar Function

Use the [Extended Pulsar Functions SDK for Java][1] to create my first Pulsar Function, which appends two dots at the end of each message. 

1. Write a Pulsar Function based on a provided examples in the [tutorial][2].
2. Write a unit test according to the [tutorial][3].
3. Run the unit test with `mvn clean test`.
4. Package the function with `mvn clean install`.

   😥 I ran into a lot of troubles using the packaged jar successfully in `localrun`. I eventually fixed it by renaming the package to match the package name for the official Java examples `org.apache.pulsar.functions.api.examples` [here][4]. This should no way be the expected behavior. 
5. Test the function using `localrun` after starting up a standalone cluster locally.
   ```bash
   $ bin/pulsar-daemon start standalone
   
   $ bin/pulsar-admin brokers healthcheck
   
   $ bin/pulsar-admin functions localrun \
       --output output_topic \
       --inputs input_topic \
       --classname org.apache.pulsar.functions.api.examples.RedFunction \
       --jar /home/tz/apache-pulsar-2.11.0/examples/tz-pulsar-function-red-1.0-SNAPSHOT.jar
   ```
   Alternatively, test it without a running Pulsar cluster as described [here][5].
6. Create the function in a running Pulsar cluster, check if it's running. 
   ```shell
   $ bin/pulsar-admin topics create input-topic
   $ bin/pulsar-admin topics create output-topic
   $ bin/pulsar-admin topics list public/default
   
   $ bin/pulsar-admin functions create \
       --function-config-file examples/red-function-config.yaml \
       --jar examples/tz-pulsar-function-red-1.0-SNAPSHOT.jar
   
   $ bin/pulsar-admin functions get --tenant public --namespace default --name red-function
   
   $ bin/pulsar-admin functions status --tenant public --namespace default --name red-function
   ```
   Check out the contents of [`red-function-config.yaml`](src/main/resources/red-function-config.yaml).
7. In a second terminal, start listening to the output topic.
   ```shell
   $ bin/pulsar-client consume -s test-sub -n 0 output_topic
   ```
8. In a third terminal, publish messages to the input topic.
   ```shell
   $ bin/pulsar-client produce -m "test-messages-`date`" -n 10 input_topic
   ```
9. Observe messages with dots appended get printed out the second terminal.

## Package NAR

1. Add the following build plugin to the POM file.
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
2. Run `mvn clean package -DskipTests=True` to obtain `/target/tz-pulsar-functions-1.0-SNAPSHOT.nar`.
3. Test the function in `localrun` mode in a local standalone cluster.
   ```shell
   tz@pantone:~/apache-pulsar-2.11.0$ bin/pulsar-admin functions localrun \
   --jar examples/tz-pulsar-functions-1.0-SNAPSHOT.nar \
   --classname org.apache.pulsar.functions.api.examples.RedFunction \
   --inputs persistent://public/default/red_input \
   --output persistent://public/default/red_output \
   --name red-function
   ```
   This should automatically create the input and output topics.
4. Publish a message to the input topic and listen for a transformed message that has two dots trailing the published content from the output topic.
```shell
tz@pantone:~/apache-pulsar-2.11.0$ bin/pulsar-client produce red_input -m "hello"
..
2023-05-05T22:47:25,311+0000 [main] INFO  org.apache.pulsar.client.cli.PulsarClientTool - 1 messages successfully produced

tz@pantone:~/apache-pulsar-2.11.0$ bin/pulsar-client consume red_output -s red_output_1
..
----- got message -----
key:[null], properties:[__pfn_input_msg_id__=CPoBEAEwAA==, __pfn_input_topic__=persistent://public/default/red_input], content:hello..
```

[1]: https://pulsar.apache.org/docs/2.11.x/functions-develop-api/#use-extended-sdk-for-java
[2]: https://pulsar.apache.org/docs/2.11.x/functions-develop-tutorial/
[3]: https://pulsar.apache.org/docs/2.11.x/functions-debug-unit-test/
[4]: https://github.com/apache/pulsar/tree/branch-2.11/pulsar-functions/java-examples/src/main/java/org/apache/pulsar/functions/api/examples
[5]: https://pulsar.apache.org/docs/2.11.x/functions-debug-localrun/