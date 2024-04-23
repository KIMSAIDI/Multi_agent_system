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
        

        // ~~~~~~~~~ Step 1 : Je vérifie que je bloque toujours le golem ~~~~~~~~~
        
        
        //whichGolem(); // determine si je reste bien à ma place, pour le moment inutile
            
    	
//    	if (checkFalseInformation()) { // si on me prend pour un golem
//    		return;
//    	}
//    	
//    	if ( mistake() ){
//    		return;// si je me suis trompé
//    	}
        
        // je vérifie que je le bloque encore
        if (!checkStillBlockGolem()){
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
            // On envoie la position du golem
            msg.setContentObject(((AgentFsm)this.myAgent).getPosition_golem());
            ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(this.myAgent.getLocalName() + " : ----Je bloque un golem, il est à la position : " + position_golem + "----	");
        // my position
        Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(myPosition.getLocationId()));
        
        
    }
    
    public boolean whichGolem(){
        // 1) je vérifie qu'on parle bien de mon golem 
        // Liste des observables
		List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
		// Liste des noeuds observables
        List<Location> noeuds_observable = new ArrayList<Location>();
        for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
            noeuds_observable.add(observable.getLeft()); 
        }
        // on convertit en string
        List<String> string_location_observable = new ArrayList<String>();
        for (Location loc : noeuds_observable) {
            string_location_observable.add(loc.getLocationId());
        }
        // 2) Si il s'agit d'un autre golem alors on ne fait rien
        if (!string_location_observable.contains(((AgentFsm)this.myAgent).getPosition_golem())){
            this.exitValue = 0;
            return false;
        }
        return true;
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
            // je vérifie que l'agent ne me prend pas pour un golem
        	try {
        		String loc = (String) msgReceived.getContentObject();
        		String maLoc = myPosition.getLocationId();
        		System.out.println("JE NE SUIS PAS LE GOLEM AARRRG");
        		if (loc == maLoc) {
        			// je ne suis pas un golem
        			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        	        msg.setProtocol("Je_Ne_Suis_Pas_Un_GolemProtocol");
        	        msg.setSender(this.myAgent.getAID());
        	        for (String agentName : this.list_agentNames) {
        				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        				
        			}
					try {
						msg.setContentObject(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
						((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
        			this.exitValue = 4; // je retourne en patrouille
        			
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
				MessageTemplate.MatchProtocol("I_Am_An_AgentBlockGolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null) {
        	System.out.println("MY BAD");
        	this.exitValue = 4;
        	return true;
        }
        return false;
    	
    }

    @Override
	public int onEnd() {
		return exitValue;
	}
}
