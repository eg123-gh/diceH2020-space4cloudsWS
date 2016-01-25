package it.polimi.diceH2020.SPACE4CloudWS.main;

import it.polimi.diceH2020.SPACE4Cloud.shared.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.Solution;
import it.polimi.diceH2020.SPACE4Cloud.shared.SolutionPerJob;
import it.polimi.diceH2020.SPACE4CloudWS.stateMachine.States;
import scala.collection.immutable.Set.Set1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Arrays;
import java.util.List;

@Configuration
public class Configurator {

	@Value("${pool.size:10}")
	private int poolSize;

	@Value("${queue.capacity:2}")
	private int queueCapacity;

	@Bean(name = "workExecutor")
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(poolSize);
		taskExecutor.setQueueCapacity(queueCapacity);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Bean
	public States state() {
		return States.IDLE;
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Bean
	@Profile("dev")
	public InstanceData applData() {
		return new InstanceData();
	}

	@Bean
	@Profile("test")
	public InstanceData applDataTest() {
		int Gamma = 240; // num cores cluster
		List<String> typeVm = Arrays.asList("T1", "T2");
		String provider = "Amazon";
		List<Integer> id_job = Arrays.asList(5, 2); // numJobs = 2
		double[] think = { 10, 5 }; // check
		int[][] cM = { { 4, 8 }, { 4, 8 } };
		int[][] cR = { { 4, 8 }, { 4, 8 } };
		double[] eta = { 0.1, 0.3 };
		int[] hUp = { 10, 10 };
		int[] hLow = { 5, 5 };
		int[] nM = { 65, 65 };
		int[] nR = { 15, 5 };
		double[] mmax = { 9.128, 17.541 }; // maximum time to execute a single
											// map
		double[] mavg = { 4.103, 8.235 };
		double[] rmax = { 0.592, 0.499 };
		double[] ravg = { 0.327, 0.297 };
		double[] d = { 180, 150 };
		double[] sH1max = { 0, 0 };
		double[] sHtypmax = { 7.942, 20.141 };
		double[] sHtypavg = { 4.831, 14.721 };
		double[] job_penalty = { 25.0, 14.99 };
		int[] r = { 30, 25 };

		return new InstanceData(Gamma, typeVm, provider, id_job, think, cM, cR, eta, hUp, hLow, nM, nR, mmax, rmax,
				mavg, ravg, d, sH1max, sHtypmax, sHtypavg, job_penalty, r);
	}

	@Bean
	@Profile("test")
	public Solution solution() {
		Solution sol = new Solution();
		SolutionPerJob sol1 = new SolutionPerJob();
		sol1.setAlfa(1250.0);
		sol1.setBeta(125.0);
		sol1.setChanged(false);
		sol1.setCost(13.3);
		sol1.setDeltaBar(1.4);
		sol1.setFeasible(false);
		sol1.setIdxVmTypeSelected(1);
		sol1.setNumberContainers(5);
		sol1.setNumberUsers(10);
		sol1.setNumberVM(15);
		sol1.setNumCores(3);
		sol1.setNumOnDemandVM(0);
		sol1.setNumReservedVM(11);
		sol1.setNumSpotVM(4);
		sol1.setPos(0);
		sol1.setRhoBar(1.1);
		sol1.setSigmaBar(0.3);
		sol1.setDuration(180.0);
		sol1.setTypeVMselected("T2");
		sol1.setEta(0.3);
		sol1.setR(25);
		sol1.setD(150.0);
		sol.getLstSolutions().add(sol1);
		SolutionPerJob sol2 = new SolutionPerJob();
        sol2.setAlfa(749.5);
        sol2.setBeta(74.95);
        sol2.setChanged(false);
        sol2.setCost(35.0);
        sol2.setDeltaBar(1.4);
        sol2.setFeasible(false);
        sol2.setIdxVmTypeSelected(1);
        sol2.setNumberContainers(13);
        sol2.setNumberUsers(10);
        sol2.setNumberVM(39);
        sol2.setNumCores(3);
        sol2.setNumOnDemandVM(3);
        sol2.setNumReservedVM(25);
        sol2.setNumSpotVM(11);
        sol2.setPos(1);
        sol2.setRhoBar(1.1);
        sol2.setSigmaBar(0.3);
        sol2.setDuration(150.0);
        sol2.setTypeVMselected("T2");
        sol2.setEta(0.3);
        sol2.setR(25);
        sol2.setD(150.0);
        sol.getLstSolutions().add(sol2);
        
		return sol;
	}

}
