package org.tuke.thesis.dto

import com.intellij.psi.PsiFile
import org.tuke.thesis.enum.Severity

class ErrorDto(
    val severity: Severity,
    val message: String,
    val stackTrace: String,
    val file: PsiFile,
    val line: Int,
)