package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.HunterAgent;
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
	
	//private boolean busy; // si l'agent est occupé à suivre un golem
	private List<String> list_agentNames;

	
	
	public FollowGolemBehaviour(final AbstractDedaleAgent myagent, List<String> list_agentNames ) {
		super(myagent);
		//this.busy = false;
		this.list_agentNames = list_agentNames;
	
	}
	


	@Override
	public void action() {
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null) {
			
			System.out.println("---------------------------------------------");
			List<Location> liste_noeuds_agents = new ArrayList<Location>();
			List<Location> liste_noeuds_accessibles = new ArrayList<Location>();
			
	        // Liste des observables
	        List<Couple<Location, List<Couple<Observation, Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
	        // liste des positions observables
	        List<Location> locations = new ArrayList<>();
	        for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
	            locations.add(observable.getLeft()); 
	        }
	        System.out.println("locations observable : " + locations);
	        // pour voir ce que fait l'agent
	        try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        
	        
	        /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ IDEE ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			 * Le but est de determiner si les agents autour de moi sont un explo ou un golem
			 * Notre agent va envoyer un ping
			 * Si notre agent recoit un message (contenant la position de l'agent) alors il y a au moins un agent explo autour de lui
			 * On ajoute cette position à la liste_noeuds_agents
			 * Au final, l'idée est de comparer la liste des noeuds accesibles + la liste des noeuds_agents avec la liste des noeuds observables
			 * Si location - (liste_noeuds_accessibles + liste_noeuds_agents) != 0, alors il y a un golem à la position golemString[0:]
			 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
			
	        
			// fait un ping pour découvrir qui est autour de moi
	        System.out.println("list_agentNames : " + list_agentNames);
	    	this.myAgent.addBehaviour(new SayHelloBehaviour(this.myAgent, 500, this.list_agentNames, "Ping"));
	    	
	    	
	    	// message qu'on recoit (ou non)
	    	MessageTemplate msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACK_Ping"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
			ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
			
			
	    	// Noeuds agents
			if (msgReceived != null ) {
				// on ajoute à la liste
				Location noeud = null;
				try {
					noeud = (Location) msgReceived.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				liste_noeuds_agents.add(noeud);
				System.out.println("On à croisé un agent");
				System.out.println("noeud agent : " + liste_noeuds_agents);
			}
			
			// Noeuds accessibles
	        Iterator<Couple<Location, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
	        while (iter.hasNext()) {
	        	//Couple<Location, List<Couple<Observation, Integer>>> agent = iter.next();
	        	// on ajoute à la liste des noeuds accesibles
	        	Location accessibleNode=iter.next().getLeft();
	        	liste_noeuds_accessibles.add(accessibleNode);    	
	        }
	        
	        
	        System.out.println("liste_noeuds_accessibles : " + liste_noeuds_accessibles);
	        
	        // On convertit liste_noeuds_accessibles et liste_noeuds_agents en List<String>
//	        List<String> accessiblesString = liste_noeuds_accessibles.stream().map(Location::toString).collect(Collectors.toList());
//	        List<String> agentsString = liste_noeuds_agents.stream().map(Location::toString).collect(Collectors.toList());
//	
	        // On fusionne les deux listes en une seule
	        List<Location> nonGolemString = new ArrayList<>(liste_noeuds_accessibles);
	        // nonGolemString contient les identifiants de tous les noeuds non golems
	        nonGolemString.addAll(liste_noeuds_agents); 
	        System.out.println("-----------------");
	       					
	       
	        // On soustrait de locations
	        List<Location> liste_golem = new ArrayList<>(locations); 
	        liste_golem.removeAll(nonGolemString); 
	        System.out.println("liste_golem : " + liste_golem);
	
	        
	        if (!liste_golem.isEmpty()) {
	        	System.out.println("il y a un Golem à la position : " + liste_golem.get(0));
	        	// on le suit
	        	((AbstractDedaleAgent)this.myAgent).moveTo(liste_golem.get(0));
	        	System.out.println("On suit le golem");
	        	
	        }else {
	        
		        //Random move from the current position
				Random r= new Random();
				int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
	
				//The move action (if any) should be the last action of your behaviour
				((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			
	        }

			
	  
	        	// si il recoit une rep, on le calcule pas
	        	// si il reçoit rien 
	        	// PAS D'ODEUR -> PING PUIS TEST MOVE CASE : SI QLQUN -> GOLEM ----> autre agent calcule plus petit chemin pour bloquer le golem de l'autre coté
	        	
	        	// si il y a un agent à coter de notre agent qui repond pas, on le suit
	        	// si il y a pas d'agent mais qu'il y a une odeur, on suit l'odeur
	        	
	        	
	        	
	        	// si il recoit rien mais il peut pas aller a la case
	//        	if (this.liste_golem.contains(golem)) {
	//        		// si le golem est dans la liste des golems
	//        		// on va vers lui
	//        		((AbstractDedaleAgent)this.myAgent).moveTo(golem.getLeft());
	//        		this.liste_golem.remove(golem);
	//        		return;
	//        	}
	//        }
	//        
	//		if (this.liste_golem.isEmpty()) {
	//			// ajout d'un comportement explo ?
	//			this.finished = true;
	//			return;
	//		}else {
	//			
	//			
	//		}
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
