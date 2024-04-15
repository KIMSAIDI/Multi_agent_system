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
	    	this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 100, list_agentNames, "HunterProtocol"));
		}
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println("~~~~~~~~~~~~~~");
		System.out.println("je suis l'agent : " + this.myAgent.getLocalName());	
		
		
		// ~~~~~~~~~~~~~~~~~~~~~~~ MODE CAPTURE ~~~~~~~~~~~~~~~~~~~~~~~
		
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
				List<String> tmp = this.myMap.getShortestPath(myPosition.getLocationId() , position_golem );
				
				// on parcourt le chemin jusqu'à arriver à position_golem
				if (tmp != null && !tmp.isEmpty()) {
					for (String nextNodeId : tmp) {
						// Si on arrive pas à acceder à un noeud, on break
						if (!((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nextNodeId))) {
							break; // soit c'est un agent qui bloque soit c'est le golem
						}else {
							myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
							// on recalcule le chemain le plus court à partir de notre nouvelle position
							tmp = this.myMap.getShortestPath(myPosition.getLocationId() , position_golem );
							if (tmp.isEmpty()) {
								// Si on est arrivé au bout du chemin, on break
								break;
							}
						}
	
					}
				}
				if (tmp != null && !tmp.isEmpty()) { // ça veut dire qu'on a break avant la fin du chemin jusqu'à position_golem
					// on va envoyer un message pour savoir qui nous entoure
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("WhoAreYouProtocol");
					msg.setSender(this.myAgent.getAID());
					for (String agentName : list_agentNames) {
						msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
					}
					// on envoie sa position
					try {
						msg.setContentObject((Serializable) myPosition); // envoyer sa position ici ça sert un peu à rien
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
							// on s'est trompé, ce n'était pas le golem mais un autre agent
							golem = false;
							break;
						}
						msgTemplate6 = MessageTemplate.and(
								MessageTemplate.MatchProtocol("ACK_WhoAreYouProtocol"),
								MessageTemplate.MatchPerformative(ACLMessage.INFORM));
						msgReceived6 = this.myAgent.receive(msgTemplate6);
					}
					if (golem) { // si on a pas reçu de message de la position qui bloque alors c'est le golem
						System.out.println(" le Golem doit être capturé");
						// je reste sur place pour le bloquer
						((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(myPosition.getLocationId()));
                    
                       	this.myAgent.doWait();
					}

					}
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
				
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
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

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
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
				}
			}
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
			// liste des noeuds à proximité qui sont des agents
			List<Location> liste_noeuds_agents = new ArrayList<Location>();				    	

			this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, this.list_agentNames));
			
			// message qu'on recoit (ou non)
			MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			// Si on recoit un message, un agent est à proximité
			while (msgReceived != null ) {
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
			// On fait en sorte de ne pas aller sur les noeuds où il y a des agents
			// obligé de convertir en string pour pouvoir comparer
			
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
			// on reconvertit les noeuds observables en location
			List<Location> liste_position_observable = new ArrayList<Location>();
			for (String loc : string_location_observable) {
				liste_position_observable.add(new gsLocation(loc)); 
																	
			}
			
			// ~~~~~~~~~~~~~~ DETECTION GOLEM ~~~~~~~~~~~~~~~~~~~
			
		    // si la liste est vide, behaviour golem sans odeur
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
				// si je ne peux pas aller sur un noeud, c'est un golem
				
				// ~~~~~~~~~~~~ MODE CAPTURE ~~~~~~~~~~~~~~~~~~~
				
				// Notre agent va partager la position du golem ainsi que sa map
				position_golem = nextNodeId; // position du golem
				SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph(); // map
				
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
				// on reste sur place pour tenter de le bloquer
				((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(myPosition.getLocationId()));
				
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
		    }else {
		    	
		    	((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
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