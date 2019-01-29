package eu.europa.ec.fisheries.ers.service.bean;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import eu.europa.ec.fisheries.ers.fa.entities.*;
import eu.europa.ec.fisheries.ers.fa.utils.FluxLocationEnum;
import eu.europa.ec.fisheries.ers.fa.utils.MovementTypeComparator;
import eu.europa.ec.fisheries.ers.service.*;
import eu.europa.ec.fisheries.ers.service.util.DatabaseDialect;
import eu.europa.ec.fisheries.ers.service.util.Oracle;
import eu.europa.ec.fisheries.ers.service.util.Postgres;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.geometry.mapper.GeometryMapper;
import eu.europa.ec.fisheries.uvms.commons.geometry.utils.GeometryUtils;
import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.Transactional;
import java.util.*;


@Stateless
@LocalBean
@Transactional
@Slf4j
public class FishingActivityEnricherBean extends BaseActivityBean {

    private static final String PREVIOUS = "PREVIOUS";
    private static final String NEXT = "NEXT";
    private static final String START_DATE = "START_DATE";
    private static final String END_DATE = "END_DATE";

    @EJB
    private MovementModuleService movementService;

    @EJB
    private AssetModuleService assetService;

    @EJB
    private PropertiesBean properties;

    @EJB
    private FishingTripService fishingTripService;

    @EJB
    private MdrModuleService mdrModuleServiceBean;

    @EJB
    private SpatialModuleService spatialModuleService;

    @EJB
    private FaMessageSaverBean faMessageSaverBean;

    private DatabaseDialect dialect;

    @PostConstruct
    public void init() {
        dialect = new Postgres();
        if ("oracle".equals(properties.getProperty("database.dialect"))) {
            dialect = new Oracle();
        }
    }

    public void enrichFaReportDocuments(Set<FaReportDocumentEntity> faReportDocuments){
        log.info("[START-ENRICHING] Going to enrich [ " + faReportDocuments.size() + " ] FaReportDocuments..");
        for (FaReportDocumentEntity faReportDocument : faReportDocuments) {
            try {
                log.info("[INFO] Enriching with guids from assets module..");
                List<String> nextRepVessGuids = enrichFishingActivityWithGuiID(faReportDocument);
                log.info("[END] Finished enriching with guids from assets module..");
                log.info("[INFO] Updating geometry with movements module..");
                updateGeometry(faReportDocument, nextRepVessGuids);
                log.info("[END] Finished updating geometry with movements module..");
            } catch (Exception e) {
                log.error("[ERROR ENRICHMENT-FAILED] Could not update Geometry OR enrich Activities for faReportDocument (asset/movement modules):" + faReportDocument.getId());
            }
        }
        log.info("[END-ENRICHING] Enrichment finished..");
    }

    /**
     * Create Geometry for FaReportDocument and FluxLocation. In Flux location we save each reported location as a point geometry.
     * In Fa Report document, all the points are converted to Multipoint and saved as a single geometry.
     * In Fishing activity we save all the points those are reported in the corresponding flux location.
     *
     * @param faReportDocumentEntity
     * @param nextRepVessGuids
     */
    private void updateGeometry(FaReportDocumentEntity faReportDocumentEntity, List<String> nextRepVessGuids) throws ServiceException {
        List<MovementType> movements = getInterpolatedGeomForArea(faReportDocumentEntity, nextRepVessGuids);
        Set<FishingActivityEntity> fishingActivityEntities = faReportDocumentEntity.getFishingActivities();
        List<Geometry> multiPointForFaReport = populateGeometriesForFishingActivities(movements, fishingActivityEntities);
        faReportDocumentEntity.setGeom(GeometryUtils.createMultipoint(multiPointForFaReport));
    }

