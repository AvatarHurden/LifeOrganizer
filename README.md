# LifeOrganizer

LifeOrganizer is a task management program inspired by [todo.txt](http://todotxt.com/). It stores its data in an (almost) human-readable 
[format](#the-format), with some modifications to allow for greater flexibility. The greatest motivator for making this program was that I could not 
find any task management that allowed for tasks to be failed. So, I decided to go ahead and create a full task manager just to be able to mark a
task as failed when needed.

Besides what is supported by **todo.txt**, LifeOrganizer also provides built-in due dates and notes (and failing tasks, of course).

It is intended to be complete usable using only a keyboard, but it also has a simple and easy to use graphical interface.

### Task Creation

To create a task, simply type what you want to do and press enter. You can set every parameter of you task in this initial insertion, by following
these rules:

- To insert a priority, start your task with a letter enclosed in parenthesis e.g. "(A)"
- To insert a project, simply start a word with a "+" sign and it will be removed from the name and added as a project
- Similarly with contexts, but the symbol used is "@"
- To insert a due date, you must precede the date with "due=". Following this, you have many choices:
	1. Insert a direct date. You can provide only the day, the day and month or the complete date.
	2. A relative time shift from today. This is done by typing "+" followed by a numeral, followed by **d**,__w__,**m** or __y__, for day, week, 
	month and year, respectively
	3. Either "tod" or "tom", for quick insertion of quick tasks.
	
	All of these formats can be followed by a time, if desired. This is done by adding "@" right after the end of the date, followed by the time in
the standard *hh:mm* format.
	
Even though all of this can be done at the time of creating the task, you can also just provide the name of the task and fill out the data that
you want in the user interface.

# The Format

Although I based the format on Gina Trapani's todo.txt, I took some liberties in extending it to allow for more powerful 
features at the cost of human legibility. Despite this, the file is non proprietary pure text and is easy to reimplement in 
another program. Below is a full explanation of how it works

- Every line in the file is a todo item, so every item is separated by a new line.The last item can be ended either with a 
new line or the end of the file.
- A task with every field filled out will look something like this:

	`[ ] (A) DUE=2015.04.27 MADE=2015.04.20@09:20 NAME="My first todo task" PROJS=+GetOrganized,+LiveFully CONTEXTS=@Home NOTE="keep working" EDIT=2015.04.21@13:20`
	
I will now go into the details of every part of this format. This is mainly for those who wish to use this format somewhere else.

Every one of the many parts of a task must be separated by a space from the others. Except for the first and last, the order in which any part appears is
irrelevant, but this order was chosen as to allow for a simple alphabetical ordering to provide the user with a meaningful order of tasks (highest
priority first, followed by closest due dates, etc.)
	
1. The first 3 characters of a line indicate whether the task is pending, done or failed.
	- *[ ]* indicates the task is pending
	- *[x]* indicates the task is done
	- *[-]* indicates the task is failed
	
	This must always be the first part of a line. If it is not present, the line is not counted as a task.

2. *(Optional)* Following the state, a single uppercase letter enclosed in parenthesis indicates the priority of a task.

3. *(Optional)* For due dates, the string **"DUE="** must be followed by a date in the format _yyyy.mm.dd_. You can also
provide a time for tasks that have a specific end time. This is done by adding "@hh.mm" after the date.

4. The creation date is always necessary for a task. It is identified by the string that follows **"MADE"**, and is in
the same format as the due date, but the time is mandatory.

5. The name of a task is determined by the string contained inside double quotes following **"NAME="**. If the name
includes double quotes, these will be escaped.

6. *(Optional)* The projects of a task are given as a comma separated list of words following the string **"PROJS="**.
Every project must start with the "+" sign and cannot have any whitespace.

7. *(Optional)* The contexts of a task are very similar to the projects, except they come after the string **"CONTEXTS="**
and must start with a "@" symbol.

8. *(Optional)* The note of a task follows the same rules as the name, with double quotes and new lines being escaped, and
its end being given by the first unescaped double quote. It is identified by the string **"NOTE"**.

9. The last part of a task is the edit date. This indicates the last time that any field of the task was modified, and
follows the same patterns as the creation date. It is preceded by the string **"EDIT="** and followed by a new line or
end of file.

I believe this is sufficient information for anyone to reuse this format in other applications, but feel free to contact
me if anything is not clear.
	