/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries  European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.ers.fa.dao;


import com.vividsolutions.jts.geom.Geometry;
import eu.europa.ec.fisheries.ers.fa.entities.FishingActivityEntity;
import eu.europa.ec.fisheries.ers.fa.utils.FaReportStatusEnum;
import eu.europa.ec.fisheries.ers.service.search.*;
import eu.europa.ec.fisheries.uvms.common.DateUtils;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.service.AbstractDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;

/**
 * Created by padhyad on 5/3/2016.
 */

public class FishingActivityDao extends AbstractDAO<FishingActivityEntity> {
    private static final Logger LOG = LoggerFactory.getLogger(FishingActivityDao.class);
  //  private  static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    private  static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss";


    private static  final String FISHING_ACTIVITY_LIST_ALL_DATA="SELECT DISTINCT a  from FishingActivityEntity a LEFT JOIN FETCH a.faReportDocument fa where fa.status = '"+ FaReportStatusEnum.NEW.getStatus() +"' order by fa.acceptedDatetime asc ";

    private EntityManager em;

    public FishingActivityDao(EntityManager em) {
        this.em = em;
    }


    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<FishingActivityEntity> getFishingActivityList() throws ServiceException {
        return getFishingActivityList(null);
    }



    public List<FishingActivityEntity> getFishingActivityListForFishingTrip(String fishingTripId, Geometry multipolgon) throws ServiceException {
        if(fishingTripId == null || fishingTripId.length() == 0)
            throw new ServiceException("fishing Trip Id is null or empty. ");
        Query query = getEntityManager().createNamedQuery(FishingActivityEntity.ACTIVITY_FOR_FISHING_TRIP);

        query.setParameter("fishingTripId", fishingTripId);
        query.setParameter("area", multipolgon);
        return query.getResultList();
    }

    public List<FishingActivityEntity> getFishingActivityList(Pagination pagination) throws ServiceException {
        LOG.info("There are no Filters present to filter Fishing Activity Data. so, fetch all the Fishing Activity Records");
        TypedQuery<FishingActivityEntity> typedQuery = em.createQuery(FISHING_ACTIVITY_LIST_ALL_DATA, FishingActivityEntity.class);

        if(pagination!=null) {
            int listSize =pagination.getListSize();
            int pageNumber = pagination.getPage();
            if(listSize ==0 || pageNumber ==0)
                  throw new ServiceException("Error is pagination list size or page number.Please enter valid values. List Size provided: "+listSize + " Page number:"+pageNumber);

            typedQuery.setFirstResult(listSize * (pageNumber - 1));
            typedQuery.setMaxResults(listSize);
        }

        return typedQuery.getResultList();
    }

    public Integer getCountForFishingActivityList()  {
        LOG.info("Get Total Count for Fishing Activities When no filter criteria is present");
        TypedQuery<FishingActivityEntity> typedQuery = em.createQuery(FISHING_ACTIVITY_LIST_ALL_DATA, FishingActivityEntity.class);
        return typedQuery.getResultList().size();
    }


    public Integer getCountForFishingActivityListByQuery(FishingActivityQuery query, Geometry multipolygon) throws ServiceException {
        LOG.info("Get Total Count for Fishing Activities When filter criteria is present");
        StringBuilder sqlToGetActivityListCount =SearchQueryBuilder.createSQL(query);

        Query countQuery= getTypedQueryForFishingActivityFilter(sqlToGetActivityListCount, query, multipolygon);

        return countQuery.getResultList().size();
    }


    // Set typed values for Dynamically generated Query
    private Query getTypedQueryForFishingActivityFilter(StringBuilder sql, FishingActivityQuery query, Geometry multipolygon){
        LOG.debug("Set Typed Parameters to Query");
        Map<Filters,String> mappings =  FilterMap.getFilterQueryParameterMappings();
        Query typedQuery = em.createQuery(sql.toString());
        Map<Filters,String> searchCriteriaMap = query.getSearchCriteriaMap();

        LOG.info("Area intersection is the minimum default condition to find the fishing activities");
        typedQuery.setParameter("area", multipolygon); // parameter name area is specified in create SQL

        if(searchCriteriaMap ==null) {
            return typedQuery;
        }
        // Assign values to created SQL Query
        for (Map.Entry<Filters,String> entry : searchCriteriaMap.entrySet()){

            Filters key =  entry.getKey();
            String value=  entry.getValue();
            //For WeightMeasure there is no mapping present, In that case
            if(mappings.get(key) ==null)
                continue;

            switch (key) {
                case PERIOD_START:
                    typedQuery.setParameter(mappings.get(key), DateUtils.parseToUTCDate(value,FORMAT));
                    break;
                case PERIOD_END:
                    typedQuery.setParameter(mappings.get(key), DateUtils.parseToUTCDate(value,FORMAT));
                    break;
                case QUNTITY_MIN:
                    typedQuery.setParameter(mappings.get(key), SearchQueryBuilder.normalizeWeightValue(value,searchCriteriaMap.get(Filters.WEIGHT_MEASURE)));
                    break;
                case QUNTITY_MAX:
                    typedQuery.setParameter(mappings.get(key), SearchQueryBuilder.normalizeWeightValue(value,searchCriteriaMap.get(Filters.WEIGHT_MEASURE)));
                    break;
                case MASTER:
                    typedQuery.setParameter(mappings.get(key), value.toUpperCase());
                    break;
                case FA_REPORT_ID:
                    typedQuery.setParameter(mappings.get(key), Integer.parseInt(value));
                    break;
                default:
                    typedQuery.setParameter(mappings.get(key), value);
                    break;
            }

        }
        return typedQuery;
    }


    /*
     Get all the Fishing Activities which match Filter criterias mentioned in the Input. Also, provide the sorted data based on what user has requested.
     Provide paginated data if user has asked for it
     */
    public List<FishingActivityEntity> getFishingActivityListByQuery(FishingActivityQuery query, Geometry multipolygon) throws ServiceException {
        LOG.info("Get Fishing Activity Report list by Query.");

        // Create Query dynamically based on filter and Sort criteria
        StringBuilder sqlToGetActivityList =SearchQueryBuilder.createSQL(query);

        // Apply real values to Query built
        Query listQuery= getTypedQueryForFishingActivityFilter(sqlToGetActivityList,query, multipolygon);

        Pagination pagination= query.getPagination();
        if(pagination!=null) {
            listQuery.setFirstResult(pagination.getListSize() * (pagination.getPage() - 1));
            listQuery.setMaxResults(pagination.getListSize());
        }

        return listQuery.getResultList();
    }










}