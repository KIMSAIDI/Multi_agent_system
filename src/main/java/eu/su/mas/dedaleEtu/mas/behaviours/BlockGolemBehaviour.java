package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class BlockGolemBehaviour extends OneShotBehaviour{

    private static final long serialVersionUID = -2058134622078521998L;

    private List<String> list_agentNames;
    private String position_golem;
    private int exitValue = 0;

    public BlockGolemBehaviour(final Agent myagent, List<String> list_agentNames, String position_golem) {
        super(myagent);
        this.list_agentNames = list_agentNames;
        this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
        
    }

    public void action() {
    	this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
        Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        this.myAgent.doWait(400);
        // ~~~~~~~~~ Step 1 : Je vérifie que je bloque toujours le golem ~~~~~~~~~
        
    	
        if ( mistake() ){
        	System.out.println("je me suis trompé");
            this.exitValue = 4; 
            //((AgentFsm)this.myAgent).getMyMap().removeNode(this.position_golem);
            return;// si je me suis trompé
        }
        
        checkFalseInformation();
        
        
        // je vérifie que je le bloque encore
        if (!checkStillBlockGolem()){
        	System.out.println("je ne le bloque plus");
            this.exitValue = 4;
            return;
        }

        this.exitValue = 0;
        // On envoie un message pour dire qu'on est un agent qui bloque un golem
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setProtocol("I_Am_An_AgentBlockGolemProtocol");
        msg.setSender(this.myAgent.getAID());
        for (String agentName : this.list_agentNames) {
            msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        try{
            // On envoie la position du golem et sa position
            String content = ((AgentFsm)this.myAgent).getPosition_golem() + "|" + myPosition;
            msg.setContent(content);
            ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
       System.out.println(this.myAgent.getLocalName() + " : ----Je bloque un golem, il est à la position : " + position_golem + "----	");
        // my position
        
        ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(myPosition.getLocationId()));
        
        
    }
    

    public boolean checkStillBlockGolem(){
        // si je peux acceder à la position du golem alors je ne le bloque plus
        if (((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(((AgentFsm)this.myAgent).getPosition_golem()))) {
            System.out.println("je ne le bloque plus");
            System.out.println("le golem est la position : " + ((AgentFsm)this.myAgent).getPosition_golem());
            //((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(((AgentFsm)this.myAgent).getPosition_golem()));
        	((AgentFsm)this.myAgent).setPosition_golem(""); 

            return false;
        }
        return true;
    }
    
     public boolean checkFalseInformation() {
     	Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        
     	MessageTemplate msgTemplate = MessageTemplate.and(
	 			MessageTemplate.MatchProtocol("I_Am_An_AgentBlockGolemProtocol"),
	 			MessageTemplate.MatchPerformative(ACLMessage.INFORM));
         ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
         if (msgReceived != null) {
            String content = msgReceived.getContent();
            // Séparation des informations
            String[] parts = content.split("\\|");
            // Récupération des informations distinctes
            String posGolem = parts[0];
            //String positionAgent = parts[1];

             // je vérifie que l'agent ne me prend pas pour un golem
         	try {
         		String loc = posGolem; // loc du golem
         		String maLoc = myPosition.getLocationId();
        		
         		if (loc.equals(maLoc)) {
         			System.out.println(this.myAgent.getLocalName() + "JE NE SUIS PAS LE GOLEM AARRRG");
         			// je ne suis pas un golem
         			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
         	        msg.setProtocol("Je_Ne_Suis_Pas_Un_GolemProtocol");
         	        msg.setSender(this.myAgent.getAID());
         	        for (String agentName : this.list_agentNames) {
         				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        				
         			}
	 				try {
	 					msg.setContent(((AbstractDedaleAgent) this.myAgent).getCurrentPosition().getLocationId());
	 					((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
                    
	 				} catch (Exception e) {
	 					e.printStackTrace();
	 				}
         			
         			return true;
         		}
         	}catch (Exception e) {
         		e.printStackTrace();
         	}
         }
     	return false;
     }
    
    public boolean mistake() {
    	MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("Je_Ne_Suis_Pas_Un_GolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null) {
            // je compare si la position de l'agent est bien la position de mon golem
            //String posAgent = (String) msgReceived.getContent();
            //String posGolem = ((AgentFsm)this.myAgent).getPosition_golem();
            System.out.println(this.myAgent.getLocalName() + " posAgent : " + msgReceived.getContent() + " posGolem : " + ((AgentFsm)this.myAgent).getPosition_golem());
            if (msgReceived.getContent().equals(((AgentFsm)this.myAgent).getPosition_golem())){
            	System.out.println("je me suis trompé");
                // je me suis trompé
                ((AgentFsm)this.myAgent).setPosition_golem(""); 
                return true;
            }
        }
        return false;

    }

    @Override
	public int onEnd() {
		return this.exitValue;
	}
}