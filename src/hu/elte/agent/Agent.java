package hu.elte.agent;

import hu.elte.agent.util.AgentUtil;
import hu.elte.agent.util.Faction;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static hu.elte.agent.AgentMain.*;

public class Agent extends Thread {

    private Faction faction;
    private int serialNumber;
    private List<String> names;
    private List<String> knownSecrets;

    private ServerSocket server;
    private List<String> toldSecrets = new ArrayList<>();
    private Map<String, Faction> knownNames = new HashMap<>();
    private Map<String, Integer> knownAgentIds = new HashMap<>();
    private Map<String, List<Integer>> alreadyTriedIds = new HashMap<>();
    private boolean isArrested = false;

    public Agent() {
    }

    public Agent(Faction faction, int serialNumber, List<String> names, List<String> secrets) {
        this.faction = faction;
        this.serialNumber = serialNumber;
        this.names = names;
        this.knownSecrets = secrets;
    }

    public List<String> getKnownSecrets() {
        return knownSecrets;
    }

    public boolean isArrested() {
        return isArrested;
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

        Runnable server = () -> {
            while (!this.isArrested && !isGameOver()) {
//                printThreads();

                try (
                        Socket agent = this.server.accept();
                        Scanner in = new Scanner(agent.getInputStream());
                        PrintWriter out = new PrintWriter(agent.getOutputStream())
                ) {
                    agent.setSoTimeout(TIMEOUT_LOWER);

                    System.out.println(this + " accepted another agent on port: " + this.server.getLocalPort());

                    String randomName = names.get(generateRandomIntInRange(0, names.size() - 1));
                    write(out, randomName);

                    System.out.println(this + " sent '" + randomName + "' on port: " + this.server.getLocalPort());

                    Faction guessedFaction = Faction.getFactionByName(in.nextLine());

                    System.out.println(this + " received '" + guessedFaction.getName() + "' as guess on port: " + this.server.getLocalPort());

                    if (!guessedFaction.equals(faction)) {
                        write(out, "DISCONNECTED");
                        continue;
                    } else {
                        write(out, "OK");
                    }

                    if (in.nextLine().equals("OK")) {
                        addSecretToList(in.nextLine(), this.knownSecrets);
                        write(out, getRandomSecretFromList(this.knownSecrets));
                    } else { // in.nextLine() == "???"
                        int guessedId = Integer.parseInt(in.nextLine());

                        System.out.println(this + " received '" + guessedId + "' as guess on port: " + this.server.getLocalPort());

                        if (guessedId == this.serialNumber) {
                            write(out, tellRandomSecret());

                            if (AgentUtil.listEqualsIgnoreOrder(this.knownSecrets, this.toldSecrets)) {
                                this.isArrested = true;
                            }
                        } else {
                            write(out, "DISCONNECTED");
                        }
                    }

//                    System.out.println(this + " knows " + knownSecrets);
                } catch (IOException | NoSuchElementException ignored) {
                    // The other agent has abruptly disconnected.
                }
            }
        };

        Runnable client = () -> {
            while (!this.isArrested && !isGameOver()) {
//                printThreads();

                try {
                    TimeUnit.MILLISECONDS.sleep(generateRandomIntInRange(TIMEOUT_LOWER, TIMEOUT_UPPER));
                } catch (InterruptedException ignored) {
                }

                try (
                        Socket agent = new Socket(HOST, findRandomPort());
                        Scanner in = new Scanner(agent.getInputStream());
                        PrintWriter out = new PrintWriter(agent.getOutputStream())
                ) {
                    agent.setSoTimeout(TIMEOUT_LOWER);

                    System.out.println(this + " connected to an agent on port: " + agent.getPort());

                    String receivedRandomName = in.nextLine();

                    System.out.println(this + " received '" + receivedRandomName + "' on port: " + agent.getPort());

                    Faction guessedFaction = guessFaction(receivedRandomName);
                    write(out, guessedFaction.getName());

                    System.out.println(this + " guessed server's faction as '" + guessedFaction.getName() + "' on port: " + agent.getPort());

                    if (in.nextLine().equals("OK")) {
                        this.knownNames.put(receivedRandomName, guessedFaction);
                    } else { // DISCONNECTED
                        continue;
                    }

                    if (this.faction.equals(guessedFaction)) {
                        write(out, "OK");

                        write(out, getRandomSecretFromList(this.knownSecrets));
                        addSecretToList(in.nextLine(), this.knownSecrets);
                    } else {
                        write(out, "???");

                        int guessedId = guessId(receivedRandomName);
                        write(out, String.valueOf(guessedId));

                        System.out.println(this + " guessed server's id as '" + guessedId + "' on port: " + agent.getPort());

                        String message = in.nextLine();
                        if (!message.equals("DISCONNECTED")) {
                            knownAgentIds.put(receivedRandomName, guessedId);
                            addSecretToList(message, this.knownSecrets);
                        } else {
                            List<Integer> triedIds = alreadyTriedIds.get(receivedRandomName);

                            if (triedIds == null) {
                                triedIds = new ArrayList<>();
                            }

                            triedIds.add(guessedId);
                            alreadyTriedIds.put(receivedRandomName, triedIds);
                        }
                    }

                    System.out.println(this + " knows " + knownSecrets);
                } catch (IOException | NoSuchElementException ignored) {
                    // The other agent has abruptly disconnected.
                }
            }
        };

        Thread serverTask = new Thread(server);
        Thread clientTask = new Thread(client);

        clientTask.start();
        serverTask.start();

        try {
            clientTask.join();
            serverTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.isArrested) {
            System.out.println(this + " has been arrested. Stopping activity.");
        }
    }

