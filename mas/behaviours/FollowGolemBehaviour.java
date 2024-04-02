package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
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

	/**
	 * 
	 */
	
//	private static final long serialVersionUID = 8567689731496787661L;
//	
//	//private boolean busy; // si l'agent est occupé à suivre un golem
//	private List<String> list_agentNames;
//	private MapRepresentation myMap;
//	
//	
//	
//	public FollowGolemBehaviour(final AbstractDedaleAgent myagent, List<String> list_agentNames, MapRepresentation myMap ) {
//		super(myagent);
//		//this.busy = false;
//		this.list_agentNames = list_agentNames;
//		this.myMap = myMap;
//	
//	}
//	
//	
//
//
//	@Override
//	public void action() {
//		
//		if(this.myMap==null) {
//			this.myMap= new MapRepresentation();
//			// fait un ping pour découvrir qui est autour de notre agent
//	        
//	    	this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 500, this.list_agentNames, "Ping"));
//	    	
//			
//		}
//		
//		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
//
//		if (myPosition!=null) {
//			
//			//System.out.println("---------------------------------------------");
//			
//			 // Liste des observables
//			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
//			List<Location> noeuds_observable = new ArrayList<>();
//	        for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
//	        	noeuds_observable.add(observable.getLeft()); 
//	        }
//			
//	        if (!noeuds_observable.isEmpty()) {
//	            noeuds_observable.remove(0); // Supprime le premier élément
//	        }
//	        
//	       
//	        try {
//				this.myAgent.doWait(1000);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//	        this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);
//
//			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
//			String nextNodeId=null;
//			Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
//			
//	        /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ IDEE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//			 * Le but est de determiner si les agents autour de moi sont un explo ou un golem
//			 * Notre agent va envoyer un ping
//			 * Si notre agent recoit un message (contenant la position de l'agent) alors il y a au moins un agent explo autour de lui
//			 * On ajoute cette position à la liste_noeuds_agents
//			 * Au final, l'idée est de comparer la liste des noeuds accesibles + la liste des noeuds_agents avec la liste des noeuds observables
//			 * Si noeuds_observable - (liste_noeuds_accessibles + liste_noeuds_agents) != 0, alors il y a un golem à la position golemString[0:]
//			 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
//			
//	        
//			
//
//			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//			
//			
//			// liste des noeuds à proximité qui sont des agents
//			List<Location> liste_noeuds_agents = new ArrayList<Location>();
//	    	
//	    	// message qu'on recoit (ou non)
//	    	MessageTemplate msgTemplate = MessageTemplate.and(
//					MessageTemplate.MatchProtocol("ACK_Ping"),
//					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
//			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
//			
//			
//	    	// Si on recoit un message, un agent est à proximité
//			if (msgReceived != null ) {
//				Location noeud = null;
//				try {
//					noeud = (Location) msgReceived.getContentObject();
//				} catch (UnreadableException e) {
//					e.printStackTrace();
//				}
//				liste_noeuds_agents.add(noeud);
//				System.out.println("On a croisé un agent");
//				System.out.println("noeud agent : " + liste_noeuds_agents);
//			}
////			
//			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//			
//			
//			while(iter.hasNext()){
//				Location accessibleNode=iter.next().getLeft();
//				//System.out.println("Noeud accessible : "+accessibleNode);
//				
//				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
//				//the node may exist, but not necessarily the edge
//				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
//					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
//					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
//				}
//			}
//	        
//	        System.out.println("my position : " + myPosition.getLocationId());
//	        System.out.println("noeuds_observable : " + noeuds_observable);
//	        System.out.println("liste_noeuds_agents : " + liste_noeuds_agents);
//	        
//	        
//	        
//
//			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//			
//	        
//	        // liste_position_golem = noeuds_observable - (liste_noeuds_agents) 
//	        List<Location> liste_position_golem = new ArrayList<Location>(noeuds_observable);
//	        liste_position_golem.removeAll(liste_noeuds_agents);
//	        
//	        
//	        nextNodeId = liste_position_golem.get(0).getLocationId();
//	        if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
//	        	System.out.println("Il ya un golem à la position : " + nextNodeId);
//	        	// on le suit
//	        	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
//	        	System.out.println("On suit le golem");
//			} else {
//				if (nextNodeId==null){
//					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
//		        }
//	        	//this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, list_agentNames));
//	    	    System.out.println("nextNodeId : " + nextNodeId);
//			    ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
//				
//		        
//				
//			}
//	        
//
//			
//	        
////	        if () {
////	        }else {
////	        
////	        	if (nextNodeId==null){
////					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
////		        }
////	        	//this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, list_agentNames));
////	    	    
////			    ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
////				
////				
////	 	        
////	        }
//	        
//	        
////	        // determiner si il y a un golem autour de notre agent
////	        
////	        // On convertit liste_noeuds_accessibles et liste_noeuds_agents en List<String>
////	       // List<Location> list_noeuds_accessible = noeuds_accessibles_string.stream().map(gsLocation::new).collect(Collectors.toList());
//////	        List<String> agentsString = liste_noeuds_agents.stream().map(Location::toString).collect(Collectors.toList());
////	        // On fusionne les deux listes en une seule
////	        List<String> noeuds_non_golem = new ArrayList<>(noeuds_accessibles_string);
////	        
////	        // liste_noeuds_agen converit en string
////	        List<String> liste_noeuds_agents_string = liste_noeuds_agents.stream().map(Location::toString).collect(Collectors.toList());
////	        
////	        // liste des observerable en string
////	        List<String> noeuds_observable_string = noeuds_observable.stream().map(Location::toString).collect(Collectors.toList());
////	        
////	        // noeuds_non_golem contient les identifiants de tous les noeuds non golems
////	        noeuds_non_golem.addAll(liste_noeuds_agents_string); 
////	        		
////	       
////	        
////	        
////	        // On soustrait de noeuds_observable
////	        List<String> liste_golem = new ArrayList<String>();
////			for (String golem : noeuds_observable_string) {
////				if (!noeuds_non_golem.contains(golem)) {
////					liste_golem.add(golem);
////				}
////			}
//	       
//	        
////	        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
////	        System.out.println("noeuds_observable : " + noeuds_observable);
////	        System.out.println("liste_noeuds_accessibles : " + noeuds_accessibles_string);
////	        System.out.println("liste_noeuds_agents : " + liste_noeuds_agents);
////	        System.out.println("noeuds_non_golem : " + noeuds_non_golem);
////	        System.out.println("liste_golem : " + liste_golem);
////	        
//	     // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//	        
////	        // convertit liste_golem en Location
////	        List<Location> liste_golem_location = liste_golem.stream().map(gsLocation::new).collect(Collectors.toList());
////	        
////	        if (!liste_golem_location.isEmpty()) {
////	        	System.out.println("il y a un Golem à la position : " + liste_golem_location.get(0));
////	        	// on le suit
////	        	((AbstractDedaleAgent)this.myAgent).moveTo(liste_golem_location.get(0));
////	        	System.out.println("On suit le golem");
////	        	
////	        }else {
////	        
////	        	if (nextNodeId==null){
////					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
////		        }
////	        	//this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, list_agentNames));
////	    	    
////			    ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
////				
////				
////	 	        
////	        }
//	        
//	       
//			
//	        
//	        
//	        
//
//			
//	  
//	        	// si il recoit une rep, on le calcule pas
//	        	// si il reçoit rien 
//	        	// PAS D'ODEUR -> PING PUIS TEST MOVE CASE : SI QLQUN -> GOLEM ----> autre agent calcule plus petit chemin pour bloquer le golem de l'autre coté
//	        	
//	        	// si il y a un agent à coter de notre agent qui repond pas, on le suit
//	        	// si il y a pas d'agent mais qu'il y a une odeur, on suit l'odeur
//	        	
//	        	
//	        	
//	        	// si il recoit rien mais il peut pas aller a la case
//	//        	if (this.liste_golem.contains(golem)) {
//	//        		// si le golem est dans la liste des golems
//	//        		// on va vers lui
//	//        		((AbstractDedaleAgent)this.myAgent).moveTo(golem.getLeft());
//	//        		this.liste_golem.remove(golem);
//	//        		return;
//	//        	}
//	//        }
//	//        
//	//		if (this.liste_golem.isEmpty()) {
//	//			// ajout d'un comportement explo ?
//	//			this.finished = true;
//	//			return;
//	//		}else {
//	//			
//	//			
//	//		}
//	} else {
//		System.out.println("Erreur : position actuelle non connue");
//	}
//			
//			
//	}
//	
	
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
		
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			List<Location> noeuds_observable = new ArrayList<>();
	        for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
	        	noeuds_observable.add(observable.getLeft()); 
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
			
			
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			
			// liste des noeuds à proximité qui sont des agents
			List<Location> liste_noeuds_agents = new ArrayList<Location>();
	    	
	    	// message qu'on recoit (ou non)
	    	MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACK_Ping"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			
			
	    	// Si on recoit un message, un agent est à proximité
			if (msgReceived != null ) {
				Location noeud = null;
				try {
					noeud = (Location) msgReceived.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				liste_noeuds_agents.add(noeud);
				System.out.println("On a croisé un agent");
				System.out.println("noeud agent : " + liste_noeuds_agents);
			}
			
			
			System.out.println("~~~~~~~~~~~~~~");
			System.out.println("my position : " + myPosition.getLocationId());
	        System.out.println("noeuds_observable : " + noeuds_observable);
	        System.out.println("liste_noeuds_agents : " + liste_noeuds_agents);
	        
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
	        
	        // liste_position_golem = noeuds_observable - (liste_noeuds_agents) 
	        List<Location> liste_position_golem = new ArrayList<Location>(noeuds_observable);
	        liste_position_golem.removeAll(liste_noeuds_agents);
	        
	        System.out.println("liste_position_golem : " + liste_position_golem);
	        
	      
			

			
			if (nextNodeId==null){
				nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
			}
			
			
		    
			
		    ((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
			
			
			
			

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
