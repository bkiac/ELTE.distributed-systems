package hu.elte.agent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static hu.elte.agent.AgentMain.*;

public class Agent extends Thread {

    private int serialNumber;
    private List<String> names;
    private List<String> knownSecrets;
    private List<String> toldSecrets;
    private List<String> enemySecrets;
    private ServerSocket server;
    private Faction faction;
    private Map<String, Faction> knownNames;
    private boolean isArrested;

    public Agent(Faction faction, int serialNumber, List<String> names, List<String> secrets) {
        this.faction = faction;
        this.serialNumber = serialNumber;
        this.names = names;
        this.knownSecrets = secrets;
        this.knownNames = new HashMap<>();
        this.toldSecrets = new ArrayList<>();
        this.enemySecrets = new ArrayList<>();
        this.isArrested = false;
    }

    private void createServerSocket() {
        // Try again until free port is found
        while (true) {
            try {
                this.server = new ServerSocket(generateRandomIntInRange(PORT_LOWER, PORT_UPPER));
                server.setSoTimeout(TIMEOUT_UPPER);
                return;
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        createServerSocket();

        System.out.println(this + " started on port: " + this.server.getLocalPort());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable server = () -> {
            try (
                    Socket agent = this.server.accept();
                    Scanner in = new Scanner(agent.getInputStream());
                    PrintWriter out = new PrintWriter(agent.getOutputStream())
            ) {
                System.out.println(this + " accepted another agent on port: " + this.server.getLocalPort());

                String randomName = names.get(generateRandomIntInRange(0, names.size()));
                write(out, randomName);

                System.out.println(this + " sent '" + randomName + "' on port: " + this.server.getLocalPort());

                Faction guessedFaction = Faction.getFactionByName(in.nextLine());

                System.out.println(this + " received '" + guessedFaction.getName() + "' as guess on port: " + this.server.getLocalPort());

                if (!guessedFaction.equals(faction)) {
                    return;
                } else {
                    write(out, "OK");
                }

                if (in.nextLine().equals("OK")) {
                    addSecretToList(in.nextLine(), knownSecrets);
                    write(out, getRandomSecretFromList(knownSecrets));
                } else { // in.nextLine() == "???"
                    if (Integer.parseInt(in.nextLine()) == this.serialNumber) {
                        write(out, tellRandomSecret());
                        if (AgentUtil.listEqualsIgnoreOrder(knownSecrets, toldSecrets)) {
                            this.isArrested = true;
                            return;
                        }
                    } else {
                        return;
                    }
                }

                System.out.println(this + " knows " + knownSecrets);
            } catch (IOException ignored) {
            }
        };

        Runnable client = () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(generateRandomIntInRange(TIMEOUT_LOWER, TIMEOUT_UPPER));
            } catch (InterruptedException ignored) {
            }

            try (
                    Socket agent = new Socket(HOST, findRandomPort());
                    Scanner in = new Scanner(agent.getInputStream());
                    PrintWriter out = new PrintWriter(agent.getOutputStream())
            ) {
                System.out.println(this + " connected to an agent on port: " + agent.getPort());

                String receivedRandomName = in.nextLine();

                System.out.println(this + " received '" + receivedRandomName + "' on port: " + agent.getPort());

                Faction guessedFaction = guessFaction(receivedRandomName);
                write(out, guessedFaction.getName());

                System.out.println(this + " guessed server's faction as '" + guessedFaction.getName() + "' on port: " + agent.getPort());

                if (in.nextLine().equals("OK")) {
                    knownNames.put(receivedRandomName, guessedFaction);
                }

                if (this.faction.equals(guessedFaction)) {
                    write(out, "OK");

                    write(out, getRandomSecretFromList(knownSecrets));
                    addSecretToList(in.nextLine(), knownSecrets);

                } else {
                    write(out, "???");

                    write(out, String.valueOf(generateRandomIntInRange(1, 5))); // TODO: check if they've already met????, range to guess in????
                    addSecretToList(in.nextLine(), knownSecrets);
                }

                System.out.println(this + " knows " + knownSecrets);
            } catch (IOException ignored) {
            }
        };

        while (!this.isArrested) {
            executor.submit(server);
            executor.submit(client);
        }

        System.out.println(this + " has been arrested. Stopping activity.");
        shutDownExecutor(executor);
    }

    private Faction guessFaction(String msg) {
        Faction faction = knownNames.get(msg);
        if (faction == null) {
            int guess = generateRandomIntInRange(1, 2);
            faction = guess == 1 ? Faction.CIA : Faction.KGB;
        }
        return faction;
    }

    private int findRandomPort() {
        int randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
        while (randomPort == server.getLocalPort()) {
            randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
        }
        return randomPort;
    }

    private synchronized void addSecretToList(String secret, List<String> secrets) {
        if (!secrets.contains(secret)) {
            secrets.add(secret);
        }
    }

    private synchronized String getRandomSecretFromList(List<String> secrets) {
        return secrets.get(generateRandomIntInRange(0, secrets.size()));
    }

    private synchronized String tellRandomSecret() {
        List<String> untoldSecrets = new ArrayList<>(this.knownSecrets);
        untoldSecrets.removeAll(this.toldSecrets);

        String secret = untoldSecrets.get(generateRandomIntInRange(0, untoldSecrets.size()));
        this.toldSecrets.add(secret);

        return secret;
    }

    private void shutDownExecutor(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
//            System.err.println("tasks interrupted");
        } finally {
            if (!executor.isTerminated()) {
//                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
//            System.out.println("shutdown finished");
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
