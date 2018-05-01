package hu.elte.agent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static hu.elte.agent.AgentMain.*;

public class Agent extends Thread {

    private int serialNumber;
    private List<String> names;
    private String msg;
    private ServerSocket server;

    public Agent(int serialNumber, List<String> names, String msg) {
        this.serialNumber = serialNumber;
        this.names = names;
        this.msg = msg;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private void createServerSocket() {
        // Try again until free port is found
        while (true) {
            try {
                this.server = new ServerSocket(generateRandomIntInRange(PORT_LOWER, PORT_UPPER));
                return;
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        createServerSocket();

        System.out.println(this + "started on port: " + server.getLocalPort());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable accept = () -> {
            try {
                server.setSoTimeout(TIMEOUT_UPPER);
                server.accept();
                System.out.println(this + "accepted another agent on port: " + server.getLocalPort());
            } catch (IOException ignored) {
            }
        };

        Runnable connect = () -> {
            try {
                int randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
                while (randomPort == this.server.getLocalPort()) {
                    randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
                }

                TimeUnit.MILLISECONDS.sleep(generateRandomIntInRange(TIMEOUT_LOWER, TIMEOUT_UPPER));
                Socket socket = new Socket("localhost", generateRandomIntInRange(PORT_LOWER, PORT_UPPER));
                System.out.println(this + "connected to an agent on port: " + socket.getPort());
            } catch (InterruptedException | IOException ignored) {
            }
        };

        while (true) {
            executor.submit(accept);
            executor.submit(connect);
        }

//        executor.submit(accept);
//        executor.submit(connect);
//
//        shutDownExecutor(executor);
    }

    private void shutDownExecutor(ExecutorService executor) {
        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }
    }

    private int generateRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.names.forEach(name -> sb.append(name).append(" "));
        return sb.toString();
    }

}
