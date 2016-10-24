/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.activity.rest.resources;

import eu.europa.ec.fisheries.ers.fa.utils.FaReportSourceEnum;
import eu.europa.ec.fisheries.ers.service.ActivityService;
import eu.europa.ec.fisheries.ers.service.FluxMessageService;
import eu.europa.ec.fisheries.ers.service.search.FishingActivityQuery;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityFeaturesEnum;
import eu.europa.ec.fisheries.uvms.activity.rest.resources.util.ActivityExceptionInterceptor;
import eu.europa.ec.fisheries.uvms.activity.rest.resources.util.IUserRoleInterceptor;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.rest.resource.UnionVMSResource;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import eu.europa.ec.fisheries.uvms.spatial.model.constants.USMSpatial;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;
import lombok.extern.slf4j.Slf4j;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FAReportDocument;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FishingActivity;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.IDType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by padhyad on 7/6/2016.
 */
@Path("/fa")
@Slf4j
@Stateless
public class FishingActivityResource extends UnionVMSResource {

    @EJB
    private FluxMessageService fluxResponseMessageService;

    @EJB
    private ActivityService activityService;

    @EJB
    private USMService usmService;

    @GET
    @Produces(value = {MediaType.APPLICATION_JSON})
    @Path("/save")
    public Response saveFaReportDocument() throws ServiceException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("fa_flux_message.xml");
        JAXBContext jaxbContext = null;
        FLUXFAReportMessage fluxfaReportMessage;
        try {
            jaxbContext = JAXBContext.newInstance(FLUXFAReportMessage.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            fluxfaReportMessage = (FLUXFAReportMessage) jaxbUnmarshaller.unmarshal(is);
        } catch (JAXBException e) {
           throw new ServiceException(e.getMessage());
        }

        for (FAReportDocument faReportDocument : fluxfaReportMessage.getFAReportDocuments()) {
            CodeType purposeCode = new CodeType();
            purposeCode.setValue("5");
            purposeCode.setListID("Test scheme Id");
            faReportDocument.getRelatedFLUXReportDocument().setPurposeCode(purposeCode);
        }

        fluxResponseMessageService.saveFishingActivityReportDocuments(fluxfaReportMessage.getFAReportDocuments(), FaReportSourceEnum.FLUX);

        List<FAReportDocument> faReportDocumentList = fluxfaReportMessage.getFAReportDocuments();
        for (FAReportDocument faReportDocument : faReportDocumentList) {
            IDType id = faReportDocument.getRelatedFLUXReportDocument().getIDS().get(0);
            faReportDocument.getRelatedFLUXReportDocument().setReferencedID(id);

            IDType newId = new IDType();
            newId.setValue("New Id 1");
            newId.setSchemeID("New scheme Id 1");
            faReportDocument.getRelatedFLUXReportDocument().setIDS(Arrays.asList(newId));

            CodeType purposeCode = new CodeType();
            purposeCode.setValue("5");
            purposeCode.setListID("Test scheme Id");
            faReportDocument.getRelatedFLUXReportDocument().setPurposeCode(purposeCode);

            for (FishingActivity fishingActivity : faReportDocument.getSpecifiedFishingActivities()) {
                fishingActivity.setRelatedFishingActivities(null);
            }
        }
        fluxResponseMessageService.saveFishingActivityReportDocuments(faReportDocumentList, FaReportSourceEnum.FLUX);
        return createSuccessResponse();
    }


    @POST
    @Path("/list")
    @Consumes(value = {MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Interceptors(ActivityExceptionInterceptor.class)
    @IUserRoleInterceptor(requiredUserRole = {ActivityFeaturesEnum.LIST_ACTIVITY_REPORTS})
    public Response listActivityReportsByQuery(@Context HttpServletRequest request,
                                               @HeaderParam("scopeName") String scopeName,
                                               @HeaderParam("roleName") String roleName,
                                               FishingActivityQuery fishingActivityQuery) throws ServiceException {

        log.info("Query Received to search Fishing Activity Reports. " + fishingActivityQuery);
        if (fishingActivityQuery == null) {
            return createErrorResponse("Query to find list is null.");
        }
        String username = request.getRemoteUser();
        List<Dataset> datasets = usmService.getDatasetsPerCategory(USMSpatial.USM_DATASET_CATEGORY, username, USMSpatial.APPLICATION_NAME, roleName, scopeName);
        Response responseMethod = createSuccessResponse(activityService.getFishingActivityListByQuery(fishingActivityQuery, datasets));
        log.info("successful");
        return responseMethod;
    }

    @GET
    @Path("/history/{referenceId}/{schemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Interceptors(ActivityExceptionInterceptor.class)
    @IUserRoleInterceptor(requiredUserRole = {ActivityFeaturesEnum.LIST_ACTIVITY_REPORTS})
    public Response getAllCorrections(@Context HttpServletRequest request,
                                      @Context HttpServletResponse response,
                                      @PathParam("referenceId") String referenceId,
                                      @PathParam("schemeId") String schemeId) throws ServiceException {

        return createSuccessResponse(activityService.getFaReportCorrections(referenceId, schemeId));
    }
}
