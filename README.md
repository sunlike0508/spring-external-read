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
    private final int maxConnections;
    private final Duration timeout;
    private final List<String> options;

    public MyDataSource(String url, String username, String password, int maxConnections, Duration timeout, List<String> options) {
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

        return new MyDataSource(url, username, password, maxConnections, timeout, options);
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

<img width="629" height="232" alt="Screenshot 2025-10-03 at 17 52 50" src="https://github.com/user-attachments/assets/5a244241-12e2-4a30-891a-6d29afe2e87d" />

**정리**
`application.properties` 에 필요한 외부 설정을 추가하고, `Environment` 를 통해서 해당 값들을 읽어서, `MyDataSource` 를 만들었다.

향후 외부 설정 방식이 달라져도, 예를 들어서 설정 데이터 ( `application.properties` )를 사용하다가 커맨드 라인 옵션 인수나 자바 시스템 속성으로 변경해도 애플리케이션 코드를 그대로 유지할 수 있다.

**단점**

이 방식의 단점은 `Environment` 를 직접 주입받고, `env.getProperty(key)` 를 통해서 값을 꺼내는 과정을 반복 해야 한다는 점이다.

스프링은 `@Value` 를 통해서 외부 설정값을 주입 받는 더욱 편리한 기능을 제공한다.

# 외부설정 사용 - @Value

`@Value` 를 사용하면 외부 설정값을 편리하게 주입받을 수 있다.

참고로 `@Value` 도 내부에서는 `Environment` 를 사용한다.

```java

@Slf4j
@Configuration
public class MyDataSourceValueConfig {

    @Value("${my.datasource.url}")
    private String url;
    @Value("${my.datasource.username}")
    private String username;
    @Value("${my.datasource.password}")
    private String password;
    @Value("${my.datasource.etc.max-connections}")
    private int maxConnections;
    @Value("${my.datasource.etc.timeout}")
    private Duration timeout;
    @Value("${my.datasource.etc.options}")
    private List<String> options;

    @Bean
    public MyDataSource myDataSource() {

        return new MyDataSource(url, username, password, maxConnections, timeout, options);
    }

    @Bean
    public MyDataSource myDataSource2(
        @Value("${my.datasource.url}") String url,
        @Value("${my.datasource.username}") String username,
        @Value("${my.datasource.password}") String password,
        @Value("${my.datasource.etc.max-connections}") int maxConnections,
        @Value("${my.datasource.etc.timeout}") Duration timeout,
        @Value("${my.datasource.etc.options}") List<String> options
    ) {
        return new MyDataSource(url, username, password, maxConnections, timeout, options);
    }
}

@Import(MyDataSourceValueConfig.class)
//@Import(MyDataSourceConfig.class)
@SpringBootApplication(scanBasePackages = "hello.datasource")
public class ExternalReadApplication {

    static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }

}
```

**결과**

<img width="800" height="345" alt="Screenshot 2025-10-03 at 18 31 47" src="https://github.com/user-attachments/assets/078c3f37-324f-4649-85a7-90347ad67040" />

**기본값**

만약 키를 찾지 못할 경우 코드에서 기본값을 사용하려면 다음과 같이 `:` 뒤에 기본값을 적어주면 된다.

예) `@Value("${my.datasource.etc.max-connection:1}")` : `key` 가 없는 경우 `1` 을 사용한 다.

```java

@Slf4j
@Configuration
public class MyDataSourceValueConfig {

    @Value("${my.datasource.url}")
    private String url;
    @Value("${my.datasource.username}")
    private String username;
    @Value("${my.datasource.password}")
    private String password;
    @Value("${my.datasource.etc.max-connections:10}")
    private int maxConnections;
    @Value("${my.datasource.etc.timeout}")
    private Duration timeout;
    @Value("${my.datasource.etc.options}")
    private List<String> options;

    @Bean
    public MyDataSource myDataSource() {

        return new MyDataSource(url, username, password, maxConnections, timeout, options);
    }

    @Bean
    public MyDataSource myDataSource2(
        @Value("${my.datasource.url}") String url,
        @Value("${my.datasource.username}") String username,
        @Value("${my.datasource.password}") String password,
        @Value("${my.datasource.etc.max-connections:20}") int maxConnections,
        @Value("${my.datasource.etc.timeout}") Duration timeout,
        @Value("${my.datasource.etc.options}") List<String> options
    ) {
        return new MyDataSource(url, username, password, maxConnections, timeout, options);
    }
}
```

```properties
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_password
#my.datasource.etc.max-connections=1
my.datasource.etc.timeout=3500ms
my.datasource.etc.options=CACHE,ADMIN
```

**결과**

