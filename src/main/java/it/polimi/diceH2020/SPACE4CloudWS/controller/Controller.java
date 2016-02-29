package it.polimi.diceH2020.SPACE4CloudWS.controller;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.settings.Settings;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.Solution;
import it.polimi.diceH2020.SPACE4CloudWS.services.EngineService;
import it.polimi.diceH2020.SPACE4CloudWS.stateMachine.Events;
import it.polimi.diceH2020.SPACE4CloudWS.stateMachine.States;

@RestController
public class Controller {

	@Autowired
	EngineService engineService;

	@Autowired
	private StateMachine<States, Events> stateHandler;

	private static Logger logger = Logger.getLogger(Controller.class.getName());
	
	private Future<String> runningInit = null;
	
	@RequestMapping(method = RequestMethod.POST, value = "/event")
	public @ResponseBody String changeState(@RequestBody Events event) throws Exception {
		
		if (event.equals(Events.RESET)) {
			//runningInit.cancel(true);
			stateHandler.sendEvent(event);
			logger.info(getWebServiceState());
			return getWebServiceState();
		}
		stateHandler.sendEvent(event);		
		States currentState = stateHandler.getState().getId();
		if (currentState.equals(States.RUNNING_INIT)) runningInit = engineService.runningInitSolution();
		if(currentState.equals(States.EVALUATING_INIT)) engineService.evaluatingInitSolution();
		if (currentState.equals(States.RUNNING_LS)) engineService.localSearch();

		return getWebServiceState();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/settings")
	public @ResponseBody String changeState(@RequestBody Settings settings) {
		if (getWebServiceState().equals("IDLE")) engineService.setAccuracyAndCycles(settings);
		return getWebServiceState();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/inputdata")
	@ResponseStatus(value = HttpStatus.OK)
	public String endpointInputData(@RequestBody InstanceData inputData) throws Exception {
		if (getWebServiceState().equals("IDLE")) {
			engineService.setInstanceData(inputData);
			stateHandler.sendEvent(Events.TO_CHARGED_INPUTDATA);
		}
		return getWebServiceState();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/solution")
	@ResponseStatus(value = HttpStatus.OK)
	public String endpointSolution(@RequestBody Solution sol) throws Exception {
		if (getWebServiceState().equals("IDLE")) {
			engineService.setSolution(sol);
			stateHandler.sendEvent(Events.TO_CHARGED_INITSOLUTION);
		}
		return getWebServiceState();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/solution")
	@ResponseStatus(value = HttpStatus.OK)
	public Solution endpointSolution() throws Exception {
		String state = stateHandler.getState().getId().toString();
		if (state.equals("CHARGED_INITSOLUTION") || state.equals("FINISH")) return engineService.getSolution();
		return null;
	}

	@RequestMapping(value = "/state", method = RequestMethod.GET)
	public @ResponseBody String getState() {
		return getWebServiceState();
	}

	private String getWebServiceState() {
		return stateHandler.getState().getId().toString();
	}

}
