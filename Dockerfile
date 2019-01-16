FROM java:8

# preserve Java 8  from the maven install.
RUN mv /etc/alternatives/java /etc/alternatives/java8
RUN apt-get update -y && apt-get install maven -y

# Restore Java 8
RUN mv -f /etc/alternatives/java8 /etc/alternatives/java
RUN ls -l /usr/bin/java && java -version

COPY . /var/www/java
WORKDIR /var/www/java
RUN javac ConverterApplication.java
CMD ["java", "ConverterApplication"]