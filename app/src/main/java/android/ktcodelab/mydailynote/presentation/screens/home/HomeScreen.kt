package android.ktcodelab.mydailynote.presentation.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.ktcodelab.mydailynote.R
import android.ktcodelab.mydailynote.admob.AdMobAds
import android.ktcodelab.mydailynote.data.repository.Notes
import android.ktcodelab.mydailynote.data.repository.RequestState
import android.ktcodelab.mydailynote.pref.ModeViewModel
import android.ktcodelab.mydailynote.pref.UserPref
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.ZonedDateTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    notes: Notes,
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit,
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    userPref: UserPref,
    modeViewModel: ModeViewModel
) {

    //STATEs
    var padding by remember {
        mutableStateOf(PaddingValues())
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior() //For Sticky Header


    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllClicked = onDeleteAllClicked
    ) {

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {

                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    onMenuClicked = onMenuClicked,
                    dateIsSelected = dateIsSelected,
                    onDateSelected = onDateSelected,
                    onDateReset = onDateReset,
                    userPref = userPref,
                    modeViewModel = modeViewModel
                )
            },
            bottomBar = {
               AdMobAds()
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(
                            end = padding.calculateEndPadding(LayoutDirection.Ltr)
                        ),
                    onClick = navigateToWrite
                ) {

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            },
            content = {

                padding = it
                when (notes) {

                    is RequestState.Success -> {

                        HomeContent(
                            paddingValues = it,
                            dailyNotes = notes.data,
                            onClick = navigateToWriteWithArgs
                        )
                    }
                    is RequestState.Error -> {

                        EmptyPage(
                            image = painterResource(id = R.drawable.ic_error),
                            title = "${notes.error.message}"
                        )
                    }
                    is RequestState.Loading -> {

                        Box(Modifier.fillMaxSize(), Alignment.Center) {

                            CircularProgressIndicator(color = Color.Gray)
                        }
                    }
                    else -> {}
                }
            }
        )
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    content: @Composable () -> Unit
) {

    val currentUser = Firebase.auth.currentUser

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(

                content = {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        Arrangement.Center,
                        Alignment.CenterHorizontally
                    ) {

                        Image(
                            modifier = Modifier.padding(top = 18.dp, bottom = 12.dp),
                            painter = painterResource(id = R.drawable.ic_diary_logo),
                            contentDescription = null,
                        )

                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = stringResource(R.string.app_name),
                            fontSize = 34.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily(Font(R.font.bulletto_killa))
                        )

                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp)
                                .background(Color.LightGray)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Bottom
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(start = 24.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = currentUser?.photoUrl.toString(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                placeholder = painterResource(id = R.drawable.ic_avatar)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = currentUser?.displayName.toString(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                )
                                Text(
                                    text = currentUser?.email.toString(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        NavigationDrawerItem(
                            label = {

                                Row(Modifier.padding(horizontal = 12.dp)) {

                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = stringResource(R.string.sign_out),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            selected = false,
                            onClick = onSignOutClicked
                        )

                        NavigationDrawerItem(
                            label = {

                                Row(Modifier.padding(horizontal = 12.dp)) {

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete All Logo"
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = "Delete All Notes",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            selected = false,
                            onClick = onDeleteAllClicked
                        )

                    }

                }
            )
        },
        content = content
    )
}