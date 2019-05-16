package org.wens.os.apiserver;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.wens.os.locate.LocateService;
import org.wens.os.locate.LocateServiceImpl;
import redis.clients.jedis.JedisPool;

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
    public JedisPool jedisPool(RedisProperties redisProperties) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        RedisProperties.Jedis jedis = redisProperties.getJedis();
        if (jedis != null) {
            config.setMaxIdle(jedis.getPool().getMaxIdle());
            config.setMaxWaitMillis(jedis.getPool().getMaxWait().toMillis());
            config.setMaxTotal(jedis.getPool().getMaxActive());
            config.setMinIdle(jedis.getPool().getMinIdle());
        }
        JedisPool jedisPool = new JedisPool(config, redisProperties.getHost(), redisProperties.getPort(), (int) redisProperties.getTimeout().toMillis(), redisProperties.getPassword());
        return jedisPool;
    }

    @Bean
    public LocateService locateService(JedisPool jedisPool ){
        return new LocateServiceImpl("locateMessage" , jedisPool );
    }

}
