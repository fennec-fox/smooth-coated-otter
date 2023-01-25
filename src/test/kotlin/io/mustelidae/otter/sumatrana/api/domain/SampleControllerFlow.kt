package io.mustelidae.otter.sumatrana.api.domain

import io.mustelidae.otter.sumatrana.api.common.Reply
import io.mustelidae.otter.sumatrana.api.permission.RoleHeader
import io.mustelidae.otter.sumatrana.utils.fromJson
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

internal class SampleControllerFlow(
    private val mockMvc: MockMvc
) {
    fun helloWorld(): String {
        val uri = linkTo<SampleController> { helloWorld(1234) }.toUri()

        return mockMvc.get(uri) {
            contentType = MediaType.APPLICATION_JSON
            header(RoleHeader.XUser.KEY, 1234)
        }.andExpect {
            status { is2xxSuccessful() }
        }.andReturn()
            .response
            .contentAsString
            .fromJson<Reply<String>>()
            .content!!
    }
}
