package hu.elte.agent;

public class AgentMain {

    public static void main(String[] args) {
        // Start thread for each of the agents
        System.out.println("CIA agent pool: " + args[0]);
        System.out.println("KGB agent pool: " + args[1]);

        // Time in milliseconds
        System.out.println("Timeout lower bound: " + args[2]);
        System.out.println("Timeout upper bound:" + args[3]);
    }

}
