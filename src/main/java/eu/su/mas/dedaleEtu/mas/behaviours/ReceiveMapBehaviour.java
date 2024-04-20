package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;
import jade.util.leap.Map;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import jade.core.Agent;

public class ReceiveMapBehaviour extends OneShotBehaviour{

    private static final long serialVersionUID = -2058134622078521998L;

    private MapRepresentation myMap;

    public ReceiveMapBehaviour(final Agent myagent) {
        super(myagent);
    }

    @Override
    public void action() {
        System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to receive its map");
        
    }
    
}
