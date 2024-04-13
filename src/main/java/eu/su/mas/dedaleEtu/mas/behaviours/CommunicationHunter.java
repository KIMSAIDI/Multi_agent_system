package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class CommunicationHunter extends SimpleBehaviour {
	
	
	public static final long serialVersionUID = 8567689731496787661L;
	
	private MapRepresentation myMap;
	private List<String> list_agentNames;
	List<Location> liste_noeuds_agents;
	
	
	public CommunicationHunter(final Agent myAgent, List<String> list_agentNames, MapRepresentation myMap, List<Location> liste_noeuds_agents) {
		super(myAgent);
		this.list_agentNames = list_agentNames;
		this.myMap = myMap;
		this.liste_noeuds_agents = liste_noeuds_agents;
		
		
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
		
		this.myAgent.addBehaviour(new ReceiveMsg(this.myAgent, this.myMap, this.list_agentNames));
		
		
		// message qu'on recoit (ou non)
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
		ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
		
		
		// Si on recoit un message, un agent est à proximité donc on crée une guild
		while (msgReceived != null ) {
			
			System.out.println("J'ai recu un message");
			Location noeud = null;
			try {
				noeud = (Location) msgReceived.getContentObject();
				
				// ~~~~~~~~~~~~~~~ MODE GUILD ~~~~~~~~~~~~~~~~~
				// j'ajoute le sender à ma guild
				//GuildMembers.add(msgReceived.getSender().getLocalName());

			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			liste_noeuds_agents.add(noeud);
			msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ACK_HunterProtocol"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));	
			msgReceived = this.myAgent.receive(msgTemplate);
			
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
}