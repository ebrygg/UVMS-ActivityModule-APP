/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.ers.fa.entities;

import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.BaseDAOTest;

import static com.ninja_squad.dbsetup.Operations.*;


public abstract class BaseErsFaDaoTest extends BaseDAOTest{
	
	 protected static final Operation DELETE_ALL = sequenceOf(
			  deleteAllFrom("activity.activity_size_distribution"),
	            deleteAllFrom("activity.activity_fa_catch"),
	            deleteAllFrom("activity.activity_fa_report_document"),
	            deleteAllFrom("activity.activity_flux_report_document"),
	           deleteAllFrom("activity.activity_vessel_transport_means"),
			 deleteAllFrom("activity.activity_fishing_trip"),
			 deleteAllFrom("activity.activity_fishing_trip_Identifier")


	    );


	protected static final Operation INSERT_ERS_FISHING_TRIP_DATA = sequenceOf(
			insertInto("activity.activity_fishing_trip")
					.columns("id", "type_code", "type_code_listid", "fa_catch_id", "fishing_activity_id")
					.values(1, "JFO", "EU_TRIP_ID", 1, 1)
					.build(),
			insertInto("activity.activity_fishing_trip")
					.columns("id", "type_code", "type_code_listid", "fa_catch_id", "fishing_activity_id")
					.values(2, "JFO", "EU_TRIP_ID", 2, 2)
					.build(),
			insertInto("activity.activity_fishing_trip")
					.columns("id", "type_code", "type_code_listid", "fa_catch_id", "fishing_activity_id")
					.values(3, "JFO", "EU_TRIP_ID", 3, 3)
					.build()
	);

	protected static final Operation INSERT_ERS_FISHING_TRIP_IDENTIFIER_DATA = sequenceOf(
			insertInto("activity.activity_fishing_trip_Identifier")
					.columns("id", "fishing_trip_id", "trip_id", "trip_scheme_id")
					.values(1, 1, "NOR-TRP-20160517234053706", "EU_TRIP_ID")
					.build(),
			insertInto("activity.activity_fishing_trip_Identifier")
					.columns("id", "fishing_trip_id", "trip_id", "trip_scheme_id")
					.values(2, 2, "NOR-TRP-20160517234053706", "EU_TRIP_ID")
					.build(),
			insertInto("activity.activity_fishing_trip_Identifier")
					.columns("id", "fishing_trip_id", "trip_id", "trip_scheme_id")
					.values(3, 3, "NOR-TRP-20160517234053706", "EU_TRIP_ID")
					.build()
	);
	    

	  	protected static final Operation INSERT_ERS_FISHING_ACTIVITY_DATA = sequenceOf(
	            insertInto("activity.activity_fishing_activity")
	                    .columns("id", "type_code", "type_code_listid", "occurence", "reason_code", "reason_code_list_id", "vessel_activity_code","vessel_activity_code_list_id",
	                    		"fishery_type_code","fishery_type_code_list_id","species_target_code","species_target_code_list_id","operation_quantity","fishing_duration_measure","source_vessel_char_id","dest_vessel_char_id","flap_document_id","flap_document_scheme_id","fa_report_document_id")
	                 .values(1, "TYPECODE","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),"REASONCODE","REASON_CODE_LIST","VESSEL_ACTIVITY","VESSEL_CODE_LIST",
	                   		"FISHERY_CODE","FISHERY_CODE_LIST","SPECIES_CODE","SPECIES_CODE_LIST",23,11.20,null,null,null,null,1)
	                    .build(),
				insertInto("activity.activity_fishing_activity")
						.columns("id", "type_code", "type_code_listid", "occurence", "reason_code", "reason_code_list_id", "vessel_activity_code","vessel_activity_code_list_id",
								"fishery_type_code","fishery_type_code_list_id","species_target_code","species_target_code_list_id","operation_quantity","fishing_duration_measure","source_vessel_char_id","dest_vessel_char_id","flap_document_id","flap_document_scheme_id","fa_report_document_id")
						.values(2, "TYPECODE","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),"REASONCODE","REASON_CODE_LIST","VESSEL_ACTIVITY","VESSEL_CODE_LIST",
								"FISHERY_CODE","FISHERY_CODE_LIST","SPECIES_CODE","SPECIES_CODE_LIST",23,11.20,null,null,null,null,1)
						.build(),
				insertInto("activity.activity_fishing_activity")
						.columns("id", "type_code", "type_code_listid", "occurence", "reason_code", "reason_code_list_id", "vessel_activity_code","vessel_activity_code_list_id",
								"fishery_type_code","fishery_type_code_list_id","species_target_code","species_target_code_list_id","operation_quantity","fishing_duration_measure","source_vessel_char_id","dest_vessel_char_id","flap_document_id","flap_document_scheme_id","fa_report_document_id")
						.values(3, "TYPECODE","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),"REASONCODE","REASON_CODE_LIST","VESSEL_ACTIVITY","VESSEL_CODE_LIST",
								"FISHERY_CODE","FISHERY_CODE_LIST","SPECIES_CODE","SPECIES_CODE_LIST",23,11.20,null,null,null,null,2)
						.build()
	   );


