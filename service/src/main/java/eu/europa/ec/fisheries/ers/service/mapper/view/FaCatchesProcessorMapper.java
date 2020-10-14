/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.ers.service.mapper.view;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.ers.fa.entities.AapProcessCodeEntity;
import eu.europa.ec.fisheries.ers.fa.entities.AapProcessEntity;
import eu.europa.ec.fisheries.ers.fa.entities.AapProductEntity;
import eu.europa.ec.fisheries.ers.fa.entities.AapStockEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FaCatchEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FishingActivityEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FishingGearEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FishingTripEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FishingTripIdentifierEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FluxCharacteristicEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FluxLocationEntity;
import eu.europa.ec.fisheries.ers.fa.entities.SizeDistributionEntity;
import eu.europa.ec.fisheries.ers.fa.entities.VesselTransportMeansEntity;
import eu.europa.ec.fisheries.ers.fa.utils.FluxLocationCatchTypeEnum;
import eu.europa.ec.fisheries.ers.fa.utils.FluxLocationSchemeId;
import eu.europa.ec.fisheries.ers.service.dto.facatch.DestinationLocationDto;
import eu.europa.ec.fisheries.ers.service.dto.facatch.FaCatchGroupDetailsDto;
import eu.europa.ec.fisheries.ers.service.dto.facatch.FaCatchGroupDto;
import eu.europa.ec.fisheries.ers.service.dto.facatch.FluxCharacteristicsViewDto;
import eu.europa.ec.fisheries.ers.service.dto.view.ActivityDetailsDto;
import eu.europa.ec.fisheries.ers.service.dto.view.FluxLocationDto;
import eu.europa.ec.fisheries.ers.service.dto.view.parent.FishingActivityViewDTO;
import eu.europa.ec.fisheries.ers.service.mapper.FluxLocationMapper;
import eu.europa.ec.fisheries.ers.service.mapper.view.base.BaseActivityViewMapper;
import eu.europa.ec.fisheries.ers.service.mdrcache.MDRAcronymType;
import eu.europa.ec.fisheries.ers.service.mdrcache.MDRCache;
import eu.europa.ec.fisheries.ers.service.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import un.unece.uncefact.data.standard.mdr.communication.ColumnDataType;
import un.unece.uncefact.data.standard.mdr.communication.ObjectRepresentation;

/**
 * Created by kovian on 03/03/2017.
 */
@Slf4j
public class FaCatchesProcessorMapper extends BaseActivityViewMapper {

    public static final String LSC = "LSC";
    public static final String BMS = "BMS";

    public FaCatchesProcessorMapper() {

    }

    /**
     * Takes a list of FaCatches and returns the list of groupDtos.
     *
     * @param faCatches
     * @return List<FaCatchGroupDto>
     */
    public static Set<FaCatchGroupDto> getCatchGroupsFromListEntity(Set<FaCatchEntity> faCatches) {
        if (CollectionUtils.isEmpty(faCatches)) {
            return new HashSet<>();
        }
        Map<String, Set<FaCatchEntity>> faCatchGroups = groupCatches(faCatches);
        return computeSumsAndMapToDtoGroups(faCatchGroups);
    }

    /**
     * Groups the faCatches List matching those that have in common all the properties but BMS, SLC and size;
     *
     * @param faCatches
     * @return
     */
    private static Map<String, Set<FaCatchEntity>> groupCatches(Set<FaCatchEntity> faCatches) {
        Map<String, Set<FaCatchEntity>> groups = new HashMap<>();
        int group_nr = 0;
        while (CollectionUtils.isNotEmpty(faCatches)) {
            groups.put(String.valueOf(group_nr), extractOneGroup(faCatches));
            group_nr++;
        }
        return groups;
    }

    /**
     * Extracts a group that has the needed properties in common with the catchesIteratorExt.next()
     *
     * @param faCatchesSet Set<FaCatchEntity>
     * @return
     */
    private static Set<FaCatchEntity> extractOneGroup(Set<FaCatchEntity> faCatchesSet) {
        Set<FaCatchEntity> group = new HashSet<>();
        Iterator<FaCatchEntity> catchesIteratorInt = faCatchesSet.iterator();
        FaCatchEntity extCatch = catchesIteratorInt.next();
        group.add(extCatch);
        while (catchesIteratorInt.hasNext()) {
            FaCatchEntity intCatch = catchesIteratorInt.next();
            if (FaCatchForViewComparator.catchesAreEqual(extCatch, intCatch)) {
                group.add(intCatch);
            }
        }
        faCatchesSet.removeAll(group);
        return group;
    }

