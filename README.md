# 외부 설정 사용 - Environment

다음과 같은 외부 설정들은 스프링이 제공하는 `Environment` 를 통해서 일관된 방식으로 조회할 수 있다.

**외부 설정**

* 설정 데이터( `application.properties` )
* OS 환경변수
* 자바 시스템 속성 커맨드 라인 옵션 인수

**다양한 외부 설정 읽기**

스프링은 `Environment` 는 물론이고 `Environment` 를 활용해서 더 편리하게 외부 설정을 읽는 방법들을 제공한다.

**스프링이 지원하는 다양한 외부 설정 조회 방법**

* `Environment`
* `@Value` - 값 주입
* `@ConfigurationProperties` - 타입 안전한 설정 속성

이번 시간에는 조금 복잡한 예제를 가지고 외부 설정을 읽어서 활용하는 다양한 방법들을 학습해보자.

예제에서는 가상의 데이터소스를 하나 만들고, 여기에 필요한 속성들을 외부 설정값으로 채운 다음 스프링 빈으로 등록 할 것이다.

이 예제는 외부 설정값을 어떤식으로 활용하는지 이해를 돕기 위해 만들었고, 실제 DB에 접근하지는 않는다.

```java

@Slf4j
public class MyDataSource {

    private final String url;
    private final String username;
    private final String password;
    private final String maxConnections;
    private final Duration timeout;
    private final List<String> options;

    public MyDataSource(String url, String username, String password, String maxConnections, Duration timeout, List<String> options) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxConnections = maxConnections;
        this.timeout = timeout;
        this.options = options;
    }

    @PostConstruct
    public void init() {
        log.info("url:{}", url);
        log.info("username:{}", username);
        log.info("password:{}", password);
        log.info("maxConnections:{}", maxConnections);
        log.info("timeout:{}", timeout);
        log.info("options:{}", options);
    }
}

@Slf4j
@Configuration
public class MyDataSourceConfig {

    private final Environment env;

    public MyDataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public MyDataSource myDataSource() {
        String url = env.getProperty("my.datasource.url");
        String username = env.getProperty("my.datasource.username");
        String password = env.getProperty("my.datasource.password");
        int maxConnections = env.getProperty("my.datasource.etc.max-connections", Integer.class);
        Duration timeout = env.getProperty("my.datasource.etc.timeout", Duration.class);
        List<String> options = env.getProperty("my.datasource.etc.options", List.class);

        return new MyDataSource(url, username, password, String.valueOf(maxConnections), timeout, options);
    }
}
```

**속성 변환기 - 스프링 공식 문서**

https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.conversion

```properties
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_password
my.datasource.etc.max-connections=1
my.datasource.etc.timeout=3500ms
my.datasource.etc.options=CACHE,ADMIN
```

**참고 - properties 캐밥 표기법**
`properties` 는 자바의 낙타 표기법( `maxConnection` )이 아니라 소문자와 `-` (dash)를 사용하는 캐밥
표기법( `max-connection` )을 주로 사용한다.

참고로 이곳에 자바의 낙타 표기법을 사용한다고 해서 문제가 되 는 것은 아니다.

스프링은 `properties` 에 캐밥 표기법을 권장한다.

```java

@Import(MyDataSourceConfig.class)
@SpringBootApplication(scanBasePackages = "hello.datasource")
public class ExternalReadApplication {

    static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }
}
```

예제에서는 `@Import` 로 설정 정보를 계속 변경할 예정이므로, 설정 정보를 바꾸면서 사용하기 위해 `hello.config` 의 위치를 피해서 컴포넌트 스캔 위치를 설정했다.

**결과**

**정리**
`application.properties` 에 필요한 외부 설정을 추가하고, `Environment` 를 통해서 해당 값들을 읽어서, `MyDataSource` 를 만들었다.

향후 외부 설정 방식이 달라져도, 예를 들어서 설정 데이터 ( `application.properties` )를 사용하다가 커맨드 라인 옵션 인수나 자바 시스템 속성으로 변경해도 애플리케이션 코드를 그대로 유지할 수 있다.

**단점**

이 방식의 단점은 `Environment` 를 직접 주입받고, `env.getProperty(key)` 를 통해서 값을 꺼내는 과정을 반복 해야 한다는 점이다.

스프링은 `@Value` 를 통해서 외부 설정값을 주입 받는 더욱 편리한 기능을 제공한다.
































