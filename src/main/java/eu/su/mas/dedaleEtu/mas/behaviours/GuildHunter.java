package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Behaviour that periodically checks for incoming messages from other agents,
 * attempting to deserialize an object from these messages. If a message is received,
 * it processes the contained object. Messages are expected to follow a specific protocol
 * and be of certain performative types (INFORM or REFUSE).
 */
public class GuildHunter extends SimpleBehaviour {

    private static final long serialVersionUID = -2058134622078521998L;
    
    private Location nextPosition;
    private Agent myAgent;
    private List<String> list_agentNames;
    private boolean busy = false;
    private boolean finished = false;


    /**
     * Constructs the ReceivePing behaviour.
     * 
     * @param myagent The agent possessing this behaviour.
     */
    public GuildHunter(final Agent myagent, Location nextPosition, List<String> list_agentNames, boolean busy) {
        this.nextPosition=nextPosition;
		this.myAgent=myagent;
		this.list_agentNames=list_agentNames;
		this.busy = busy;
    }

    @Override
    public void action() {
    	
    	
    	// list_agentNames est la liste 
    	
    	// ~~~~~~~~~~~~~~~~ BUSY ~~~~~~~~~~~~~~~~
    	
    	
    	
    	// ~~~~~~~~~~~~~~~~ PAS BUSY ~~~~~~~~~~~~~~~~
    	
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-NEXT-POSITION");
		msg.setSender(this.myAgent.getAID());
		for (String agentName : list_agentNames) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
			
		try {
			msg.setContentObject(nextPosition);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

		
	}

    
    
    @Override
	public boolean done() {
		// TODO Auto-generated method stub
		return !finished;
	}

}