    /**
     * Subgroups and fills the List<FaCatchGroupDto> mapping the required properties.
     *
     * @param faCatchGroups
     * @return
     */
    private static Set<FaCatchGroupDto> computeSumsAndMapToDtoGroups(Map<String, Set<FaCatchEntity>> faCatchGroups) {
        Set<FaCatchGroupDto> faCatchGroupsDtoList = new HashSet<>();
        for (Map.Entry<String, Set<FaCatchEntity>> group : faCatchGroups.entrySet()) {
            faCatchGroupsDtoList.add(mapFaCatchListToCatchGroupDto(group.getValue()));
        }
        return faCatchGroupsDtoList;
    }

    /**
     * Maps a list of CatchEntities (rappresenting a froup) to a  FaCatchGroupDto;
     *
     * @param groupCatchList
     * @return
     */
    private static FaCatchGroupDto mapFaCatchListToCatchGroupDto(Set<FaCatchEntity> groupCatchList) {
        FaCatchGroupDto groupDto = new FaCatchGroupDto();
        FaCatchEntity catchEntity = groupCatchList.iterator().next();
        // Set primary properties on groupDto
        groupDto.setType(catchEntity.getTypeCode());
        groupDto.setSpecies(catchEntity.getSpeciesCode());
        // Fill the denomination location part of the GroupDto.
        groupDto.setLocations(FaCatchGroupMapper.INSTANCE.mapFaCatchEntityToDenominationLocation(catchEntity));
        // calculate Totals And Fill Soecified Locations and Gears Per each Subgroup (subgrupped on BMS/LSC)
        calculateTotalsAndFillSubgroups(groupCatchList, groupDto);
        return groupDto;
    }

    /**
     * Subgroups by BMS and LSC and :
     * Calculates the total and fills all the details related to each subgroup.
     *
     * @param groupCatchList
     * @param groupDto
     */
    private static void calculateTotalsAndFillSubgroups(Set<FaCatchEntity> groupCatchList, FaCatchGroupDto groupDto) {
        Map<String, FaCatchGroupDetailsDto> groupingDetailsMap = groupDto.getGroupingDetails();
        FaCatchGroupDetailsDto lscGroupDetailsDto = new FaCatchGroupDetailsDto();
        FaCatchGroupDetailsDto bmsGroupDetailsDto = new FaCatchGroupDetailsDto();
        // Weight and units totals
        Double lscGroupTotalWeight = null;
        Double lscGroupTotalUnits = null;
        Double bmsGroupTotalWeight = null;
        Double bmsGroupTotalUnits = null;
        Double totalWeight = null;
        for (FaCatchEntity entity : groupCatchList) {
            Double calculatedWeightMeasure = entity.getCalculatedWeightMeasure();
            if (calculatedWeightMeasure == null) {
                calculatedWeightMeasure = extractLiveWeight(entity.getAapProcesses());
            }
            Double unitQuantity = entity.getUnitQuantity();
            String fishClassCode = entity.getFishClassCode() != null ? entity.getFishClassCode() : StringUtils.EMPTY;
            totalWeight = Utils.addDoubles(calculatedWeightMeasure, totalWeight);
            switch (fishClassCode) {
                case LSC:
                    // Weight and Units calculation
                    lscGroupTotalWeight = Utils.addDoubles(calculatedWeightMeasure, lscGroupTotalWeight);
                    lscGroupTotalUnits = Utils.addDoubles(unitQuantity, lscGroupTotalUnits);
                    fillDetailsForSubGroup(lscGroupDetailsDto, entity);
                    break;
                case BMS:
                    // Weight and Units calculation
                    bmsGroupTotalWeight = Utils.addDoubles(calculatedWeightMeasure, bmsGroupTotalWeight);
                    bmsGroupTotalUnits = Utils.addDoubles(unitQuantity, bmsGroupTotalUnits);
                    fillDetailsForSubGroup(bmsGroupDetailsDto, entity);
                    break;
                default:
                    log.error("While constructing Fa Catch Section of the view the FaCatchEntity with id : " + entity.getId() + " is neither LSC nor BMS!");
            }
        }
        setWeightsForSubGroup(groupDto, lscGroupDetailsDto, bmsGroupDetailsDto, lscGroupTotalWeight, lscGroupTotalUnits, bmsGroupTotalWeight, bmsGroupTotalUnits, totalWeight);
        // Put the 2 subgroup properties in the groupingDetailsMap (property of FaCatchGroupDto).

        List<FluxLocationDto> lscGroupDetailsDtoSpecifiedFluxLocations = lscGroupDetailsDto.getSpecifiedFluxLocations();
        lscGroupDetailsDto.setSpecifiedFluxLocations(new ArrayList<>(new LinkedHashSet<>(lscGroupDetailsDtoSpecifiedFluxLocations))); // remove duplicates
        List<FluxLocationDto> bmsGroupDetailsDtoSpecifiedFluxLocations = bmsGroupDetailsDto.getSpecifiedFluxLocations();
        bmsGroupDetailsDto.setSpecifiedFluxLocations(new ArrayList<>(new LinkedHashSet<>(bmsGroupDetailsDtoSpecifiedFluxLocations))); // remove duplicates
        groupingDetailsMap.put(BMS, bmsGroupDetailsDto);
        groupingDetailsMap.put(LSC, lscGroupDetailsDto);

    }
    