    private synchronized boolean isGameOver() {
        if (KGB.isWinner() || CIA.isWinner()) {
            return true;
        }

        if (this.faction.equals(Faction.CIA)) {
            if (KGB.areAllAgentsArrested() || KGB.isCompromised(this)) {
                CIA.setWinner(true);

                return true;
            }
        } else { // Faction.KGB
            if (CIA.areAllAgentsArrested() || CIA.isCompromised(this)) {
                KGB.setWinner(true);

                return true;
            }
        }

        return false;
    }

    private Faction guessFaction(String receivedName) {
        Faction faction = knownNames.get(receivedName);

        if (faction == null) {
            int guess = generateRandomIntInRange(1, 2);
            faction = guess == 1 ? Faction.CIA : Faction.KGB;
        }

        return faction;
    }

    private int guessId(String receivedName) {
        Integer id = this.knownAgentIds.get(receivedName);

        if (id == null) {
            // always guesses in range of 1 - MAX_AGENCY_SIZE because files aren't necessarily read in the right order
            List<Integer> wrongIds = this.alreadyTriedIds.get(receivedName);
            int guess = generateRandomIntInRange(1, MAX_AGENCY_SIZE);

            if (wrongIds != null) {
                while (wrongIds.contains(guess)) {
                    guess = generateRandomIntInRange(1, MAX_AGENCY_SIZE);
                }
            }

            id = guess;
        }

        return id;
    }

    private int findRandomPort() {
        int randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
        while (randomPort == this.server.getLocalPort()) {
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
        return secrets.get(generateRandomIntInRange(0, secrets.size() - 1));
    }

    private synchronized String tellRandomSecret() {
        List<String> untoldSecrets = new ArrayList<>(this.knownSecrets);
        untoldSecrets.removeAll(this.toldSecrets);

        String secret = untoldSecrets.get(generateRandomIntInRange(0, untoldSecrets.size() - 1));
        this.toldSecrets.add(secret);

        return secret;
    }

    private int generateRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static void write(PrintWriter pw, String message) {
        pw.println(message);
        pw.flush();
    }

    private static void printThreads() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        System.out.println(threadSet);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.names.forEach(name -> sb.append(name).append(" "));
        sb.append("(").append(this.serialNumber).append(") ");
        sb.append("from ").append(this.faction.getName());
        return sb.toString();
    }

}
