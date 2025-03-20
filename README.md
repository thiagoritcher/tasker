# Tasker

Simple file based task manager. 

## Why

I needed a simple way to create files to be used as tasks descriptions and git commit messages.


Now after creating a task, I can set it to be the current (task current number) and do 
    
    git commit -F ~/tasks/.task/current

and my commit message is done. 


I can also use the task text as a template with 

    git commit -t ~/task/.task/current.

