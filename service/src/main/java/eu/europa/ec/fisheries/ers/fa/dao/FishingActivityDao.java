/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries  European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.ers.fa.dao;


import eu.europa.ec.fisheries.ers.fa.entities.FishingActivityEntity;
import eu.europa.ec.fisheries.ers.fa.utils.FaReportStatusEnum;
import eu.europa.ec.fisheries.ers.fa.utils.WeightConversion;
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
import java.util.Set;

/**
 * Created by padhyad on 5/3/2016.
 */

public class FishingActivityDao extends AbstractDAO<FishingActivityEntity> {
    private static final Logger LOG = LoggerFactory.getLogger(FishingActivityDao.class);
    final static String FORMAT = "yyyy-MM-dd HH:mm:ss";
    final static String JOIN =" JOIN FETCH ";
    final static String FISHING_ACTIVITY_JOIN=" from FishingActivityEntity a JOIN FETCH a.faReportDocument fa ";
    final static String FISHING_ACTIVITY_LIST_ALL_DATA="SELECT DISTINCT a  from FishingActivityEntity a JOIN FETCH a.faReportDocument fa where fa.status = '"+ FaReportStatusEnum.NEW.getStatus() +"' order by fa.acceptedDatetime asc ";

    private EntityManager em;

    public FishingActivityDao(EntityManager em) {
        this.em = em;
    }


    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<FishingActivityEntity> getFishingActivityList(){
        return getFishingActivityList(null);
    }



    public List<FishingActivityEntity> getFishingActivityListForFishingTrip(String fishingTripId,Pagination pagination) throws ServiceException {
        if(fishingTripId == null || fishingTripId.length() == 0)
            throw new ServiceException("fishing Trip Id is null or empty. ");
        Query query = getEntityManager().createNamedQuery(FishingActivityEntity.ACTIVITY_FOR_FISHING_TRIP);

        query.setParameter("fishingTripId", fishingTripId);
        if(pagination!=null) {
            query.setFirstResult(pagination.getListSize() * (pagination.getPage() - 1));
            query.setMaxResults(pagination.getListSize());
        }

        return query.getResultList();
    }

    public List<FishingActivityEntity> getFishingActivityList(Pagination pagination)  {

        TypedQuery<FishingActivityEntity> typedQuery = em.createQuery(FISHING_ACTIVITY_LIST_ALL_DATA, FishingActivityEntity.class);
        if(pagination!=null) {
            typedQuery.setFirstResult(pagination.getListSize() * (pagination.getPage() - 1));
            typedQuery.setMaxResults(pagination.getListSize());
        }

        return typedQuery.getResultList();
    }

    public Integer getCountForFishingActivityList(Pagination pagination)  {

        TypedQuery<FishingActivityEntity> typedQuery = em.createQuery(FISHING_ACTIVITY_LIST_ALL_DATA, FishingActivityEntity.class);
        return typedQuery.getResultList().size();
    }


    public Integer getCountForFishingActivityListByQuery(FishingActivityQuery query) throws ServiceException {
        StringBuilder sqlToGetActivityListCount =createSQL(query);
        LOG.info("countQuery :"+sqlToGetActivityListCount);
        Query countQuery= getTypedQueryForFishingActivityFilter(sqlToGetActivityListCount,query);

        return countQuery.getResultList().size();
    }

    public List<FishingActivityEntity> getFishingActivityListByQuery(FishingActivityQuery query) throws ServiceException {


        StringBuilder sqlToGetActivityList =createSQL(query);
        LOG.info("listQuery :"+sqlToGetActivityList);
        Query listQuery= getTypedQueryForFishingActivityFilter(sqlToGetActivityList,query);

        Pagination pagination= query.getPagination();

        if(pagination!=null) {
            listQuery.setFirstResult(pagination.getListSize() * (pagination.getPage() - 1));
            listQuery.setMaxResults(pagination.getListSize());
        }

        return listQuery.getResultList();
    }

    private Query getTypedQueryForFishingActivityFilter(StringBuilder sql,FishingActivityQuery query){

        Map<Filters,String> mappings =  FilterMap.getFilterQueryParameterMappings();
        Query typedQuery = em.createQuery(sql.toString());


        Map<Filters,String> searchCriteriaMap = query.getSearchCriteriaMap();

        if(searchCriteriaMap == null || searchCriteriaMap.isEmpty())
            return typedQuery;

        // Assign values to created SQL Query
        for(Filters key:searchCriteriaMap.keySet()){

            if(mappings.get(key) ==null)
                continue;

            String value=searchCriteriaMap.get(key);
            switch (key) {
                case PERIOD_START:
                    typedQuery.setParameter(mappings.get(key), DateUtils.parseToUTCDate(value,FORMAT));
                    break;
                case PERIOD_END:
                    typedQuery.setParameter(mappings.get(key), DateUtils.parseToUTCDate(value,FORMAT));
                    break;
                case QUNTITY_MIN:
                    typedQuery.setParameter(mappings.get(key), normalizeWeightValue(value,searchCriteriaMap.get(Filters.WEIGHT_MEASURE)));
                    break;
                case QUNTITY_MAX:
                    typedQuery.setParameter(mappings.get(key), normalizeWeightValue(value,searchCriteriaMap.get(Filters.WEIGHT_MEASURE)));
                    break;
                case MASTER:
                    typedQuery.setParameter(mappings.get(key), value.toUpperCase());
                    break;
                default:
                    typedQuery.setParameter(mappings.get(key), value);
                    break;
            }

        }
         return typedQuery;
    }


