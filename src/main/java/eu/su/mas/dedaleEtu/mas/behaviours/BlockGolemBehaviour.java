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
            
        
        // je vérifie que je le bloque encore
        if (!checkStillBlockGolem()){
            return;
        }else{
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
            return;
        }
        
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
            ((AgentFsm)this.myAgent).setPosition_golem(""); 
            this.exitValue = 4; // je retourne en patrouille
            return false;
        }
        return true;
    }

    @Override
	public int onEnd() {
		return exitValue;
	}
}
