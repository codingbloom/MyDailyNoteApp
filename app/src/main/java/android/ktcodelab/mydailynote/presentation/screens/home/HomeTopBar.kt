package android.ktcodelab.mydailynote.presentation.screens.home

import android.ktcodelab.mydailynote.R
import android.ktcodelab.mydailynote.pref.ModeViewModel
import android.ktcodelab.mydailynote.pref.UserPref
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@ExperimentalMaterial3Api
@Composable
fun HomeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onMenuClicked: () -> Unit,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit,
    userPref: UserPref,
    modeViewModel: ModeViewModel
) {

    //STATEs
    val dateDialog = rememberSheetState()
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }

    var onModeClick by remember {

        modeViewModel.isDarkModeEnabled
    }
    val scope = rememberCoroutineScope()


    /*--------------------TOP BAR------------------*/
    TopAppBar(
        scrollBehavior = scrollBehavior, //For Sticky Header
        navigationIcon = {
            IconButton(onClick = onMenuClicked) {

                Icon(imageVector = Icons.Default.Menu, contentDescription = "Hamburger Menu Icon")
            }
        },
        title = {
            Text(text = "Note")
        },
        actions = {

            IconButton(onClick = {
                onModeClick = !onModeClick
                scope.launch { userPref.saveMode(onModeClick) }
            } ) {

                Icon(
                    painter = painterResource(id = R.drawable.ic_dark_mode),
                    contentDescription = "Mode Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }


            if (dateIsSelected) {

                IconButton(onClick = onDateReset ) {

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {

                IconButton(onClick = { dateDialog.show() }) {

                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date Icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
    
    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date { localDate ->

            pickedDate = localDate

            onDateSelected(

                ZonedDateTime.of(

                    pickedDate,
                    LocalTime.now(),
                    ZoneId.systemDefault()
                )
            )
        },

        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )
}
