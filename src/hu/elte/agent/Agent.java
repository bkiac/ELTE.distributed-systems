package hu.elte.agent.thread;

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

    protected int serialNumber;
    protected List<String> names;
    protected List<String> knownSecrets;
    protected List<String> toldSecrets;
    protected ServerSocket server;
    protected Faction faction;
    protected Map<String, Faction> knownNames;
    protected boolean isArrested;

    public Agent() {
    }

    public Agent(Faction faction, int serialNumber, List<String> names, List<String> secrets) {
        this.faction = faction;
        this.serialNumber = serialNumber;
        this.names = names;
        this.knownSecrets = secrets;
        this.knownNames = new HashMap<>();
        this.toldSecrets = new ArrayList<>();
        this.isArrested = false;
    }

    public List<String> getKnownSecrets() {
        return knownSecrets;
    }

    public boolean isArrested() {
        return isArrested;
    }

    protected void createServerSocket() {
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

        Thread serverTask = new AgentServerTask();
        Thread clientTask = new AgentClientTask();

        serverTask.start();
        clientTask.start();

        try {
            serverTask.join();
            clientTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.isArrested) {
            System.out.println(this + " has been arrested. Stopping activity.");
        }
    }

    protected synchronized boolean isGameOver() {
        if (KGB.isWinner() || CIA.isWinner()) {
            System.out.println("&&&&& ALREADY OVER &&&&&");

            Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
            System.out.println(threadSet);
            return true;
        }

        if (this.faction.equals(Faction.CIA)) {
            if (KGB.areAllAgentsArrested() || KGB.isCompromised(this)) {
                CIA.setWinner(true);
                System.out.println("########## CIA WON ##########");
                return true;
            }
        } else { // Faction.KGB
            if (CIA.areAllAgentsArrested() || CIA.isCompromised(this)) {
                KGB.setWinner(true);

                System.out.println("########## KGB WON ##########");
                return true;
            }
        }

        return false;
    }

    protected Faction guessFaction(String msg) {
        Faction faction = knownNames.get(msg);
        if (faction == null) {
            int guess = generateRandomIntInRange(1, 2);
            faction = guess == 1 ? Faction.CIA : Faction.KGB;
        }
        return faction;
    }

    protected int findRandomPort() {
        int randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
        while (randomPort == this.server.getLocalPort()) {
            randomPort = generateRandomIntInRange(PORT_LOWER, PORT_UPPER);
        }
        return randomPort;
    }

    protected synchronized void addSecretToList(String secret, List<String> secrets) {
        if (!secrets.contains(secret)) {
            secrets.add(secret);
        }
    }

    protected synchronized String getRandomSecretFromList(List<String> secrets) {
        return secrets.get(generateRandomIntInRange(0, secrets.size() - 1));
    }

    protected synchronized String tellRandomSecret() {
        List<String> untoldSecrets = new ArrayList<>(this.knownSecrets);
        untoldSecrets.removeAll(this.toldSecrets);

        String secret = untoldSecrets.get(generateRandomIntInRange(0, untoldSecrets.size() - 1));
        this.toldSecrets.add(secret);

        return secret;
    }

    protected int generateRandomIntInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    protected static void write(PrintWriter pw, String message) {
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
