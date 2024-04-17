package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import jade.core.behaviours.OneShotBehaviour;

public class InitBehaviour extends OneShotBehaviour{
    private static final long serialVersionUID = 8567689731496787661L;

    @Override
    public void action() {
        MapRepresentation myMap = new MapRepresentation();

        ((AgentFsm)this.myAgent).setMyMap(myMap);

    }
    
}
