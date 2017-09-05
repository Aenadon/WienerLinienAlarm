package aenadon.wienerlinienalarm.exceptions;

public class NetworkClientException extends Exception {

    private static final long serialVersionUID = 4787896566751405515L;

    public NetworkClientException() {
    }

    public NetworkClientException(String message) {
        super(message);
    }

    public NetworkClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkClientException(Throwable cause) {
        super(cause);
    }

}
