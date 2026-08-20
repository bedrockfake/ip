# Luke User Guide

Luke is a command-line chatbot for tracking tasks. It stores tasks only for the current run of the program; when the program exits, the list is not saved.

## Quick Start

Run Luke from the project root:

```bash
sh run.sh
```

You will see a prompt:

```text
>
```

Type one command per line. Use `bye` to exit.

## Command Summary

| Command | Format | Purpose |
| --- | --- | --- |
| `todo` | `todo DESCRIPTION` | Adds a todo task. |
| `deadline` | `deadline DESCRIPTION /by TIME` | Adds a deadline task. |
| `event` | `event DESCRIPTION /from START /to END` | Adds an event task. |
| `list` | `list` | Shows all tasks. |
| `mark` | `mark INDEX` | Marks a task as done. |
| `unmark` | `unmark INDEX` | Marks a task as not done. |
| `delete` | `delete INDEX` | Deletes a task. |
| `bye` | `bye` | Exits Luke. |

`DESCRIPTION`, `TIME`, `START`, and `END` can contain spaces. For example, `return book`, `next Sunday`, and `Mon 2pm` are all valid values.

## Adding Todos

Use `todo` followed by the task description:

```text
todo read book
```

Expected output:

```text
Got it. I've added this task:
  [T][ ] read book
Now you have 1 tasks in the list.
```

Todos do not accept flags. For example, `todo read book /by Sunday` is rejected because `/by` belongs to deadlines.

## Adding Deadlines

Use `deadline`, a description, and the required `/by` flag:

```text
deadline return book /by Sunday
```

Expected output:

```text
Got it. I've added this task:
  [D][ ] return book (by: Sunday)
Now you have 1 tasks in the list.
```

The description must appear before `/by`. This is invalid because the description is missing:

```text
deadline /by Sunday
```

## Adding Events

Use `event`, a description, and both required flags: `/from` and `/to`.

```text
event project meeting /from Mon 2pm /to 4pm
```

Expected output:

```text
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 1 tasks in the list.
```

Both `/from` and `/to` are required. Event flags can contain spaces in their values.

## Listing Tasks

Use `list` with no extra arguments:

```text
list
```

Example output:

```text
1. [T][ ] read book
2. [D][ ] return book (by: Sunday)
3. [E][ ] project meeting (from: Mon 2pm to: 4pm)
```

Task display format:

```text
[TYPE][STATUS] DESCRIPTION (flags)
```

Types:

| Symbol | Meaning |
| --- | --- |
| `T` | Todo |
| `D` | Deadline |
| `E` | Event |

Statuses:

| Symbol | Meaning |
| --- | --- |
| blank space | Not done |
| `X` | Done |

## Marking and Unmarking Tasks

Use the number shown by `list`.

```text
mark 1
```

Expected output:

```text
Nice! I've marked this task as done:
 [T][X] read book
```

Use `unmark` to mark it as not done:

```text
unmark 1
```

Expected output:

```text
OK! I've marked this task as not done yet:
 [T][ ] read book
```

Indexes are 1-based, so `1` means the first task in the list.

## Deleting Tasks

Use `delete` with the number shown by `list`.

```text
delete 1
```

Expected output:

```text
Noted. I've removed this task:
 [T][ ] read book
Now you have 0 tasks in the list.
```

After a task is deleted, the remaining tasks are renumbered the next time you
use `list`.

## Exiting

Use `bye` with no extra arguments:

```text
bye
```

Expected output:

```text
Bye. Hope to see you again soon!
```

## Input Validation

Luke reports user input problems as normal chatbot errors. Common examples:

| Input | Error |
| --- | --- |
| `list something` | `` `list` command does not take arguments: something `` |
| `bye later` | `` `bye` command does not take arguments: later `` |
| `todo` | `Missing description for todo task.` |
| `deadline return book` | `Missing required flag: /by` |
| `deadline /by Sunday` | `Missing description for deadline task.` |
| `deadline return book /by Sunday /by Monday` | `Duplicate flag: /by` |
| `deadline return book /er idk` | `Unidentified flag: /er` |
| `todo read book /by Sunday` | `Unsupported flag: /by` |
| `mark banana` | `` `mark` command received invalid index: banana `` |
| `mark 999` | `` `mark` command received out-of-bounds index: 999 `` |
| `delete 999` | `` `delete` command received out-of-bounds index: 999 `` |

## Developer Notes

### Source Layout

Main source files are in `src/main/java`.

Important classes:

| File | Purpose |
| --- | --- |
| `Luke.java` | Reads user input, parses commands and flags, and displays errors. |
| `Commands.java` | Defines non-task commands such as `list`, `mark`, `unmark`, `delete`, and `bye`. |
| `TaskTypes.java` | Defines task-creation keywords and their required flags. |
| `AddTaskCommands.java` | Validates and adds todo, deadline, and event tasks. |
| `ItemList.java` | Stores tasks and formats them for display. |
| `exceptions/` | Contains grouped user-input exceptions. |

### Tests

Tests are in `src/test/java/LukeTest.java`. They run the full command-line program with scripted input and check the printed output.

Run tests from the project root:

```bash
sh test.sh
```

The tests are dependency-free, so no Gradle, Maven, or JUnit setup is required.
