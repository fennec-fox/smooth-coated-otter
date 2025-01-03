package io.mustelidae.smoothcoatedotter.api.config.redis

import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.mustelidae.smoothcoatedotter.api.common.Constant
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableConfigurationProperties(value = [RedisProperties::class])
@EnableRedisRepositories
class RedisConfiguration(
    private val properties: RedisProperties,
) {
    @Bean(name = [Constant.Redis.USER_LOCK])
    fun userLockRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate().apply {
            connectionFactory = redisConnectionFactory
        }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val clientConfiguration =
            LettuceClientConfiguration
                .builder()
                .commandTimeout(properties.timeout)
                .clientOptions(
                    ClusterClientOptions
                        .builder()
                        .topologyRefreshOptions(
                            ClusterTopologyRefreshOptions
                                .builder()
                                .enablePeriodicRefresh(properties.lettuce.cluster.refresh.period)
                                .enableAllAdaptiveRefreshTriggers()
                                .build(),
                        ).build(),
                ).build()

        val configuration =
            RedisClusterConfiguration(
                properties.cluster.nodes,
            ).apply {
                this.password = RedisPassword.of(properties.password)
            }

        return LettuceConnectionFactory(configuration, clientConfiguration).apply {
            afterPropertiesSet()
        }
    }
}
