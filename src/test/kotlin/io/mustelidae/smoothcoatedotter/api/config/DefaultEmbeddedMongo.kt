package io.mustelidae.smoothcoatedotter.api.config

import com.asarkar.spring.test.redis.EmbeddedRedisLifecycle
import com.mongodb.ConnectionString
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.io.IOException

@Lazy(false)
@Component
@EnableConfigurationProperties(value = [MongoProperties::class])
class DefaultEmbeddedMongo(
    private val mongoProperties: MongoProperties,
) {
    lateinit var mongoProcess: TransitionWalker.ReachedState<RunningMongodProcess>
    var port: Int = -1
    lateinit var host: String

    @PostConstruct
    fun startup() {
        mongoProcess = Mongod.instance().start(Version.Main.V7_0)
        host = mongoProcess.current().serverAddress.host
        port = mongoProcess.current().serverAddress.port
    }

    @Bean
    @Throws(IOException::class)
    fun mongoTemplate(): MongoTemplate {
        val mongoClient = MongoClients.create(ConnectionString("mongodb://$host:$port"))
        return MongoTemplate(mongoClient, mongoProperties.database)
    }

    @PreDestroy
    fun shutdown() {
        mongoProcess.current().stop()
    }
}

@Order(1)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(
    name = [
        "redis.embedded.RedisServer",
    ],
)
class CustomEmbeddedRedisAutoConfiguration {
    @Bean
    fun embeddedRedisLifecycle(): EmbeddedRedisLifecycle = EmbeddedRedisLifecycle()
}