    public static Double getCalculatedWeightMeasure(FaCatchEntity entity) {
        Double calculatedWeightMeasure = entity.getCalculatedWeightMeasure();
        if (calculatedWeightMeasure == null) {
            calculatedWeightMeasure = extractLiveWeight(entity.getAapProcesses());
        }
        return calculatedWeightMeasure;
    }

    private static Double extractLiveWeight(Set<AapProcessEntity> aapProcesses) {
        Double totalWeight = null;
        Double convFc = null;  
        Double weightSum = 0.0;
        Integer catchId = 0;
        if (CollectionUtils.isNotEmpty(aapProcesses)) {
            for (AapProcessEntity aapProc : aapProcesses) {
                catchId = aapProc.getFaCatch().getId();
                convFc = extractConversionFactor(aapProc);
                weightSum = addToTotalWeightFromSetOfAapProduct(aapProc.getAapProducts(), weightSum);
            }
        }
        if(convFc == null) {
            convFc = 0.0;
            log.error("Couldn't find conversion factor for FaCatchEntity with id: " + catchId);
        }
        if (weightSum > 0.0) {
            totalWeight = convFc * weightSum;
            BigDecimal bd = new BigDecimal(totalWeight).setScale(2, RoundingMode.HALF_UP);
            totalWeight = bd.doubleValue();
        }
        return totalWeight;
    }

    private static Double addToTotalWeightFromSetOfAapProduct(Set<AapProductEntity> aapProducts, Double weightSum) {
        if (CollectionUtils.isNotEmpty(aapProducts)) {
            for (AapProductEntity aapProd : aapProducts) {
                weightSum = Utils.addDoubles(aapProd.getWeightMeasure(), weightSum);
            }
        }
        return weightSum;
    }

    private static void setWeightsForSubGroup(FaCatchGroupDto groupDto, FaCatchGroupDetailsDto lscGroupDetailsDto, FaCatchGroupDetailsDto bmsGroupDetailsDto, Double lscGroupTotalWeight, Double lscGroupTotalUnits, Double bmsGroupTotalWeight, Double bmsGroupTotalUnits, Double totalWeight) {

        // Set total weight and units for BMS and LSC
        lscGroupDetailsDto.setUnit(lscGroupTotalUnits);
        lscGroupDetailsDto.setWeight(lscGroupTotalWeight);
        bmsGroupDetailsDto.setUnit(bmsGroupTotalUnits);
        bmsGroupDetailsDto.setWeight(bmsGroupTotalWeight);

        // Set total group weight (LSC + BMS) (checking nullity : if both of them are null the sum must be 0.0)
        if (lscGroupTotalWeight == null) {
            lscGroupTotalWeight = 0.0;
        }

        if (bmsGroupTotalWeight == null) {
            bmsGroupTotalWeight = 0.0;
        }

        // Set total group weight (LSC + BMS)
        groupDto.setCalculatedWeight(totalWeight);
    }


