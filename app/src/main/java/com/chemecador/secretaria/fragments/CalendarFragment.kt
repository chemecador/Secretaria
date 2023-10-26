package com.chemecador.secretaria.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.chemecador.secretaria.R
import com.chemecador.secretaria.adapters.TaskAdapter
import com.chemecador.secretaria.databinding.CalendarDayLayoutBinding
import com.chemecador.secretaria.databinding.FragmentCalendarBinding
import com.chemecador.secretaria.interfaces.OnItemClickListener
import com.chemecador.secretaria.items.Task
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class CalendarFragment : Fragment(), OnItemClickListener {
    private lateinit var binding: FragmentCalendarBinding
    private var btnDay: Button? = null
    private lateinit var taskList: MutableList<Task>
    private var taskAdapter: TaskAdapter? = null
    private lateinit var ctx: Context
    private lateinit var selectedDay: LocalDateTime


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMonth = YearMonth.now()
        val firstMonth = currentMonth.minusMonths(1)
        val lastMonth = currentMonth.plusMonths(1)

        val daysOfWeek = daysOfWeek()
        calendarView = binding.calendarView
        calendarView.setup(firstMonth, lastMonth, java.time.DayOfWeek.SUNDAY)
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()
            }
        }
        val startMonth = currentMonth.minusMonths(100)  // Adjust as needed
        val endMonth = currentMonth.plusMonths(100)  // Adjust as needed
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.scrollToMonth(currentMonth)

        val titlesContainer = view.findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

        calendarView.monthHeaderBinder = object :
            MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                // Remember that the header is reused so this will be called for each month.
                // However, the first day of the week will not change so no need to bind
                // the same view every time it is reused.
                if (container.titlesContainer.tag == null) {
                    container.titlesContainer.tag = data.yearMonth
                    container.titlesContainer.children.map { it as TextView }
                        .forEachIndexed { index, textView ->
                            val dayOfWeek = daysOfWeek[index]
                            val title =
                                dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            textView.text = title
                            // In the code above, we use the same `daysOfWeek` list
                            // that was created when we set up the calendar.
                            // However, we can also get the `daysOfWeek` list from the month data:
                            // val daysOfWeek = data.weekDays.first().map { it.date.dayOfWeek }
                            // Alternatively, you can get the value for this specific index:
                            // val dayOfWeek = data.weekDays.first()[index].date.dayOfWeek
                        }
                }
            }
        }
    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        // Alternatively, you can add an ID to the container layout and use findViewById()
        val titlesContainer = view as ViewGroup
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
        lateinit var day: CalendarDay
        private var selectedDate: LocalDate? = null

        init {
            view.setOnClickListener {
                // Check the day position as we do not want to select in or out dates.
                if (day.position == DayPosition.MonthDate) {
                    // Keep a reference to any previous selection
                    // in case we overwrite it and need to reload it.
                    val currentSelection = selectedDate
                    if (currentSelection == day.date) {
                        // If the user clicks the same date, clear selection.
                        selectedDate = null
                        // Reload this date so the dayBinder is called
                        // and we can REMOVE the selection background.
                        calendarView.notifyDateChanged(currentSelection)
                    } else {
                        selectedDate = day.date
                        // Reload the newly selected date so the dayBinder is
                        // called and we can ADD the selection background.
                        calendarView.notifyDateChanged(day.date)
                        if (currentSelection != null) {
                            // We need to also reload the previously selected
                            // date so we can REMOVE the selection background.
                            calendarView.notifyDateChanged(currentSelection)
                        }
                    }
                }
            }
            calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: CalendarDay) {
                    container.day = data
                    val day = data
                    val textView = container.textView
                    textView.text = day.date.dayOfMonth.toString()
                    if (day.position == DayPosition.MonthDate) {
                        // Show the month dates. Remember that views are reused!
                        textView.visibility = View.VISIBLE
                        if (day.date == selectedDate) {
                            // If this is the selected date, show a round background and change the text color.
                            textView.setTextColor(Color.YELLOW)
                            //textView.setBackgroundResource(R.drawable.selection_background)
                        } else {
                            // If this is NOT the selected date, remove the background and reset the text color.
                            textView.setTextColor(Color.BLACK)
                            textView.background = null
                        }
                    } else {
                        // Hide in and out dates
                        textView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    companion object {
        lateinit var calendarView: CalendarView
    }

}