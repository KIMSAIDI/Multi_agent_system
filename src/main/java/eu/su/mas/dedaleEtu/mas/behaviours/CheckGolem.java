package eu.su.mas.dedaleEtu.mas.behaviours;


import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

public class CheckGolem extends OneShotBehaviour {
        
        private static final long serialVersionUID = 8567689731496787661L;
    
        private int exitValue = 0; // on ne fait rien par défaut
        private List<String> list_agentNames;
    
        public CheckGolem(final AbstractDedaleAgent myagent, List<String> list_agentNames) {
            super(myagent);
            this.list_agentNames = list_agentNames;
            
        }

        public void action(){
        	// si la position du golem fait partie de mes noeuds observable, je bloque
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
            	
            if ((string_location_observable.contains(((AgentFsm)this.myAgent).getPosition_golem()))) {
            	this.exitValue = 7; // On va bloquer le golem
            	return;
            }else {
            	((AgentFsm)this.myAgent).setPosition_golem(""); 
            	this.exitValue = 0; // On va patrouiller
            	return;
            }
       
        }

        
    public int onEnd(){
        return this.exitValue;
    }
    
}