<img width="633" height="355" alt="Screenshot 2025-10-03 at 18 38 08" src="https://github.com/user-attachments/assets/04d3b983-063c-4362-8fae-0da81b4e0684" />

**정리**

`application.properties` 에 필요한 외부 설정을 추가하고, `@Value` 를 통해서 해당 값들을 읽어서, `MyDataSource` 를 만들었다.

**단점**

`@Value` 를 사용하는 방식도 좋지만, `@Value` 로 하나하나 외부 설정 정보의 키 값을 입력받고, 주입 받아와야 하는 부분이 번거롭다.

그리고 설정 데이터를 보면 하나하나 분리되어 있는 것이 아니라 정보의 묶음으로 되어있다.

여기서는 `my.datasource` 부분으로 묶여있다.

이런 부분을 객체로 변환해서 사용할 수 있다면 더 편리하고 더 좋을 것이다.

# 외부설정 사용 - @ConfigurationProperties 시작

**Type-safe Configuration Properties**

스프링은 외부 설정의 묶음 정보를 객체로 변환하는 기능을 제공한다.

이것을 **타입 안전한 설정 속성**이라 한다.

객체를 사용하면 타입을 사용할 수 있다.

따라서 실수로 잘못된 타입이 들어오는 문제도 방지할 수 있고, 객체를 통해서 활용할 수 있는 부분들이 많아진다.

쉽게 이야기해서 외부 설정을 자바 코드로 관리할 수 있는 것이다.

그리고 설정 정보 그 자체도 타입을 가지게 된다.

```java

@Slf4j
@EnableConfigurationProperties(MyDataSourcePropertiesV1.class)
public class MyDataSourceConfigV1 {

    private final MyDataSourcePropertiesV1 properties;

    public MyDataSourceConfigV1(MyDataSourcePropertiesV1 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource myDataSource() {
        return new MyDataSource(properties.getUrl(),
            properties.getUsername(),
            properties.getPassword(),
            properties.getEtc().getMaxConnections(),
            properties.getEtc().getTimeout(),
            properties.getEtc().getOptions()
        );
    }
}

@Data
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV1 {

    private String url;
    private String username;
    private String password;
    private final Etc etc = new Etc();

    @Data
    public static class Etc {

        private int maxConnections;
        private Duration timeout;
        private final List<String> options = new ArrayList<>();
    }
}

@Import(MyDataSourceConfigV1.class)
//@Import(MyDataSourceValueConfig.class)
//@Import(MyDataSourceConfig.class)
@SpringBootApplication(scanBasePackages = "hello.datasource")
public class ExternalReadApplication {

    static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }
}
```

외부 설정을 주입 받을 객체를 생성한다. 그리고 각 필드를 외부 설정의 키 값에 맞추어 준비한다.

`@ConfigurationProperties` 이 있으면 외부 설정을 주입 받는 객체라는 뜻이다.

여기에 외부 설정 KEY의 묶음 시작점인 `my.datasource` 를 적어준다.

기본 주입 방식은 자바빈 프로퍼티 방식이다. `Getter` , `Setter` 가 필요하다. (롬복의 `@Data` 에 의해 자동 생성된다.)

`@EnableConfigurationProperties(MyDataSourcePropertiesV1.class)`

스프링에게 사용할 `@ConfigurationProperties` 를 지정해주어야 한다.

이렇게 하면 해당 클래 스는 스프링 빈으로 등록되고, 필요한 곳에서 주입 받아서 사용할 수 있다.

`private final MyDataSourcePropertiesV1 properties` 설정 속성을 생성자를 통해 주입 받아서 사용한다.

**결과**

<img width="597" height="231" alt="Screenshot 2025-10-03 at 19 02 34" src="https://github.com/user-attachments/assets/470ab093-8e19-45a0-b07b-3eb46be10585" />

**타입 안전**

`ConfigurationProperties` 를 사용하면 타입 안전한 설정 속성을 사용할 수 있다.

`maxConnection=abc` 로 입력하고 실행해보자.

```properties
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_password
my.datasource.etc.max-connections=abc
my.datasource.etc.timeout=3500ms
my.datasource.etc.options=CACHE,ADMIN
```

**결과**

<img width="973" height="270" alt="Screenshot 2025-10-03 at 19 03 24" src="https://github.com/user-attachments/assets/b536aa19-6bcc-4212-b101-2435672db695" />

실행 결과를 보면 숫자가 들어와야 하는데 문자가 들어와서 오류가 발생한 것을 확인할 수 있다.

타입이 다르면 오류가 발생하는 것이다.

실수로 숫자를 입력하는 곳에 문자를 입력하는 문제를 방지해준다.