    private List<Geometry> populateGeometriesForFishingActivities(List<MovementType> movements, Set<FishingActivityEntity> fishingActivityEntities) throws ServiceException {
        List<Geometry> multiPointForFaReport = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fishingActivityEntities)) {
            for (FishingActivityEntity fishingActivity : fishingActivityEntities) {
                List<Geometry> multiPointForFa = new ArrayList<>();
                Date activityDate = fishingActivity.getOccurence() != null ? fishingActivity.getOccurence() : getFirstDateFromDelimitedPeriods(fishingActivity.getDelimitedPeriods());
                Geometry interpolatedPoint = interpolatePointFromMovements(movements, activityDate);
                for (FluxLocationEntity fluxLocation : fishingActivity.getFluxLocations()) {
                    Geometry point = null;
                    String fluxLocationStr = fluxLocation.getTypeCode();
                    if (fluxLocationStr.equalsIgnoreCase(FluxLocationEnum.AREA.name())) {
                        point = interpolatedPoint;
                        fluxLocation.setGeom(point);
                    } else if (fluxLocationStr.equalsIgnoreCase(FluxLocationEnum.LOCATION.name())) {
                        point = getGeometryForLocation(fluxLocation);
                        log.debug("[INFO] Geometry calculated for location is : " + point);
                        fluxLocation.setGeom(point);
                    } else if (fluxLocationStr.equalsIgnoreCase(FluxLocationEnum.POSITION.name())) {
                        point = GeometryUtils.createPoint(fluxLocation.getLongitude(), fluxLocation.getLatitude());
                        fluxLocation.setGeom(point);
                    }
                    if (point != null) {
                        multiPointForFa.add(point);
                        multiPointForFaReport.add(point);
                    }
                }
                fishingActivity.setGeom(GeometryUtils.createMultipoint(multiPointForFa));
            }
        }
        return multiPointForFaReport;
    }

    private Geometry getGeometryForLocation(FluxLocationEntity fluxLocation) throws ServiceException {
        Geometry point;
        if (fluxLocation.getLongitude() != null && fluxLocation.getLatitude() != null) {
            point = GeometryUtils.createPoint(fluxLocation.getLongitude(), fluxLocation.getLatitude());
        } else {
            point = getGeometryFromMdr(fluxLocation.getFluxLocationIdentifier());
            if (point == null) {
                point = getGeometryFromSpatial(fluxLocation.getFluxLocationIdentifier());
            }
        }
        return point;
    }

    private List<MovementType> getInterpolatedGeomForArea(FaReportDocumentEntity faReportDocumentEntity, List<String> nextRepVessGuids) throws ServiceException {
        if (CollectionUtils.isEmpty(faReportDocumentEntity.getVesselTransportMeans())) {
            return Collections.emptyList();
        }
        Set<VesselIdentifierEntity> vesselIdentifiers = faReportDocumentEntity.getVesselTransportMeans().iterator().next().getVesselIdentifiers();
        Map<String, Date> dateMap = findStartAndEndDate(faReportDocumentEntity);
        return getAllMovementsForDateRange(vesselIdentifiers, dateMap.get(START_DATE), dateMap.get(END_DATE), nextRepVessGuids);
    }

    /**
     * Find geometry for fluxLocation code in MDR
     *
     * @param fluxLocationIdentifier
     * @return
     */
    private Geometry getGeometryFromMdr(String fluxLocationIdentifier) throws ServiceException {
        log.debug("[INFO] Get Geometry from MDR for : " + fluxLocationIdentifier);
        if (fluxLocationIdentifier == null) {
            return null;
        }
        final List<String> columnsList = new ArrayList<>(Collections.singletonList("code"));
        try {
            Map<String, List<String>> portValuesFromMdr = mdrModuleServiceBean.getAcronymFromMdr("LOCATION", fluxLocationIdentifier, columnsList, 1, "latitude", "longitude");
            List<String> latitudeValues = portValuesFromMdr.get("latitude");
            List<String> longitudeValues = portValuesFromMdr.get("longitude");
            Double latitude = null;
            Double longitude = null;
            if (CollectionUtils.isNotEmpty(latitudeValues)) {
                String latitudeStr = latitudeValues.get(0);
                if (latitudeStr != null) {
                    latitude = Double.parseDouble(latitudeStr);
                }
            }
            if (CollectionUtils.isNotEmpty(longitudeValues)) {
                String longitudeStr = longitudeValues.get(0);
                if (longitudeStr != null) {
                    longitude = Double.parseDouble(longitudeStr);
                }
            }
            return GeometryUtils.createPoint(longitude, latitude);
        } catch (ServiceException e) {
            log.error("Error while retriving values from MDR.", e);
        }
        return null;
    }

    private List<MovementType> getAllMovementsForDateRange(Set<VesselIdentifierEntity> vesselIdentifiers, Date startDate, Date endDate, List<String> nextRepVessGuids) throws ServiceException {
        //List<String> assetGuids = assetService.getAssetGuids(vesselIdentifiers); // Call asset to get Vessel Guids, Removed since we calle this service upwards..
        return movementService.getMovement(nextRepVessGuids, startDate, endDate); // Send Vessel Guids to movements
    }

    private Geometry interpolatePointFromMovements(List<MovementType> movements, Date activityDate) throws ServiceException {
        if (movements == null || movements.isEmpty()) {
            return null;
        }
        Geometry faReportGeom;
        movements.sort(new MovementTypeComparator());
        Map<String, MovementType> movementTypeMap = getPreviousAndNextMovement(movements, activityDate);
        MovementType nextMovement = movementTypeMap.get(NEXT);
        MovementType previousMovement = movementTypeMap.get(PREVIOUS);
        try {

            if (previousMovement == null && nextMovement == null) {
                faReportGeom = null;
            } else if (nextMovement == null) {
                faReportGeom = GeometryMapper.INSTANCE.wktToGeometry(previousMovement.getWkt()).getValue();
                faReportGeom.setSRID(dialect.defaultSRID());
            } else if (previousMovement == null) {
                faReportGeom = GeometryMapper.INSTANCE.wktToGeometry(nextMovement.getWkt()).getValue();
                faReportGeom.setSRID(dialect.defaultSRID());
            } else {
                faReportGeom = calculateIntermediatePoint(previousMovement, nextMovement, activityDate);
            }
        } catch (ParseException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        return faReportGeom;
    }

    private Map<String, MovementType> getPreviousAndNextMovement(List<MovementType> movements, Date inputDate) {
        Map<String, MovementType> movementMap = new HashMap<>();
        for (MovementType movement : movements) {
            if (movement.getPositionTime().compareTo(inputDate) <= 0) {
                movementMap.put(PREVIOUS, movement);
            } else if (movement.getPositionTime().compareTo(inputDate) > 0) {
                movementMap.put(NEXT, movement);
                break;
            }
        }
        return movementMap;
    }

    private Geometry calculateIntermediatePoint(MovementType previousMovement, MovementType nextMovement, Date acceptedDate) throws ServiceException {

        Geometry point;
        Long durationAB = nextMovement.getPositionTime().getTime() - previousMovement.getPositionTime().getTime();
        long durationAC = acceptedDate.getTime() - previousMovement.getPositionTime().getTime();
        long durationBC = nextMovement.getPositionTime().getTime() - acceptedDate.getTime();
        try {
            if (durationAC == 0) {
                log.info("The point is same as the start point");
                point = GeometryMapper.INSTANCE.wktToGeometry(previousMovement.getWkt()).getValue();
            } else if (durationBC == 0) {
                log.info("The point is the same as end point");
                point = GeometryMapper.INSTANCE.wktToGeometry(nextMovement.getWkt()).getValue();
            } else {
                log.info("The point is between start and end point");
                LengthIndexedLine lengthIndexedLine = GeometryUtils.createLengthIndexedLine(previousMovement.getWkt(), nextMovement.getWkt());
                Double index = durationAC * (lengthIndexedLine.getEndIndex() - lengthIndexedLine.getStartIndex()) / durationAB;
                point = GeometryUtils.calculateIntersectingPoint(lengthIndexedLine, index);
            }
        } catch (ParseException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        point.setSRID(dialect.defaultSRID());
        return point;
    }

    private Map<String, Date> findStartAndEndDate(FaReportDocumentEntity faReportDocumentEntity) {
        TreeSet<Date> dates = new TreeSet<>();
        for (FishingActivityEntity fishingActivity : faReportDocumentEntity.getFishingActivities()) {
            if (fishingActivity.getOccurence() != null) {
                dates.add(fishingActivity.getOccurence());
            } else if (CollectionUtils.isNotEmpty(fishingActivity.getDelimitedPeriods())) {
                Date firstDate = getFirstDateFromDelimitedPeriods(fishingActivity.getDelimitedPeriods());
                if (firstDate != null) {
                    dates.add(firstDate);
                }
            }
        }
        return ImmutableMap.<String, Date>builder().put(START_DATE, dates.first()).put(END_DATE, dates.last()).build();
    }

    /**
     * Get Geometry information from spatial for FLUXLocation code
     *
     * @param fluxLocationIdentifier
     * @return
     */
    private Geometry getGeometryFromSpatial(String fluxLocationIdentifier) throws ServiceException {
        log.debug("Get Geometry from Spatial for:" + fluxLocationIdentifier);
        if (fluxLocationIdentifier == null) {
            return null;
        }
        Geometry geometry = null;
        try {
            String geometryWkt = spatialModuleService.getGeometryForPortCode(fluxLocationIdentifier);
            if (geometryWkt != null) {
                Geometry value = GeometryMapper.INSTANCE.wktToGeometry(geometryWkt).getValue();
                Coordinate[] coordinates = value.getCoordinates();
                if (coordinates.length > 0) {
                    Coordinate coordinate = coordinates[0];
                    double x = coordinate.x;
                    double y = coordinate.y;
                    geometry = GeometryUtils.createPoint(x, y);
                }
            }
            log.debug(" Geometry received from Spatial for:" + fluxLocationIdentifier + "  :" + geometryWkt);
        } catch (ParseException e) {
            log.error("Exception while trying to get geometry from spatial");
            throw new ServiceException(e.getMessage(), e);
        }
        return geometry;
    }



    private Date getFirstDateFromDelimitedPeriods(Collection<DelimitedPeriodEntity> delimitedPeriods) {
        TreeSet<Date> set = new TreeSet<>();
        for (DelimitedPeriodEntity delimitedPeriodEntity : delimitedPeriods) {
            if (delimitedPeriodEntity.getStartDate() != null)
                set.add(delimitedPeriodEntity.getStartDate());
        }
        if (CollectionUtils.isEmpty(set)) {
            return null;
        }
        return set.first();
    }

    // TODO : urgent need to avoid those loops!!! Need so send one request to assets instead of looping!!
    private List<String> enrichFishingActivityWithGuiID(FaReportDocumentEntity faReportDocument) {
        List<String> nextVessGuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(faReportDocument.getVesselTransportMeans())) {
            boolean nextVessel = true;
            for (VesselTransportMeansEntity vesselTransportMeansEntity : faReportDocument.getVesselTransportMeans()) {
                List<String> guids = enrichWithGuidFromAssets(vesselTransportMeansEntity);
                vesselTransportMeansEntity.setFaReportDocument(faReportDocument);
                if(nextVessel && CollectionUtils.isNotEmpty(guids)){
                    nextVessGuids.addAll(guids);
                    nextVessel = false;
                }
            }
        }
        Set<FishingActivityEntity> fishingActivities = faReportDocument.getFishingActivities();
        if (CollectionUtils.isEmpty(fishingActivities)) {
            return nextVessGuids;
        }
        for (FishingActivityEntity fishingActivityEntity : fishingActivities) {
            enrichFishingActivityVesselWithGuiId(fishingActivityEntity);
            if (fishingActivityEntity.getRelatedFishingActivity() != null)
                enrichFishingActivityVesselWithGuiId(fishingActivityEntity.getRelatedFishingActivity());
        }
        return nextVessGuids;
    }

    private void enrichFishingActivityVesselWithGuiId(FishingActivityEntity fishingActivityEntity) {
        Set<VesselTransportMeansEntity> vesselTransportMeansEntityList = fishingActivityEntity.getVesselTransportMeans();
        if (CollectionUtils.isEmpty(vesselTransportMeansEntityList)) {
            return;
        }
        for (VesselTransportMeansEntity entity : vesselTransportMeansEntityList) {
            enrichWithGuidFromAssets(entity);
            fishingActivityEntity.setVesselTransportGuid(entity.getGuid());
        }
    }

    /**
     * This method enriches the VesselTransportMeansEntity we got from FLUX with the related GUIDs.
     *
     * @param
     */
    private List<String> enrichWithGuidFromAssets(VesselTransportMeansEntity vesselTransport) {
        try {
            List<String> guids = assetService.getAssetGuids(vesselTransport.getVesselIdentifiers());
            if (CollectionUtils.isNotEmpty(guids)) {
                vesselTransport.setGuid(guids.get(0));
            }
            return guids;
        } catch (ServiceException e) {
            log.error("[ERROR] Error while trying to get guids from Assets Module!");
        }
        return null;
    }

}
