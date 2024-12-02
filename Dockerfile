FROM ubuntu:22.04

ENV JDK_MAJOR_VERSION=17
ENV CORAX_VERSION 2.20
ENV CORAX_JAVA_ARTIFACT_NAME "corax-java-cli-community-$CORAX_VERSION"
ENV CORAX_JAVA_ARTIFACT_ZIP "$CORAX_JAVA_ARTIFACT_NAME.zip"
ENV CORAX_JAVA_CLI_NAME "corax-cli-community-${CORAX_VERSION}.jar"

RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
        openjdk-${JDK_MAJOR_VERSION}-jdk openjdk-${JDK_MAJOR_VERSION}-jdk-headless \
        apt-transport-https \
        ca-certificates \
        curl findutils

# CLEANUP
RUN apt-get -qq -y autoremove && \
    apt-get -qq -y clean && \
    rm -rf /var/lib/apt/lists/*

ADD . /corax-community
WORKDIR /corax-community

RUN if [ ! -f $CORAX_JAVA_ARTIFACT_ZIP ]; then curl -SLO "https://github.com/Feysh-Group/corax-community/releases/download/v$CORAX_VERSION/$CORAX_JAVA_ARTIFACT_ZIP"; else echo "file already exists!"; fi

RUN jar xf $CORAX_JAVA_ARTIFACT_ZIP
# [兼容异常处理] 第一次 build 必失败，|| true 使其永真
RUN ./gradlew build || true
RUN sed -i "s#^coraxEnginePath=.*#coraxEnginePath=/corax-community/$CORAX_JAVA_ARTIFACT_NAME/$CORAX_JAVA_CLI_NAME#g" gradle-local.properties && \
    ./gradlew build && \
    ln -s ./$CORAX_JAVA_ARTIFACT_NAME/$CORAX_JAVA_CLI_NAME corax-cli.jar

CMD [ "java", "-jar", "corax-cli.jar"]