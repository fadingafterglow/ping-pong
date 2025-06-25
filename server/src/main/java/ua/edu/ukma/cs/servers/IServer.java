package ua.edu.ukma.cs.servers;

import java.io.IOException;

public interface IServer {

    void start() throws IOException;

    void stop() throws IOException;
}
