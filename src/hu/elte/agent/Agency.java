package hu.elte.agent.util;

import hu.elte.agent.thread.Agent;

import java.util.ArrayList;
import java.util.List;

public class Agency {

    private List<Agent> agentList;
    private List<String> secretList;
    private boolean isWinner;

    public Agency() {
    }

    public Agency(List<Agent> agentList) {
        this.agentList = agentList;

        this.secretList = new ArrayList<>();
        this.agentList.forEach(agent -> secretList.addAll(agent.getKnownSecrets()));

        this.isWinner = false;
    }

    public List<Agent> getAgentList() {
        return agentList;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    public boolean areAllAgentsArrested() {
        return agentList.stream().allMatch(Agent::isArrested);
    }

    public boolean isCompromised(Agent enemyAgent) {
        List<String> secrets = new ArrayList<>(enemyAgent.getKnownSecrets());
        return secrets.containsAll(this.secretList);
    }

}
