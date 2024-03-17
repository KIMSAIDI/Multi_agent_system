package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Behaviour that periodically checks for incoming messages from other agents,
 * attempting to deserialize an object from these messages. If a message is received,
 * it processes the contained object. Messages are expected to follow a specific protocol
 * and be of certain performative types (INFORM or REFUSE).
 */
public class ReceivePing extends TickerBehaviour {

    private static final long serialVersionUID = -2058134622078521998L;
    
    private MapRepresentation myMap;


    /**
     * Constructs the ReceivePing behaviour.
     * 
     * @param myagent The agent possessing this behaviour.
     */
    public ReceivePing(final Agent myagent, MapRepresentation myMap) {
        super(myagent, 3000); // Calls the parent class constructor with the agent and the period.
        this.myMap=myMap;
		
    }

    @Override
    public void onTick() {
        // Attempt to get the current position of the agent.
        Location myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        
        MessageTemplate template=MessageTemplate.and(
				MessageTemplate.MatchProtocol("UselessProtocol"),
				MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
						)
						) ;
        ACLMessage msg = this.myAgent.receive(template);
        if (msg != null) {
        	String textMessage=msg.getContent();
        	try {
				Myobject o = (Myobject) msg.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        // 
        
        /*
        // Define a message template that matches the desired protocol and performative types.
        MessageTemplate template=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        // Try to receive a message that matches the template.
        ACLMessage msg = this.myAgent.receive(template);
        if (msg != null) {
        	System.out.println("LA ON A RECU UN TRUC");
        	SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
            try {
                // Attempt to deserialize the object from the message.
                Myobject o = (Myobject) msg.getContentObject();
                System.out.println("Received object: " + o.toString());
                System.out.println("ON PARTAGE LA MAP LA");
                this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.myMap,list_agentNames));
                
            	sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msg.getContentObject();
            
            	
                // Process the object as needed. This might include logging, responding, etc.
            } catch (UnreadableException e) {
                // Handle the case where the message's object could not be deserialized.
                System.err.println("Error deserializing object from message: " + e.getMessage());
                e.printStackTrace();
            }
            
            if (sgreceived!=null) {
            	System.out.println("ON A RECU UNE MAP");
            	
            }
            
        } 
        
       */
        
        
    }
}
