package org.wens.os.apiserver;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.wens.os.locate.LocateService;
import org.wens.os.locate.LocateServiceImpl;

@SpringBootApplication
public class ApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiServerApplication.class, args);
    }

    @Bean
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }


    @Bean
    public LocateService locateService() {
        return new LocateServiceImpl("locateMessage");
    }

}
