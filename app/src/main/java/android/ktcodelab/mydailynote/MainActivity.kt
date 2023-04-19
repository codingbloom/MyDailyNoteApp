package android.ktcodelab.mydailynote

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.ktcodelab.mydailynote.data.database.ImageToDeleteDao
import android.ktcodelab.mydailynote.data.database.ImageToUploadDao
import android.ktcodelab.mydailynote.navigation.Screen
import android.ktcodelab.mydailynote.navigation.SetupNavGraph
import android.ktcodelab.mydailynote.pref.ModeViewModel
import android.ktcodelab.mydailynote.pref.UserPref
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.ktcodelab.mydailynote.ui.theme.MyDailyNoteTheme
import android.ktcodelab.mydailynote.util.Constants.APP_ID
import android.ktcodelab.mydailynote.util.retryDeletingImageFromFirebase
import android.ktcodelab.mydailynote.util.retryUploadingImageToFirebase
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.*
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao

    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao

    var keepSplashOpened = true

    private val modeViewModel: ModeViewModel by viewModels()
    private lateinit var userPref: UserPref


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        //Splash
        installSplashScreen().setKeepOnScreenCondition {

            keepSplashOpened
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        userPref = UserPref(applicationContext)

        val systemMode =
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    true
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    false
                }
                else -> {
                    false
                }
            }

        /*--------------------------SetContent----------------------------*/
        setContent {

            val mode = userPref.getMode(systemMode).collectAsState(initial = systemMode)

            MyDailyNoteTheme(darkTheme = mode.value, dynamicColor = false) {

                val navController = rememberNavController()

                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {

                        keepSplashOpened = false
                    },
                    userPref = userPref,
                    modeViewModel = modeViewModel
                )
            }
        }


        val firebaseRemoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

        val remoteConfigSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1
        }
        firebaseRemoteConfig.setConfigSettingsAsync(remoteConfigSettings)
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                val newVersionCode = firebaseRemoteConfig.getString("new_version_Code")

                if (newVersionCode.toInt() > getCurrentVersionCode(this)) displayUpdateDialog(this)
            }
        }

        cleanupCheck(
            scope = lifecycleScope,
            imageToUploadDao = imageToUploadDao,
            imageToDeleteDao = imageToDeleteDao
        )
    }
}


@Suppress("DEPRECATION")
private fun getCurrentVersionCode(context: Context): Int {

    val packageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(context.packageName, 0)

    val versionName = packageInfo.versionName

    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            packageInfo.longVersionCode.toInt()

        } else {
            packageInfo.versionCode
        }
    return versionCode
}

private fun displayUpdateDialog(context: Context) {

    AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_diary_logo)
        .setTitle(context.getString(R.string.new_update_title))
        .setMessage(context.getString(R.string.new_update_message))
        .setCancelable(false)
        .setPositiveButton(HtmlCompat.fromHtml("<h3> Update Now </h3>", HtmlCompat.FROM_HTML_MODE_LEGACY)) { dialog, i ->
            try {
                (context as Activity).finish()
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.goto_playstore))
                    )
                )
            } catch (e: Exception) {
                Log.d("TAG", "showUpdateDialogBox: Something went wrong...")
            }
        }.show()
}

private fun cleanupCheck(

    scope: CoroutineScope,
    imageToUploadDao: ImageToUploadDao,
    imageToDeleteDao: ImageToDeleteDao
) {

    scope.launch(Dispatchers.IO) {

        val result = imageToUploadDao.getAllImages()

        result.forEach { imageToUpload ->

            retryUploadingImageToFirebase(

                imageToUpload = imageToUpload,
                onSuccess = {

                    scope.launch(Dispatchers.IO) {

                        imageToUploadDao.cleanupImage(imageId = imageToUpload.id)
                    }
                }
            )
        }

        val result2 = imageToDeleteDao.getAllImages()

        result2.forEach { imageToDelete ->

            retryDeletingImageFromFirebase(

                imageToDelete = imageToDelete,
                onSuccess = {

                    scope.launch(Dispatchers.IO) {

                        imageToDeleteDao.cleanupImage(imageId = imageToDelete.id)
                    }
                }
            )
        }
    }
}

private fun getStartDestination(): String {

    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route else Screen.Authentication.route
}
