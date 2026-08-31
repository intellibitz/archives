package cc.thevar.blukit.di

import cc.thevar.blukit.data.crypto.CryptoManager
import cc.thevar.blukit.domain.security.SecureSessionManager
import cc.thevar.blukit.data.local.MessageRepository
import cc.thevar.blukit.data.local.BlukitDatabase
import androidx.room3.Room
import cc.thevar.blukit.domain.usecase.ConsensusUseCase
import cc.thevar.blukit.domain.usecase.RitualUseCase
import cc.thevar.blukit.data.power.HarmonyManager
import cc.thevar.blukit.data.repository.ContactRepository
import cc.thevar.blukit.data.repository.IdentityRepository
import cc.thevar.blukit.data.repository.IdentityRepositoryImpl
import cc.thevar.blukit.data.system.HapticManager
import cc.thevar.blukit.data.system.RadioStateManager
import cc.thevar.blukit.data.system.SpreadPermissionManager
import cc.thevar.blukit.domain.logic.AssistantManager
import cc.thevar.blukit.domain.logic.AutonomousManager
import cc.thevar.blukit.domain.usecase.ConnectivityUseCase
import cc.thevar.blukit.network.p2p.BleConnectionController
import cc.thevar.blukit.network.p2p.P2pConnectionController
import cc.thevar.blukit.network.p2p.ConnectionController
import cc.thevar.blukit.ui.viewmodels.ConnectionViewModel
import cc.thevar.blukit.ui.viewmodels.MainViewModel
import cc.thevar.blukit.ui.viewmodels.HarmonyViewModel
import cc.thevar.blukit.ui.viewmodels.NavigationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Infrastructure
    single { CryptoManager() }
    single { SecureSessionManager(get()) }
    single<BlukitDatabase> {
        val dbFile = androidContext().getDatabasePath("blukit.db")
        Room.databaseBuilder<BlukitDatabase>(
            context = androidContext(),
            name = dbFile.absolutePath
        ).build()
    }
    single { get<BlukitDatabase>().messageDao() }
    single { get<BlukitDatabase>().groupDao() }
    single { get<BlukitDatabase>().contactDao() }
    
    single { MessageRepository(androidContext(), get(), get(), get(), get()) }
    single { RadioStateManager(androidContext()) }
    single { SpreadPermissionManager(androidContext()) }
    single { HapticManager(androidContext()) }
    
    // Scopes
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    
    // Repositories
    single<IdentityRepository> { IdentityRepositoryImpl(androidContext()) }
    single { ContactRepository(get()) }
    
    // Controllers
    single<ConnectionController> { 
        P2pConnectionController(
            context = androidContext(),
            repository = get(),
            messageLedger = get(),
            hapticManager = get(),
            radioStateManager = get(),
            cryptoManager = get(),
            sessionManager = get(),
            ioDispatcher = Dispatchers.IO,
        )
    }
    
    // Use Cases & Managers
    single { ConsensusUseCase(
        upsertMessage = { msg -> get<MessageRepository>().upsertMessage(msg) },
        deviceIdProvider = { get<IdentityRepository>().getDeviceId() },
        nicknameProvider = { get<IdentityRepository>().getCurrentNickname() }
    ) }
    single { RitualUseCase(
        getGroupMembers = { gid -> get<MessageRepository>().getGroup(gid)?.allMemberIds ?: emptySet() },
        sendMessage = { content, receiverId, groupId -> 
            get<ConnectionController>().sendMessage(content, receiverId, groupId = groupId)
        }
    ) }

    single { HarmonyManager(get(), get(), get(), get()) }
    single { ConnectivityUseCase(get(), get(), get(), get()) }
    single(createdAtStart = true) { AssistantManager(androidContext(), get(), get()) }
    single(createdAtStart = true) { AutonomousManager(androidContext(), get(), get()) }
    
    // ViewModels
    viewModelOf(::MainViewModel)
    viewModelOf(::ConnectionViewModel)
    viewModelOf(::HarmonyViewModel)
    viewModelOf(::NavigationViewModel)
}
