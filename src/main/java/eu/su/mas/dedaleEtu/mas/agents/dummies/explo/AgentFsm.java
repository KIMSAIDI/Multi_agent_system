package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;
import org.graphstream.graph.Node;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import eu.su.mas.dedaleEtu.mas.behaviours.FollowGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FollowGolemBehaviourV2;
import eu.su.mas.dedaleEtu.mas.behaviours.GuildBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.InitBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SayHelloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.CheckGolemBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.GuildBehaviour;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.util.leap.Map;

public class AgentFsm extends AbstractDedaleAgent {
    private static final long serialVersionUID = 8567689731496787661L;
    private MapRepresentation myMap;
   
    private int nbAgent;
	private List<String> list_agentNames;
	private List<Behaviour> lb;
	private FSMBehaviour fsm;


    private static final String STATE_INIT = "STATE_INIT";
    private static final String STATE_EXPLORE = "STATE_EXPLORE";
    private static final String STATE_SEND_MAP = "STATE_SEND_MAP";
    private static final String STATE_RECEIVE_MAP = "STATE_RECEIVE_MAP";
    private static final String STATE_SAYHELLO = "STATE_SAYHELLO";
    private static final String STATE_FOLLOW_GOLEM = "STATE_FOLLOW_GOLEM";
    private static final String STATE_CHECK_GOLEM = "STATE_CHECK_GOLEM";
    private static final String STATE_GUILD = "STATE_GUILD";
    private static final String SUCCES = "SUCCES";
    private static final String END = "END";



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

        fsm = new FSMBehaviour(this);

        // Definition des etats
        fsm.registerFirstState(new InitBehaviour(), STATE_INIT);
        fsm.registerState(new ExploCoopBehaviour(this, myMap, list_agentNames), STATE_EXPLORE);
        fsm.registerState(new ShareMapBehaviour(this, myMap, list_agentNames), STATE_SEND_MAP);
        fsm.registerState(new ReceiveMapBehaviour(this), STATE_RECEIVE_MAP);
        fsm.registerState(new SayHelloBehaviour(this, list_agentNames), STATE_SAYHELLO);
        fsm.registerState(new FollowGolemBehaviour(this, list_agentNames, myMap), STATE_FOLLOW_GOLEM);
        fsm.registerState(new CheckGolemBehaviour(this), STATE_CHECK_GOLEM);
        fsm.registerState(new GuildBehaviour(this), STATE_GUILD);
        

        // Definition des transitions
        fsm.registerDefaultTransition(STATE_INIT, STATE_EXPLORE);
        fsm.registerTransition(STATE_EXPLORE, STATE_SAYHELLO, 1);
        // ect

    }

    public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    public MapRepresentation getMyMap() {
        return myMap;
    }

}
