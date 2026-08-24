package luke.commands;

import java.util.EnumMap;

import luke.Luke;
import luke.exceptions.InvalidArgumentException;
import luke.exceptions.InvalidFlagException;
import luke.exceptions.UserInputException;
import luke.tasks.Flag;
import luke.tasks.TaskType;

/**
 * Creates a task of one configured {@link TaskType}. The same command class is
 * reused for todos, deadlines, and events because they share the same add flow:
 * validate the description, validate flags, add to the list, and show feedback.
 */
public class AddTaskCommands implements Command {
    private final TaskType tasktype;

    /**
     * Creates an add-task command for the given task type.
     *
     * @param tasktype the task type this command should create
     */
    public AddTaskCommands(TaskType tasktype) {
        this.tasktype = tasktype;
    }

    /**
     * Adds a task after checking that the description is present, all required
     * flags are supplied, and no unsupported flags were provided.
     *
     * @param bot the chatbot, used for output and access to the task list
     * @param argument the task description before the first flag
     * @param flags parsed flag values from the user input
     * @throws UserInputException if the description or flags are invalid
     */
    @Override
    public void execute(
            Luke bot,
            String argument,
            EnumMap<Flag, String> flags) throws UserInputException {
        if (argument.isBlank()) {
            throw InvalidArgumentException.missingDescription(tasktype.toString().toLowerCase());
        }
        
        for (Flag flag : flags.keySet()) {
            if (!tasktype.getFlags().contains(flag)) {
                throw InvalidFlagException.unsupported(flag.name().toLowerCase());
            }
        }

        for (Flag requiredFlag : tasktype.getFlags()) {
            if (!flags.containsKey(requiredFlag)) {
                throw InvalidFlagException.missingRequired(requiredFlag.name().toLowerCase());
            }
        }

        bot.items().add(argument, tasktype, flags);
        int size = bot.items().size();
        bot.say("Got it. I've added this task:\n"
                + "  %s\n".formatted(bot.items().format_one_item(size - 1))
                + "Now you have %d tasks in the list.".formatted(size)
        );
    }
}
