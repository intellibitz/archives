package cc.thevar.blukit.di

import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.data.local.EchoLedger
import cc.thevar.blukit.data.power.HarmonyManager
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.repository.IdentityRepositoryImpl
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.logic.AtmosphereManager
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.NearbyResonanceController
import cc.thevar.blukit.network.p2p.ResonanceController
import cc.thevar.blukit.ui.viewmodels.BluetoothViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.SupremePowerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Infrastructure
    single { CryptoManager() }
    single { EchoLedger(androidContext(), get()) }
    single { RadioStateManager(androidContext()) }
    single { SpreadPermissionManager(androidContext()) }
    single { HapticManager(androidContext()) }
    
    // Scopes
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    
    // Repositories
    single<IdentityRepository> { IdentityRepositoryImpl(androidContext()) }
    single { ContactRepository(get()) }
    
    // Controllers
    single<ResonanceController> { 
        NearbyResonanceController(
            context = androidContext(),
            repository = get(),
            echoLedger = get(),
            hapticManager = get(),
            radioStateManager = get(),
            cryptoManager = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
    
    // Use Cases & Managers
    single { HarmonyManager(get(), get(), get(), get()) }
    single { ConnectivityUseCase(get(), get(), get(), get()) }
    single(createdAtStart = true) { AtmosphereManager(androidContext(), get(), get()) }
    
    // ViewModels
    viewModelOf(::MainViewModel)
    viewModelOf(::BluetoothViewModel)
    viewModelOf(::HarmonyViewModel)
}
