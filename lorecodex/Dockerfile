FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Gradle configuration files first for better caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src
COPY .envEmpty .env

# Build the application (with more detailed output)
RUN ./gradlew build -x test --info

# Verify the JAR exists before copying
RUN find /app -name "*.jar" | grep -q . || (echo "No JAR found after build!" && exit 1)

# Copy JAR file from the correct location
RUN mkdir -p build/libs
RUN find /app -name "*.jar" -exec cp {} build/libs/app.jar \;

# Debug: List contents of build directory
RUN ls -la build/libs/

# Set debugging options
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Expose ports
EXPOSE 8080 5005

# Run the application
ENTRYPOINT ["java", "-jar", "build/libs/app.jar"]