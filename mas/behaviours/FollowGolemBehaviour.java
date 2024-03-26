package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class FollowGolemBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 8567689731496787661L;
	private boolean finished;
	private List<String> liste_golem;
	private boolean busy; // si l'agent est occupé à suivre un golem
	
	
	
	public FollowGolemBehaviour(final AbstractDedaleAgent myagent, List<String> liste_golem) {
		super(myagent);
		this.finished = false;
		this.liste_golem = liste_golem; // liste des golem qu'on 
	}
	
	
	@Override
	public void action() {
       
		// TODO Auto-generated method stub
        // trouver un golem dans la liste des observables
        List<Couple<Location, List<Couple<Observation, Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
        if (lobs.isEmpty()) {
			// pas d'observation on suit l'odeur si il y en a une
        	return;
        }
        // faire un ping 
        // lobs en list de string
        ArrayList<String> lobsString = new ArrayList<String>();
		for (Couple<Location, List<Couple<Observation, Integer>>> couple : lobs) {
			lobsString.add(couple.getLeft().toString());
		}
    	this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 500, lobsString));
		
    	
        Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
        while (iter.hasNext()) {
        	Couple<Location, List<Couple<Observation, Integer>>> agent = iter.next();
  
        	// si il recoit une rep, on le calcule pas
        	// si il reçoit rien , alors on hunt
        	
        	// si il y a un agent à coter de notre agent qui repond pas, on le suit
        	// si il y a pas d'agent mais qu'il y a une odeur, on suit l'odeur
        	
        	if (this.liste_golem.contains(golem)) {
        		// si le golem est dans la liste des golems
        		// on va vers lui
        		((AbstractDedaleAgent)this.myAgent).moveTo(golem.getLeft());
        		this.liste_golem.remove(golem);
        		return;
        	}
        }
        
		if (this.liste_golem.isEmpty()) {
			// ajout d'un comportement explo ?
			this.finished = true;
			return;
		}else {
			
			
		}
			
			
	}
	

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 
	 */
	
}
