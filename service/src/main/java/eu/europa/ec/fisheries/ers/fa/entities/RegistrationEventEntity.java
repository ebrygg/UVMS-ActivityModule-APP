/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.ers.fa.entities;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "activity_registration_event")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"vesselTransportMeanses", "registrationLocation"})
@ToString(exclude = {"vesselTransportMeanses", "registrationLocation"})
public class RegistrationEventEntity implements Serializable {

	@Id
	@Column(unique = true, nullable = false)
	@SequenceGenerator(name = "SEQ_GEN", sequenceName = "reg_event_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GEN")
	private int id;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "registration_location_id", nullable = false)
	private RegistrationLocationEntity registrationLocation;

	@Column(columnDefinition = "text")
	private String description;

	@Column(name = "desc_language_id")
	private String descLanguageId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "occurrence_datetime", length = 29)
	private Date occurrenceDatetime;

	@OneToOne(mappedBy = "registrationEvent")
	private VesselTransportMeansEntity vesselTransportMeanses;

}