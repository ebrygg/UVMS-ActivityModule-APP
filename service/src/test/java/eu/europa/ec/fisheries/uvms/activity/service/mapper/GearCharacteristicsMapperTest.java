/*
 *
 * Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries © European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package eu.europa.ec.fisheries.uvms.activity.service.mapper;

import eu.europa.ec.fisheries.uvms.activity.fa.entities.FishingGearEntity;
import eu.europa.ec.fisheries.uvms.activity.fa.entities.GearCharacteristicEntity;
import eu.europa.ec.fisheries.uvms.activity.service.dto.view.GearDto;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;
import static org.mockito.internal.util.collections.Sets.newSet;

@RunWith(JUnitParamsRunner.class)
public class GearCharacteristicsMapperTest {

    @Test
    @Parameters(method = "methodName")
    public void testMapGearDtoToFishingGearEntityWithTypeCodeDG(GearCharacteristicEntity entity, String typeCode, GearDto expectedDto) {
        entity.setTypeCode(typeCode);
        FishingGearEntity fishingGearEntity = new FishingGearEntity();
        fishingGearEntity.setGearCharacteristics(newSet(entity));
        GearDto mappedDto = GearCharacteristicsMapper.INSTANCE.mapGearDtoToFishingGearEntity(fishingGearEntity);
        assertEquals(expectedDto, mappedDto);
    }

    protected Object[] methodName(){
        GearCharacteristicEntity entity = new GearCharacteristicEntity().
                toBuilder(). // weird way of creating the builder, but otherwise Lombok removes field initializers
                        valueMeasure(20.25).
                        valueMeasureUnitCode("kg").
                        typeCode(GearCharacteristicConstants.GEAR_CHARAC_TYPE_CODE_GD).
                        description("Trawls & Seines")
                .build();

        return $(
                $(entity, GearCharacteristicConstants.GEAR_CHARAC_TYPE_CODE_GD, GearDto.builder().description("Trawls & Seines").build()),
                $(entity, GearCharacteristicConstants.GEAR_CHARAC_TYPE_CODE_ME, GearDto.builder().meshSize("20.25kg").build())
                // TODO TEST ALL CASES
        );
    }
}
