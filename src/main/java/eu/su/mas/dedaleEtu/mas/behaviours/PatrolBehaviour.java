package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.core.Node;
import dataStructures.tuple.Couple;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;


public class PatrolBehaviour extends OneShotBehaviour{

    
    private static final long serialVersionUID = 8567689731496787661L;

    /* Le Chasseur cherche un golem */

    private List<String> list_agentNames;
    private int exitValue = 0; // on ne fait rien par défaut
    private List<Location> liste_noeuds_agents;
    private String position_golem;
    private MapRepresentation myMap;

    public PatrolBehaviour(final AbstractDedaleAgent myagent, List<String> list_agentNames, String position_golem, MapRepresentation myMap) {
		super(myagent);
		this.list_agentNames = list_agentNames;
        this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
        this.myMap = ((AgentFsm)this.myAgent).getMyMap();
		
	}


    public void action(){
    	this.liste_noeuds_agents = new ArrayList<Location>();
        this.myMap = ((AgentFsm)this.myAgent).getMyMap();
        this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
       
        // ~~~~~~~~~~~~~~~~~~~~ Step 1 : On envoie sa position ~~~~~~~~~~~~~~~~~~~~
        this.exitValue = 2;

        // ~~~~~~~~~~~~~~~~~~~~ Step 2 : On check ses messages ~~~~~~~~~~~~~~~~~~~~
        if (checkMessage_position2()) {
        	return;
        }
		
        
        checkMessage_position() ;
		
		
		//checkMessage_position();
//        if (mistake()) {
//            ((AgentFsm)this.myAgent).setPosition_golem(""); 
//			return;
//        }
//       
//        if (checkMessage_need_help()) { // j'ai reçu un appel à l'aide
//            // la position du golem n'est pas un agent dont j'ai reçu la position
//            this.exitValue = 5; // on va aider pour bloquer le golem, on va a CatchGolem               
//            return; 
//        }
//        
        System.out.println("-----position des autres agents : " + liste_noeuds_agents);
        
        // ~~~~~~~~~~~~~~~~~~~~ Step 3 : On cherche où le golem pourrait être ~~~~~~~~~~~~~~~~~~~~

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
        // Pour supprimer notre position
        if (!noeuds_observable.isEmpty()) {
            noeuds_observable.remove(0); 
        }
        if (!liste_position_odeur.isEmpty()) {
            liste_position_odeur.remove(0);
        }
        
        try {
            this.myAgent.doWait(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			
        /* Pour ne pas aller sur les mêmes noeuds que les autres agents et être efficace */
        String nextNodeId=null;
        
        if (!liste_noeuds_agents.isEmpty()) {
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
	        if (liste_position_observable.isEmpty()) {
	         	// je reste sur place
	         	return;
	        }
	
	        // ~~~~~~~~~~~~~~~~~~~~ Step 4 : On se déplace ~~~~~~~~~~~~~~~~~~~~
	
	        if (liste_position_golem.isEmpty()) { // si la liste des odeurs est vide
	            // on va vers un noeud aléatoire
	            Random rand = new Random();
	            int randomIndex = rand.nextInt(liste_position_observable.size());
	            nextNodeId = liste_position_observable.get(randomIndex).getLocationId();
	        }else {
	            // on va vers un noeud avec une odeur
	            Random rand = new Random();
	            int randomIndex = rand.nextInt(liste_position_golem.size());   
	            nextNodeId = liste_position_golem.get(randomIndex).getLocationId();
	        }
		} else {
			// on va vers un noeud aléatoire
			Random rand = new Random();
			int randomIndex = rand.nextInt(noeuds_observable.size());
			nextNodeId = noeuds_observable.get(randomIndex).getLocationId();
		}
        
       
        // Si je peux pas avancer alors c'est que j'ai trouvé un golem
        if (!((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId))) {

//            
            if (!checkFalseInformation()) {
        		//  on me prend pour un golem -> je doit bouger 
        		Random rand = new Random();
        		int randomIndex = rand.nextInt(noeuds_observable.size());
        		nextNodeId = noeuds_observable.get(randomIndex).getLocationId();
        		((AbstractDedaleAgent)this.myAgent).moveTo(new gsLocation(nextNodeId));
        	}else{
                // un agent bloque le golem, peut être que ma position est inutile
        		// si oui je vais au moins essayer de catch le golem
        		// si non je vais bloquer
                System.out.println("---- this.exitvalue : " + this.exitValue);
                return;
            }
        	
           
            ((AgentFsm)this.myAgent).setPosition_golem(nextNodeId); // on enregistre la position du golem 
//            
//            // j'envoie la position du golem pour de l'aide
            System.out.println(this.myAgent.getLocalName() + " : A L'AAIDEEEEEEE !");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol("NeedHelpProtocol");
            msg.setSender(this.myAgent.getAID()); 
            for (String agentName : this.list_agentNames) {
                msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
            }
            try {
                msg.setContent(nextNodeId);
                ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            this.exitValue = 3; // on va bloquer le golem
           
        }
        System.out.println(" ------------------- " + this.myAgent.getLocalName() + "PatrolBehaviour");
        
        
    }

    public void checkMessage_position(){
        // Message de position
        MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("SendPositionProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    	
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        while (msgReceived != null) {
            try {
                String pos = msgReceived.getContent();
                // ajout de la position de l'agent dans la liste
                if (pos == ""){
                    return;
                }
                liste_noeuds_agents.add(new gsLocation(pos));
              
            }catch(Exception e) {
                e.printStackTrace();
            }
            msgTemplate = MessageTemplate.and(
    				MessageTemplate.MatchProtocol("SendPositionProtocol"),
    				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            msgReceived = this.myAgent.receive(msgTemplate);
           
        }
       
    }
    
    public boolean checkMessage_position2() {    
        // j'ai rencontré un explo
        MessageTemplate msgTemplate2 = MessageTemplate.and(
    				MessageTemplate.MatchProtocol("HelloProtocol"),
    				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived2 = this.myAgent.receive(msgTemplate2);
        if (msgReceived2 != null) {
            try {
                String m = msgReceived2.getContent();
                String pos = getPosString(m);
                // ajout de la position de l'agent dans la liste
                liste_noeuds_agents.add(new gsLocation(pos));
                this.exitValue = 13; // on va répondre
                return true; 
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
 
        return false;
    }

    
    
    public boolean checkFalseInformation() {
    	Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        
    	MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("I_Am_An_AgentBlockGolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null && msgReceived.getContent() != "") {
            // je vérifie que l'agent ne me prend pas pour un golem
        	try {
        		String loc = msgReceived.getContent(); // loc du golem
        		String maLoc = myPosition.getLocationId();
        		
        		if (loc.equals(maLoc)) { // on me prend pour un golem
        			System.out.println(this.myAgent.getLocalName() + "JE NE SUIS PAS LE GOLEM AARRRG");
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
        			//System.out.println(this.myAgent.getLocalName() + " : ---- JE NE SUIS PAS LE GOLEM");
        			return false;
        		}else { // on ne me prend pas pour un golem
        			
        			// est-ce que ma position est indispensable ?
        			// je check si le golem est autour de moi
        			
        			((AgentFsm)this.myAgent).setPosition_golem((String) msgReceived.getContent());
        			
        			
        			// Liste des observables
            		List<Couple<Location,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
            		// Liste des noeuds observables
                    List<Location> noeuds_observable = new ArrayList<Location>();
                    for (Couple<Location, List<Couple<Observation, Integer>>> observable : lobs) {
                        noeuds_observable.add(observable.getLeft()); 
                    }
                    // on convertit en string
                    List<String> string_location_observable = new ArrayList<String>();
                    for (Location loca : noeuds_observable) {
                        string_location_observable.add(loca.getLocationId());
                    }
                    	
                    if ((string_location_observable.contains(((AgentFsm)this.myAgent).getPosition_golem()))) {
                    	// je bloque
                    	this.exitValue = 7; 
                    }else {
                    	// j'essaye d'aller catch le golem
                    	this.exitValue = 5;
                    }
        			
        			return true;
        			
        		}   
        	}catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    	return false; // 
    }

    public boolean checkMessage_need_help(){
    
        // Message de blocage de golem
        MessageTemplate msgTemplate2 = MessageTemplate.and(
				MessageTemplate.MatchProtocol("NeedHelpProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived2 = this.myAgent.receive(msgTemplate2);
        if (msgReceived2 != null) {
            try {
            	// ------ je vérifie si la position du golem n'est pas un agent
				if (liste_noeuds_agents.contains(new gsLocation(msgReceived2.getContent()))) {
					// je ne fais rien
					return false;
				}else {   	
                   // System.out.println(this.myAgent.getLocalName() + " : J'ARRIVE AIDER !!!!!");
                
	            	((AgentFsm)this.myAgent).setPosition_golem((String) msgReceived2.getContent());
	                return true; 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
            
            
        }
        
        return false;
    } 


    public static String getPosString(String chaine) {
        int indexVirgule = chaine.indexOf(',');
        if (indexVirgule != -1) { // Si une virgule est trouvée
            return chaine.substring(0, indexVirgule);
        } else { // Si aucune virgule n'est trouvée
            return "-1";
        }
    }

    public boolean mistake() {
    	MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("Je_Ne_Suis_Pas_Un_GolemProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if (msgReceived != null) {
        	System.out.println("MY BAD");
        	return true;
        }
        return false;
    }

    // public Boolean checkMessageWhoAreYou(){
    //     // Message de position
    //     MessageTemplate msgTemplate = MessageTemplate.and(
    //             MessageTemplate.MatchProtocol("WhoAreYouProtocol"),
    //             MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    //     ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
    //     if (msgReceived != null) {
    //         try {
    //             ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    //             msg.setProtocol("I_AM_AgentProtocol");
    //             msg.setSender(this.myAgent.getAID());
    //             for (String agentName : this.list_agentNames) {
    //                 msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
    //             }
    //             try {
    //                 msg.setContent(((AbstractDedaleAgent)this.myAgent).getCurrentPosition().toString());
    //                 ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }catch(Exception e) {
    //             e.printStackTrace();
    //         }
    //         return true;
    //     } // j'envoie ma pos
    //     return false;
    // }

    // public Boolean checkMessageReponse(){
    //     // Message de position
    //     MessageTemplate msgTemplate = MessageTemplate.and(
    //             MessageTemplate.MatchProtocol("I_AM_AgentProtocol"),
    //             MessageTemplate.MatchPerformative(ACLMessage.INFORM));
    //     ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
    //     if (msgReceived != null) {
    //         try {
    //             String pos = msgReceived.getContent();
    //             // ajout de la position de l'agent dans la liste
    //             liste_noeuds_agents.add(new gsLocation(pos));
    //         }catch(Exception e) {
    //             e.printStackTrace();
    //         }
    //         return true;
    //     }
    //     return false;
    // }


    

    @Override
	public int onEnd() {
		return exitValue;
	}
}