		protected static final Operation INSERT_ERS_SIZE_DISTRIBUTION_DATA = sequenceOf(
	            insertInto("activity.activity_size_distribution")
	                    .columns("id", "class_code", "class_code_list_id", "category_code", "category_code_list_id")
	                    .values(1, "LSC","FA_SIZE_CLASS","FA_SIZE_CATEGORY","FA_SIZE_CATEGORY")
	                    .build()
	    );
		
		protected static final Operation INSERT_ERS_FA_CATCH_DATA = sequenceOf(
	            insertInto("activity.activity_fa_catch")
	                    .columns("id", "fishing_activity_id", "type_code", "type_code_list_id", "species_code", "species_code_listid", "unit_quantity","weight_measure","weight_measure_unit_code","weight_measure_list_id","usage_code","usage_code_list_id","weighing_means_code","weighing_means_code_list_id","size_distribution_id")
	                    .values(1, 1,"AREA","FLUX_LOCATION_TYPE","speciesCode","CODE_LIST",12,10.00,"kg","KG_LISTID","PROD_USAGE","PROD_USAGE_LISTID","WM_CODE","WEIGHT_MEASURE",1)
	                    .build(),
				insertInto("activity.activity_fa_catch")
						.columns("id", "fishing_activity_id", "type_code", "type_code_list_id", "species_code", "species_code_listid", "unit_quantity","weight_measure","weight_measure_unit_code","weight_measure_list_id","usage_code","usage_code_list_id","weighing_means_code","weighing_means_code_list_id","size_distribution_id")
						.values(2, 1,"AREA","FLUX_LOCATION_TYPE","speciesCode","CODE_LIST",12,10.00,"kg","KG_LISTID","PROD_USAGE","PROD_USAGE_LISTID","WM_CODE","WEIGHT_MEASURE",1)
						.build(),
				insertInto("activity.activity_fa_catch")
						.columns("id", "fishing_activity_id", "type_code", "type_code_list_id", "species_code", "species_code_listid", "unit_quantity","weight_measure","weight_measure_unit_code","weight_measure_list_id","usage_code","usage_code_list_id","weighing_means_code","weighing_means_code_list_id","size_distribution_id")
						.values(3, 1,"AREA","FLUX_LOCATION_TYPE","speciesCode","CODE_LIST",12,10.00,"kg","KG_LISTID","PROD_USAGE","PROD_USAGE_LISTID","WM_CODE","WEIGHT_MEASURE",1)
						.build()
	   );
		

		
		//flux_report_document_id
		protected static final Operation INSERT_ERS_FLUX_REPORT_DOCUMENT_DATA = sequenceOf(
	            insertInto("activity.activity_flux_report_document")
	                    .columns("id", "flux_report_document_id", "reference_id", "creation_datetime","purpose_code","purpose_code_list_id","purpose","owner_flux_party_id","owner_flux_party_name")
	                    .values(1, "ID 1","ID 1",java.sql.Date.valueOf("2014-12-12"),"CODE11","CODELISTID","PURPOSE","OWNER_PARTY_ID","NAME")
	                    .build(),
				insertInto("activity.activity_flux_report_document")
						.columns("id", "flux_report_document_id", "reference_id", "creation_datetime","purpose_code","purpose_code_list_id","purpose","owner_flux_party_id","owner_flux_party_name")
						 .values(2, "FLUX_REPORT_DOCUMENT2", null, "2016-06-27 07:47:31.711","PURPOSE", "PURPOSE_CODE_LIST", null, "OWNER_FLUX_ID_2","flux2")
						.build(),
				insertInto("activity.activity_flux_report_document")
						.columns("id", "flux_report_document_id", "reference_id", "creation_datetime","purpose_code","purpose_code_list_id","purpose","owner_flux_party_id","owner_flux_party_name")
						.values(3, "ID 3","ID 1",java.sql.Date.valueOf("2014-12-12"),"CODE11","CODELISTID","PURPOSE","OWNER_PARTY_ID","NAME")
						.build(),
				insertInto("activity.activity_flux_report_document")
						.columns("id", "flux_report_document_id", "reference_id", "creation_datetime","purpose_code","purpose_code_list_id","purpose","owner_flux_party_id","owner_flux_party_name")
						.values(4, "ID 4", "ID 3", java.sql.Date.valueOf("2015-12-12"),"PURPOSE", "PURPOSE_CODE_LIST", null, "OWNER_FLUX_ID_2","flux2")
						.build()
	  );

		
			
