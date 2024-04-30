package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.env.Location;

import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;


public class CatchGolem extends OneShotBehaviour{

        
        private static final long serialVersionUID = 8567689731496787661L;
    
        /* Le Chasseur va aider pour bloquer un golem */


        private String position_golem;
        private MapRepresentation myMap;
        private int exitValue = 0; 
        private List<String> list_pos_agents_block;

        public CatchGolem(final AbstractDedaleAgent myagent, String position_golem, MapRepresentation myMap, List<String> list_pos_agents_block) {
            super(myagent);
            this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
            this.myMap = ((AgentFsm)this.myAgent).getMyMap();
            this.list_pos_agents_block = ((AgentFsm)this.myAgent).getList_pos_agents_block();
        }

        public void action(){
            this.myAgent.doWait(400);
        	this.exitValue = 0;
            this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
        	this.myMap = ((AgentFsm)this.myAgent).getMyMap();
            this.list_pos_agents_block = ((AgentFsm)this.myAgent).getList_pos_agents_block();
            Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
            
            // On trouve le chemin le plus court pour aller aider
            if (this.position_golem == "" || this.position_golem.isEmpty() || !this.myMap.isNode(this.position_golem)) {
                //this.exitValue = 5; // On a pas de golem à aider
                return;
            }
            List<String> path;
            SerializableSimpleGraph<String,MapAttribute> graph_temp = this.myMap.getSerializableGraph();
            try{
                this.myMap.removeNode(this.list_pos_agents_block.get(0));
                path = this.myMap.getShortestPath(myPosition.getLocationId() , ((AgentFsm)this.myAgent).getPosition_golem());
                this.myMap.mergeMap(graph_temp);
            }catch(Exception e){
                return;
                //System.out.println("Erreur lors de la suppression du noeud");
            }
			if (path != null && !path.isEmpty()){
                
				for (String nodeId : path){
                    if (!((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nodeId))) {
                        this.exitValue = 6; // On check si c'est un golem ou un agent qui bloque
                        return; 
                        
                    }else {
                        myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
                        // on recalcule le chemin le plus court à partir de notre nouvelle position
                        path = this.myMap.getShortestPath(myPosition.getLocationId() , ((AgentFsm)this.myAgent).getPosition_golem() );
                        if (path.isEmpty()) {
                            // Si on est arrivé au bout du chemin, on à pas trouver de golem, on retourne patrouiller
                        	((AgentFsm)this.myAgent).setPosition_golem("");
                        	break;
                            
                        }
                    }
                    this.myAgent.doWait(400);


                }
            }
            
			System.out.println(this.myAgent.getLocalName() + " : ------ CATCHGOLEM ---- JE SUIS ARRIVE A DESTINATION");
			
        }


        @Override
        public int onEnd() {
            return this.exitValue;
        }
}
