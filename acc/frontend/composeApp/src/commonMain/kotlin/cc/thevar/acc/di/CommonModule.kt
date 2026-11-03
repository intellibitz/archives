package cc.thevar.acc.di

import cc.thevar.acc.ui.AgentViewModel
import cc.thevar.acc.ui.ConsoleViewModel
import cc.thevar.acc.ui.SystemViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun commonModule() = module {
    viewModelOf(::AgentViewModel)
    viewModelOf(::SystemViewModel)
    viewModelOf(::ConsoleViewModel)
}