		//vessel_transport_means_id
		protected static final Operation INSERT_ERS_VESSEL_TRANSPORT_MEANS_DATA = sequenceOf(
	            insertInto("activity.activity_vessel_transport_means")
	                    .columns("id", "role_code", "role_code_list_id", "name","registration_event_id","flap_document_id","flap_document_scheme_id")
	                   .values(1, "ROLE_CODE","LIST_ID","name",null,"SCHEMA_ID","NAME")
	                    .build(),
				insertInto("activity.activity_vessel_transport_means")
						.columns("id", "role_code", "role_code_list_id", "name","registration_event_id","flap_document_id","flap_document_scheme_id")
						.values(2, "ROLE_CODE","LIST_ID","name",null,"SCHEMA_ID","NAME")
						.build()
	  );
		
	 

		protected static final Operation INSERT_ERS_FA_REPORT_DOCUMENT_DATA = sequenceOf(
	            insertInto("activity.activity_fa_report_document")
	                    .columns("id", "type_code", "type_code_list_id", "accepted_datetime", "flux_report_document_id","vessel_transport_means_id","fmc_marker","fmc_marker_list_id")
	                    .values(1,"AREA","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),1,1,"fmc","fmc_list")
	                    .build(),
				insertInto("activity.activity_fa_report_document")
						.columns("id", "type_code", "type_code_list_id", "accepted_datetime", "flux_report_document_id","vessel_transport_means_id","fmc_marker","fmc_marker_list_id")
						.values(2,"AREA","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),2,2,"fmc","fmc_list")
						.build(),
				insertInto("activity.activity_fa_report_document")
						.columns("id", "type_code", "type_code_list_id", "accepted_datetime", "flux_report_document_id","vessel_transport_means_id","fmc_marker","fmc_marker_list_id")
						.values(3,"AREA","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),3,1,"fmc","fmc_list")
						.build(),
				insertInto("activity.activity_fa_report_document")
						.columns("id", "type_code", "type_code_list_id", "accepted_datetime", "flux_report_document_id","vessel_transport_means_id","fmc_marker","fmc_marker_list_id")
						.values(4,"AREA","FLUX_LOCATION_TYPE",java.sql.Date.valueOf("2014-12-12"),4,2,"fmc","fmc_list")
						.build()
	  );

		protected static final Operation INSERT_ERS_FA_REPORT_IDENTIFIER_DATA = sequenceOf(
				insertInto("activity.activity_fa_report_identifier")
					.columns("id", "fa_report_document_id", "fa_report_identifier_id", "fa_report_identifier_scheme_id")
					.values(1, 1, "ID 1", "test schema id 1")
					.build()
		);

		 @Override
	    protected String getSchema() {
	        return "activity";
	    }

	    @Override
	    protected String getPersistenceUnitName() {
	        return "testPU";
	    }

}