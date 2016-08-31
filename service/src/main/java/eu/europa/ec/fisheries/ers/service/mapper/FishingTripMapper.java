/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.ers.service.mapper;

import eu.europa.ec.fisheries.ers.fa.entities.*;
import eu.europa.ec.fisheries.uvms.activity.model.dto.fareport.details.FishingTripDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.DelimitedPeriod;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._18.FishingTrip;
import un.unece.uncefact.data.standard.unqualifieddatatype._18.IDType;


import java.util.*;

/**
 * Created by padhyad on 6/15/2016.
 */
@Mapper(uses = {DelimitedPeriodMapper.class})
public abstract class FishingTripMapper extends BaseMapper {

    public static final FishingTripMapper INSTANCE = Mappers.getMapper(FishingTripMapper.class);

    @Mappings({
            @Mapping(target = "typeCode", expression = "java(getCodeType(fishingTrip.getTypeCode()))"),
            @Mapping(target = "typeCodeListId", expression = "java(getCodeTypeListId(fishingTrip.getTypeCode()))"),
            @Mapping(target = "fishingTripIdentifiers", expression = "java(mapToFishingTripIdentifierEntities(fishingTrip.getIDS(), fishingTripEntity))"),
            @Mapping(target = "fishingActivity", expression = "java(fishingActivityEntity)"),
            @Mapping(target = "delimitedPeriods", expression = "java(getDelimitedPeriodEntities(fishingTrip.getSpecifiedDelimitedPeriods(), fishingTripEntity))")
    })
    public abstract FishingTripEntity mapToFishingTripEntity(FishingTrip fishingTrip, FishingActivityEntity fishingActivityEntity, @MappingTarget FishingTripEntity fishingTripEntity);

    @Mappings({
            @Mapping(target = "typeCode", expression = "java(getCodeType(fishingTrip.getTypeCode()))"),
            @Mapping(target = "typeCodeListId", expression = "java(getCodeTypeListId(fishingTrip.getTypeCode()))"),
            @Mapping(target = "fishingTripIdentifiers", expression = "java(mapToFishingTripIdentifierEntities(fishingTrip.getIDS(), fishingTripEntity))"),
            @Mapping(target = "faCatch", expression = "java(faCatchEntity)"),
            @Mapping(target = "delimitedPeriods", expression = "java(getDelimitedPeriodEntities(fishingTrip.getSpecifiedDelimitedPeriods(), fishingTripEntity))")
    })
    public abstract FishingTripEntity mapToFishingTripEntity(FishingTrip fishingTrip, FaCatchEntity faCatchEntity, @MappingTarget FishingTripEntity fishingTripEntity);

    @Mappings({
            @Mapping(target = "tripType", source = "typeCode"),
            @Mapping(target = "tripIds", expression = "java(getTripIds(fishingTripEntity.getFishingTripIdentifiers()))"),
            @Mapping(target = "delimitedPeriods", source = "delimitedPeriods")
    })
    public abstract FishingTripDetailsDTO mapToFishingTripDetailsDTO(FishingTripEntity fishingTripEntity);

    public FishingTripDetailsDTO mapToFishingTripDetailsDTO(Set<FishingTripEntity> fishingTripEntities) {
        if (fishingTripEntities != null && !fishingTripEntities.isEmpty()) {
            return mapToFishingTripDetailsDTO(fishingTripEntities.iterator().next());
        }
        return null;
    }

    public abstract List<FishingTripDetailsDTO> mapToFishingTripDetailsDTOList(Set<FishingTripEntity> fishingTripEntities);

    protected List<String> getTripIds(Set<FishingTripIdentifierEntity> fishingTripIdentifiers) {
        List<String> ids = new ArrayList<>();
        for (FishingTripIdentifierEntity identifierEntity : fishingTripIdentifiers) {
            ids.add(identifierEntity.getTripId());
        }
        return ids;
    }

    @Mappings({
            @Mapping(target = "tripId", expression = "java(getIdType(idType))"),
            @Mapping(target = "tripSchemeId", expression = "java(getIdTypeSchemaId(idType))")
    })
    protected abstract FishingTripIdentifierEntity mapToFishingTripIdentifierEntity(IDType idType);

    protected Set<DelimitedPeriodEntity> getDelimitedPeriodEntities(List<DelimitedPeriod> delimitedPeriods, FishingTripEntity fishingTripEntity) {
        if (delimitedPeriods == null || delimitedPeriods.isEmpty()) {
            return Collections.emptySet();
        }
        Set<DelimitedPeriodEntity> delimitedPeriodEntities = new HashSet<>();
        for (DelimitedPeriod delimitedPeriod : delimitedPeriods) {
            DelimitedPeriodEntity delimitedPeriodEntity = DelimitedPeriodMapper.INSTANCE.mapToDelimitedPeriodEntity(delimitedPeriod, fishingTripEntity, new DelimitedPeriodEntity());
            delimitedPeriodEntities.add(delimitedPeriodEntity);
        }
        return delimitedPeriodEntities;
    }

    protected Set<FishingTripIdentifierEntity> mapToFishingTripIdentifierEntities(List<IDType> idTypes, FishingTripEntity fishingTripEntity) {
        if (idTypes == null || idTypes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<FishingTripIdentifierEntity> identifierEntities = new HashSet<>();
        for (IDType idType : idTypes) {
            FishingTripIdentifierEntity identifierEntity = FishingTripMapper.INSTANCE.mapToFishingTripIdentifierEntity(idType);
            identifierEntity.setFishingTrip(fishingTripEntity);
            identifierEntities.add(identifierEntity);
        }
        return identifierEntities;
    }
}