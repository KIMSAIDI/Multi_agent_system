package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class GuildHunter extends SimpleBehaviour {
	
	/* Détermine le comportement collectif des Hunter */

    private static final long serialVersionUID = -2058134622078521998L;
    

    private Agent myAgent;
    private List<String> guildMembers;
    private List<Location> liste_position_odeur;		
    private boolean busy = false;
    private boolean finished = false;


   
    public GuildHunter(final Agent myagent, List<String> guildMembers, List<Location> liste_position_odeur, boolean busy) {
		this.myAgent=myagent;
		this.guildMembers=guildMembers;
		this.liste_position_odeur = liste_position_odeur;
		this.busy = busy;
    }

    @Override
    public void action() {
    	
    	// La guild avance collecivement
    	
    	
    	// soit un agent est sur la liste d'un golem
    	if (busy) {
    		
    		// commnique les positions possible du golem
    		;
    	
    	
    	
    	}
    	
    	
    	else {
    		
    		// avance en groupe
    		;
    	}
    	
    	
    	;
    	
    
    
    
    }

    
    
    @Override
	public boolean done() {
		// TODO Auto-generated method stub
		return !finished;
	}

}