    /**
     * Fills all the details related to the specific LCS / BMS subgroup.
     *
     * @param groupDetailsDto
     * @param actualEntity
     */
    private static void fillDetailsForSubGroup(FaCatchGroupDetailsDto groupDetailsDto, FaCatchEntity actualEntity) {
        fillSpecifiedAndDestinationLocationsInGroupDetails(actualEntity.getFluxLocations(), groupDetailsDto);
        fillFishingGears(actualEntity.getFishingGears(), groupDetailsDto);
        if (!groupDetailsDto.areDetailsSet()) {
            fillGroupDetails(groupDetailsDto, actualEntity);
        }
        fillFluxCharacteristics(groupDetailsDto, actualEntity.getFluxCharacteristics());
    }


    /**
     * Fills the locations on FaCatchGroupDetailsDto DTO.
     *
     * @param fluxLocations
     * @param groupDetailsDto
     */
    private static void fillSpecifiedAndDestinationLocationsInGroupDetails(Set<FluxLocationEntity> fluxLocations, FaCatchGroupDetailsDto groupDetailsDto) {
        if (CollectionUtils.isEmpty(fluxLocations)) {
            return;
        }
        List<DestinationLocationDto> destLocDtoList = groupDetailsDto.getDestinationLocation();
        List<FluxLocationDto> specifiedFluxLocDto = groupDetailsDto.getSpecifiedFluxLocations();

        for (FluxLocationEntity actLoc : fluxLocations) {
            String fluxLocationType = actLoc.getFluxLocationType();
            if (StringUtils.equals(fluxLocationType, FluxLocationCatchTypeEnum.FA_CATCH_DESTINATION.getType())) {
                destLocDtoList.add(new DestinationLocationDto(actLoc.getFluxLocationIdentifier(), actLoc.getCountryId(), actLoc.getName()));
            } else if (StringUtils.equals(fluxLocationType, FluxLocationCatchTypeEnum.FA_CATCH_SPECIFIED.getType())) {
                specifiedFluxLocDto.add(FluxLocationMapper.INSTANCE.mapEntityToFluxLocationDto(actLoc));
            }
        }
        groupDetailsDto.setDestinationLocation(destLocDtoList);
        groupDetailsDto.setSpecifiedFluxLocations(specifiedFluxLocDto);
    }

    /**
     * Fills the fishing gears toi the FaCatchGroupDetailsDto.
     *
     * @param fishingGears
     * @param groupDetailsDto
     */
    private static void fillFishingGears(Set<FishingGearEntity> fishingGears, FaCatchGroupDetailsDto groupDetailsDto) {
        groupDetailsDto.setGears(ActivityArrivalViewMapper.INSTANCE.getGearsFromEntity(fishingGears));
    }

    /**
     * Fill remaining details on group level;
     *
     * @param groupDetailsDto
     * @param actualEntity
     */
    private static void fillGroupDetails(FaCatchGroupDetailsDto groupDetailsDto, FaCatchEntity actualEntity) {
        groupDetailsDto.setStockId(CollectionUtils.isNotEmpty(actualEntity.getAapStocks()) ? actualEntity.getAapStocks().iterator().next().getStockId() : null);
        groupDetailsDto.setSize(actualEntity.getSizeDistribution() != null ? actualEntity.getSizeDistribution().getCategoryCode() : null);
        groupDetailsDto.setWeightingMeans(actualEntity.getWeighingMeansCode());
        groupDetailsDto.setUsage(actualEntity.getUsageCode());
        Set<FishingTripIdentifierEntity> fishingTripIdentifierEntities = CollectionUtils.isNotEmpty(actualEntity.getFishingTrips()) ? actualEntity.getFishingTrips().iterator().next().getFishingTripIdentifiers() : null;
        groupDetailsDto.setTripId(CollectionUtils.isNotEmpty(fishingTripIdentifierEntities) ? fishingTripIdentifierEntities.iterator().next().getTripId() : null);
        groupDetailsDto.setDetailsAreSet(true);
    }

