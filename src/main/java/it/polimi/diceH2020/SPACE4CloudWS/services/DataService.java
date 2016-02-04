/**
 * 
 */
package it.polimi.diceH2020.SPACE4CloudWS.services;

import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.JobClass;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.Profile;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.TypeVM;
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.TypeVMJobClassKey;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.Solution;
import it.polimi.diceH2020.SPACE4CloudWS.model.EntityKey;
import it.polimi.diceH2020.SPACE4CloudWS.model.EntityProvider;
import it.polimi.diceH2020.SPACE4CloudWS.model.EntityTypeVM;
import lombok.Data;

/**
 * @author ciavotta
 */
@Service
@Data
public class DataService {

	@Autowired(required = false)
	private InstanceData data;

	@Autowired
	private DAOService daoService;

	private int jobNumber;

	private Solution currentSolution;

	private Map<EntityKey, EntityTypeVM> mapTypeVM;
	private String NameProvider;

	public Double getDeltaBar(TypeVM tVM) {
		EntityKey key = new EntityKey(tVM.getId(), this.NameProvider);
		return this.mapTypeVM.get(key).getDeltabar();
	}

	//
	public Double getRhoBar(TypeVM tVM) {
		EntityKey key = new EntityKey(tVM.getId(), this.NameProvider);
		return this.mapTypeVM.get(key).getRhoBar();
	}

	//
	public Double getSigmaBar(TypeVM tVM) {
		EntityKey key = new EntityKey(tVM.getId(), this.NameProvider);
		return this.mapTypeVM.get(key).getSigmaBar();
	}

	public List<Integer> getNumCores(List<TypeVM> tVM) {
		return tVM.stream().map(vm->
			getNumCores(vm)).collect(toList());

	}
	
	public Integer getNumCores(TypeVM tVM) {
		EntityKey key = new EntityKey(tVM.getId(), this.NameProvider);
		return this.mapTypeVM.get(key).getNumCores();
	}

	public void setInstanceData(InstanceData inputData) {
		this.data = inputData;
		this.jobNumber = data.getNumberJobs();
		this.NameProvider = data.getProvider();
		loadDataFromDB(new EntityProvider(this.NameProvider));
	}

	private void loadDataFromDB(EntityProvider provider) {
		this.mapTypeVM = daoService.typeVMFindAllToMap(provider);
	}

	public Double getEta(TypeVMJobClassKey key) {
		return data.getEta(key);
	}

	public int getR(TypeVMJobClassKey key) {
		return data.getR(key);
	}


	public List<JobClass> getListJobClass() {
		return data.getLstClass();
	}

	public List<TypeVM> getListTypeVM(JobClass jobClass) {
		return data.getLstTypeVM(jobClass);
	}

	public Profile getProfile(JobClass jobClass, TypeVM tVM) {
		return data.getProfile(jobClass, tVM);
	}

	public int getGamma() {
		return data.getGamma();
	}
	
	public Double getD(JobClass jobClass){
		return data.getD(jobClass);
	}

}
