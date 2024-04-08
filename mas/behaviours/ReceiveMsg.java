package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
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
public class ReceiveMsg extends SimpleBehaviour {

    private static final long serialVersionUID = -2058134622078521998L;
    
    private MapRepresentation myMap;
    private Agent myAgent;
    private List<String> list_agentNames;
    private boolean finished = false;


    /**
     * Constructs the ReceivePing behaviour.
     * 
     * @param myagent The agent possessing this behaviour.
     */
    public ReceiveMsg(final Agent myagent, MapRepresentation myMap, List<String> list_agentNames) {
        this.myMap=myMap;
		this.myAgent=myagent;
		this.list_agentNames=list_agentNames;
    }

    @Override
    public void action() {
    	
    	
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
    	if(this.myMap.hasOpenNode()) { // Vérifie si l'exploration n'est pas finie)
        
	    	// Si je recois un HelloProtocol, je partage ma map
	    	MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("HelloProtocol"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			
			if (msgReceived != null) {
				// create a new message
				System.out.println("------J ai recu un bonjour ------");
				this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent, this.myMap, list_agentNames));
				
			}
	    	
	    	
	    	// Si je recois une Map, je la merge avec la mienne
	    	MessageTemplate msgTemplate2=MessageTemplate.and(
					MessageTemplate.MatchProtocol("SHARE-TOPO"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			ACLMessage msgReceived2=this.myAgent.receive(msgTemplate2);
			if (msgReceived2!=null) {
				SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
				try {
					sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived2.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("---------on merge les map---------");
				this.myMap.mergeMap(sgreceived);
			}
        
		} else {
			//System.out.println("-----------------Exploration terminée pour "+this.myAgent.getLocalName()+"------------------");
			finished = true;
		}
    	
    	
    	// Si je recois un Ping, j'envoie ma position 
    	MessageTemplate msgTemplate3 = MessageTemplate.and(
				MessageTemplate.MatchProtocol("Ping"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived3 = this.myAgent.receive(msgTemplate3);
       
		if (msgReceived3 != null) {
			// create a new message
			System.out.println("J ai recu un ping");
			ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("ACK_Ping");
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(msgReceived3.getSender());
			try {
				System.out.println("---- jai recu un ping, je renvoie ma position ----");
				msg.setContentObject(myPosition);
			} catch (Exception e) {
				e.printStackTrace();
			}
			((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
			
		}
    }
    
    @Override
	public boolean done() {
		// TODO Auto-generated method stub
		return !finished;
	}

}
