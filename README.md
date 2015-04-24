# LifeOrganizer

LifeOrganizer is a task management program inspired by [todo.txt](http://todotxt.com/). It stores its data in an (almost) human-readable form, 
with some modifications to allow for greater flexibility. The greatest motivator for making this program was that I could not find any task
management that allowed for tasks to be failed. So, I decided to go ahead and create a full task manager just to be able to mark a task as
failed when needed.

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
	2. A relative time shift from today. This is done by typing "+" followed by a numeral, followed by **d**,__w__,**m** or __y__, for day, week, month and year, 
	respectively
	3. Either "tod" or "tom", for quick insertion of quick tasks.
	
	All of these formats can be followed by a time, if desired. This is done by adding "@" right after the end of the date, followed by the time in
	the standard *hh***:***mm* format.
	
Even though all of this can be done at the time of creating the task, you can also just provide the name of the task and fill out the data that
you want in the user interface.

`code segment`