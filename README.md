# Spring boot logger for requests/responses

ginger-log

## Supported frameworks:

- webflux
- feign
- reactive feign

## Log examples

Text logger

``` text
HTTP_REQUEST: POST /api/v1/test1 headers: {"Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"} queryParams: {"param1":"pa**","param2":"value ok"} body: {"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
HTTP_RESPONSE: 200 POST /api/v1/test1 headers: {"Content-Type":"application/json"} body: {"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
```

Json logger

``` text
{"type":"HTTP_REQUEST","common":{"method":"POST","uri":"/api/v1/test1"},"headers":{"Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"},"queryParams":{"param1":"pa**","param2":"value ok"},"body":"{\"login\":\"loginValue\",\"accessToken\":\"maskedAccessToken\",\"userInfo\":\"infoValue\",\"refreshToken\":\"maskedRefreshToken\"}"}
{"type":"HTTP_RESPONSE","common":{"method":"POST","uri":"/api/v1/test1"},"code":"200","headers":{"Content-Type":"application/json"},"body":"{\"login\":\"loginValue\",\"accessToken\":\"maskedAccessToken\",\"userInfo\":\"infoValue\",\"refreshToken\":\"maskedRefreshToken\"}"}
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
  logger-type: text # text/json/custom (default: text)
  http: # http/feign
    enabled: true # true/false (default: true)
    probability: 100 # min=0 max=100 percentage probability of log recording (default: 100)
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
  feign: # http/feign
    enabled: true
```

## Loggers

- Json logger (default)
- Text logger
- Custom logger (implement
  interface [Logger.kt](logger%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fmarkklim%2Flibs%2Fginger%2Flogger%2FLogger.kt))

## Cache

- Internal cache (default)
- Custom cache (implement
  interface [LoggingCache.kt](logger%2Fsrc%2Fmain%2Fkotlin%2Fcom%2Fmarkklim%2Flibs%2Fginger%2Fcache%2FLoggingCache.kt))