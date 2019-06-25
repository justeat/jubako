package com.justeat.jubako.platform

import com.justeat.jubako.ContentDescriptionProvider
import com.justeat.jubako.SimpleJubakoAssembler

class TestContentAssembler : SimpleJubakoAssembler() {
    override fun onAssemble(list: MutableList<ContentDescriptionProvider<Any>>) = with(list) {
        add(TestContentDescriptionProvider("Do"))
        add(TestContentDescriptionProvider("Ray", ID_ROW_RAY))
        add(TestContentDescriptionProvider("Mi"))
        add(TestContentDescriptionProvider("Fa"))
        add(TestContentDescriptionProvider("So"))
        add(TestContentDescriptionProvider("La"))
        add(TestContentDescriptionProvider("Ti"))
        add(TestContentDescriptionProvider("Da"))
        add(TestContentDescriptionProvider("Do"))
        add(TestContentDescriptionProvider("Ray"))
        add(TestContentDescriptionProvider("Mi"))
        add(TestContentDescriptionProvider("Fa"))
        add(TestContentDescriptionProvider("So"))
        add(TestContentDescriptionProvider("La"))
        add(TestContentDescriptionProvider("Ti"))
        add(TestContentDescriptionProvider("Da"))
        add(TestContentDescriptionProvider("Do"))
        add(TestContentDescriptionProvider("Ray"))
        add(TestContentDescriptionProvider("Mi"))
        add(TestContentDescriptionProvider("Fa"))
        add(TestContentDescriptionProvider("So"))
        add(TestContentDescriptionProvider("La"))
        add(TestContentDescriptionProvider("Ti"))
        add(TestContentDescriptionProvider("Da"))
        add(TestContentDescriptionProvider("Do"))
        add(TestContentDescriptionProvider("Ray"))
        add(TestContentDescriptionProvider("Mi"))
        add(TestContentDescriptionProvider("Fa"))
        add(TestContentDescriptionProvider("So"))
        add(TestContentDescriptionProvider("La"))
        add(TestContentDescriptionProvider("Ti"))
        add(TestContentDescriptionProvider("Da"))
    }

    companion object {
        const val ID_ROW_RAY = "Unique-Id-For-First-Row-With-Ray"
    }
}
