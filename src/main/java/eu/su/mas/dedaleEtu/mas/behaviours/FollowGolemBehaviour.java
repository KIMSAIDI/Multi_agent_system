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

public class FollowGolemBehaviour extends SimpleBehaviour {

	
	
	private static final long serialVersionUID = 8567689731496787661L;


	private MapRepresentation myMap;
	private List<String> list_agentNames;
	boolean busy = false;
	List<String> GuildMembers = new ArrayList<String>();
	String position_golem = null;
	boolean done;

	public FollowGolemBehaviour(final AbstractDedaleAgent myagent, List<String> list_agentNames, MapRepresentation myMap ) {
		super(myagent);
		//this.busy = false;
		this.list_agentNames = list_agentNames;
		this.myMap = myMap;
	
	}

	@Override
	public void action() {
		done = false;
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
	    	this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 100, list_agentNames, "HunterProtocol"));
		}
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		System.out.println("~~~~~~~~~~~~~~");
		System.out.println("je suis l'agent : " + this.myAgent.getLocalName());	
		//System.out.println("je suis à la position : " + ((AbstractDedaleAgent)this.myAgent).getCurrentPosition().getLocationId());
		
		
		// ~~~~~~~~~~~~~~~~~~~~~~~ MODE CAPTURE  ~~~~~~~~~~~~~~~~~~~~~~~
		// Si je recois un message de capture, j'adopte le mode capture
		MessageTemplate msgTemplate4 = MessageTemplate.and(
				MessageTemplate.MatchProtocol("CaptureGolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived4 = this.myAgent.receive(msgTemplate4);
		
		if (msgReceived4 != null) { // est une liste de la forme [position_golem, map]
			try {
				Map<String, Serializable> map = (Map<String, Serializable>) msgReceived4.getContentObject();
				// on recupère la position du golem
				position_golem = (String) map.get("position_golem");
				// et la map
				SerializableSimpleGraph<String, MapAttribute> sg = (SerializableSimpleGraph<String, MapAttribute>) map.get("map");
				// on merge les deux maps
				this.myMap.mergeMap(sg);
				// on trouve le chemin le plus court vers position_golem
				System.out.println();
				List<String> tmp = this.myMap.getShortestPath(myPosition.getLocationId() , position_golem );
				
				if (tmp.isEmpty()) {
                    ;
                }else {
             
					for (String nextNodeId : tmp) {
						// Si on arrive pas à acceder à un noeud, on break
						if (!((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNodeId))) {
							break;
						}
						((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNodeId));
						myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
						tmp = this.myMap.getShortestPath(myPosition.getLocationId() , position_golem );
						if (tmp.isEmpty()) {
							// Si on est arrivé au bout du chemin, on break
							break;
						}	
					}
                }
				if (!tmp.isEmpty()) {
					// on va envoyer un message au noeud qui bloque le chemin
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("WhoAreYouProtocol");
					msg.setSender(this.myAgent.getAID());
					for (String agentName : list_agentNames) {
						msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
					}
					// on envoie sa position
					try {
						msg.setContentObject((Serializable) myPosition);
	                    ((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
					
					// si on recoit un message de protocol who are you, on envoie sa position
					MessageTemplate msgTemplate5 = MessageTemplate.and(
							MessageTemplate.MatchProtocol("WhoAreYouProtocol"),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					ACLMessage msgReceived5 = this.myAgent.receive(msgTemplate5);
					if (msgReceived5 != null) {
						// on envoie sa position
						ACLMessage position_msg=new ACLMessage(ACLMessage.INFORM);
						position_msg.setProtocol("ACK_WhoAreYouProtocol");
						position_msg.setSender(this.myAgent.getAID());
						for (String agentName : list_agentNames) {
							position_msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
						}
						// on envoie sa position
						try {
							position_msg.setContentObject((Serializable) myPosition);
		                    ((AbstractDedaleAgent) this.myAgent).sendMessage(position_msg);
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }
					}
					
					// si je capture une la réponse
					MessageTemplate msgTemplate6 = MessageTemplate.and(
							MessageTemplate.MatchProtocol("ACK_WhoAreYouProtocol"),
							MessageTemplate.MatchPerformative(ACLMessage.INFORM));
					ACLMessage msgReceived6 = this.myAgent.receive(msgTemplate6);
					boolean golem = true;
					while (msgReceived6 != null) {
						Location noeud = null;
						try {
							noeud = (Location) msgReceived6.getContentObject();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						// je regarde si le noeud correspond à la position du golem
						if (noeud.getLocationId() == position_golem) {
							// on s'est trompé, ce n'était pas le golem
							golem = false;
							break;
						}
						msgTemplate6 = MessageTemplate.and(
								MessageTemplate.MatchProtocol("ACK_WhoAreYouProtocol"),
								MessageTemplate.MatchPerformative(ACLMessage.INFORM));
						msgReceived6 = this.myAgent.receive(msgTemplate6);
					}
					if (golem) {
						System.out.println("Golem capturé");
						((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(myPosition.getLocationId()));
                        done = true;
                        this.myAgent.doWait(1000000);
							
					}

				}
				
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
				
		
		
		if (myPosition!=null && done == false){
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
				this.myAgent.doWait(1990);
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
					//if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				
				}
			}
			
			// ~~~~~~~~~~~~~~~~~~~~~~~~~AGENTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			
//			System.out.println("my position : " + myPosition.getLocationId());
//	        System.out.println("noeuds_observable : " + noeuds_observable);
//	        System.out.println("liste_noeuds_agents : " + liste_noeuds_agents);
	        
			// ~~~~~~~~~~~~~~~~~~~~~~~GOLEM SANS ODEUR~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
	       // if (liste_position_odeur.isEmpty()) {
//            	
//	        	System.out.println("liste_position_odeur est vide");
//	        	
//	        	this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, this.list_agentNames));
//				
//				
//				
//				
//				// liste des noeuds à proximité qui sont des agents
//				List<Location> liste_noeuds_agents = new ArrayList<Location>();
//		    	
//		    	// message qu'on recoit (ou non)
//		    	MessageTemplate msgTemplate = MessageTemplate.and(
//						MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
//						MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
//				ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
//				
//				
//		    	// Si on recoit un message, un agent est à proximité donc on crée une guild
//				while (msgReceived != null ) {
//					
//					//System.out.println("J'ai recu un message");
//					Location noeud = null;
//					try {
//						noeud = (Location) msgReceived.getContentObject();
//						// ~~~~~~~~~~~~~~~ MODE GUILD ~~~~~~~~~~~~~~~~~
//						// j'ajoute le sender à ma guild
//						GuildMembers.add(msgReceived.getSender().getLocalName());
//						
//						// on partage notre prochain noeud 
//					
//					} catch (UnreadableException e) {
//						e.printStackTrace();
//					}
//					liste_noeuds_agents.add(noeud);
//					msgTemplate = MessageTemplate.and(
//							MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
//							MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
//					msgReceived = this.myAgent.receive(msgTemplate);
//					
//					
////					System.out.println("On a croisé un agent");
////					System.out.println("noeud agent : " + liste_noeuds_agents);
//				}
//				
//      
//		        // liste_position_golem = noeuds_observable - (liste_noeuds_agents) 
//				liste_noeuds_agents.add(myPosition);
//		        List<Location> liste_position_golem = new ArrayList<Location>(noeuds_observable);
//		        liste_position_golem.removeAll(liste_noeuds_agents);
//		        
//		        //System.out.println("liste_position_golem : " + liste_position_golem);
//		        
//		      
//				//if (nextNodeId==null){
//					// son prochain noeud est fait partie de la liste des possibles positions du golem si il y en a un
//				Random rand = new Random();
//					// Génère un index aléatoire entre 0 (inclus) et la taille de la liste (exclus)
//				int randomIndex = rand.nextInt(liste_position_golem.size());
//				nextNodeId = liste_position_golem.get(randomIndex).getLocationId();
//				    //System.out.println("Prochaine noeud choisit : " + nextNodeId);
//				//}
//				
//				
//			    if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
//			    	//System.out.println("Il ya un golem à la position : " + nextNodeId);
//	            	// on le suit
////			    	while (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))){
////			    		// tant qu'on peut pas y accèder, le golem est surement coincé on ne bouge pas
////			    		//System.out.println("Le golem est coincé");
////			    		((AbstractDedaleAgent)this.myAgent).moveTo(myPosition);
////			    	}
//	            	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId)); // si ils arrivent tjrs pas à a atteindre alors le golem est coincé
//	            	busy = true;
////	            	System.out.println("On suit le golem");
//			    }else {
//			    	//System.out.println("j'ai changé de position");
//			    	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
//			    }
	    //    } 
		
		 // ~~~~~~~~~~~~~~~~~~~~~~~~GOLEM ODEUR~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		    
		    
		//else {
			
			// ~~~~~~~~~~~~~~~~ COMMUNICATION ~~~~~~~~~~~~~~~~~~~~~~
			
			
			
			// liste des noeuds à proximité qui sont des agents
			List<Location> liste_noeuds_agents = new ArrayList<Location>();				    	

			this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, this.list_agentNames));
			
			// message qu'on recoit (ou non)
			MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			
			
			// Si on recoit un message, un agent est à proximité donc on crée une guild
			while (msgReceived != null ) {
				
				//System.out.println("J'ai recu un message");
				Location noeud = null;
				try {
					noeud = (Location) msgReceived.getContentObject();

				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				liste_noeuds_agents.add(noeud);
				msgTemplate = MessageTemplate.and(
						MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
				msgReceived = this.myAgent.receive(msgTemplate);
				
			}
			
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// On fait en sorte de ne pas aller sur les noeuds où il y a des agents
			// convertit en string des positions des agents
			List<String> string_location_agent = new ArrayList<String>();
			for (Location loc : liste_noeuds_agents) {
				string_location_agent.add(loc.getLocationId());
			}
			// convertit les noeuds observables en string
			List<String> string_location_observable = new ArrayList<String>();
			for (Location loc : noeuds_observable) {
				string_location_observable.add(loc.getLocationId());
			}
			// convertit en string la liste des positions des golems
			List<String> string_location_golem = new ArrayList<String>();
			for (Location loc : liste_position_odeur) {
				string_location_golem.add(loc.getLocationId());
			}
			
			string_location_golem.removeAll(string_location_agent); // on enlève les positions des agents
			string_location_observable.removeAll(string_location_agent); // on enlève les positions des agents
			
			// on reconvertit les positions des golems en location
			List<Location> liste_position_golem = new ArrayList<Location>();
			for (String loc : string_location_golem) {
		        liste_position_golem.add(new gsLocation(loc)); // liste_position_golem est la liste des noeuds avec une odeur mais sans agents
			}
			// on reconvertis les noeuds observables en location
			List<Location> liste_position_observable = new ArrayList<Location>();
			for (String loc : string_location_observable) {
				liste_position_observable.add(new gsLocation(loc)); 
																	
			}
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			
			// ~~~~~~~~~~~~~~ DEPLACEMENT ~~~~~~~~~~~~~~~~~~~
			
		    // si la liste est vide, behaviour golem sans odeur
			if (liste_position_golem.isEmpty()) {
				nextNodeId = liste_position_observable.get(0).getLocationId();
			}else {
				Random rand = new Random();
			    int randomIndex = rand.nextInt(liste_position_golem.size());
				nextNodeId = liste_position_golem.get(randomIndex).getLocationId();
			
			}

			if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {
				
				//System.out.println("le golem est à la position : " + nextNodeId);
				// ~~~~~~~~~~~~ MODE CAPTURE ~~~~~~~~~~~~~~~~~~~
				
				// Notre agent va partager la position du golem ainsi que sa map
				position_golem = nextNodeId;
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
				
				// On crée une liste contenant ces deux informations
				Map<String, Serializable> map = new HashMap<String, Serializable>();
				map.put("position_golem", position_golem);
				map.put("map", sg);
				
				// partage l'information avec tout les hunters
				
				ACLMessage msg_=new ACLMessage(ACLMessage.INFORM);
				msg_.setProtocol("CaptureGolemProtocol");
				msg_.setSender(this.myAgent.getAID());
				for (String agentName : list_agentNames) {
					msg_.addReceiver(new AID(agentName,AID.ISLOCALNAME));	
				}
				try {
					msg_.setContentObject((Serializable) map);
					((AbstractDedaleAgent) this.myAgent).sendMessage(msg_);
				} catch (IOException e) {
					e.printStackTrace();
				}				
				
				((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(myPosition.getLocationId()));
			
				
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				
			

		    }else {
		    	
		    	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
		    }
			
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		    
		    
		}		
	//}
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