    private static void fillFluxCharacteristics(FaCatchGroupDetailsDto groupDetailsDto, Set<FluxCharacteristicEntity> fluxCharacteristics) {
        if (CollectionUtils.isEmpty(fluxCharacteristics)) {
            return;
        }
        List<FluxCharacteristicsViewDto> applicableFluxCharacteristics = groupDetailsDto.getCharacteristics();
        for (FluxCharacteristicEntity flCharacEnt : fluxCharacteristics) {
            applicableFluxCharacteristics.add(FluxCharacteristicsViewDtoMapper.INSTANCE.mapFluxCharacteristicsEntityListToDtoList(flCharacEnt));
        }
    }

    @Override
    public FishingActivityViewDTO mapFaEntityToFaDto(FishingActivityEntity faEntity) {
        return null;
    }

    @Override
    protected ActivityDetailsDto populateActivityDetails(FishingActivityEntity faEntity, ActivityDetailsDto activityDetails) {
        return null;
    }

    /**
     * Class that serves as a comparator for 2 FaCatch entities.
     */
    private static class FaCatchForViewComparator {

        public static boolean catchesAreEqual(FaCatchEntity thisCatch, FaCatchEntity thatCatch) {
            if (thisCatch == thatCatch)
                return true;
            if (thisCatch == null && thatCatch == null)
                return true;
            if (thisCatch == null || thatCatch == null)
                return false;
            if (thisCatch.getTypeCode() != null ? !thisCatch.getTypeCode().equals(thatCatch.getTypeCode()) : thatCatch.getTypeCode() != null)
                return false;
            if (thisCatch.getSpeciesCode() != null ? !thisCatch.getSpeciesCode().equals(thatCatch.getSpeciesCode()) : thatCatch.getSpeciesCode() != null)
                return false;
            if (thisCatch.getUsageCode() != null ? !thisCatch.getUsageCode().equals(thatCatch.getUsageCode()) : thatCatch.getUsageCode() != null)
                return false;
            if (thisCatch.getTerritory() != null ? !thisCatch.getTerritory().equals(thatCatch.getTerritory()) : thatCatch.getTerritory() != null)
                return false;
            if (thisCatch.getFaoArea() != null ? !thisCatch.getFaoArea().equals(thatCatch.getFaoArea()) : thatCatch.getFaoArea() != null)
                return false;
            if (thisCatch.getIcesStatRectangle() != null ? !thisCatch.getIcesStatRectangle().equals(thatCatch.getIcesStatRectangle()) : thatCatch.getIcesStatRectangle() != null)
                return false;
            if (thisCatch.getEffortZone() != null ? !thisCatch.getEffortZone().equals(thatCatch.getEffortZone()) : thatCatch.getEffortZone() != null)
                return false;
            if (thisCatch.getRfmo() != null ? !thisCatch.getRfmo().equals(thatCatch.getRfmo()) : thatCatch.getRfmo() != null)
                return false;
            if (thisCatch.getGfcmGsa() != null ? !thisCatch.getGfcmGsa().equals(thatCatch.getGfcmGsa()) : thatCatch.getGfcmGsa() != null)
                return false;
            if (thisCatch.getGfcmStatRectangle() != null ? !thisCatch.getGfcmStatRectangle().equals(thatCatch.getGfcmStatRectangle()) : thatCatch.getGfcmStatRectangle() != null)
                return false;
            if (!sizeDestributionsAreEqual(thisCatch.getSizeDistribution(), thatCatch.getSizeDistribution()))
                return false;
            if (!aapStocsAreEqual(thisCatch.getAapStocks(), thatCatch.getAapStocks()))
                return false;
            if (!fishingTripsAreEquals(thisCatch.getFishingTrips(), thatCatch.getFishingTrips()))
                return false;
            return true;
        }

        private static boolean sizeDestributionsAreEqual(SizeDistributionEntity sizeThis, SizeDistributionEntity sizeThat) {
            if (sizeThis == sizeThat)
                return true;
            if (sizeThis == null && sizeThat == null)
                return true;
            if (sizeThis == null || sizeThat == null)
                return false;
            if (!StringUtils.equals(sizeThis.getCategoryCode(), sizeThat.getCategoryCode()))
                return false;
            return true;
        }

