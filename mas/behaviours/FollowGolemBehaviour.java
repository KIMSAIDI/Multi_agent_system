package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
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

public class FollowGolemBehaviour extends SimpleBehaviour {

	
	
	private static final long serialVersionUID = 8567689731496787661L;


	private MapRepresentation myMap;
	private List<String> list_agentNames;
	

	public FollowGolemBehaviour(final AbstractDedaleAgent myagent, List<String> list_agentNames, MapRepresentation myMap ) {
		super(myagent);
		//this.busy = false;
		this.list_agentNames = list_agentNames;
		this.myMap = myMap;
	
	}

	@Override
	public void action() {

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			
			// fait un ping pour découvrir qui est autour de notre agent
	      
	    	this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 500, this.list_agentNames, "Ping"));
	    	
		}
		
		System.out.println("~~~~~~~~~~~~~~");
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			// Liste de tous les noeuds avec une odeur
			List<Location> liste_position_odeur = new ArrayList<>();			
			List<Location> noeuds_observable = new ArrayList<>();
	        for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
	        	noeuds_observable.add(observable.getLeft()); 
	        	for (Couple<Observation, Integer> obs : observable.getRight()) {
	                if (obs.getLeft().equals(Observation.STENCH)) {
	                    // Si une odeur est détectée, ajoutez la location à la liste des noeuds avec une odeur
	                	liste_position_odeur.add(observable.getLeft());
	                    break; // Pas besoin de chercher d'autres observations pour ce noeud
	                }
	        	}
	        }
	        
			// pour supprimer notre propre position
	        if (!noeuds_observable.isEmpty()) {
	            noeuds_observable.remove(0); // Supprime le premier élément
	        }
	        
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			
			this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
		
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				//System.out.println("Noeud accessible : "+accessibleNode);
				
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				
				}
			}
			
			// ~~~~~~~~~~~~~~~~~~~~~~~~~AGENTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, this.list_agentNames));
			
			
			
			// liste des noeuds à proximité qui sont des agents
			List<Location> liste_noeuds_agents = new ArrayList<Location>();
	    	
	    	// message qu'on recoit (ou non)
	    	MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACK_Ping"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			
			
	    	// Si on recoit un message, un agent est à proximité
			if (msgReceived != null ) {
				System.out.println("J'ai recu un message");
				Location noeud = null;
				try {
					noeud = (Location) msgReceived.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				liste_noeuds_agents.add(noeud);
//				System.out.println("On a croisé un agent");
//				System.out.println("noeud agent : " + liste_noeuds_agents);
			}
			
			
//			System.out.println("my position : " + myPosition.getLocationId());
//	        System.out.println("noeuds_observable : " + noeuds_observable);
	        System.out.println("liste_noeuds_agents : " + liste_noeuds_agents);
	        
			// ~~~~~~~~~~~~~~~~~~~~~~~GOLEM SANS ODEUR~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
	        if (liste_position_odeur.isEmpty()) {
            	
      
		        // liste_position_golem = noeuds_observable - (liste_noeuds_agents) 
		        List<Location> liste_position_golem = new ArrayList<Location>(noeuds_observable);
		        liste_position_golem.removeAll(liste_noeuds_agents);
		        
		        //System.out.println("liste_position_golem : " + liste_position_golem);
		        
		      
				if (nextNodeId==null){
					// son prochain noeud est fait partie de la liste des possibles positions du golem si il y en a un
					Random rand = new Random();
					// Génère un index aléatoire entre 0 (inclus) et la taille de la liste (exclus)
				    int randomIndex = rand.nextInt(liste_position_golem.size());
				    nextNodeId = liste_position_golem.get(randomIndex).getLocationId();
				    //System.out.println("Prochaine noeud choisit : " + nextNodeId);
				}
				
				
			    if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
//	            	System.out.println("Il ya un golem à la position : " + nextNodeId);
	            	// on le suit
	            	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
//	            	System.out.println("On suit le golem");
			    }else {
			    	System.out.println("j'ai changé de position");
			    	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
			    }
	        } 
		
		 // ~~~~~~~~~~~~~~~~~~~~~~~~GOLEM ODEUR~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		    
		    
		else {
		    // On suit l'odeur
			if (nextNodeId==null) {
				Random rand = new Random();
				// Génère un index aléatoire entre 0 (inclus) et la taille de la liste (exclus)
			    int randomIndex = rand.nextInt(liste_position_odeur.size());
				nextNodeId = liste_position_odeur.get(randomIndex).getLocationId();
			}
			
			if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
            	//System.out.println("On à réussi à rattraper le goelem à la position : " + nextNodeId);
            	// on le suit
            	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
            	//System.out.println("On suit le golem");
		    }else {
		    	System.out.println("j'ai changé de position");
		    	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
		    }
		}
		    
		    
			
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