package cc.thevar.blukit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cc.thevar.blukit.BlukitApplication

class ViewModelFactory(private val application: BlukitApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    application.identityRepository,
                    application.vibeStore
                ) as T
            }
            modelClass.isAssignableFrom(BluetoothViewModel::class.java) -> {
                BluetoothViewModel(
                    application.p2pController,
                    application.radioStateManager,
                    application.identityRepository,
                    application.spreadPermissionManager,
                    application.vibeStore
                ) as T
            }
            modelClass.isAssignableFrom(SupremePowerViewModel::class.java) -> {
                SupremePowerViewModel(
                    application.supremePowerManager
                ) as T
            }
            modelClass.isAssignableFrom(ContactsViewModel::class.java) -> {
                ContactsViewModel(
                    application.contactRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