        private static boolean aapStocsAreEqual(Set<AapStockEntity> aapList_1, Set<AapStockEntity> aapList_2) {
            AapStockEntity aapStockThis = CollectionUtils.isNotEmpty(aapList_1) ? aapList_1.iterator().next() : null;
            AapStockEntity aapStockThat = CollectionUtils.isNotEmpty(aapList_2) ? aapList_2.iterator().next() : null;
            if (aapStockThis == aapStockThat)
                return true;
            if (aapStockThis == null && aapStockThat == null)
                return true;
            if (aapStockThis == null || aapStockThat == null)
                return false;
            if (!StringUtils.equals(aapStockThis.getStockId(), aapStockThat.getStockId()))
                return false;
            return true;
        }

        private static boolean fishingTripsAreEquals(Set<FishingTripEntity> aapList_1, Set<FishingTripEntity> aapList_2) {
            FishingTripEntity aapStockThis = CollectionUtils.isNotEmpty(aapList_1) ? aapList_1.iterator().next() : null;
            FishingTripEntity aapStockThat = CollectionUtils.isNotEmpty(aapList_2) ? aapList_2.iterator().next() : null;
            if (aapStockThis == aapStockThat)
                return true;
            if (aapStockThis == null && aapStockThat == null)
                return true;
            if (aapStockThis == null || aapStockThat == null)
                return false;
            FishingTripIdentifierEntity identifierThis = CollectionUtils.isNotEmpty(aapStockThis.getFishingTripIdentifiers()) ? aapStockThis.getFishingTripIdentifiers().iterator().next() : null;
            FishingTripIdentifierEntity identifierThat = CollectionUtils.isNotEmpty(aapStockThis.getFishingTripIdentifiers()) ? aapStockThis.getFishingTripIdentifiers().iterator().next() : null;
            if (identifierThis == identifierThat)
                return true;
            if (identifierThis == null && identifierThat == null)
                return true;
            if (identifierThis == null || identifierThat == null)
                return false;
            if (!StringUtils.equals(identifierThis.getTripId(), identifierThat.getTripId()))
                return false;
            return true;
        }

    }

    private static Double extractConversionFactor(AapProcessEntity aapProc) {
        FaCatchEntity entity = aapProc.getFaCatch();
        List<FluxLocationEntity> catchLocations = entity.getFluxLocations().stream()
                .filter(location -> FluxLocationSchemeId.TERRITORY.name().equals(location.getCountryIdSchemeId())
                        || FluxLocationSchemeId.MANAGEMENT_AREA.name().equals(location.getFluxLocationIdentifierSchemeId())
                        || FluxLocationSchemeId.TERRITORY.name().equals(location.getFluxLocationIdentifierSchemeId())).collect(Collectors.toList());
        if (catchLocations.size() != 1) {
            return getConversionFactorIfReported(aapProc);
        } else {
            String speciesCode = entity.getSpeciesCode();
            String preservation = entity.getAapProcesses().stream().flatMap(aapProcess -> aapProcess.getAapProcessCode().stream())
                    .filter(x -> x.getTypeCodeListId().equals("FISH_PRESERVATION")).findFirst().map(AapProcessCodeEntity::getTypeCode).orElse("");
            String presentation = entity.getAapProcesses().stream().flatMap(aapProcess -> aapProcess.getAapProcessCode().stream())
                    .filter(x -> x.getTypeCodeListId().equals("FISH_PRESENTATION")).findFirst().map(AapProcessCodeEntity::getTypeCode).orElse("");
            FluxLocationEntity catchLocation = catchLocations.get(0);
            String locationSchemeId;
            String locationId;
            if(catchLocation.getTypeCode().equals("AREA")) {
                locationSchemeId = catchLocation.getFluxLocationIdentifierSchemeId();
                locationId = catchLocation.getFluxLocationIdentifier();
            } else {
                locationSchemeId = catchLocation.getCountryIdSchemeId();
                locationId = catchLocation.getCountryId();
            }
            boolean condition = false;
            if(catchLocation.getTypeCode().equals("AREA") && locationSchemeId.equals(FluxLocationSchemeId.MANAGEMENT_AREA.name())) {
                condition = isPresentInMdr(MDRAcronymType.MANAGEMENT_AREA, locationId);
            } else if (locationSchemeId.equals(FluxLocationSchemeId.TERRITORY.name())) {
                condition = !locationId.equals("XEU") && !isPresentInMdr(MDRAcronymType.MEMBER_STATE, locationId);
            }
            if(condition) {
                Double factor = getConversionFactorFromMdr(new MDRCondition(MDRCondition.CODE, speciesCode),
                        new MDRCondition(MDRCondition.PRESENTATION, presentation),
                        new MDRCondition(MDRCondition.STATE, preservation),
                        new MDRCondition(MDRCondition.PLACES_CODE, locationId));
                if(factor != null) {
                    return factor;
                } else {
                    return getEUConversionFactor(aapProc, speciesCode, preservation, presentation);
                }
            } else {
                return getEUConversionFactor(aapProc, speciesCode, preservation, presentation);
            }
        }
    }

