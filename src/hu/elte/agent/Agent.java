package hu.elte.agent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
    private Faction faction;
    private Map<String, Faction> knownNames;

    public Agent(Faction faction, int serialNumber, List<String> names, String msg) {
        this.faction = faction;
        this.serialNumber = serialNumber;
        this.names = names;
        this.msg = msg;
        this.knownNames = new HashMap<>();
    }

    public Faction getFaction() {
        return faction;
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

        System.out.println(this + " started on port: " + server.getLocalPort());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // TODO: try with resources
        Runnable accept = () -> {
            try {
                server.setSoTimeout(TIMEOUT_UPPER);
                Socket client = server.accept();

                // successfull connection
                System.out.println(this + " accepted another agent on port: " + server.getLocalPort());

                Scanner in = new Scanner(client.getInputStream());
                PrintWriter out = new PrintWriter(client.getOutputStream());

                String randomName = names.get(generateRandomIntInRange(0, names.size()));
                write(out, randomName);

                System.out.println(this + " sent '" + randomName + "' on port: " + server.getLocalPort());

                Faction guessedFaction = Faction.getFactionByName(in.nextLine());

                System.out.println(this + " received '" + guessedFaction.getName() + "' as guess on port: " + server.getLocalPort());

                if (!guessedFaction.equals(faction)) {
                    client.close();
                } else {
                    write(out, "OK");
                }

            } catch (IOException ignored) {
            }
        };

        Runnable connect = () -> {
            try {
                int randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
                while (randomPort == server.getLocalPort()) {
                    randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
                }

                TimeUnit.MILLISECONDS.sleep(generateRandomIntInRange(TIMEOUT_LOWER, TIMEOUT_UPPER));
                Socket server = new Socket("localhost", randomPort);

                System.out.println(this + " connected to an agent on port: " + server.getPort());

                Scanner in = new Scanner(server.getInputStream());
                PrintWriter out = new PrintWriter(server.getOutputStream());

                String receivedRandomName = in.nextLine();

                System.out.println(this + " received '" + receivedRandomName + "' on port: " + server.getPort());

                Faction guessedFaction = guessFaction(receivedRandomName);
                write(out, guessedFaction.getName());

                System.out.println(this + " guessed server's faction as '" + guessedFaction.getName() + "' on port: " + server.getPort());

                String confirmationMessage = in.nextLine();

                if (confirmationMessage.equals("OK")) {
                    knownNames.put(receivedRandomName, guessedFaction);
                }

                System.out.println(this + " knows " + knownNames);
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

    private Faction guessFaction(String msg) {
        Faction faction = knownNames.get(msg);
        if (faction == null) {
            int guess = generateRandomIntInRange(1, 2);
            faction = guess == 1 ? Faction.CIA : Faction.KGB;
        }
        return faction;
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

    private static void write(PrintWriter pw, String message) {
        pw.println(message);
        pw.flush();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.names.forEach(name -> sb.append(name).append(" "));
        sb.append("from ").append(this.faction.getName());
        return sb.toString();
    }

}
