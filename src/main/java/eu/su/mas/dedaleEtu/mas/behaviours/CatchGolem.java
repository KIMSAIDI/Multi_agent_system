package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import eu.su.mas.dedale.env.Location;

import eu.su.mas.dedale.env.gs.gsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.AgentFsm;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;


public class CatchGolem extends OneShotBehaviour{

        
        private static final long serialVersionUID = 8567689731496787661L;
    
        /* Le Chasseur va aider pour bloquer un golem */


        private String position_golem;
        private MapRepresentation myMap;
        private int exitValue = 0; 

        public CatchGolem(final AbstractDedaleAgent myagent, String position_golem, MapRepresentation myMap) {
            super(myagent);
            this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
            this.myMap = ((AgentFsm)this.myAgent).getMyMap();
        }

        public void action(){
            this.position_golem = ((AgentFsm)this.myAgent).getPosition_golem();
        	this.myMap = ((AgentFsm)this.myAgent).getMyMap();
        	
            Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
            
//			 if (this.position_golem == "") {
//			 	System.out.println("======ERREUR : je n'ai pas de position");
//			 	return;
//			 }
            // On trouve le chemin le plus court pour aller aider
            List<String> path = this.myMap.getShortestPath(myPosition.getLocationId() , ((AgentFsm)this.myAgent).getPosition_golem());
			if (path != null && !path.isEmpty()){
                
				for (String nodeId : path){
                    if (!((AbstractDedaleAgent) this.myAgent).moveTo(new gsLocation(nodeId))) {
                        this.exitValue = 6; // On check si c'est un golem ou un agent qui bloque
                        //return; 
                        break;
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
                    try {
                        this.myAgent.doWait(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
			//this.exitValue = 0;
			System.out.println(this.myAgent.getLocalName() + " : ------ CATCHGOLEM ---- JE SUIS ARRIVE A DESTINATION");
			
        }


        @Override
        public int onEnd() {
            return exitValue;
        }
}
