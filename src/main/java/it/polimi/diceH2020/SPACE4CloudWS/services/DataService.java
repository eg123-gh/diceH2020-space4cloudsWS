/**
 * 
 */
package it.polimi.diceH2020.SPACE4CloudWS.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.polimi.diceH2020.SPACE4Cloud.shared.InstanceData;
import it.polimi.diceH2020.SPACE4Cloud.shared.Solution;
import it.polimi.diceH2020.SPACE4CloudWS.model.Key;
import it.polimi.diceH2020.SPACE4CloudWS.model.Provider;
import it.polimi.diceH2020.SPACE4CloudWS.model.TypeVM;

/**
 * @author ciavotta
 */
@Service
public class DataService {

	@Autowired(required = false)
	private InstanceData data;

	@Autowired
	private DAOService daoService;

	public void setData(InstanceData data) {
		this.data = data;
	}

	private int jobNumber;

	public int getNumberJobs() {
		return jobNumber;
	}


	private Solution currentSolution;

	private Map<Key, TypeVM> mapTypeVM;

	private List<TypeVM> lstTypeVM = new ArrayList<>();
	private List<Double> lstSigmaBar = new ArrayList<>();
	private List<Double> lstDeltaBar = new ArrayList<>();
	private List<Double> lstRhoBar = new ArrayList<>();
	private List<Integer> lstCore = new ArrayList<>();
	private List<List<Integer>> matrixJobCores = new ArrayList<List<Integer>>();

	private String NameProvider;

	public List<List<Integer>> getMatrixJobCores() {
		return matrixJobCores;
	}

	public List<Integer> getLstCore() {
		return lstCore;
	}

	public List<Double> getLstRhoBar() {
		return lstRhoBar;
	}

	public List<Double> getLstDeltaBar() {
		return lstDeltaBar;
	}

	public Double getDeltaBar(String tVM){
		Key key = new Key(tVM, this.NameProvider);
		return this.mapTypeVM.get(key).getDeltabar();
	}
	
	public Double getRhoBar(String tVM){
		Key key = new Key(tVM, this.NameProvider);
		return this.mapTypeVM.get(key).getRhoBar();
	}
	
	public Double getSigmaBar(String tVM){
		Key key = new Key(tVM, this.NameProvider);
		return this.mapTypeVM.get(key).getSigmaBar();
	}
	
	public List<Double> getLstSigmaBar() {
		return lstSigmaBar;
	}

	public Solution getCurrentSolution() {
		return currentSolution;
	}

	public void setCurrentSolution(Solution currentSolution) {
		this.currentSolution = currentSolution;
	}

	public DataService() {

	}

	public void setInstanceData(InstanceData inputData) {
		this.data = inputData;
		this.jobNumber = data.getNumberJobs();
		//this.vmNumber = data.getNumberTypeVM();
		this.NameProvider = data.getProvider();
		loadDataFromDB(new Provider(this.NameProvider));
	}

	public String getNameProvider() {
		return NameProvider;
	}

	/**
	 * @return the data
	 */
	public InstanceData getData() {
		return data;
	}

	private void loadDataFromDB(Provider provider) {
		this.mapTypeVM = daoService.typeVMFindAllToMap(provider);

		mapTypeVM.values().forEach(new Consumer<TypeVM>() {
			@Override
			public void accept(TypeVM s) {
				DataService.this.lstTypeVM.add(s);
			}
		});
		lstTypeVM.forEach(new Consumer<TypeVM>() {
			@Override
			public void accept(TypeVM s) {
				DataService.this.lstSigmaBar.add(s.getSigmaBar());
				DataService.this.lstDeltaBar.add(s.getDeltabar());
				DataService.this.lstRhoBar.add(s.getRhoBar());
				DataService.this.lstCore.add(s.getNumCores());
			}
		});
		//TODO check this
		data.getId_job().forEach(new Consumer<Integer>() {
			@Override
			public void accept(Integer s) {
				DataService.this.matrixJobCores.add(DataService.this.lstCore);
			}
		});
	}

	public List<Integer> getNumCores(List<String> selectedVMtypes) {
		List<Integer> lstCores = new ArrayList<>();
		for (int i = 0; i < selectedVMtypes.size(); i++) {
			Key k = new Key(selectedVMtypes.get(i), this.NameProvider);
			TypeVM tVM = mapTypeVM.get(k);
			lstCores.add(tVM.getNumCores());
		}
		return lstCores;
	}
	
	public Integer getNumCores(String selectedVMType){
		Key k = new Key(selectedVMType, this.NameProvider);
		TypeVM tVM = mapTypeVM.get(k);
		return tVM.getNumCores();
	}
	
}
