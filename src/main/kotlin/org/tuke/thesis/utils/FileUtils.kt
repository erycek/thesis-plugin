package org.tuke.thesis.utils

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.json.JSONObject
import org.tuke.thesis.ConsoleManager
import org.tuke.thesis.dto.ErrorDto

fun traverseProjectFiles(project: Project) {
    val errors = mutableListOf<ErrorDto>()
    ProjectFileIndex.getInstance(project).iterateContent {
        if (
            it.isFile.not() ||
            it.path.contains("/node_modules/") ||
            it.path.contains("/build/") ||
            it.path.contains("/dist/") ||
            it.path.contains("/target/") ||
            it.path.contains("/.next/")
        ) return@iterateContent true

        if (it.extension == "tsx" || it.extension == "ts" || it.extension == "js" || it.extension == "jsx") {
            val psiManager = PsiManager.getInstance(project)
            val psiFile = psiManager.findFile(it) ?: return@iterateContent true
            errors.addAll(processFile(psiFile))
        } else if (it.name == "package.json") {
            val psiManager = PsiManager.getInstance(project)
            val psiFile = psiManager.findFile(it) ?: return@iterateContent true
            val content = String(it.contentsToByteArray())
            errors.addAll(checkForSecurityIssuesInPackages(content, psiFile))
        }
        return@iterateContent true
    }

    errors.forEach {
        logError(it)
    }
}

fun logError(error: ErrorDto) {
    val filePath = error.file.virtualFile.path
    val consoleMessage = "$filePath:${error.line} ERROR: ${error.message}"
    ConsoleManager.log(consoleMessage, ConsoleViewContentType.ERROR_OUTPUT)
}

fun processFile(file: PsiFile): List<ErrorDto> {
    val content = String(file.virtualFile.contentsToByteArray())
    return testForPatterns(file, content)
}

fun transformPackageJson(packageJson: String): String {
    val json = JSONObject(packageJson)

    val transformedJson = JSONObject()
    transformedJson.put("name", json.getString("name"))
    transformedJson.put("version", json.getString("version"))

    val dependencies = json.getJSONObject("dependencies")
    val requires = JSONObject()
    val dependenciesObj = JSONObject()

    val dependenciesKeys = dependencies.keys()
    while (dependenciesKeys.hasNext()) {
        val key = dependenciesKeys.next()
        val version = dependencies.getString(key)
        requires.put(key, version)
        val depObj = JSONObject()
        depObj.put("version", version)
        dependenciesObj.put(key, depObj)
    }

    transformedJson.put("requires", requires)
    transformedJson.put("dependencies", dependenciesObj)

    return transformedJson.toString()
}