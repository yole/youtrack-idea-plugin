package com.github.jk1.ytplugin.search.actions

import com.github.jk1.ytplugin.common.components.ComponentAware
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class RefreshIssuesAction(): AnAction("Refresh issues",
        "Update issue list from YouTrack server",
        AllIcons.Actions.Refresh) {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        if (project != null && project.isInitialized) {
            ComponentAware.of(project).issueStoreComponent.update()
        }
    }
}