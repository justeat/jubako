package com.justeat.jubako.platform

import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.SimpleJubakoAssembler

class TestConditionalContentAssembler(
    private val showDo: Boolean = true,
    private val showRe: Boolean = true,
    private val showMi: Boolean = true
) : SimpleJubakoAssembler() {
    override fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) = with(list) {
        if (showDo) add(
            TestContentDescriptionProvider(
                data = "Do"
            )
        )
        if (showRe) add(
            TestContentDescriptionProvider(
                data = "Re"
            )
        )
        if (showMi) add(
            TestContentDescriptionProvider(
                data = "Mi"
            )
        )
    }
}
