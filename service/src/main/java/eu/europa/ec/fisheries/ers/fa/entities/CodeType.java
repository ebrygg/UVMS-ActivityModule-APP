/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.ers.fa.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import java.io.Serializable;

import lombok.Data;

@Embeddable
@Data
public class CodeType implements Serializable {

    private String value;

    @Column(name = "list_id")
    private String listID;

    @Column(name = "list_agency_id")
    private String listAgencyID;

    @Column(name = "list_agency_name")
    private String listAgencyName;

    @Column(name = "list_name")
    private String listName;

    @Column(name = "list_version_id")
    private String listVersionID;

    @Column(name = "name")
    private String name;

    @Column(name = "language_id")
    private String languageID;

    @Column(name = "list_uri")
    private String listURI;

    @Column(name = "list_scheme_id")
    private String listSchemeURI;

}
