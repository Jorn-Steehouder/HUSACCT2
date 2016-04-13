package husacct.analyse.task.reconstruct.layers.goldstein;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import husacct.analyse.domain.IModelQueryService;
import husacct.analyse.task.reconstruct.ReconstructArchitecture;
import husacct.common.dto.ModuleDTO;
import husacct.common.dto.ReconstructArchitectureDTO;
import husacct.common.dto.SoftwareUnitDTO;
import husacct.define.IDefineSarService;

public class LayersGoldstein_RootOriginal extends AlgorithmGoldstein{

	
		private int layerThreshold;
		private IModelQueryService queryService;
		private ArrayList<SoftwareUnitDTO> internalRootPackagesWithClasses;
		private TreeMap<Integer, ArrayList<SoftwareUnitDTO>> layers = new TreeMap<Integer, ArrayList<SoftwareUnitDTO>>();
		private final Logger logger = Logger.getLogger(ReconstructArchitecture.class);
		IDefineSarService defineSarService = husacct.ServiceProvider.getInstance().getDefineService().getSarService();
		private String xLibrariesRootPackage = "xLibraries";
		
		@Override
		public void executeAlgorithm(ReconstructArchitectureDTO dto, IModelQueryService queryService, String xLibrariesRootPackage) {
			layerThreshold = dto.getThreshold();
			this.queryService = queryService;
			determineInternalRootPackagesWithClasses();
			identifyLayers();
			//identifyLayersAtRootLevel(dependencyType);
		}
		
		private void identifyLayers() {
			// 1) Assign all internalRootPackages to bottom layer
			int layerId = 1;
			ArrayList<SoftwareUnitDTO> assignedUnits = new ArrayList<SoftwareUnitDTO>();
			assignedUnits.addAll(internalRootPackagesWithClasses);
			layers.put(layerId, assignedUnits);
			
			// 2) Identify the bottom layer. Look for packages with dependencies to external systems only.
			identifyTopLayerBasedOnUnitsInBottomLayer(layerId);
			
			// 3) Look iteratively for packages on top of the bottom layer, et cetera.
			while (layers.lastKey() > layerId) {
				layerId ++;
				identifyTopLayerBasedOnUnitsInBottomLayer(layerId);
			}
			
			// 4) Add the layers to the intended architecture
			int highestLevelLayer = layers.size();
			if (highestLevelLayer > 1) {
				// Reverse the layer levels. The numbering of the layers within the intended architecture is different: the highest level layer has hierarchcalLevel = 1
				int lowestLevelLayer = 1;
				int raise = highestLevelLayer - lowestLevelLayer;
				TreeMap<Integer, ArrayList<SoftwareUnitDTO>> tempLayers = new TreeMap<Integer, ArrayList<SoftwareUnitDTO>>();
				for (int i = lowestLevelLayer ; i <= highestLevelLayer ; i++) {
					ArrayList<SoftwareUnitDTO> unitsOfLayer = layers.get(i);
					int level = lowestLevelLayer + raise; 
					tempLayers.put(level, unitsOfLayer);
					raise --;
				}
				layers = tempLayers;
				for (Integer herarchicalLevel : layers.keySet()) {
					ModuleDTO newModule = defineSarService.addModule("Layer" + herarchicalLevel, "**", "Layer", herarchicalLevel, layers.get(herarchicalLevel));
					addToReverseReconstructionList(newModule); //add to cache for reverse
				}
			}

			logger.info(" Number of added Layers: " + layers.size());
		}
		
