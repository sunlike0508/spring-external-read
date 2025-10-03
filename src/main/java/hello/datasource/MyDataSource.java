package hello.datasource;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

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