    private Double normalizeWeightValue(String value, String weightMeasure){
        Double valueConverted = Double.parseDouble(value);
        if(WeightConversion.TON.equals(weightMeasure))
            valueConverted= WeightConversion.convertToKiloGram(Double.parseDouble(value),WeightConversion.TON);

        return valueConverted;
    }

    private StringBuilder createSQL(FishingActivityQuery query) throws ServiceException {

        Map<Filters,String> searchCriteriaMap = query.getSearchCriteriaMap();
        Map<Filters, FilterDetails> mappings= FilterMap.getFilterMappings();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT a ");
        sql.append(FISHING_ACTIVITY_JOIN);

        // Create join part of SQL query

        Set<Filters> keySet =searchCriteriaMap.keySet();
        for(Filters key:keySet){
            FilterDetails details=mappings.get(key);
           if(details == null)
               continue;
           String joinString = details.getJoinString();

           // Add join statement only if its not already been added
           if(sql.indexOf(joinString)==-1){

               //If table join is already present in Query, we want to reuse that join alias. so, treat it differently
               if(Filters.MASTER.equals(key) && sql.indexOf(FilterMap.VESSEL_TRANSPORT_TABLE_ALIAS)!=-1 ){
                   sql.append(JOIN).append(FilterMap.MASTER_MAPPING).append(" ");
               }// Add table alias if not already present
                else if( Filters.VESSEL_IDENTIFIRE.equals(key) && sql.indexOf(FilterMap.VESSEL_TRANSPORT_TABLE_ALIAS)==-1){
                   sql.append(JOIN).append(FilterMap.VESSEL_TRANSPORT_TABLE_ALIAS);
                   sql.append(JOIN).append(details.getJoinString()).append(" ");
               } else if( Filters.SPECIES.equals(key) && sql.indexOf(FilterMap.FA_CATCH_TABLE_ALIAS)==-1){
                   sql.append(JOIN).append(FilterMap.FA_CATCH_TABLE_ALIAS);
                   sql.append(JOIN).append(details.getJoinString()).append(" ");
               } else{
                   sql.append(JOIN).append(details.getJoinString()).append(" ");
               }
           }
       }


        SortKey sort = query.getSortKey();

          if (sort != null ) {
              Filters field = sort.getField();
              if (Filters.PERIOD_START.equals(field) || Filters.PERIOD_END.equals(field) && sql.indexOf(FilterMap.DELIMITED_PERIOD_TABLE_ALIAS) == -1) {
                  sql.append(JOIN).append(FilterMap.DELIMITED_PERIOD_TABLE_ALIAS);
              } else if (Filters.PURPOSE.equals(field) && sql.indexOf(FilterMap.FLUX_REPORT_DOC_TABLE_ALIAS) == -1) {
                  sql.append(JOIN).append(FilterMap.FLUX_REPORT_DOC_TABLE_ALIAS);
              } else if(Filters.FROM_NAME.equals(field) && sql.indexOf(FilterMap.FLUX_PARTY_TABLE_ALIAS) == -1){
                  sql.append(JOIN).append(FilterMap.FLUX_PARTY_TABLE_ALIAS);
              }
          }


            sql.append("where ");

            // Create Where part of SQL Query
            int listSize = searchCriteriaMap.size();
            int i=0;

          for(Filters key:keySet){

              if(Filters.QUNTITY_MIN.equals(key) || mappings.get(key) == null )
                  continue;

              String mapping = mappings.get(key).getCondition();


              if(Filters.QUNTITY_MAX.equals(key)){
                    sql.append(" and ").append(mappings.get(Filters.QUNTITY_MIN).getCondition()).append(" and ").append(mapping);
                    sql.append(" OR (aprod.weightMeasure  BETWEEN :").append(FilterMap.QUNTITY_MIN).append(" and :").append(FilterMap.QUNTITY_MAX+")");
                }else if (i != 0) {
                    sql.append(" and ").append(mapping);
                }
                else {
                    sql.append(mapping);
                }
              i++;
            }

             sql.append(" and fa.status = '"+ FaReportStatusEnum.NEW.getStatus() +"'");


                if (sort != null) {
                    sql.append(" order by " + FilterMap.getFilterSortMappings().get(sort.getField()) + " " + sort.getOrder());
                } else {
                    sql.append(" order by fa.acceptedDatetime ASC ");
                }


        return sql;
    }




}