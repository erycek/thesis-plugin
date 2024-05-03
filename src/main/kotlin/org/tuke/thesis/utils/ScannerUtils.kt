package org.tuke.thesis.utils

import com.intellij.psi.PsiFile
import org.json.JSONException
import org.json.JSONObject
import org.tuke.thesis.dto.ErrorDto
import org.tuke.thesis.enum.Severity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

val evalPattern = Regex("""eval\(""")
val documentWritePattern = Regex("""document.write\(""")
val documentWriteLnPattern = Regex("""document.writeln\(""")
val innerHtmlPattern = Regex("""innerHTML\s*=\s*["'][^"']*<\s*script\b[^>]*>.*["']""")
val outerHtmlPattern = Regex("""outerHTML\s*=\s*["'][^"']*<\s*script\b[^>]*>.*["']""")
val eventHandlerPattern = Regex("""on\w+\s*=\s*["'][^"']*<\s*script\b[^>]*>.*["']""")
val urlScriptPattern = Regex("""<\s*\w+\s+[^>]*\w+\s*=\s*["'][^"']*javascript:.*["'][^>]*>""")
val attributeValuePattern = Regex("""<\s*\w+\s+[^>]*\w+\s*=\s*["'][^"']*javascript:.*["'][^>]*>""")
val scriptPattern = Regex("""<\s*script\b[^>]*>.*<\s*/\s*script\s*>""")

fun testForPatterns(file: PsiFile, fileContent: String): List<ErrorDto> {
    val output = mutableListOf<ErrorDto>()
    output.addAll(testPattern(evalPattern, fileContent, Severity.ERROR, "eval() calls", file))
    output.addAll(
        testPattern(
            documentWritePattern, fileContent, Severity.ERROR, "document.write() calls", file
        )
    )
    output.addAll(
        testPattern(
            documentWriteLnPattern, fileContent, Severity.ERROR, "document.writeln() calls", file
        )
    )
    output.addAll(testPattern(innerHtmlPattern, fileContent, Severity.ERROR, "innerHTML assignments", file))
    output.addAll(testPattern(outerHtmlPattern, fileContent, Severity.ERROR, "outerHTML assignments", file))
    output.addAll(testPattern(scriptPattern, fileContent, Severity.WARNING, "Script tags", file))
    output.addAll(
        testPattern(
            eventHandlerPattern, fileContent, Severity.ERROR, "JavaScript Event Handlers in HTML", file
        )
    )
    output.addAll(
        testPattern(
            urlScriptPattern, fileContent, Severity.ERROR, "JavaScript Execution in URLs", file
        )
    )
    output.addAll(
        testPattern(
            attributeValuePattern, fileContent, Severity.ERROR, "JavaScript Execution in Attribute Values", file
        )
    )
    return output
}

fun checkForSecurityIssuesInPackages(fileContent: String, file: PsiFile): List<ErrorDto> {
    run {
        val output = mutableListOf<ErrorDto>()
        val url = URL("https://registry.npmjs.org/-/npm/v1/security/audits")
        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            val transformedJson = try {
                transformPackageJson(fileContent)
            } catch (e: JSONException) {
                return mutableListOf()
            }
            val wr = OutputStreamWriter(outputStream)
            wr.write(transformedJson)
            wr.flush()

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }

                val json = JSONObject(response.toString())
                val advisories = json.getJSONObject("advisories")
                val keys = advisories.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val advisory = advisories.getJSONObject(key)
                    val title = advisory.getString("title")
                    val moduleName = advisory.getString("module_name")
                    output.add(
                        ErrorDto(Severity.ERROR, title, "PACKAGE: $moduleName", file, 0)
                    )
                }

                return output
            }
        }
    }
}

fun testPattern(
    pattern: Regex, example: String, severity: Severity, patternName: String, file: PsiFile
): MutableList<ErrorDto> {
    val output = mutableListOf<ErrorDto>()
    example.lines().forEachIndexed { index, line ->
        if (pattern.containsMatchIn(line)) {
            output.add(
                ErrorDto(severity, patternName, "Line $index: $line", file, index + 1)
            )
        }
    }
    return output
}
