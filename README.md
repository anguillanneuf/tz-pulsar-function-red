# My First Pulsar Functions

In this repo, my goal is to use the [extended Pulsar Functions SDK for Java][1] to create my first Pulsar Function.

```aidl
 bin/pulsar-admin functions localrun \
    --classname org.example.red.RedFunction \
    --jar examples/tz-pulsar-function-red-1.0-SNAPSHOT.jar \
    --inputs persistent://test/test-namespace/test_src \
    --output persistent://test/test-namespace/test_result \
    --tenant test \
    --namespace test-namespace \
    --name red
    
    
 bin/pulsar-admin functions localrun \
    --jar examples/tz-pulsar-function-red-1.0-SNAPSHOT.jar \
    --inputs persistent://test/test-namespace/test_src \
    --output persistent://test/test-namespace/test_result \
    --tenant test \
    --namespace test-namespace \
    --name red-function
```


[1]: https://pulsar.apache.org/docs/2.11.x/functions-develop-api/#use-extended-sdk-for-java