그래서 타입 안전한 설정 속성이라고 한다.

`ConfigurationProperties` 로 만든 외부 데이터는 타입에 대해서 믿고 사용할 수 있다.

**정리**

`application.properties` 에 필요한 외부 설정을 추가하고, `@ConfigurationProperties` 를 통해서 `MyDataSourcePropertiesV1` 에 외부 설정의 값들을 설정했다.

그리고 해당 값들을 읽어서 `MyDataSource` 를 만들었다.

**표기법 변환**

`maxConnection` 은 표기법이 서로 다르다.

스프링은 캐밥 표기법을 자바 낙타 표기법으로 중간에서 자동으로 변환해준다.

`application.properties` 에서는 `max-connection` 자바 코드에서는 `maxConnection`

**@ConfigurationPropertiesScan**

`@ConfigurationProperties` 를 하나하나 직접 등록할 때는 `@EnableConfigurationProperties` 를 사용한다.

`@EnableConfigurationProperties(MyDataSourcePropertiesV1.class)` `@ConfigurationProperties` 를 특정 범위로 자동 등록할 때는 `@ConfigurationPropertiesScan` 을 사용하면
된다.

`@ConfigurationPropertiesScan` 예시

```java

@Slf4j
//@EnableConfigurationProperties(MyDataSourcePropertiesV1.class) -> 주석
public class MyDataSourceConfigV1 {

    private final MyDataSourcePropertiesV1 properties;

    public MyDataSourceConfigV1(MyDataSourcePropertiesV1 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource myDataSource() {
        return new MyDataSource(properties.getUrl(),
            properties.getUsername(),
            properties.getPassword(),
            properties.getEtc().getMaxConnections(),
            properties.getEtc().getTimeout(),
            properties.getEtc().getOptions()
        );
    }
}
```

**결과**

<img width="1256" height="296" alt="Screenshot 2025-10-03 at 23 38 56" src="https://github.com/user-attachments/assets/4be6a70e-f974-4f34-ab70-2ad710054011" />

**메인에 @ConfigurationPropertiesScan 추가**

```java

@Import(MyDataSourceConfigV1.class)
@SpringBootApplication(scanBasePackages = "hello.datasource")
@ConfigurationPropertiesScan // 추가
public class ExternalReadApplication {

    static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }

}
```

**결과**

<img width="419" height="268" alt="Screenshot 2025-10-03 at 23 39 51" src="https://github.com/user-attachments/assets/9fe0f172-86e5-4d8c-84a3-d89888bd936d" />

빈을 직접 등록하는 것과 컴포넌트 스캔을 사용하는 차이와 비슷하다.

**스프링부트에서 만들어놓은 프로퍼티스**

```java

@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties implements BeanClassLoaderAware, InitializingBean {

}
```

**문제**

`MyDataSourcePropertiesV1` 은 스프링 빈으로 등록된다.

그런데 `Setter` 를 가지고 있기 때문에 누군가 실수로 값을 변경하는 문제가 발생할 수 있다.

```java
public MyDataSourceConfigV1(MyDataSourcePropertiesV1 properties) {
    this.properties = properties;
    this.properties.setPassword("prod_password");
}
```

여기에 있는 값들은 외부 설정값을 사용해서 초기에만 설정되고, 이후에는 변경하면 안된다.

이럴 때 `Setter` 를 제거하고 대신에 생성자를 사용하면 중간에 데이터를 변경하는 실수를 근본적으로 방지할 수 있다.

이런 문제가 없을 것 같지만, 한번 발생하면 정말 잡기 어려운 버그가 만들어진다.

대부분의 개발자가 `MyDataSourcePropertiesV1` 의 값은 변경하면 안된다고 인지하고 있지만, 어떤 개발자가 자신의 문제를 해결하기 위해 `setter` 를 통해서 값을 변경하게 되면, 애플리케이션 전체에 심각한 버그를 유발할 수 있다.

좋은 프로그램은 제약이 있는 프로그램이다.

# 외부설정 사용 - @ConfigurationProperties 생성자

`@ConfigurationProperties` 는 Getter, Setter를 사용하는 자바빈 프로퍼티 방식이 아니라 생성자를 통해서 객체를 만드는 기능도 지원한다.

다음 코드를 통해서 확인해보자.

