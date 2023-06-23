package common.communication;

/**
 * Enum of response codes
 */
public enum ResponseCode {
    OK,
    CLIENT_EXIT,
    SERVER_EXIT,
    ERROR;
    public static ResponseCode outOfString(String arg) {
        for (ResponseCode x : new ResponseCode[]{ResponseCode.OK, ResponseCode.ERROR}) {
            if (x.toString().equals(arg)) {
                return x;
            }
        }
        return null;
    }
}