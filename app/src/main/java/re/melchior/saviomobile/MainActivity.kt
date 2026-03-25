package re.melchior.saviomobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import re.melchior.saviomobile.data.local.database.TokenDataStore
import re.melchior.saviomobile.ui.navigation.AppNavigation
import re.melchior.saviomobile.ui.theme.SavioTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenDataStore: TokenDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavioTheme {
                AppNavigation(tokenDataStore = tokenDataStore)
            }
        }
    }
}