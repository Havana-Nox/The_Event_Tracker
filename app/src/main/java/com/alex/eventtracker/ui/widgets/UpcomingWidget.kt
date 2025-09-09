package com.alex.eventtracker.ui.widgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.alex.eventtracker.MainActivity
import com.alex.eventtracker.data.database.AppDatabase
import com.alex.eventtracker.data.model.Event
import com.alex.eventtracker.ui.theme.SoftTransparentBlack
import com.alex.eventtracker.ui.theme.ThreeDaysOrange
import com.alex.eventtracker.ui.theme.TodayRed
import com.alex.eventtracker.ui.theme.TomorrowRed
import com.alex.eventtracker.ui.theme.TwoDaysOrange
import com.alex.eventtracker.utils.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout


class UpcomingWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context.applicationContext)
        val eventDao = database.eventDao()

        // Get events from repository
        val events = try {
            withTimeout(2000) { // Prevent hanging
                eventDao.getAllEvents().first().sortedBy { event ->
                    DateUtils.calculateDaysUntilNextEvent(event)
                }.take(4) // Only show next 4 events
            }
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                WidgetContent(events)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun WidgetContent(events: List<Event>) {
        val context = LocalContext.current
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(SoftTransparentBlack)
                .clickable {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    pendingIntent.send()
                }
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                events.forEach { event ->
                    EventRow(event)
                    Spacer(modifier = GlanceModifier.height(1.dp))
                }
                if (events.isEmpty()) {
                    Box(
                        modifier = GlanceModifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming events",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }


    @SuppressLint("RestrictedApi")
    @Composable
    private fun EventRow(event: Event) {
        val daysUntil = DateUtils.calculateDaysUntilNextEvent(event)
        val age = DateUtils.calculateAge(event)
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // Name
            Text(
                text = event.name.take(30) + if (event.name.length > 16) "..." else "",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,

                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(1.dp))
            // Age or placeholder
            Text(
                text = age?.toString() ?: "--",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = GlanceModifier.width(24.dp)
            )
            Spacer(modifier = GlanceModifier.width(1.dp))
            // Days until
            Box(
                modifier = GlanceModifier.width(70.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (daysUntil == 0L) "Today" else if (daysUntil == 1L) "Tommorow" else "$daysUntil days",
                    style = TextStyle(
                        color = when (daysUntil) {
                            0L -> ColorProvider(TodayRed)
                            1L -> ColorProvider(TomorrowRed)
                            2L -> ColorProvider(TwoDaysOrange)
                            3L -> ColorProvider(ThreeDaysOrange)
                            else -> ColorProvider(Color.White)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }
}