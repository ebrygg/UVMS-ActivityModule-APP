/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.ers.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ec.fisheries.ers.fa.entities.FaReportDocumentEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FishingActivityEntity;
import eu.europa.ec.fisheries.ers.fa.entities.FluxFaReportMessageEntity;
import eu.europa.ec.fisheries.ers.fa.utils.FaReportSourceEnum;
import eu.europa.ec.fisheries.ers.service.mapper.view.ActivityArrivalViewMapper;
import eu.europa.ec.fisheries.ers.service.dto.view.parent.FishingActivityViewDTO;
import eu.europa.ec.fisheries.ers.service.mapper.view.base.ActivityViewEnum;
import eu.europa.ec.fisheries.ers.service.mapper.view.base.ActivityViewMapperFactory;
import eu.europa.ec.fisheries.ers.service.mapper.view.base.BaseActivityViewMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by kovian on 09/02/2017.
 */
public class FishingActivityViewMapperTest {

    FishingActivityEntity fishingActivity;

    @Before
    @SneakyThrows
    public void initFishingActivityEntity(){
        FLUXFAReportMessage fluxfaReportMessage = getActivityDataFromXML();
        FluxFaReportMessageEntity fluxRepMessageEntity = FluxFaReportMessageMapper.INSTANCE.mapToFluxFaReportMessage(fluxfaReportMessage, FaReportSourceEnum.FLUX, new FluxFaReportMessageEntity());
        List<FaReportDocumentEntity> faReportDocuments = new ArrayList<>(fluxRepMessageEntity.getFaReportDocuments());
        fishingActivity = faReportDocuments.get(0).getFishingActivities().iterator().next();
    }

    @Test
    @SneakyThrows
    public void testActivityArrivalViewMapper(){

        BaseActivityViewMapper mapperForView = ActivityViewMapperFactory.getMapperForView(ActivityViewEnum.ARRIVAL);

        FishingActivityViewDTO fishingActivityViewDTO = mapperForView.mapFaEntityToFaDto(getFishingActivityEntity());

        assertNotNull(fishingActivityViewDTO.getActivityDetails());
        assertNotNull(fishingActivityViewDTO.getGears());
        assertNotNull(fishingActivityViewDTO.getReportDoc());
        assertTrue(fishingActivityViewDTO.getGears().size() == 1);
        assertNull(mapperForView.mapFaEntityToFaDto(null));

        printDtoOnConsole(fishingActivityViewDTO);
    }

    private void printDtoOnConsole(FishingActivityViewDTO fishingActivityViewDTO) throws JsonProcessingException {
        System.out.println(new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true).writeValueAsString(fishingActivityViewDTO));
    }

    @Test
    @SneakyThrows
    public void testActivityLandingViewMapper(){

        BaseActivityViewMapper mapperForView = ActivityViewMapperFactory.getMapperForView(ActivityViewEnum.LANDING);
        FishingActivityViewDTO fishingActivityViewDTO = mapperForView.mapFaEntityToFaDto(getFishingActivityEntity());

        assertNotNull(fishingActivityViewDTO.getActivityDetails());
        assertNotNull(fishingActivityViewDTO.getReportDoc());
        assertNull(ActivityArrivalViewMapper.INSTANCE.mapFaEntityToFaDto(null));

        printDtoOnConsole(fishingActivityViewDTO);
    }

    private FLUXFAReportMessage getActivityDataFromXML() throws JAXBException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("fa_flux_message.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(FLUXFAReportMessage.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (FLUXFAReportMessage) jaxbUnmarshaller.unmarshal(is);
    }

    private FishingActivityEntity getFishingActivityEntity() throws JAXBException {
        if(fishingActivity == null){
            initFishingActivityEntity();
        }
        return fishingActivity;
    }
}
