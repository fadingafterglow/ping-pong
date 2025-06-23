package ua.edu.ukma.cs.api.routing.exceptions;

public class NotRegisteredHttpMethod extends Exception {
    public NotRegisteredHttpMethod(String method) {
        super("Not registered HTTP method: " + method);
    }
}
