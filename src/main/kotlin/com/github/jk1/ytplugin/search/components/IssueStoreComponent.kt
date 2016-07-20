package com.github.jk1.ytplugin.search.components

import com.github.jk1.ytplugin.search.model.Issue
import com.github.jk1.ytplugin.search.rest.IssuesRestClient
import com.intellij.openapi.components.AbstractProjectComponent
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.util.containers.SortedList
import java.io.File
import java.io.IOException
import java.util.*

class IssueStoreComponent(val project: Project) : AbstractProjectComponent(project) {

    private val issues = HashMap<String, Issue>()
    private val sortedIssues = SortedList(Comparator<String> { o1, o2 ->
        o1.compareTo(o2)
    })
    private var currentCallback: ActionCallback = ActionCallback.Done()

    var searchQuery = ""

    fun getAllIssues(): Collection<Issue> {
        return issues.values
    }

    fun update(): ActionCallback {
        if (isUpdating()) {
            return currentCallback
        }

        currentCallback = refresh()


        return currentCallback
    }

    private fun refresh(): ActionCallback {
        val future = ActionCallback()
        object : Task.Backgroundable(project, "Updating issues from server", true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    setIssues(IssuesRestClient(project).getIssues(searchQuery))
                } catch (e: Exception) {
                    // todo: notification and logging
                    e.printStackTrace()
                }
            }

            override fun onCancel() {
                future.setDone()
            }

            override fun onSuccess() {
                future.setDone()
                save()
            }
        }.queue()
        return future
    }

    private fun setIssues(updatedIssues: List<Issue>) {
        issues.putAll(updatedIssues.associateBy { it.id })
        sortedIssues.clear()
        sortedIssues.addAll(this.issues.keys)
    }

    fun isUpdating(): Boolean {
        return !currentCallback.isDone
    }


    fun getIssue(id: String): Issue? {
        return issues[id]
    }

    fun getSortedIssues(): SortedList<String> {
        return sortedIssues
    }

    fun save() {
        /*if (MyyConnectionUtil.isLogged(myProject)) {
            val path = getIssuesXmlPath()
            val file = File(path)
            ApplicationManager.getApplication().invokeLater {
                ApplicationManager.getApplication().runWriteAction {
                    try {
                        writeTo(file)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }*/
    }

    private fun getIssuesXmlPath(): String {
        /*var url = YoutrackSession(myProject).getUrl()
        if (url.toLowerCase().startsWith("http://")) {
            url = url.substring("http://".length)
        }
        if (url.toLowerCase().startsWith("https://")) {
            url = url.substring("https://".length)
        }
        return PathManager.getSystemPath() + File.separator
        +"myy" + File.separator
        +url.replace("\\", "/").replace("/", File.separator)*/
        return ""
    }

    @Throws(IOException::class)
    fun writeTo(file: File) {
        /*if (!file.exists()) {
            if (!file.mkdirs()) {
                throw IOException("Can't create " + file.path)
            }
        }
        val issues = File(file, "issues.xml")
        if (!issues.exists()) {
            issues.createNewFile()
        }
        val xml = Element("issues")
        for (issue in issues.values) {
            val element = issue.toXml()

            xml.addContent(element.clone() as Element)
        }

        JDOMUtil.writeDocument(Document(xml), issues, "\n")*/
    }

    fun load() {
        /*if (MyyConnectionUtil.isLogged(myProject)) {
            val file = File(getIssuesXmlPath() + File.separator + "issues.xml")
            if (file.exists()) {
                ApplicationManager.getApplication().runReadAction {
                    try {
                        val document = JDOMUtil.loadDocument(file)
                        val issues = document.rootElement
                        val storedIssues = ArrayList<Issue>()
                        for (child in issues.children) {
                            if (child is Element) {
                                storedIssues.add(Issue(child))
                            }
                        }
                        setIssues(storedIssues)
                    } catch (e: JDOMException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }*/
    }

    override fun projectOpened() {
        load()
    }

    override fun projectClosed() {
//        if (MyyConnectionUtil.isLogged(myProject)) {
//            val path = getIssuesXmlPath()
//            val file = File(path)
//            try {
//                writeTo(file)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//        }
    }

    /*fun getIssueUrl(issue: Issue): String {
        return YoutrackSession(myProject).getUrl() + "/issue/" + issue.getId()
    }*/


    override fun getComponentName(): String = javaClass.simpleName

}