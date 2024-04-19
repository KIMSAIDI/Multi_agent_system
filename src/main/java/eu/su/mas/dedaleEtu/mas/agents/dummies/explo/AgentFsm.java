package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.InitBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.BlockGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.CatchGolem;
import eu.su.mas.dedaleEtu.mas.behaviours.PatrolBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosition;
import eu.su.mas.dedaleEtu.mas.behaviours.CheckGolem;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class AgentFsm extends AbstractDedaleAgent {
    private static final long serialVersionUID = 8567689731496787661L;
    private MapRepresentation myMap;
   
    private int nbAgent;
	private List<String> list_agentNames;
	private List<Behaviour> lb;
	private FSMBehaviour fsm;

    private String position_golem = "";


    private static final String STATE_INIT = "STATE_INIT";
    private static final String STATE_EXPLORE = "STATE_EXPLORE";
    private static final String STATE_SEND_MAP = "STATE_SEND_MAP";
    private static final String STATE_RECEIVE_MAP = "STATE_RECEIVE_MAP";
    private static final String STATE_SAYHELLO = "STATE_SAYHELLO";
    
    private static final String STATE_BLOCK_GOLEM = "STATE_BLOCK_GOLEM";
    private static final String STATE_CATCH_GOLEM = "STATE_CATCH_GOLEM";
    private static final String STATE_SENDPOSITION = "STATE_SENDPOSITION";
    private static final String STATE_PATROL = "STATE_PATROL";
    private static final String STATE_CHECK_GOLEM = "STATE_CHECK_GOLEM";



    protected void setup(){
        super.setup();
		
		final Object[] args = getArguments();
		System.out.println("Arg given by the user to "+this.getLocalName()+": "+args[2]);
		
		
		List<String> list_agentNames=new ArrayList<String>();
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}
		this.nbAgent = list_agentNames.size();
		this.list_agentNames = list_agentNames;

        fsm = new FSMBehaviour(this);

        // Definition des etats
        fsm.registerFirstState(new InitBehaviour(), STATE_INIT);
        fsm.registerState(new ExploCoopBehaviour(this, this.myMap, this.list_agentNames), STATE_EXPLORE);
        fsm.registerState(new ShareMapBehaviour(this, this.myMap, list_agentNames), STATE_SEND_MAP);
        fsm.registerState(new ReceiveMapBehaviour(this), STATE_RECEIVE_MAP);
        fsm.registerState(new SayHelloBehaviour(this, this.list_agentNames), STATE_SAYHELLO);
        
        fsm.registerState(new PatrolBehaviour(this, list_agentNames, this.position_golem , this.myMap), STATE_PATROL);
        fsm.registerState(new BlockGolemBehaviour(this, list_agentNames, this.position_golem), STATE_BLOCK_GOLEM);
        fsm.registerState(new SendPosition(this, list_agentNames), STATE_SENDPOSITION);
        fsm.registerState(new CatchGolem(this, this.position_golem, this.myMap), STATE_CATCH_GOLEM);
        fsm.registerState(new CheckGolem(this, this.list_agentNames), STATE_CHECK_GOLEM);
        // behaviour tracking jusqu'au golem / position utile (?)
        
        // Definition des transitions
        fsm.registerDefaultTransition(STATE_INIT, STATE_EXPLORE);
        fsm.registerDefaultTransition(STATE_EXPLORE, STATE_EXPLORE);
        
        // ~~~~~~~~~~~~~~~~~~~ Exploration ~~~~~~~~~~~~~~~~~~~
        fsm.registerTransition(STATE_EXPLORE, STATE_SAYHELLO, 1);
        fsm.registerDefaultTransition(STATE_SAYHELLO,STATE_EXPLORE);//Back to explo
        fsm.registerTransition(STATE_EXPLORE, STATE_PATROL, 10);
        // ~~~~~~~~~~~~~~~~~~~ Hunt ~~~~~~~~~~~~~~~~~~~
        // SendPosition
        fsm.registerDefaultTransition(STATE_PATROL, STATE_PATROL);
        fsm.registerTransition(STATE_PATROL, STATE_SENDPOSITION, 2); 
        fsm.registerDefaultTransition(STATE_SENDPOSITION, STATE_PATROL);
        
        // BlockGolem
        fsm.registerDefaultTransition(STATE_BLOCK_GOLEM, STATE_BLOCK_GOLEM);
        fsm.registerTransition(STATE_PATROL, STATE_BLOCK_GOLEM, 3);
        fsm.registerTransition(STATE_BLOCK_GOLEM, STATE_PATROL, 4);

        // CatchGolem
        fsm.registerTransition(STATE_PATROL, STATE_CATCH_GOLEM, 5);
        fsm.registerDefaultTransition(STATE_CATCH_GOLEM, STATE_PATROL);

        // CheckGolem
        fsm.registerTransition(STATE_CATCH_GOLEM, STATE_CHECK_GOLEM, 6);
        fsm.registerTransition(STATE_CHECK_GOLEM, STATE_BLOCK_GOLEM, 7);
        fsm.registerDefaultTransition(STATE_CHECK_GOLEM, STATE_PATROL);
        

        this.lb = new ArrayList<Behaviour>();
        this.lb.add(fsm);
        addBehaviour(new startMyBehaviours(this, this.lb));
        System.out.println("the  agent "+this.getLocalName()+ " is started");

    }

    public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    public MapRepresentation getMyMap() {
    	
        return this.myMap;
    }
    
    public FSMBehaviour getFSM() {
		return fsm;
	}

    public void setPosition_golem(String position_golem) {
        this.position_golem = position_golem;
    }

    public String getPosition_golem() {
        return this.position_golem;
    }

    public void setNbAgent(int nbAgent) {
        this.nbAgent = nbAgent;
    }

    public int getNbAgent() {
        return nbAgent;
    }

    public void setList_agentNames(List<String> list_agentNames) {
        this.list_agentNames = list_agentNames;
    }

    public List<String> getList_agentNames() {
        return list_agentNames;
    }

}
