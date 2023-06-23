package common.communication;

import java.io.Serializable;

/**
 * Class for get request value.
 */
public class Request implements Serializable {
    private String commandName;
    private String commandStringArgument;
    private Serializable commandObjectArgument;
    private User user;

    public Request(String commandName, String commandStringArgument, Serializable commandObjectArgument, User user) {
        this.commandName = commandName;
        this.commandStringArgument = commandStringArgument;
        this.commandObjectArgument = commandObjectArgument;
        this.user = user;
    }

    public Request(String commandName, String commandStringArgument, User user) {
        this(commandName, commandStringArgument, null, user);
    }

    public Request(User user) {
        this("", "", user);
    }
    public Request() {
        this("", "",null);
    }

    /**
     * @return Command name.
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * @return Command string argument.
     */
    public String getCommandStringArgument() {
        return commandStringArgument;
    }

    /**
     * @return Command object argument.
     */
    public Object getCommandObjectArgument() {
        return commandObjectArgument;
    }
    public User getUser() {
        return user;
    }
    /**
     * @return Is this request empty.
     */
    public boolean isEmpty() {
        return commandName.isEmpty() && commandStringArgument.isEmpty() && commandObjectArgument == null;
    }
    @Override
    public String toString() {
        return "Request[" + commandName + ", " + commandStringArgument + ", " + commandObjectArgument + "]";
    }

    public String getObjectArgumentString(){
        if (commandName.equals("add") || commandName.equals("update") ||
                commandName.equals("add_if_min") || commandName.equals("remove_lower")) {
            LabWorkForRequest raw = (LabWorkForRequest) commandObjectArgument;
            return raw.toString();
        } else if (commandName.equals("remove_all_by_author")) {
            AuthorForRequest raw = (AuthorForRequest) commandObjectArgument;
            return raw.toString();
        }
        return null;
    };
    public byte[] getBytes() {
        return (commandName + "~~~" + commandStringArgument + "~~~" + getObjectArgumentString() + "~~~" + user.toString()).getBytes();
    }
    public static Request outOfString(String arg) {
        String[] request = arg.split("~~~");
        switch (request[0]) {
            case ("add"):
            case ("add_if_min"):
            case ("remove_lower"):
                return new Request(request[0], "", LabWorkForRequest.outOfString(request[2]), User.outOfString(request[3]));
            case ("update"):
                return new Request(request[0], request[1], LabWorkForRequest.outOfString(request[2]), User.outOfString(request[3]));
            case ("remove_all_by_author"):
                return new Request(request[0], "", AuthorForRequest.outOfString(request[2]), User.outOfString(request[3]));
            case ("remove_by_id"):
            case ("execute_script"):
            case ("count_less_than_minimal_point"):
            case ("filter_contains_name"):
                return new Request(request[0], request[1], null, User.outOfString(request[3]));
            case ("login"):
            case("register"):
                return new Request(request[0],"",User.outOfString(request[3]));
            default:
                return new Request(request[0], "", null, User.outOfString(request[3]));
        }
    }
    /*public Request getData(String[] request) {
        return getDataStrategy.getData(request);
    }

    public void setGetDataStrategy(GetDataStrategy getDataStrategy) {
        this.getDataStrategy = getDataStrategy;
    }*/
}