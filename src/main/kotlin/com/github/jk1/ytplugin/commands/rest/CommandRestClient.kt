package com.github.jk1.ytplugin.commands.rest

import com.github.jk1.ytplugin.common.logger
import com.github.jk1.ytplugin.commands.model.CommandAssistResponse
import com.github.jk1.ytplugin.commands.model.CommandExecutionResponse
import com.github.jk1.ytplugin.commands.model.YouTrackCommand
import com.github.jk1.ytplugin.commands.model.YouTrackCommandExecution
import com.github.jk1.ytplugin.common.rest.RestClientTrait
import com.github.jk1.ytplugin.common.rest.ResponseLoggerTrait
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.jdom.input.SAXBuilder
import java.net.URLEncoder

class CommandRestClient(override val project: Project) : RestClientTrait, ResponseLoggerTrait {

    fun assistCommand(command: YouTrackCommand): CommandAssistResponse {
        val method = GetMethod(command.intellisenseCommandUrl)
        val startTime = System.currentTimeMillis()
        try {
            val status = createHttpClient().executeMethod(method)
            if (status == 200) {
                return CommandAssistResponse(method.responseBodyAsLoggedStream())
            } else {
                throw RuntimeException("HTTP $status: ${method.responseBodyAsLoggedString()}")
            }
        } finally {
            method.releaseConnection()
            logger.debug("Intellisense request to YouTrack took ${System.currentTimeMillis() - startTime} ms")
        }
    }

    fun executeCommand(command: YouTrackCommandExecution): CommandExecutionResponse {
        val method = PostMethod(command.executeCommandUrl)
        val startTime = System.currentTimeMillis()
        try {
            val status = createHttpClient().executeMethod(method)
            if (status != 200) {
                val string = method.responseBodyAsLoggedStream()
                val element = SAXBuilder(false).build(string).rootElement
                if ("error".equals(element.name)) {
                    return CommandExecutionResponse(errors = listOf(element.text))
                } else {
                    return CommandExecutionResponse(messages = listOf(element.text))
                }
            }
            method.responseBodyAsLoggedString()
            return CommandExecutionResponse()
        } finally {
            method.releaseConnection()
            logger.debug("Command execution request to YouTrack took ${System.currentTimeMillis() - startTime} ms")
        }
    }

    private val YouTrackCommandExecution.executeCommandUrl: String
        get() {
            with (command) {
                val baseUrl = taskManagerComponent.getActiveYouTrackRepository().url
                val execUrl = "$baseUrl/rest/issue/execute/${issues.first().id}"
                return "$execUrl?command=${command.urlencoded}&comment=${comment?.urlencoded}&group=${commentVisibleGroup?.urlencoded}&disableNotifications=$silent"
            }
        }

    private val YouTrackCommand.intellisenseCommandUrl: String
        get() {
            val baseUrl = taskManagerComponent.getActiveYouTrackRepository().url
            val assistUrl = "$baseUrl/rest/command/underlineAndSuggestAndCommands"
            return "$assistUrl?command=${command.urlencoded}&caret=$caret&query=${issues.first().id}&noIssuesContext=false"
        }

    private val String.urlencoded: String
        get() = URLEncoder.encode(this, "UTF-8")
}