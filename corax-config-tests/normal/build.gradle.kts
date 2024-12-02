val coroutinesVersion: String by rootProject
val junit4Version: String by rootProject
val kotlinVersion: String by project
val mybatisApacheVersion: String by rootProject
val mybatisPlusApacheVersion: String by rootProject
val mybatisPlusSupportVersion: String by rootProject


dependencies {
    compileOnly(group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = coroutinesVersion)
    compileOnly(group = "org.projectlombok", name = "lombok", version ="1.18.32")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version ="1.18.32")

    compileOnly("com.google.guava:guava:19.0"){ isTransitive = false }

    compileOnly(group = "org.mybatis", name = "mybatis", version = mybatisApacheVersion)

    compileOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.15.0")

    compileOnly("org.springframework.boot:spring-boot-starter-web:1.5.1.RELEASE") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        isTransitive = false
    }
    compileOnly("org.springframework.boot:spring-boot-starter-security:1.5.1.RELEASE") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        isTransitive = false
    }
    compileOnly("org.mybatis.spring.boot:mybatis-spring-boot-starter:1.3.2") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        isTransitive = false
    }
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-web
    implementation("org.springframework.security:spring-security-web:4.2.20.RELEASE"){ isTransitive = true }
    // https://mvnrepository.com/artifact/org.springframework.security/spring-security-config
    compileOnly("org.springframework.security:spring-security-config:4.2.20.RELEASE"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.springframework/spring-webmvc
    compileOnly("org.springframework:spring-webmvc:5.3.0"){ isTransitive = true }
    // https://mvnrepository.com/artifact/org.springframework/org.springframework.web
    compileOnly("org.springframework:spring-web:5.3.0"){ isTransitive = true }
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.8.0"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.8.6"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    compileOnly("com.fasterxml.jackson.core:jackson-core:2.14.0"){ isTransitive = false }


    compileOnly("javax.ws.rs:javax.ws.rs-api:2.0"){ isTransitive = false }
    compileOnly("javax:javaee-api:7.0"){ isTransitive = true }
    compileOnly("org.apache.wink:wink-server:1.1.3-incubating")
    compileOnly("com.sun.jersey.contribs:jersey-multipart:1.8")

    // https://mvnrepository.com/artifact/org.apache.tapestry/tapestry-core
    compileOnly("org.apache.tapestry:tapestry-core:5.8.2"){ isTransitive = false }
    compileOnly("org.apache.tapestry:tapestry5-annotations:5.8.2"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.wicket/wicket-core
    compileOnly("org.apache.wicket:wicket-core:9.12.0"){ isTransitive = false }
    // https://mvnrepository.com/artifact/org.apache.wicket/wicket-request
    compileOnly("org.apache.wicket:wicket-request:9.12.0"){ isTransitive = false }
    compileOnly("org.apache.wicket:wicket-util:9.12.0"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.struts/struts-core
    compileOnly("org.apache.struts:struts-core:1.3.10"){ isTransitive = false }

//    https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    compileOnly("org.apache.commons:commons-lang3:3.14.0"){ isTransitive = false}

    // https://mvnrepository.com/artifact/org.apache.commons/commons-email
    compileOnly("org.apache.commons:commons-email:1.5"){ isTransitive = false }

    // https://mvnrepository.com/artifact/commons-io/commons-io
    compileOnly("commons-io:commons-io:2.11.0"){ isTransitive = false }
    // https://mvnrepository.com/artifact/commons-httpclient/commons-httpclient
    compileOnly("commons-httpclient:commons-httpclient:3.1"){ isTransitive = false }


    // https://mvnrepository.com/artifact/com.mitchellbosecke/pebble
    compileOnly("com.mitchellbosecke:pebble:2.3.0"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.freemarker/freemarker
    compileOnly("org.freemarker:freemarker:2.3.31"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.velocity/velocity
//    compileOnly("org.apache.velocity:velocity:1.7"){ isTransitive = false }
    compileOnly("org.apache.velocity:velocity-engine-core:2.3"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.owasp.esapi/esapi
    compileOnly("org.owasp.esapi:esapi:2.0.1"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    compileOnly("org.apache.httpcomponents:httpclient:4.5.14"){ isTransitive = false }

    // android
    compileOnly(files("dist/platforms/android-7/android.jar"))
    compileOnly("com.google.android:support-v4:r6"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on
    compileOnly("org.bouncycastle:bcprov-jdk15on:1.60"){ isTransitive = false }

    // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-core
    compileOnly("com.amazonaws:aws-java-sdk-core:1.12.297"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    compileOnly("com.auth0:java-jwt:3.19.2"){ isTransitive = false }
    // https://mvnrepository.com/artifact/org.apache.shiro/shiro-web
    compileOnly("org.apache.shiro:shiro-web:1.2.3"){ isTransitive = true }
    // https://mvnrepository.com/artifact/commons-net/commons-net
    compileOnly("commons-net:commons-net:3.6"){ isTransitive = false }
    // https://mvnrepository.com/artifact/org.apache.sshd/sshd-core
    compileOnly("org.apache.sshd:sshd-core:2.2.0"){ isTransitive = true }

    compileOnly("com.azure:azure-core:1.10.0"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.azure/azure-identity
    compileOnly("com.azure:azure-identity:1.2.0"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
    compileOnly("com.azure:azure-security-keyvault-secrets:4.2.3"){ isTransitive = false }
    // https://mvnrepository.com/artifact/ch.ethz.ganymed/ganymed-ssh2
    compileOnly("ch.ethz.ganymed:ganymed-ssh2:262"){ isTransitive = false }
    compileOnly("sshtools:j2ssh-core:0.2.2"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.jcraft/jsch
    compileOnly("com.jcraft:jsch:0.1.42"){ isTransitive = false }
    // https://mvnrepository.com/artifact/org.mongodb/mongo-java-driver
    compileOnly("org.mongodb:mongo-java-driver:3.11.0"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc
    compileOnly("com.microsoft.sqlserver:mssql-jdbc:11.2.1.jre8"){ isTransitive = false }
    // https://mvnrepository.com/artifact/net.schmizz/sshj
    compileOnly("net.schmizz:sshj:0.1.1"){ isTransitive = false }
    // https://mvnrepository.com/artifact/com.trilead/trilead-ssh2
    compileOnly("com.trilead:trilead-ssh2:1.0.0-build220"){ isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    compileOnly("commons-lang:commons-lang:2.6") { isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.xmlrpc/xmlrpc-client
    compileOnly("org.apache.xmlrpc:xmlrpc-client:3.1.3") { isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.xmlrpc/xmlrpc-server
    compileOnly("org.apache.xmlrpc:xmlrpc-server:3.1.3") { isTransitive = false }

    // https://mvnrepository.com/artifact/org.apache.xmlrpc/xmlrpc-common
    compileOnly("org.apache.xmlrpc:xmlrpc-common:3.1.3") { isTransitive = false }

    // https://mvnrepository.com/artifact/com.hazelcast/hazelcast
    compileOnly("com.hazelcast:hazelcast:4.0") { isTransitive = false }

    // https://mvnrepository.com/artifact/commons-fileupload/commons-fileupload
    compileOnly("commons-fileupload:commons-fileupload:1.5") { isTransitive = false }

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    compileOnly("commons-codec:commons-codec:1.10")

    compileOnly("io.jsonwebtoken:jjwt:0.9.1") { isTransitive = false }

    // https://mvnrepository.com/artifact/org.opensaml/xmltooling
    compileOnly(group = "org.opensaml", name = "xmltooling", version ="1.4.4") { isTransitive = false }

    compileOnly(group = "org.apache.poi", name = "poi-ooxml", version ="3.10-FINAL") { isTransitive = false }
    compileOnly(group = "org.apache.poi", name = "poi", version ="3.10-FINAL") { isTransitive = false }

    compileOnly(group = "com.squareup.okhttp3", name = "okhttp", version ="4.9.0") { isTransitive = false }
    compileOnly(group = "com.squareup.okio", name = "okio", version ="2.8.0") { isTransitive = false }

    compileOnly(group = "cn.hutool", name = "hutool-all", version ="5.8.16") { isTransitive = false }

    compileOnly(group = "org.thymeleaf", name = "thymeleaf-spring5", version ="3.0.15.RELEASE") { isTransitive = true }

    compileOnly(group ="com.aliyun.oss", name = "aliyun-sdk-oss", version = "3.18.0")

    compileOnly(group ="org.springframework", name = "spring-jdbc", version = "6.1.12")

    compileOnly(group ="com.baomidou", name = "mybatis-plus-extension", version = mybatisPlusApacheVersion)
    compileOnly(group ="com.baomidou", name = "mybatis-plus-core", version = mybatisPlusApacheVersion)
    compileOnly(group ="com.baomidou", name = "mybatis-plus-spring", version = mybatisPlusApacheVersion)
    compileOnly(group ="com.baomidou", name = "mybatis-plus-support", version = mybatisPlusSupportVersion)

    // https://mvnrepository.com/artifact/com.alibaba/fastjson
    compileOnly(group ="com.alibaba", name = "fastjson", version = "1.2.83")
}

tasks.withType<JavaCompile>() {
    options.isWarnings = false
    options.isDeprecation = false
    options.isIncremental = true
    options.isVerbose = false
    options.compilerArgs.add("-Xlint:-deprecation")
    options.compilerArgs.add("-Xlint:-removal")
    options.compilerArgs.add("-Xlint:-unchecked")
}