		private void identifyTopLayerBasedOnUnitsInBottomLayer(int bottomLayerId) {
			ArrayList<SoftwareUnitDTO> assignedUnitsOriginalBottomLayer = layers.get(bottomLayerId);
			@SuppressWarnings("unchecked")
			ArrayList<SoftwareUnitDTO> assignedUnitsBottomLayerClone = (ArrayList<SoftwareUnitDTO>) assignedUnitsOriginalBottomLayer.clone();
			ArrayList<SoftwareUnitDTO> assignedUnitsNewBottomLayer = new ArrayList<SoftwareUnitDTO>();
			ArrayList<SoftwareUnitDTO> assignedUnitsTopLayer = new ArrayList<SoftwareUnitDTO>();
			for (SoftwareUnitDTO softwareUnit : assignedUnitsOriginalBottomLayer) {
				boolean rootPackageDoesNotUseOtherPackage = true;
				for (SoftwareUnitDTO otherSoftwareUnit : assignedUnitsBottomLayerClone) {
					if (!otherSoftwareUnit.uniqueName.equals(softwareUnit.uniqueName)) {
						int nrOfDependenciesFromsoftwareUnitToOther = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName, otherSoftwareUnit.uniqueName).length;
						int nrOfDependenciesFromOtherTosoftwareUnit = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(otherSoftwareUnit.uniqueName, softwareUnit.uniqueName).length;
						if (nrOfDependenciesFromsoftwareUnitToOther > ((nrOfDependenciesFromOtherTosoftwareUnit / 100) * layerThreshold)) {
							rootPackageDoesNotUseOtherPackage = false;
						}
					}
				}
				if (rootPackageDoesNotUseOtherPackage) { // Leave unit in the lower layer
					assignedUnitsNewBottomLayer.add(softwareUnit);
				} else { // Assign unit to the higher layer
					assignedUnitsTopLayer.add(softwareUnit);
				}
				
			}
			if ((assignedUnitsTopLayer.size() > 0) && (assignedUnitsNewBottomLayer.size() > 0)) {
				layers.remove(bottomLayerId);
				layers.put(bottomLayerId, assignedUnitsNewBottomLayer);
				bottomLayerId ++;
				layers.put(bottomLayerId, assignedUnitsTopLayer);
			}
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
				// Temporal solution useful for HUSACCT20 test. To be improved! E.g., classes in root are excluded from the process. 
				String newRoot = internalRootPackagesWithClasses. get(0).uniqueName;
				internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
				for (SoftwareUnitDTO child : queryService.getChildUnitsOfSoftwareUnit(newRoot)) {
					if (child.type.equalsIgnoreCase("package")) {
						internalRootPackagesWithClasses.add(child);
					}
				}
			}
		}

	// Code below is not used in version 28/03/2016	
		
		public ArrayList<SoftwareUnitDTO> getClasses(String library) {
			internalRootPackagesWithClasses = new ArrayList<SoftwareUnitDTO>();
			SoftwareUnitDTO[] allRootUnits = queryService.getSoftwareUnitsInRoot();
			for (SoftwareUnitDTO rootModule : allRootUnits) {
				if (!rootModule.uniqueName.equals(library)) {
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
			return internalRootPackagesWithClasses;
		}

		private void identifyLayersAtRootLevel(String dependencyType) {
			determineInternalRootPackagesWithClasses();
			identifyLayers(internalRootPackagesWithClasses, dependencyType);
			
			//defineSarService.addModule("Classes", "**", "Layer", 0, internalClasses);
			
			for (Integer hierarchicalLevel : layers.keySet()) {
				ModuleDTO newModule = defineSarService.addModule("Layer" + hierarchicalLevel, "**", "Layer", hierarchicalLevel, layers.get(hierarchicalLevel));
				addToReverseReconstructionList(newModule); //add to cache for reverse
			}
		}
		
		private void identifyLayers(ArrayList<SoftwareUnitDTO> units, String depedencyType) {
			// 1) Assign all internalRootPackages to bottom layer
			int layerId = 1;
			ArrayList<SoftwareUnitDTO> assignedUnits = new ArrayList<SoftwareUnitDTO>();
			assignedUnits.addAll(units);
			layers.put(layerId, assignedUnits);

			// 2) Identify the bottom layer. Look for packages with dependencies to
			// external systems only.
			identifyTopLayerBasedOnUnitsInBottomLayer(layerId, depedencyType);

			// 3) Look iteratively for packages on top of the bottom layer, et
			// cetera.
			while (layers.lastKey() > layerId) {
				layerId++;
				identifyTopLayerBasedOnUnitsInBottomLayer(layerId, depedencyType);
			}

			// 4) Add the layers to the intended architecture
			int highestLevelLayer = layers.size();
			if (highestLevelLayer > 1) {
				// Reverse the layer levels. The numbering of the layers within the
				// intended architecture is different: the highest level layer has
				// hierarchcalLevel = 1
				int lowestLevelLayer = 1;
				int raise = highestLevelLayer - lowestLevelLayer;
				TreeMap<Integer, ArrayList<SoftwareUnitDTO>> tempLayers = new TreeMap<Integer, ArrayList<SoftwareUnitDTO>>();
				for (int i = lowestLevelLayer; i <= highestLevelLayer; i++) {
					ArrayList<SoftwareUnitDTO> unitsOfLayer = layers.get(i);
					int level = lowestLevelLayer + raise;
					tempLayers.put(level, unitsOfLayer);
					raise--;
				}
				layers = tempLayers;
				
			}

			logger.info(" Number of added Layers: " + layers.size());
		}
		
		private void identifyTopLayerBasedOnUnitsInBottomLayer(int bottomLayerId, String dependencyType) {
			ArrayList<SoftwareUnitDTO> assignedUnitsOriginalBottomLayer = layers.get(bottomLayerId);
			@SuppressWarnings("unchecked")
			ArrayList<SoftwareUnitDTO> assignedUnitsBottomLayerClone = (ArrayList<SoftwareUnitDTO>) assignedUnitsOriginalBottomLayer
					.clone();
			ArrayList<SoftwareUnitDTO> assignedUnitsNewBottomLayer = new ArrayList<SoftwareUnitDTO>();
			ArrayList<SoftwareUnitDTO> assignedUnitsTopLayer = new ArrayList<SoftwareUnitDTO>();
			
			for (SoftwareUnitDTO softwareUnit : assignedUnitsOriginalBottomLayer) {
				boolean rootPackageDoesNotUseOtherPackage = true;
				
				for (SoftwareUnitDTO otherSoftwareUnit : assignedUnitsBottomLayerClone) {
					if (!otherSoftwareUnit.uniqueName.equals(softwareUnit.uniqueName)) {
						int nrOfDependenciesFromsoftwareUnitToOther =0;
						int nrOfDependenciesFromOtherTosoftwareUnit=0;
						
						switch(dependencyType){
						case "umlDependency":
							nrOfDependenciesFromsoftwareUnitToOther = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName, otherSoftwareUnit.uniqueName).length;
							nrOfDependenciesFromOtherTosoftwareUnit = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(otherSoftwareUnit.uniqueName, softwareUnit.uniqueName).length;
							break;
							
						case "softwareUnitDependency":
							nrOfDependenciesFromsoftwareUnitToOther = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(softwareUnit.uniqueName, otherSoftwareUnit.uniqueName).length;
							nrOfDependenciesFromOtherTosoftwareUnit = queryService.getDependenciesFromSoftwareUnitToSoftwareUnit(otherSoftwareUnit.uniqueName, softwareUnit.uniqueName).length;
							break;
						}
						
						if (nrOfDependenciesFromsoftwareUnitToOther > ((nrOfDependenciesFromOtherTosoftwareUnit / 100) * layerThreshold)) {
							rootPackageDoesNotUseOtherPackage = false;
						}
					}
				}
				
				if (rootPackageDoesNotUseOtherPackage) { // Leave unit in the lower layer
					assignedUnitsNewBottomLayer.add(softwareUnit);
				} else { // Assign unit to the higher layer
					assignedUnitsTopLayer.add(softwareUnit);
				}

			}
			if ((assignedUnitsTopLayer.size() > 0) && (assignedUnitsNewBottomLayer.size() > 0)) {
				layers.remove(bottomLayerId);
				layers.put(bottomLayerId, assignedUnitsNewBottomLayer);
				bottomLayerId++;
				layers.put(bottomLayerId, assignedUnitsTopLayer);
			}
		}
	
	


}