    private static Double getConversionFactorIfReported(AapProcessEntity aapProc) {
        return aapProc.getConversionFactor();
    }

    private static Double getEUConversionFactor(AapProcessEntity aapProc, String speciesCode, String preservation, String presentation) {
        Double factor = getConversionFactorFromMdr(new MDRCondition(MDRCondition.CODE, speciesCode),
                new MDRCondition(MDRCondition.PRESENTATION, presentation),
                new MDRCondition(MDRCondition.STATE, preservation),
                new MDRCondition(MDRCondition.PLACES_CODE, "XEU"));
        if(factor != null) {
            return factor;
        } else {
            return getFlagStateConversionFactor(aapProc, speciesCode, preservation, presentation);
        }
    }

    private static Double getFlagStateConversionFactor(AapProcessEntity aapProc, String speciesCode, String preservation, String presentation) {
        FaCatchEntity entity = aapProc.getFaCatch();
        String flagState = entity.getFishingActivity().getFaReportDocument().getVesselTransportMeans().stream().map(VesselTransportMeansEntity::getCountry).findFirst().orElse("");
        if(!flagState.isEmpty()) {
            Double factor = getConversionFactorFromMdr(new MDRCondition(MDRCondition.CODE, speciesCode),
                    new MDRCondition(MDRCondition.PRESENTATION, presentation),
                    new MDRCondition(MDRCondition.STATE, preservation),
                    new MDRCondition(MDRCondition.PLACES_CODE, flagState));
            if(factor != null) {
                return factor;
            } else {
                return getConversionFactorIfReported(aapProc);
            }
        } else {
            return getConversionFactorIfReported(aapProc);
        }
    }

    private static Double getConversionFactorFromMdr(MDRCondition... conditions) {
        Predicate<ObjectRepresentation> predicates = is -> true;
        for(MDRCondition condition: conditions){
            predicates = predicates.and(entry -> entry.getFields().stream().anyMatch(column -> column.getColumnName().equals(condition.getColumn()) && column.getColumnValue().equals(condition.getValue())));
        }

        List<ObjectRepresentation> entries = getMDRCacheBean().getEntry(MDRAcronymType.CONVERSION_FACTOR);
        ObjectRepresentation result = entries.stream().filter(predicates).findAny().orElse(null);
        if(result != null) {
            String factor = result.getFields().stream().filter(field -> field.getColumnName().equals("factor")).findFirst().map(ColumnDataType::getColumnValue).orElse("");
            return Double.parseDouble(factor);
        } else {
            return null;
        }
    }

    private static Boolean isPresentInMdr(MDRAcronymType acronym, String value) {
        List<ObjectRepresentation> entries = getMDRCacheBean().getEntry(acronym);
        for(ObjectRepresentation entry: entries) {
            if(entry.getFields().stream().anyMatch(column -> column.getColumnName().equals("code") && column.getColumnValue().equals(value))){
                return true;
            }
        }
        return false;
    }

    private static MDRCache getMDRCacheBean() {
        try {
            Context context = new InitialContext();
            return (MDRCache) context.lookup("java:module/MDRCache");
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup MDRCache bean ", e);
        }
    }

    static class MDRCondition {

        static final String CODE = "code"; //species code
        static final String PRESENTATION = "presentation"; //self-explanatory
        static final String STATE = "state"; //preservation state
        static final String PLACES_CODE = "placesCode"; //catch location

        String column;
        String value;

        public MDRCondition(String column, String value) {
            this.column = column;
            this.value = value;
        }

        public String getColumn() {
            return column;
        }

        public String getValue() {
            return value;
        }
    }

}

