package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CommunicationHunter extends SimpleBehaviour {
    
    private static final long serialVersionUID = 8567689731496787661L;
    
    private List<String> list_agentNames;
    private List<Location> liste_noeuds_agents;

    public CommunicationHunter(final Agent myAgent, List<String> list_agentNames, List<Location> liste_noeuds_agents) {
        super(myAgent);
        this.list_agentNames = list_agentNames;
        this.liste_noeuds_agents = liste_noeuds_agents;
    }

    @Override
    public void action() {
        // ~~~~~~~~~~~ Step 1 : SayHello ~~~~~~~~~~~
        ACLMessage hello_msg=new ACLMessage(ACLMessage.INFORM);
        hello_msg.setProtocol("HunterProtocol");
        hello_msg.setSender(this.myAgent.getAID());
        for (String agentName : list_agentNames) {
            hello_msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
        }
        try {
            hello_msg.setContentObject("HunterProtocol");
            ((AbstractDedaleAgent) this.myAgent).sendMessage(hello_msg);
            this.myAgent.doWait(500); // wait 500ms
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ~~~~~~~~~~~ Step 2 : HelloBackProtocol ~~~~~~~~~~~
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol("HunterProtocol"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msg = this.myAgent.receive(mt);
        if (msg != null) {
            // on répond hello et on envoie sa position
            ACLMessage ack_msg=new ACLMessage(ACLMessage.CONFIRM);
            ack_msg.setProtocol("HelloBackProtocol");
            ack_msg.setSender(this.myAgent.getAID());
            ack_msg.addReceiver(msg.getSender());
            try {
                ack_msg.setContentObject(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
                ((AbstractDedaleAgent) this.myAgent).sendMessage(ack_msg);
                this.myAgent.doWait(500); // On attend la réponse
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ~~~~~~~~~~~ Step 3 : ACKHelloBackProtocol ~~~~~~~~~~~
        mt = MessageTemplate.and(MessageTemplate.MatchProtocol("HelloBackProtocol"),
                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
        msg = this.myAgent.receive(mt);
        if (msg != null) {
            // on renvoie sa position
            ACLMessage ack_msg=new ACLMessage(ACLMessage.CONFIRM);
            ack_msg.setProtocol("ACKHelloBackProtocol");
            ack_msg.setSender(this.myAgent.getAID());
            ack_msg.addReceiver(msg.getSender());
            try {
                ack_msg.setContentObject(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
                ((AbstractDedaleAgent) this.myAgent).sendMessage(ack_msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                // on récupère la position de l'agent
                Location loc = (Location) msg.getContentObject();
                // on ajoute la position de l'agent à la liste des noeuds
                this.liste_noeuds_agents.add(loc);
            } catch (UnreadableException e) {
                
                e.printStackTrace();
            }
        }

        // ~~~~~~~~~~~ Step 4 : ReceiveACKHelloBack ~~~~~~~~~~~
        mt = MessageTemplate.and(MessageTemplate.MatchProtocol("ACKHelloBackProtocol"),
                MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));
        msg = this.myAgent.receive(mt);
        if (msg != null) {
            try {
                // on récupère la position de l'agent
                Location loc = (Location) msg.getContentObject();
                // on ajoute la position de l'agent à la liste des noeuds
                this.liste_noeuds_agents.add(loc);
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Location> getListeNoeudsAgents() {
        // Supprimer les doublons de la liste des noeuds
    	
    	// on convertit en string pour pouvoir comparer
    	List <String> liste_noeuds_agents_string = new ArrayList<String>();
    	for (Location loc : liste_noeuds_agents) {
    		liste_noeuds_agents_string.add(loc.toString());
    	}
        Set<String> set = new HashSet<>(liste_noeuds_agents_string);
        liste_noeuds_agents_string.clear();
        liste_noeuds_agents_string.addAll(set);
        
        // on convertit en location
        List<Location> liste_noeuds_agents = new ArrayList<Location>();
		for (String loc : liste_noeuds_agents_string) {
			liste_noeuds_agents.add(new gsLocation(loc));
		}
		
        return liste_noeuds_agents;
    }
	   

    @Override
    public boolean done() {
        return false;
    }
}
