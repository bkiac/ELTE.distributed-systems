package hu.elte.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static hu.elte.agent.util.AgentUtil.shutDownExecutor;

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

    public boolean isWinner() {
        return isWinner;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

    public void startAll() {
//        ExecutorService executor = Executors.newFixedThreadPool(agentList.size());

        agentList.forEach(Agent::start);

    }

    public boolean areAllAgentsArrested() {
        return agentList.stream().allMatch(Agent::isArrested);
    }

    public boolean isCompromised(Agent enemyAgent) {
        return enemyAgent.getKnownSecrets().containsAll(this.secretList);
    }

}
