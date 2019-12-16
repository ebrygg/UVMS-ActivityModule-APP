/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it 
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of 
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.activity.fa.entities;

import eu.europa.ec.fisheries.uvms.activity.fa.dao.FaReportDocumentDao;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mapstruct.ap.internal.util.Collections;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Ignore
public class FaReportDocumentDaoTest extends BaseErsFaDaoTest {

    private FaReportDocumentDao dao = new FaReportDocumentDao(em);

    @Before
    public void prepare() {
        super.prepare();
    }

    @Test
    public void loadReports_getAll() {
        // Given

        // When
        List<FaReportDocumentEntity> result = dao.loadReports(null, null, null, null);

        // Then
        assertReportsHaveIds(result, 1, 2, 3, 6);
    }

    @Test
    public void loadReports_after20160701() {
        // Given
        Instant startDate = Instant.parse("2016-07-01T00:00:01Z");

        // When
        List<FaReportDocumentEntity> result = dao.loadReports(null, null, startDate, null);

        // Then
        assertReportsHaveIds(result, 6);
    }

    @Test
    public void loadReports_before20160630() {
        // Given
        Instant endDate = Instant.parse("2016-06-30T23:59:59Z");

        // When
        List<FaReportDocumentEntity> result = dao.loadReports(null, null, null, endDate);

        // Then
        assertReportsHaveIds(result, 1, 2, 3);
    }

    @Test
    public void loadReports_narrowTimeRange() {
        // Given
        Instant startDate = Instant.parse("2016-06-27T05:59:00Z");
        Instant endDate =   Instant.parse("2016-06-27T06:01:00Z");

        // When
        List<FaReportDocumentEntity> result = dao.loadReports(null, null, startDate, endDate);

        // Then
        assertReportsHaveIds(result, 2);
    }

    private void assertReportsHaveIds(Collection<FaReportDocumentEntity> entities, Integer ... ids) {
        Set<Integer> idSet = Collections.asSet(ids);

        for (FaReportDocumentEntity entity : entities) {
            Integer id = entity.getId();
            assertTrue("Report with ID " + id + " not expected", idSet.remove(id));
        }

        assertTrue("Not all IDs found among reports", idSet.isEmpty());
    }
}