```java

@Slf4j
@EnableConfigurationProperties(MyDataSourcePropertiesV2.class)
public class MyDataSourceConfigV2 {

    private final MyDataSourcePropertiesV2 properties;

    public MyDataSourceConfigV2(MyDataSourcePropertiesV2 properties) {
        this.properties = properties;
    }

    @Bean
    public MyDataSource myDataSource() {
        return new MyDataSource(properties.getUrl(),
            properties.getUsername(),
            properties.getPassword(),
            properties.getEtc().maxConnections(),
            properties.getEtc().timeout(),
            properties.getEtc().options()
        );
    }
}

@Getter
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV2 {

    private final String url;
    private final String username;
    private final String password;
    private final Etc etc;

    public MyDataSourcePropertiesV2(String url, String username, String password, Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    public record Etc(int maxConnections, Duration timeout, List<String> options) {

    }
}

//@Import(MyDataSourceValueConfig.class)
//@Import(MyDataSourceConfig.class)
//@Import(MyDataSourceConfigV1.class)
@Import(MyDataSourceConfigV2.class)
@SpringBootApplication(scanBasePackages = "hello.datasource")
//@ConfigurationPropertiesScan
public class ExternalReadApplication {

    static void main(String[] args) {
        SpringApplication.run(ExternalReadApplication.class, args);
    }

}
```

**결과**

<img width="606" height="330" alt="Screenshot 2025-10-04 at 00 42 20" src="https://github.com/user-attachments/assets/815b7140-828d-4519-99d4-3f13f4f5e0f5" />

```java

@Getter
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV2 {

    private final String url;
    private final String username;
    private final String password;
    private final Etc etc;

    public MyDataSourcePropertiesV2(String url, String username, String password, Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    public record Etc(int maxConnections, Duration timeout, @DefaultValue("DEFAULT") List<String> options) {

    }
}
```

```properties
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_password
my.datasource.etc.max-connections=1
my.datasource.etc.timeout=3500ms
#my.datasource.etc.options=CACHE,ADMIN
```

**결과**

<img width="451" height="191" alt="Screenshot 2025-10-04 at 00 45 56" src="https://github.com/user-attachments/assets/c491600a-fcc8-4ccb-8a34-cf0a79987290" />

생성자를 만들어 두면 생성자를 통해서 설정 정보를 주입한다.

`@Getter` 롬복이 자동으로 `getter` 를 만들어준다.

`@DefaultValue` : 해당 값을 찾을 수 없는 경우 기본값을 사용한다.

```java

@Getter
@ConfigurationProperties("my.datasource")
public class MyDataSourcePropertiesV2 {

    private final String url;
    private final String username;
    private final String password;
    private final Etc etc;

    public MyDataSourcePropertiesV2(String url, String username, String password, @DefaultValue Etc etc) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.etc = etc;
    }

    public record Etc(int maxConnections, Duration timeout, List<String> options) {

    }
}
```

```properties
my.datasource.url=local.db.com
my.datasource.username=local_user
my.datasource.password=local_password
#my.datasource.etc.max-connections=1
#my.datasource.etc.timeout=3500ms
#my.datasource.etc.options=CACHE,ADMIN
```

**결과**

<img width="734" height="252" alt="Screenshot 2025-10-04 at 00 50 58" src="https://github.com/user-attachments/assets/047737d7-a0e8-455e-852e-fd139cd4bfb1" />

`@DefaultValue Etc etc` : `etc` 를 찾을 수 없을 경우 `Etc` 객체를 생성하고 내부에 들어가는 값은 비워둔다. (`null` , `0` )

`@DefaultValue("DEFAULT") List<String> options` : `options` 를 찾을 수 없을 경우 `DEFAULT` 라는 이름의 값을 사용한다.

**참고**

`@ConstructorBinding`

스프링 부트 3.0 이전에는 생성자 바인딩시에 `@ConstructorBinding` 애노테이션을 필수로 사용해야했다.

스프링 부트 3.0 부터는 생성자가 하나일 때는 생략할 수 있다.

생성자가 둘 이상인 경우에는 사용할 생성자에 `@ConstructorBinding` 애노테이션 적용하면 된다.

**정리**

`application.properties` 에 필요한 외부 설정을 추가하고, `@ConfigurationProperties` 의 생성자 주입을 통해서 값을 읽어들였다.

`Setter` 가 없으므로 개발자가 중간에 실수로 값을 변경하는 문제가 발생하지 않는다.

**문제**

타입과 객체를 통해서 숫자에 문자가 들어오는것 같은 기본적인 타입 문제들은 해결이 되었다.

그런데 타입은 맞는데 숫자의 범위가 기대하는 것과 다르면 어떻게 될까?

예를 들어서 `max-connection` 의 값을 `0` 으로 설정하면 커넥션이 하나도 만들어지지 않는 심각한 문제가 발생한다고 가정해보자.

`max-connection` 은 최소 `1` 이상으로 설정하지 않으면 애플리케이션 로딩 시점에 예외를 발생시켜서 빠르게 문제를 인지할 수 있도록 하고 싶다.






