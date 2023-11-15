FROM openjdk:17
ENV CORAX_VERSION 2.3

ADD . /corax-community
WORKDIR /corax-community

RUN curl -SLO "https://github.com/Feysh-Group/corax-community/releases/download/v$CORAX_VERSION/corax-java-cli-community-$CORAX_VERSION.zip" && \ 
    jar xf corax-java-cli-community-$CORAX_VERSION.zip
# [兼容异常处理] 第一次 build 必失败，|| true 使其永真
RUN ./gradlew build || true
RUN sed -i "s#^coraxEnginePath=.*#coraxEnginePath=/corax-community/corax-java-cli-community-${CORAX_VERSION}/corax-cli-community-${CORAX_VERSION}.jar#g" gradle-local.properties && \
    ./gradlew build && \
    ln -s ./corax-java-cli-community-${CORAX_VERSION}/corax-cli-community-${CORAX_VERSION}.jar corax-cli.jar

CMD [ "java", "-jar", "corax-cli.jar"]