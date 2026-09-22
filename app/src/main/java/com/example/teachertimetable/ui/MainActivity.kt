package com.example.teachertimetable.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.teachertimetable.ui.course.EditCourseScreen
import com.example.teachertimetable.ui.memo.EditMemoScreen
import com.example.teachertimetable.ui.memo.ImageEditorScreen
import com.example.teachertimetable.ui.memo.MemoScreen
import com.example.teachertimetable.ui.settings.SettingsScreen
import com.example.teachertimetable.ui.theme.TeacherTimetableTheme
import com.example.teachertimetable.ui.timetable.TimetableScreen

class MainActivity : ComponentActivity() {

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestExactAlarmIfNeeded()

        setContent {
            TeacherTimetableTheme {
                AppRoot()
            }
        }
    }

    private fun requestExactAlarmIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                runCatching {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val vm: AppViewModel = viewModel()

    val tabs = listOf(
        Triple("timetable", "课表", Icons.Filled.DateRange),
        Triple("memo", "备忘", Icons.Filled.EditNote),
        Triple("settings", "设置", Icons.Filled.Settings)
    )

    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
        ?.substringBefore("?")?.substringBefore("/")
    val showBar = tabs.any { it.first == current }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                nav.navigate(route) {
                                    popUpTo("timetable") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "timetable",
            modifier = Modifier.padding(padding)
        ) {
            composable("timetable") {
                TimetableScreen(
                    vm = vm,
                    onAddCourse = { nav.navigate("course?id=-1") },
                    onEditCourse = { id -> nav.navigate("course?id=" + id) },
                    onOpenMemo = { cls ->
                        nav.navigate("memoEdit?className=" + Uri.encode(cls))
                    }
                )
            }

            composable("memo") {
                MemoScreen(
                    vm = vm,
                    onEditMemo = { id -> nav.navigate("memoEdit?id=" + id) },
                    onNewClassMemo = { cls ->
                        nav.navigate("memoEdit?className=" + Uri.encode(cls))
                    }
                )
            }

            composable("settings") {
                SettingsScreen(vm = vm)
            }

            composable(
                "course?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType; defaultValue = -1L
                })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: -1L
                EditCourseScreen(
                    vm = vm,
                    courseId = id,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                "memoEdit?id={id}&className={cls}&courseId={cid}&week={w}&day={d}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("cls") { type = NavType.StringType; defaultValue = "" },
                    navArgument("cid") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("w") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("d") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { entry ->
                val a = entry.arguments
                EditMemoScreen(
                    vm = vm,
                    memoId = a?.getLong("id") ?: -1L,
                    defaultClassName = a?.getString("cls").orEmpty(),
                    defaultCourseId = (a?.getLong("cid") ?: -1L).takeIf { it > 0 },
                    defaultWeek = (a?.getInt("w") ?: -1).takeIf { it > 0 },
                    defaultDay = (a?.getInt("d") ?: -1).takeIf { it > 0 },
                    savedStateHandle = entry.savedStateHandle,
                    onEditImage = { path ->
                        nav.navigate("imageEdit?path=" + Uri.encode(path))
                    },
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                "imageEdit?path={path}",
                arguments = listOf(navArgument("path") { type = NavType.StringType })
            ) { entry ->
                val path = Uri.decode(entry.arguments?.getString("path").orEmpty())
                ImageEditorScreen(
                    sourcePath = path,
                    onSaved = { newPath ->
                        nav.previousBackStackEntry?.savedStateHandle
                            ?.set("edited_image", newPath)
                        nav.popBackStack()
                    },
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
