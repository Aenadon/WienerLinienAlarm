package aenadon.wienerlinienalarm.exceptions;

public class NetworkServerException extends Exception {

    private static final long serialVersionUID = 4575280229286046242L;

    public NetworkServerException() {
    }

    public NetworkServerException(int errorCode) {
        super("Error code: " + errorCode);
    }

    public NetworkServerException(String message) {
        super(message);
    }

    public NetworkServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkServerException(Throwable cause) {
        super(cause);
    }

}
