FROM openjdk:8-jre
LABEL maintainer="David Jacot <david.jacot@gmail.com>"

ENTRYPOINT ["/usr/bin/java", "-cp", "/usr/lib/kfn/*", "io.dajac.kfn.invoker.FunctionInvoker"]

COPY target/libs/* /usr/lib/kfn/

ARG JAR_FILE
COPY target/${JAR_FILE} /usr/lib/kfn/kfn-invoker.jar
