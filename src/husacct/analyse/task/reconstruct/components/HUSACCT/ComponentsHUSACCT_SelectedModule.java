package husacct.analyse.task.reconstruct.components.HUSACCT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import husacct.ServiceProvider;
import husacct.analyse.domain.IModelQueryService;
import husacct.common.dto.DependencyDTO;
import husacct.common.dto.ModuleDTO;
import husacct.common.dto.ReconstructArchitectureDTO;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.common.enums.DependencySubTypes;
import husacct.common.enums.ModuleTypes;
import husacct.define.IDefineSarService;
import husacct.define.IDefineService;

public class ComponentsHUSACCT_SelectedModule extends AlgorithmHUSACCT{
	private IModelQueryService queryService;
	private ArrayList<SoftwareUnitDTO> internalRootPackagesWithClasses; 
	private ArrayList<SoftwareUnitDTO> identifiedInterfaceClasses; 
	private String xLibrariesRootPackage = "xLibraries";
	private IDefineService defineService;
	private IDefineSarService defineSarService;
	private final Logger logger = Logger.getLogger(ComponentsHUSACCT_SelectedModule.class);
	
	public ComponentsHUSACCT_SelectedModule(){
		defineService = ServiceProvider.getInstance().getDefineService();
		defineSarService = defineService.getSarService();
	}
	@Override
	public void executeAlgorithm(ReconstructArchitectureDTO dto, IModelQueryService queryService, String xLibrariesRootPackage) {
		this.queryService = queryService;
		identifyComponentsInRootOfSelectedModule(dto.getSelectedModule());
	}
	/**
	 * Identified assigned Software units as Components if conditions apply.
	 * a) Identifies an assigned SoftwareUnit as a Components, if the SoftwareUnit's direct children implement a unit-specific interface.
	 * b) In case there is one assigned SoftwareUnit only:
	 * b1) If selectedModule is the root or a Layer and the assigned SU proofs to be a component, then a component + interface is added. 
	 * b2) If selectedModule is a Component, the algorithm is applied on the first set of children of the assigned SU (otherwise nothing would happen).
	 * b3) If selectedModule is a SubSytem, and the assigned SU proofs to be a component, selectedModule is edited to a component + interface.
	 * b4) If selectedModule is Facade or ExternalLibrary, nothing is done.
	 * @param selectedModule
	 */
	public void identifyComponentsInRootOfSelectedModule(ModuleDTO selectedModule) {
		try {
			HashMap<String,ArrayList<SoftwareUnitDTO>> interfacesPerPackage = new HashMap<String, ArrayList<SoftwareUnitDTO>>();
			HashMap<String,ArrayList<SoftwareUnitDTO>> classesPerPackage = new HashMap<String, ArrayList<SoftwareUnitDTO>>();
			HashMap<String,ArrayList<SoftwareUnitDTO>> exceptionsPerPackage = new HashMap<String, ArrayList<SoftwareUnitDTO>>();
			HashMap<String,ArrayList<SoftwareUnitDTO>> enumsPerPackage = new HashMap<String, ArrayList<SoftwareUnitDTO>>();
			
			String selectedModuleUniqueName = selectedModule.logicalPath;
			if (selectedModuleUniqueName.equals("")) {
				selectedModuleUniqueName = "**"; // Root of intended software architecture
			}
			if (selectedModule.type.equals(ModuleTypes.EXTERNAL_LIBRARY.toString()) || selectedModule.type.equals(ModuleTypes.FACADE.toString())) {
				return;
			}
			// 1) Select the first set of SoftwareUnits in the decomposition hierarchy of the selected module.  
			ArrayList<String> softwareUnitsInSelectedModuleList = new ArrayList<String>();
			softwareUnitsInSelectedModuleList.addAll(defineService.getAssignedSoftwareUnitsOfModule(selectedModuleUniqueName));
			if (softwareUnitsInSelectedModuleList.size() == 1) {
				if (selectedModule.type.equals(ModuleTypes.COMPONENT.toString())) {
					softwareUnitsInSelectedModuleList = getSetOfChildSoftwareUnits(softwareUnitsInSelectedModuleList.get(0));
				} else if (selectedModule.type.equals(ModuleTypes.SUBSYSTEM.toString())){
					// To do: set boolean and act in step 6) on it ...
				}
			}
			for (String softwareUnitInSelectedModule : softwareUnitsInSelectedModuleList) {
				for (SoftwareUnitDTO softwareUnit : queryService.getChildUnitsOfSoftwareUnit(softwareUnitInSelectedModule)) {
					if (softwareUnit.type.equals("class")) {
						// 2) Get all dependencies on softwareUnit
						ArrayList<DependencyDTO> dependenciesList = new ArrayList<DependencyDTO>();
						Collections.addAll(dependenciesList, queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName, ""));
						// 3) Determine if the software unit implement an interface	
						for(DependencyDTO dependency : dependenciesList){		
							if(dependency.subType.equals(DependencySubTypes.INH_IMPLEMENTS_INTERFACE.toString())){
								// 4) Get the used interface. Continue only if the interface is implemented ones only (otherwise it is possibly a utility).
								SoftwareUnitDTO interfaceClass = queryService.getSoftwareUnitByUniqueName(dependency.to);
								boolean interfaceIsSpecificForUnit = isInterfaceSpecificForUnit(interfaceClass);
								boolean isInterfaceOfParent = isInterfaceTheInterfaceOfTheParentModule(interfaceClass, selectedModule);
								if (interfaceIsSpecificForUnit && !isInterfaceOfParent) {
									// 5) Relate the interface with the parent package of the implementingClassesInSelectedModule
									// Determine the parent package of the implementing class, and relate package and interface.
									// Note: a package can implement several interfaces.
									if(!interfacesPerPackage.containsKey(softwareUnitInSelectedModule)){
										ArrayList<SoftwareUnitDTO> softwareUnitsOfInterface = new ArrayList<SoftwareUnitDTO>();
										softwareUnitsOfInterface.add(interfaceClass);
										interfacesPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfInterface);
									}
									else{
										ArrayList<SoftwareUnitDTO> softwareUnitsOfInterface = interfacesPerPackage.get(softwareUnitInSelectedModule);
										softwareUnitsOfInterface.add(interfaceClass);
										interfacesPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfInterface);
									}
								}
								
								// 6) Get the used classes implementing the interfaces
								SoftwareUnitDTO implementingClass = queryService.getSoftwareUnitByUniqueName(dependency.from);
								if(!classesPerPackage.containsKey(softwareUnitInSelectedModule)){
									ArrayList<SoftwareUnitDTO> softwareUnitsOfClass = new ArrayList<SoftwareUnitDTO>();
									softwareUnitsOfClass.add(implementingClass);
									classesPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfClass);
								}
								else{
									ArrayList<SoftwareUnitDTO> softwareUnitsOfClass = classesPerPackage.get(softwareUnitInSelectedModule);
									softwareUnitsOfClass.add(implementingClass);
									classesPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfClass);
								}
								
								//implementingClass
							}
							if(dependency.subType.equals(DependencySubTypes.DECL_EXCEPTION.toString())){
								SoftwareUnitDTO exceptionClass = queryService.getSoftwareUnitByUniqueName(dependency.to);
								
								if(!exceptionsPerPackage.containsKey(softwareUnitInSelectedModule)){
									ArrayList<SoftwareUnitDTO> softwareUnitsOfExceptions = new ArrayList<SoftwareUnitDTO>();
									softwareUnitsOfExceptions.add(exceptionClass);
									exceptionsPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfExceptions);
								}
								else{
									ArrayList<SoftwareUnitDTO> softwareUnitsOfExceptions = exceptionsPerPackage.get(softwareUnitInSelectedModule);
									softwareUnitsOfExceptions.add(exceptionClass);
									exceptionsPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfExceptions);
								}
							}
							
							if(dependency.subType.equals(DependencySubTypes.ACC_ENUMERATION_VAR.toString())){
								SoftwareUnitDTO enumClass = queryService.getSoftwareUnitByUniqueName(dependency.to);
								
								if(!enumsPerPackage.containsKey(softwareUnitInSelectedModule)){
									ArrayList<SoftwareUnitDTO> softwareUnitsOfEnums = new ArrayList<SoftwareUnitDTO>();
									softwareUnitsOfEnums.add(enumClass);
									enumsPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfEnums);
								}
								else{
									ArrayList<SoftwareUnitDTO> softwareUnitsOfEnums = enumsPerPackage.get(softwareUnitInSelectedModule);
									softwareUnitsOfEnums.add(enumClass);
									enumsPerPackage.put(softwareUnitInSelectedModule, softwareUnitsOfEnums);
								}
							}
							
						}
					}
				}
			}
			// 7) Create the component and its interface, and assign the software units to these modules
			for(String parentPackageUniqueName : interfacesPerPackage.keySet()){
				SoftwareUnitDTO parentUnit = queryService.getSoftwareUnitByUniqueName(parentPackageUniqueName);
				ArrayList<SoftwareUnitDTO> parentUnitsList = new ArrayList<SoftwareUnitDTO>();
				parentUnitsList.add(parentUnit);
				
				ModuleDTO newModule = defineSarService.addModule(parentUnit.name, selectedModuleUniqueName, ModuleTypes.COMPONENT.toString(), 0, parentUnitsList);
				addToReverseReconstructionList(newModule); //add to cache for reverse
				ArrayList<SoftwareUnitDTO> interfaceUnits = interfacesPerPackage.get(parentPackageUniqueName);
				ModuleDTO newInterfaceModule = defineSarService.addModule(parentUnit.name + "Interface", selectedModuleUniqueName + "." + parentUnit.name, ModuleTypes.FACADE.toString(), 0, interfaceUnits);
				addToReverseReconstructionList(newInterfaceModule); //add to cache for reverse
			}
			
			// 8) Add classes to the component
			for(String parentPackageUniqueName : classesPerPackage.keySet()){
				SoftwareUnitDTO parentUnit = queryService.getSoftwareUnitByUniqueName(parentPackageUniqueName);
				ArrayList<SoftwareUnitDTO> parentUnitsList = new ArrayList<SoftwareUnitDTO>();
				parentUnitsList.add(parentUnit);
				
				ArrayList<SoftwareUnitDTO> classUnits = classesPerPackage.get(parentPackageUniqueName);
				ModuleDTO newClassModule = defineSarService.addModule(parentUnit.name + " Classes", selectedModuleUniqueName + "." + parentUnit.name, "", 0, classUnits);
				addToReverseReconstructionList(newClassModule); //add to cache for reverse
			}
			
			//  Add exceptions to the component
			for(String parentPackageUniqueName : exceptionsPerPackage.keySet()){
				SoftwareUnitDTO parentUnit = queryService.getSoftwareUnitByUniqueName(parentPackageUniqueName);
				ArrayList<SoftwareUnitDTO> parentUnitsList = new ArrayList<SoftwareUnitDTO>();
				parentUnitsList.add(parentUnit);
				
				ArrayList<SoftwareUnitDTO> classUnits = exceptionsPerPackage.get(parentPackageUniqueName);
				ModuleDTO newClassModule = defineSarService.addModule(parentUnit.name + " ExceptionClasses", selectedModuleUniqueName + "." + parentUnit.name, "", 0, classUnits);
				addToReverseReconstructionList(newClassModule); //add to cache for reverse
			}
			// Add enumerations to the component
			for(String parentPackageUniqueName : enumsPerPackage.keySet()){
				SoftwareUnitDTO parentUnit = queryService.getSoftwareUnitByUniqueName(parentPackageUniqueName);
				ArrayList<SoftwareUnitDTO> parentUnitsList = new ArrayList<SoftwareUnitDTO>();
				parentUnitsList.add(parentUnit);
				
				ArrayList<SoftwareUnitDTO> classUnits = enumsPerPackage.get(parentPackageUniqueName);
				//defineSarService.editModule(parentPackageUniqueName + "." + parentUnit.name + "Interface", null, 0, classUnits);
				ModuleDTO newClassModule = defineSarService.addModule(parentUnit.name + " EnumerationClasses", selectedModuleUniqueName + "." + parentUnit.name, "", 0, classUnits);
				addToReverseReconstructionList(newClassModule); //add to cache for reverse
			}
		} catch (Exception e) {
	        logger.error(" Exception: "  + e );
        }
	}

	public void identifyComponentsInRoot() { // Old mechanism, not used currently
		// Select all interface classes.
		determineInternalRootPackagesWithClasses();
		identifiedInterfaceClasses = new ArrayList<SoftwareUnitDTO>();
		for(int i = 0; i<internalRootPackagesWithClasses.size(); i++){
			String newRoot = internalRootPackagesWithClasses.get(i).uniqueName;
			for (SoftwareUnitDTO child : queryService.getChildUnitsOfSoftwareUnit(newRoot)) {
				if (child.type.equalsIgnoreCase("interface")) {
					identifiedInterfaceClasses.add(child);
				}
			}
		}
		// Select the package(s) implementing an interface and relate packages and interface
		HashMap<String,ArrayList<SoftwareUnitDTO>> interfacesPerPackage = new HashMap<String, ArrayList<SoftwareUnitDTO>>();
		for(SoftwareUnitDTO interfaceClass : identifiedInterfaceClasses){
			// Get all dependencies on the interfaceClass
			ArrayList<DependencyDTO> dependenciesList = new ArrayList<DependencyDTO>();
			Collections.addAll(dependenciesList, queryService.getDependenciesFromSoftwareUnitToSoftwareUnit("",interfaceClass.uniqueName));
			for(DependencyDTO dependencyOnInterface : dependenciesList){		
				// Determine the classes that implement the interface	
				if(dependencyOnInterface.subType.equals(DependencySubTypes.INH_IMPLEMENTS_INTERFACE.toString()) && queryService.getParentUnitOfSoftwareUnit(dependencyOnInterface.to).type.equals("package")){
					// Determine the parent package of the implementing class, and relate package and interface.
					// Note: a package can implement several interfaces.
					String parentName = queryService.getParentUnitOfSoftwareUnit(dependencyOnInterface.to).uniqueName;
					if(!interfacesPerPackage.containsKey(parentName)){
						ArrayList<SoftwareUnitDTO> softwareUnitsOfInterface = new ArrayList<SoftwareUnitDTO>();
						softwareUnitsOfInterface.add(interfaceClass);
						interfacesPerPackage.put(parentName, softwareUnitsOfInterface);
					}
					else{
						ArrayList<SoftwareUnitDTO> softwareUnitsOfInterface = interfacesPerPackage.get(parentName);
						softwareUnitsOfInterface.add(interfaceClass);
						interfacesPerPackage.put(parentName, softwareUnitsOfInterface);
					}
				}
			}
		}
		// Create the component and its interface, and assign the software units to these modules
		for(String parentPackageUniqueName : interfacesPerPackage.keySet()){
			SoftwareUnitDTO parentUnit = queryService.getSoftwareUnitByUniqueName(parentPackageUniqueName);
			ArrayList<SoftwareUnitDTO> parentUnitsList = new ArrayList<SoftwareUnitDTO>();
			parentUnitsList.add(parentUnit);
			
			defineSarService.addModule(parentUnit.name, "**", ModuleTypes.COMPONENT.toString(), 0, parentUnitsList);
			ArrayList<SoftwareUnitDTO> interfaceUnits = interfacesPerPackage.get(parentPackageUniqueName);
			defineSarService.addModule(parentUnit.name + "Interface", parentUnit.name, ModuleTypes.FACADE.toString(), 0, interfaceUnits);
		}
		
		
		//logicalpath: **, validate/domain/validation/moduletype/moduletypes.java, 0, 
		
		//IDEfineServce.getmodule_thechildrenofthemmodule -> if interface, dan koppel gevonden interface
		
		//map key: uniquename value: arraylist<interfaces
		
		
		//dependencies op uniquename van interface
		//getDependenciesFromSoftwareUnitToSoftwareUnit
		//dependencyDTO.subType
		//INH_IMPLEMENTS_INTERFACE("Implements Interface", DependencyTypes.INHERITANCE),
		//getParentUnit returns softwareunitDTO,
		
		//in package get classes that uses interface
		//validate.propertyrulestype.facadeconvention.check 
		//module component get child, edit module
		//threshold 20% begin met 50%
	}

	
	//@Override
	public ArrayList<SoftwareUnitDTO> getClasses(String library) {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	public TreeMap<Integer, ArrayList<SoftwareUnitDTO>> getClasses(String library, TreeMap<Integer, ArrayList<SoftwareUnitDTO>> layers) {
		// TODO Auto-generated method stub
		return null;
	}
	private void determineInternalRootPackagesWithClasses() {
		internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
		SoftwareUnitDTO[] allRootUnits = queryService.getSoftwareUnitsInRoot();
		for (SoftwareUnitDTO rootModule : allRootUnits) {
			if (!rootModule.uniqueName.equals(xLibrariesRootPackage)) {
				for (String internalPackage : queryService.getRootPackagesWithClass(rootModule.uniqueName)) {
					internalRootPackagesWithClasses.add(queryService.getSoftwareUnitByUniqueName(internalPackage));

				}
			}
		}
		if (internalRootPackagesWithClasses.size() == 1) {
			// Temporal solution useful for HUSACCT20 test. To be improved!
			// E.g., classes in root are excluded from the process.
			String newRoot = internalRootPackagesWithClasses.get(0).uniqueName;
			internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
			for (SoftwareUnitDTO child : queryService.getChildUnitsOfSoftwareUnit(newRoot)) {
				if (child.type.equalsIgnoreCase("package")) {
					internalRootPackagesWithClasses.add(child);
				}
			}
		}
	}

	// Checks if the interface is implemented only a few times. Otherwise it is possibly a utility.
	private boolean isInterfaceSpecificForUnit(SoftwareUnitDTO interfaceClass) {
		/* if (interfaceClass.uniqueName.contains("IAnalyseService")) {
			boolean breakpoint = true;
		} */
		boolean interfaceIsSpecificForUnit = false;
		// Get all dependencies on the interfaceClass
		ArrayList<DependencyDTO> dependenciesList = new ArrayList<DependencyDTO>();
		Collections.addAll(dependenciesList, queryService.getDependenciesFromSoftwareUnitToSoftwareUnit("",interfaceClass.uniqueName));
		int numberOfImplementsDependencies = 0;
		for(DependencyDTO dependencyOnInterface : dependenciesList){		
			if(dependencyOnInterface.subType.equals(DependencySubTypes.INH_IMPLEMENTS_INTERFACE.toString())){
				numberOfImplementsDependencies ++;
			}
		}
		if (numberOfImplementsDependencies <= 2) { // One for the implementing facade and maybe one for a service stub. 
			interfaceIsSpecificForUnit = true;
		}
		return interfaceIsSpecificForUnit;
	}

	// Checks if the passed interface is assigned, in case the parent module is a component, to the interface of the parent module 
	private boolean isInterfaceTheInterfaceOfTheParentModule(SoftwareUnitDTO interfaceClass, ModuleDTO parent) {
		boolean isInterfaceOfParent = false;
		if (parent.type.equals("Component")) {
			ModuleDTO[] children = defineService.getModule_TheChildrenOfTheModule(parent.logicalPath);
			for (ModuleDTO child : children) {
				if (child.type.equals("Facade")) {
					HashSet<String> softwareUnits = defineService.getAssignedSoftwareUnitsOfModule(child.logicalPath);
					for (String softwareUnit : softwareUnits) {
						if (softwareUnit.equals(interfaceClass.uniqueName)) {
							isInterfaceOfParent = true;
						}
					}
				}
			}
		}
		return isInterfaceOfParent;
	}

	/** Returns the first set of children (number of children >= 2) in the decomposition hierarchy of the parent SoftwareUnit
	 * @param parentUniqueName (of a SoftwareUnit)
	 * @return ArrayList<String> with unique names of children, or an empty list, if no child SoftwareUnits are existing.
	 */
	private ArrayList<String> getSetOfChildSoftwareUnits(String parentUniqueName) {
		ArrayList<String> childSoftwareUnits = new ArrayList<String>();
		String parentUnitUniqueName = parentUniqueName;
		while (childSoftwareUnits.size() < 2) {
			SoftwareUnitDTO[] childUnits = (queryService.getChildUnitsOfSoftwareUnit(parentUnitUniqueName));
			if (childUnits.length == 0) {
				break;
			} else if ((childUnits.length == 1)) {
				String newParentUniqueName = childUnits[0].uniqueName;
				childSoftwareUnits = getSetOfChildSoftwareUnits(newParentUniqueName);
			} else if ((childUnits.length >= 2)) {
				for (SoftwareUnitDTO childUnit : childUnits) {
					childSoftwareUnits.add(childUnit.uniqueName);
				}
			}
		}
		return childSoftwareUnits;
	}
}
