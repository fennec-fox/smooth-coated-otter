package io.mustelidae.smoothcoatedotter.api.config

import io.mustelidae.smoothcoatedotter.api.permission.RoleHeader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.Optional

@Configuration
@EnableJpaAuditing
class AuditConfiguration {
    @Bean
    internal fun auditorAware(): AuditorAware<*> = AuditorAwareImpl()
}

class AuditorAwareImpl : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        var auditor = UNKNOWN_AUDITOR

        if (RequestContextHolder.getRequestAttributes() != null) {
            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

            val adminId = request.getHeader(RoleHeader.XAdmin.KEY)
            val partnerId = request.getHeader(RoleHeader.XPartner.KEY)
            val userId = request.getHeader(RoleHeader.XUser.KEY)

            auditor = adminId?.let { "A:".plus(it) } ?: partnerId?.let { "P:".plus(it) } ?: userId?.let { "U:".plus(it) } ?: UNKNOWN_AUDITOR
        }
        return Optional.of(auditor)
    }

    companion object {
        const val UNKNOWN_AUDITOR = "S:unknown"
    }
}
