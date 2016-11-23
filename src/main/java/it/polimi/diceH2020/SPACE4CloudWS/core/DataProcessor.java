/*
Copyright 2016 Jacopo Rigoli
Copyright 2016 Eugenio Gianniti

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package it.polimi.diceH2020.SPACE4CloudWS.core;

import it.polimi.diceH2020.SPACE4Cloud.shared.settings.Settings;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.Matrix;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.Solution;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.SolutionPerJob;
import it.polimi.diceH2020.SPACE4CloudWS.fileManagement.FileUtility;
import it.polimi.diceH2020.SPACE4CloudWS.fineGrainedLogicForOptimization.ContainerLogicForEvaluation;
import it.polimi.diceH2020.SPACE4CloudWS.services.DataService;
import it.polimi.diceH2020.SPACE4CloudWS.services.SolverProxy;
import it.polimi.diceH2020.SPACE4CloudWS.stateMachine.Events;
import it.polimi.diceH2020.SPACE4CloudWS.stateMachine.States;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This component interfaces with Solvers.
 */
@Component
public class DataProcessor {

	private final Logger logger = Logger.getLogger(getClass());

	@Autowired
	protected SolverProxy solverCache;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private DataService dataService;

	@Autowired
	private StateMachine<States, Events> stateHandler;

	long calculateMetric(@NonNull Solution sol, BiConsumer<SolutionPerJob, Double> resultSaver,
						 Consumer<SolutionPerJob> ifEmpty) {
		return calculateMetric(sol.getLstSolutions(), resultSaver, ifEmpty);
	}

	void calculateDuration(@NonNull Matrix matrix) {
		calculateDurationFineGrained(matrix.getAllSolutions());
	}

	//TODO collapse also calculateDuration in this method, by implementing fineGrained Matrix also in the public case
	private void calculateDurationFineGrained(@NonNull List<SolutionPerJob> spjList) {
		spjList.forEach(s -> {
			ContainerLogicForEvaluation container =
					(ContainerLogicForEvaluation) context.getBean("containerLogicForEvaluation", s);
			container.start();
		});
	}

	private long calculateMetric(@NonNull List<SolutionPerJob> spjList, BiConsumer<SolutionPerJob, Double> resultSaver,
								 Consumer<SolutionPerJob> ifEmpty) {
		//to support also parallel stream.
		AtomicLong executionTime = new AtomicLong();

		spjList.forEach(spj -> {
			Pair<Optional<Double>, Long> result = simulateClass(spj);
			executionTime.addAndGet(result.getRight());
			Optional<Double> optionalValue = result.getLeft();
			if (optionalValue.isPresent()) resultSaver.accept(spj, optionalValue.get());
			else ifEmpty.accept(spj);
		});

		return executionTime.get();
	}

	Pair<Optional<Double>, Long> simulateClass(@NonNull SolutionPerJob solPerJob) {
		Pair<Optional<Double>, Long> result = solverCache.evaluate(solPerJob);
		Optional<Double> optionalValue = result.getLeft();
		if (optionalValue.isPresent()) {
			String message = String.format("%s-> A metric with %d VMs and h = %d has been simulated: %f",
					solPerJob.getId(), solPerJob.getNumberVM(), solPerJob.getNumberUsers(), optionalValue.get());
			logger.info(message);
		} else solverCache.invalidate(solPerJob);
		return result;
	}

	public void restoreDefaults() {
		solverCache.restoreDefaults();
	}

	void changeSettings(Settings settings) {
		solverCache.changeSettings(settings);
	}

	public String getCurrentInputsSubFolderPath() {
		String currentInputSubFolder = dataService.getSimFoldersName();
		File f = new File(currentInputSubFolder);
		if(!f.exists()){
			logger.error("Error with Current Input Folder! It doesn't exist!");
			stateHandler.sendEvent(Events.STOP);
		}
		return currentInputSubFolder;
	}

	public String getCurrentInputsSubFolderName() {
		return Paths.get(getCurrentInputsSubFolderPath()).getFileName().toString();
	}

	private List<File> getCurrentReplayerInputFiles() throws IOException {
		List<File> txtList = new ArrayList<>();
		for(String fileName: FileUtility.listFile(getCurrentInputsSubFolderPath(),  ".txt")){
			File file = new File(getCurrentInputsSubFolderPath()+File.separator+fileName);
			txtList.add(file);
		}
		return txtList;
	}

	public String getProviderName() {
		return dataService.getProviderName();
	}

	public List<File> getCurrentReplayerInputFiles(String solutionID, String spjID, String provider, String typeVM)
			throws IOException {
		return getCurrentReplayerInputFiles().stream().filter(s->s.getName().contains(spjID+provider+typeVM))
				.filter(s->s.getName().contains(solutionID)).collect(Collectors.toList());
	}

}
