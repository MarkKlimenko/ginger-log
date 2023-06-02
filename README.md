# Spring boot logger for requests/responses
ginger-log

## Supported frameworks:
- webflux

## Log examples
Json logger
``` text
2023-06-02 01:09:24.447  INFO [service,124c19e44585c3b7,124c19e44585c3b7] 73273 --- [     parallel-3] c.m.libs.ginger.logger.Logger        : {"type":"HTTP_REQUEST","common":{"method":"GET","uri":"/api/v1/namespaces"},"headers":{"WebTestClient-Request-Id":"11"},"queryParams":{}}
2023-06-02 01:09:24.453  INFO [service,124c19e44585c3b7,124c19e44585c3b7] 73273 --- [3 @coroutine#35] c.m.libs.ginger.logger.Logger        : {"type":"HTTP_RESPONSE","common":{"method":"GET","uri":"/api/v1/namespaces"},"headers":{"Content-Type":"application/json","Content-Length":"776"},"code":"no_value","timeSpent":6,"body":"{\"namespaces\":[{\"id\":\"012b567d-bfb2-4341-861e-c0de2da992ad\",\"name\":\"more-less\",\"status\":\"ENABLED\",\"version\":0},{\"id\":\"83a44703-6fc2-4ce9-afe7-fe7569facbd5\",\"name\":\"contains\",\"status\":\"ENABLED\",\"version\":0},{\"id\":\"e05b1a1e-aa71-474a-aa60-37c913fe1395\",\"name\":\"version\",\"status\":\"ENABLED\",\"version\":0},{\"id\":\"9223c0fc-f13b-4f41-80f7-02d98d6bc5b4\",\"name\":\"mix\",\"status\":\"ENABLED\",\"version\":0},{\"id\":\"3bd6ed24-5e09-4a7b-93e8-1ec46f80f88a\",\"name\":\"eq\",\"status\":\"ENABLED\",\"version\":0},{\"id\":\"7b154341-560c-4481-8737-dec11bce68bb\",\"name\":\"default\",\"status\":\"ENABLED\",\"version\":0},{\"id\":\"20fcd9dd-40b7-45f0-ac22-786685467269\",\"name\":\"editedNamespace\",\"status\":\"DISABLED\",\"version\":1},{\"id\":\"4617ffaf-4776-4cf4-8958-0acc720f1a8a\",\"name\":\"newNamespace\",\"status\":\"ENABLED\",\"version\":0}]}"}
``` 

## Usage
build.gradle
``` groovy
// https://mvnrepository.com/artifact/com.markklim.libs/ginger-log
implementation "com.markklim.libs:ginger-log:$version"
```
application.yml (more examples in tests [resources](webflux-test%2Fsrc%2Ftest%2Fresources))
```yaml
logging:
  http:
    enabled: true # true/false (default: true)
    uris:
      include:
        - "regex_pattern" # (default: everything included)
      exclude:
        - "regex_pattern" # (default: nothing excluded)
    content-types:
      include:
        - "regex_pattern" # (default: everything included)
      exclude:
        - "regex_pattern" # (default: nothing excluded)
    methods:
      include:
        - "regex_pattern" # (default: everything included)
      exclude:
        - "regex_pattern" # (default: nothing excluded)
    headers:
      properties:
        include:
          - "regex_pattern" # (default: everything included)
        exclude:
          - "regex_pattern" # (default: nothing excluded)
        masked:
          - property: "string"
            value-pattern: "regex_pattern" # (default: match any)
            substitution-value: "string"
    query-params:
      properties:
        include:
          - "regex_pattern" # (default: everything included)
        exclude:
          - "regex_pattern" # (default: nothing excluded)
        masked:
          - property: "string"
            value-pattern: "regex_pattern" # (default: match any)
            substitution-value: "string"
    body:
      enabled: true
      uris:
        include:
          - "regex_pattern" # (default: everything included)
        exclude:
          - "regex_pattern" # (default: nothing excluded)
      binary-content-logging: DISABLED # ENABLED/DISABLED (default: DISABLED)
      threshold: "1KB" # Java DataSize (default: disabled)
      masked:
        - pattern: "regex_pattern"
          substitutionValue: "string"
```

## Loggers
- Json logger (default)
- Text logger
- Custom logger (implement interface [Logger.kt](logger%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fmarkklim%2Flibs%2Fginger%2Flogger%2FLogger.kt))

## Cache
- Internal cache (default)
- Custom cache (implement interface [LoggingCache.kt](logger%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fmarkklim%2Flibs%2Fginger%2Fcache%2FLoggingCache.kt))