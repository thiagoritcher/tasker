# Tasker

Simple file based task manager. 

## Installation

Requires Babashka or Clojure

Update the DIR directory inside task script 

Copy the task script to the PATH


## Use

Inside project execute
    
    task init

This will create .task dir structure inside project

Executing task will show options

To select a task to work on 

    task current <filter>

Then use the tas as commit message

    git commit -F .task/current


## Why

I needed a simple way to create files to be used as tasks descriptions and git commit messages.


Now after creating a task, I can set it to be the current (task current number) and do 
    
    git commit -F ~/tasks/.task/current

and my commit message is done. 


I can also use the task text as a template with 

    git commit -t ~/task/.task/current

