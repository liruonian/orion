package liruonian.orion.example;

import java.util.concurrent.CountDownLatch;

import liruonian.orion.Server;
import liruonian.orion.core.StandardServer;
import liruonian.orion.lifecycle.LifecycleException;

public class ExampleServer {
    private Server server;
    private CountDownLatch latch = new CountDownLatch(1);

    public ExampleServer() {
    }

    public static void main(String[] args) throws LifecycleException {
        new ExampleServer().start();
    }

    public void start() throws LifecycleException {
        server = new StandardServer();
        try {
            server.start();
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    public void stop() throws LifecycleException {
        try {
            server.stop();
        } finally {
            latch.countDown();
        }
    }
}
