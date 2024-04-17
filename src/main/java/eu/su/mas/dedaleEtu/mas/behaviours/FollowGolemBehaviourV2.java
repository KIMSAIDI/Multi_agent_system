package eu.su.mas.dedaleEtu.mas.behaviours;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import debug.Debug;
import eu.su.mas.dedale.env.IEnvironment;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.HunterAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.behaviours.CommunicationHunter;


public class FollowGolemBehaviourV2 extends SimpleBehaviour{

    
	private static final long serialVersionUID = 8567689731496787661L;


	private MapRepresentation myMap;
	private List<String> list_agentNames;
	private List<Location> liste_noeuds_agents;
	boolean mode_capture = false;
	
    
	public FollowGolemBehaviourV2(final AbstractDedaleAgent myagent, List<String> list_agentNames, MapRepresentation myMap ) {
		super(myagent);
		this.list_agentNames = list_agentNames;
		this.myMap = myMap;
		
	}
    @Override
    public void action(){
    	
    	if (this.myMap==null) {
    		this.myMap= new MapRepresentation();	            
    	}
    	
    	if (!mode_capture) {
	        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Step 1 : Communication ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	    	
	        // Position des autres agents
	    	liste_noeuds_agents = new ArrayList<Location>();
	        CommunicationHunter communicationBehaviour = new CommunicationHunter(this.myAgent, this.list_agentNames, liste_noeuds_agents);
	        communicationBehaviour.action();
	        liste_noeuds_agents = communicationBehaviour.getListeNoeudsAgents();
	        
	        
	        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Step 2 : FollowGolem ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	    
												/* Variables */
	        // Position actuelle de l'agent
	        Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			// Liste des observables
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			// Liste des noeuds observables
			List<Location> noeuds_observable = new ArrayList<Location>();
			// Liste des noeuds avec une odeur
			List<Location> liste_position_odeur = new ArrayList<Location>();
	
			// On récupère les noeuds observables
			for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
				noeuds_observable.add(observable.getLeft()); 
				for (Couple<Observation, Integer> obs : observable.getRight()) {
					if (obs.getLeft().equals(Observation.STENCH)) {
						// Si une odeur est détectée
						liste_position_odeur.add(observable.getLeft());
						break; // Pas besoin de chercher d'autres observations pour ce noeud
					}
				}
			}
			if (!noeuds_observable.isEmpty()) {
	            noeuds_observable.remove(0); // Supprime le premier élément  
	        }
			if (!liste_position_odeur.isEmpty()) {
				liste_position_odeur.remove(0); // Supprime le premier élément
			}
			// On wait pour pas aller trop vite
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
							/*  Pour dessiner notre map */
	
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);	
			Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
				}
			}
	
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			/* Pour ne pas aller sur les mêmes noeuds que les autres agents et être efficace */
			
			// convertit en string des positions des agents
			List<String> string_location_agent = new ArrayList<String>();
			for (Location loc : liste_noeuds_agents) {
				string_location_agent.add(loc.getLocationId());
			}
			// convertit en string les noeuds observables
			List<String> string_location_observable = new ArrayList<String>();
			for (Location loc : noeuds_observable) {
				string_location_observable.add(loc.getLocationId());
			}
			// convertit en string la liste des positions des golems
			List<String> string_location_odeur = new ArrayList<String>();
			for (Location loc : liste_position_odeur) { // il faut enlever notre position
				string_location_odeur.add(loc.getLocationId());
			}
			
			string_location_odeur.removeAll(string_location_agent); // on enlève les positions des agents
			string_location_observable.removeAll(string_location_agent); // on enlève les positions des agents
			
			// on reconvertit les positions des golems en location
			List<Location> liste_position_golem = new ArrayList<Location>();
			for (String loc : string_location_odeur) {
				liste_position_golem.add(new gsLocation(loc)); // liste_position_golem est la liste des noeuds avec une odeur mais sans agents
			}
			// on reconvertit les noeuds observables en location
			List<Location> liste_position_observable = new ArrayList<Location>();
			for (String loc : string_location_observable) {
				liste_position_observable.add(new gsLocation(loc)); 
																	
			}
	
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
									/* Déplacement */
	
			String nextNodeId=null;
			
			// si la liste des odeurs est vide, behaviour golem sans odeur
			if (liste_position_golem.isEmpty()) {
				// si pas d'odeur, on va vers un noeud aléatoire
				nextNodeId = liste_position_observable.get(0).getLocationId();
			}else {
				Random rand = new Random();
				int randomIndex = rand.nextInt(liste_position_golem.size());
				// on va vers un noeud avec une odeur
				nextNodeId = liste_position_golem.get(randomIndex).getLocationId();
			}
	
			if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
				// si je ne peux pas aller sur ce noeud, c'est certainement un golem
				
				// je vérifie que c'est bien un golem

				// Je partage ma connaissance
				ACLMessage msg_capture = new ACLMessage(ACLMessage.INFORM);
				msg_capture.setSender(this.myAgent.getAID());
				msg_capture.setProtocol("ModeCaptureProtocol");
				for (String agentName : list_agentNames) {
					msg_capture.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				}
				try{
					msg_capture.setContentObject((Serializable) new gsLocation(nextNodeId));
					((AbstractDedaleAgent) this.myAgent).sendMessage(msg_capture);
				}catch (IOException e) {
					e.printStackTrace();
				}
				mode_capture = true;
			}
			
    	}else {
    		;
    	}
				

    }

    @Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
}
