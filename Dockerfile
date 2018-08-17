FROM maven:3.5.4-jdk-8 AS build
COPY src /usr/src/kfn/src
COPY pom.xml /usr/src/kfn
RUN mvn -f /usr/src/kfn/pom.xml clean package

FROM openjdk:8
COPY --from=build /usr/src/kfn/target/kfn-invoker-0.1.0-SNAPSHOT.jar /usr/lib/kfn/kfn-invoker-0.1.0-SNAPSHOT.jar
COPY --from=build /usr/src/kfn/target/libs/* /usr/lib/kfn/

ENTRYPOINT ["/usr/bin/java", "-cp", "/usr/lib/kfn/*", "io.dajac.kfn.invoker.FunctionInvoker"]