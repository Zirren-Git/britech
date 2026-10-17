package com.dayflow.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); val dao=PlannerDatabase.get(this).dao(); setContent { DayFlow(dao) } } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DayFlow(dao:PlannerDao){
 val tasks by dao.tasks().collectAsState(initial=emptyList()); val habits by dao.habits().collectAsState(initial=emptyList()); val notes by dao.notes().collectAsState(initial=emptyList()); val goals by dao.goals().collectAsState(initial=emptyList()); val diary by dao.diary().collectAsState(initial=emptyList()); val checks by dao.checkins().collectAsState(initial=emptyList())
 var page by remember{mutableStateOf("Today")}; var dialog by remember{mutableStateOf("")}; var input by remember{mutableStateOf("")}; var dark by remember{mutableStateOf(false)}; var search by remember{mutableStateOf("")}; var showDone by remember{mutableStateOf(false)}
 val scope= rememberCoroutineScope(); val today=LocalDate.now().toString(); val todays=tasks.filter{it.due==today}; val done=todays.count{it.done}; val color=MaterialTheme.colorScheme
 MaterialTheme(colorScheme=if(dark) darkColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF9DD6C8)) else lightColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF177D68),secondary=androidx.compose.ui.graphics.Color(0xFFFFB86C))){
 Scaffold(topBar={TopAppBar(title={Column{Text("DayFlow",fontWeight=FontWeight.Bold);Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),style=MaterialTheme.typography.labelMedium)}},actions={IconButton(onClick={page="Search"}){Icon(Icons.Default.Search,"Search")}})}, floatingActionButton={FloatingActionButton(onClick={dialog="Add"}){Icon(Icons.Default.Add,"Add")}}, bottomBar={NavigationBar{listOf("Today","Calendar","Habits","Notes","More").forEach { val icon=when(it){"Today"->Icons.Default.Home;"Calendar"->Icons.Default.DateRange;"Habits"->Icons.Default.CheckCircle;"Notes"->Icons.Default.Description;else->Icons.Default.Menu}; NavigationBarItem(selected=page==it,onClick={page=it},icon={Icon(icon,it)},label={Text(it)}) } }}){ pad->
 Column(Modifier.padding(pad).fillMaxSize().padding(horizontal=18.dp)) {
 when(page){
 "Today"->{ Spacer(Modifier.height(14.dp)); Text("A little progress goes a long way.",style=MaterialTheme.typography.titleMedium); Spacer(Modifier.height(16.dp)); Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("TODAY'S PROGRESS",style=MaterialTheme.typography.labelMedium);Text("$done of ${todays.size} tasks done",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}; Box(Modifier.size(58.dp).border(5.dp,MaterialTheme.colorScheme.primary,CircleShape),contentAlignment=Alignment.Center){Text("${if(todays.isEmpty())0 else done*100/todays.size}%")}}}; Heading("Today's tasks"){dialog="Task"}; if(todays.isEmpty()) Empty("Your day is open","Add a task to get started."); todays.forEach{TaskRow(it,{scope.launch{dao.updateTask(it.copy(done=!it.done))}},{scope.launch{dao.deleteTask(it)}})}; Heading("Habits to check in"){page="Habits"}; habits.forEach{h->val checked=checks.any{it.habitId==h.id&&it.date==today}; Row(Modifier.fillMaxWidth().clickable{if(!checked)scope.launch{dao.checkin(HabitCheckIn(habitId=h.id))}}.padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(checked,{if(!checked)scope.launch{dao.checkin(HabitCheckIn(habitId=h.id))}});Text(h.title,Modifier.weight(1f));Text(if(checked)"Done" else "Check in",color=MaterialTheme.colorScheme.primary)} } }
 "Calendar"->{var selected by remember{mutableStateOf(today)}; Row(verticalAlignment=Alignment.CenterVertically){Text("Your calendar",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));TextButton(onClick={selected=today}){Text("Today")}}; Text("Choose a date to see its plans",color=MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Row(Modifier.horizontalScroll(rememberScrollState())){(0..13).forEach{off->val d=LocalDate.now().plusDays(off.toLong());FilterChip(selected=selected==d.toString(),onClick={selected=d.toString()},label={Text(d.format(DateTimeFormatter.ofPattern("EEE d")))})}}; Heading("${selected}"){dialog="Task"}; val items=tasks.filter{it.due==selected};if(items.isEmpty())Empty("Nothing planned","Add a task for this day.");items.forEach{TaskRow(it,{scope.launch{dao.updateTask(it.copy(done=!it.done))}},{scope.launch{dao.deleteTask(it)}})} }
 "Habits"->{
  Text("Build small, steady routines",style=MaterialTheme.typography.titleMedium)
  Heading("Your habits"){dialog="Habit"}
  if(habits.isEmpty()) Empty("Start a habit","Pick one small action and make it part of your day.")
  habits.forEach { h ->
   val count=checks.count { it.habitId==h.id }
   val checked=checks.any { it.habitId==h.id && it.date==today }
   Card(Modifier.fillMaxWidth().padding(vertical=5.dp)) {
    Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
     Column(Modifier.weight(1f)) { Text(h.title,fontWeight=FontWeight.SemiBold); Text("$count check-ins · daily",style=MaterialTheme.typography.bodySmall) }
     Button(onClick={ if(!checked) scope.launch { dao.checkin(HabitCheckIn(habitId=h.id)) } }) { Text(if(checked) "Done ✓" else "Check in") }
    }
   }
  }
  TextButton(onClick={dialog="Habit library"}) { Text("Browse habit ideas") }
 }
 "Notes"->{Heading("Notes & reflections"){dialog="Note"}; if(notes.isEmpty())Empty("A place for your thoughts","Capture an idea, list, or reminder.");notes.forEach{n->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(14.dp)){Text(n.title,fontWeight=FontWeight.Bold);Text(n.body,maxLines=4);if(n.tags.isNotBlank())Text(n.tags,style=MaterialTheme.typography.labelSmall)}}}; Heading("Diary · ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}"){dialog="Diary"}; diary.firstOrNull{it.date==today}?.let{Text("${listOf("😞","🙁","😐","🙂","😄").getOrElse(it.mood){"😐"}}  ${it.text}")} ?: Text("Write a short daily reflection.") }
 "More"->{Text("More",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);listOf("Diary","Goals","Stats","Settings").forEach{ElevatedButton(onClick={page=it},modifier=Modifier.fillMaxWidth().padding(vertical=5.dp)){Text(it)}}}
 "Diary"->{Heading("Daily diary"){dialog="Diary"};diary.forEach{Text("${it.date}  ${listOf("😞","🙁","😐","🙂","😄").getOrElse(it.mood){"😐"}}\n${it.text}",Modifier.padding(12.dp))}}
 "Goals"->{Heading("Goals"){dialog="Goal"};if(goals.isEmpty())Empty("Set a direction","Create a goal and track progress at your own pace.");goals.forEach{g->Column(Modifier.fillMaxWidth().padding(10.dp)){Text(g.title,fontWeight=FontWeight.Bold);Slider(value=g.progress.toFloat(),onValueChange={v->scope.launch{dao.updateGoal(g.copy(progress=v.toInt()))}},valueRange=0f..100f);Text("${g.progress}% complete")}}}
 "Stats"->{Text("Your week",style=MaterialTheme.typography.headlineSmall);Text("${tasks.count{it.done}} tasks completed · ${checks.size} habit check-ins · ${diary.size} diary entries",Modifier.padding(vertical=12.dp))}
 "Settings"->{Text("Settings",style=MaterialTheme.typography.headlineSmall);Row(verticalAlignment=Alignment.CenterVertically){Text("Dark theme",Modifier.weight(1f));Switch(dark,{dark=it})};Text("Your data stays on this device.",Modifier.padding(vertical=12.dp));TextButton(onClick={scope.launch{dao.clearTasks();dao.clearHabits();dao.clearNotes();dao.clearGoals();dao.clearDiary();dao.clearChecks()}}){Text("Delete all data")};Text("Export and import are not yet available.",style=MaterialTheme.typography.bodySmall)}
 "Search"->{OutlinedTextField(search,{search=it},label={Text("Search tasks and notes")},modifier=Modifier.fillMaxWidth());tasks.filter{it.title.contains(search,true)}.forEach{TaskRow(it,{scope.launch{dao.updateTask(it.copy(done=!it.done))}},{scope.launch{dao.deleteTask(it)}})};notes.filter{it.title.contains(search,true)||it.body.contains(search,true)}.forEach{Text(it.title+" — "+it.body,Modifier.padding(8.dp))}}
 }
 }
 } }
 if(dialog.isNotBlank()) AlertDialog(onDismissRequest={dialog=""},title={Text(if(dialog=="Add")"Quick add" else dialog)},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){if(dialog=="Add")listOf("Task","Habit","Note","Diary").forEach{TextButton(onClick={dialog=it}){Text(it)}}else if(dialog=="Habit library") listOf("Drink water","Exercise","Meditate","Read","Stretch","Walk","Journal","Sleep well","Eat fruit","Practice gratitude","Learn something","Call a friend","Tidy up","Take vitamins","Breathe deeply","Plan tomorrow","Spend time outside","Create","No screen time","Care for plants").forEach{TextButton(onClick={scope.launch{dao.habit(Habit(title=it))};dialog=""}){Text(it)}} else {OutlinedTextField(input,{input=it},label={Text(when(dialog){"Diary"->"Reflection";"Habit"->"Habit name";"Note"->"Title";"Goal"->"Goal title";else->"Task title"})});if(dialog=="Task")Text("Due today · medium priority",style=MaterialTheme.typography.bodySmall);if(dialog=="Diary")Text("Mood: tap a face in order from low to great") } }},confirmButton={if(dialog!="Add"&&dialog!="Habit library")TextButton(onClick={scope.launch{when(dialog){"Task"->if(input.isNotBlank())dao.task(Task(title=input));"Habit"->if(input.isNotBlank())dao.habit(Habit(title=input));"Note"->if(input.isNotBlank())dao.note(Note(title=input));"Goal"->if(input.isNotBlank())dao.goal(Goal(title=input));"Diary"->dao.diary(DiaryEntry(mood=3,text=input))}};input="";dialog=""}){Text("Save")}},dismissButton={TextButton(onClick={dialog=""}){Text("Cancel")}})
}

@Composable private fun Heading(text:String,onClick:()->Unit){Row(Modifier.fillMaxWidth().padding(top=18.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically){Text(text,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);TextButton(onClick=onClick){Text("Add")}}}
@Composable private fun Empty(title:String,sub:String){Column(Modifier.fillMaxWidth().padding(vertical=22.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(title,fontWeight=FontWeight.SemiBold);Text(sub,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable private fun TaskRow(task:Task,onToggle:()->Unit,onDelete:()->Unit){var ask by remember{mutableStateOf(false)};Row(Modifier.fillMaxWidth().padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(task.done,{onToggle()});Column(Modifier.weight(1f).clickable{onToggle()}){Text(task.title,fontWeight=FontWeight.Medium);Text("${task.due} · ${listOf("Low","Medium","High").getOrElse(task.priority){"Medium"}}${if(task.tag.isNotBlank())" · ${task.tag}" else ""}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton(onClick={ask=true}){Icon(Icons.Default.Delete,"Delete task")}};if(ask)AlertDialog(onDismissRequest={ask=false},title={Text("Delete task?")},text={Text(task.title)},confirmButton={TextButton(onClick={onDelete();ask=false}){Text("Delete")}},dismissButton={TextButton(onClick={ask=false}){Text("Cancel")}})} }
