/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.storage.es.dao;

import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.skywalking.apm.collector.client.elasticsearch.ElasticSearchClient;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.dao.IServiceReferenceMetricPersistenceDAO;
import org.skywalking.apm.collector.storage.es.base.dao.EsDAO;
import org.skywalking.apm.collector.storage.table.serviceref.ServiceReferenceMetric;
import org.skywalking.apm.collector.storage.table.serviceref.ServiceReferenceMetricTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng
 */
public class ServiceReferenceEsMetricPersistenceDAO extends EsDAO implements IServiceReferenceMetricPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder, ServiceReferenceMetric> {

    private final Logger logger = LoggerFactory.getLogger(ServiceReferenceEsMetricPersistenceDAO.class);

    public ServiceReferenceEsMetricPersistenceDAO(ElasticSearchClient client) {
        super(client);
    }

    @Override public ServiceReferenceMetric get(String id) {
        GetResponse getResponse = getClient().prepareGet(ServiceReferenceMetricTable.TABLE, id).get();
        if (getResponse.isExists()) {
            ServiceReferenceMetric serviceReferenceMetric = new ServiceReferenceMetric(id);
            Map<String, Object> source = getResponse.getSource();
            serviceReferenceMetric.setEntryServiceId(((Number)source.get(ServiceReferenceMetricTable.COLUMN_ENTRY_SERVICE_ID)).intValue());
            serviceReferenceMetric.setFrontServiceId(((Number)source.get(ServiceReferenceMetricTable.COLUMN_FRONT_SERVICE_ID)).intValue());
            serviceReferenceMetric.setBehindServiceId(((Number)source.get(ServiceReferenceMetricTable.COLUMN_BEHIND_SERVICE_ID)).intValue());
            serviceReferenceMetric.setS1Lte(((Number)source.get(ServiceReferenceMetricTable.COLUMN_S1_LTE)).longValue());
            serviceReferenceMetric.setS3Lte(((Number)source.get(ServiceReferenceMetricTable.COLUMN_S3_LTE)).longValue());
            serviceReferenceMetric.setS5Lte(((Number)source.get(ServiceReferenceMetricTable.COLUMN_S5_LTE)).longValue());
            serviceReferenceMetric.setS5Gt(((Number)source.get(ServiceReferenceMetricTable.COLUMN_S5_GT)).longValue());
            serviceReferenceMetric.setSummary(((Number)source.get(ServiceReferenceMetricTable.COLUMN_SUMMARY)).longValue());
            serviceReferenceMetric.setError(((Number)source.get(ServiceReferenceMetricTable.COLUMN_ERROR)).longValue());
            serviceReferenceMetric.setCostSummary(((Number)source.get(ServiceReferenceMetricTable.COLUMN_COST_SUMMARY)).longValue());
            serviceReferenceMetric.setTimeBucket(((Number)source.get(ServiceReferenceMetricTable.COLUMN_TIME_BUCKET)).longValue());
            return serviceReferenceMetric;
        } else {
            return null;
        }
    }

    @Override public IndexRequestBuilder prepareBatchInsert(ServiceReferenceMetric data) {
        Map<String, Object> source = new HashMap<>();
        source.put(ServiceReferenceMetricTable.COLUMN_ENTRY_SERVICE_ID, data.getEntryServiceId());
        source.put(ServiceReferenceMetricTable.COLUMN_FRONT_SERVICE_ID, data.getFrontServiceId());
        source.put(ServiceReferenceMetricTable.COLUMN_BEHIND_SERVICE_ID, data.getBehindServiceId());
        source.put(ServiceReferenceMetricTable.COLUMN_S1_LTE, data.getS1Lte());
        source.put(ServiceReferenceMetricTable.COLUMN_S3_LTE, data.getS3Lte());
        source.put(ServiceReferenceMetricTable.COLUMN_S5_LTE, data.getS5Lte());
        source.put(ServiceReferenceMetricTable.COLUMN_S5_GT, data.getS5Gt());
        source.put(ServiceReferenceMetricTable.COLUMN_SUMMARY, data.getSummary());
        source.put(ServiceReferenceMetricTable.COLUMN_ERROR, data.getError());
        source.put(ServiceReferenceMetricTable.COLUMN_COST_SUMMARY, data.getCostSummary());
        source.put(ServiceReferenceMetricTable.COLUMN_TIME_BUCKET, data.getTimeBucket());

        return getClient().prepareIndex(ServiceReferenceMetricTable.TABLE, data.getId()).setSource(source);
    }

    @Override public UpdateRequestBuilder prepareBatchUpdate(ServiceReferenceMetric data) {
        Map<String, Object> source = new HashMap<>();
        source.put(ServiceReferenceMetricTable.COLUMN_ENTRY_SERVICE_ID, data.getEntryServiceId());
        source.put(ServiceReferenceMetricTable.COLUMN_FRONT_SERVICE_ID, data.getFrontServiceId());
        source.put(ServiceReferenceMetricTable.COLUMN_BEHIND_SERVICE_ID, data.getBehindServiceId());
        source.put(ServiceReferenceMetricTable.COLUMN_S1_LTE, data.getS1Lte());
        source.put(ServiceReferenceMetricTable.COLUMN_S3_LTE, data.getS3Lte());
        source.put(ServiceReferenceMetricTable.COLUMN_S5_LTE, data.getS5Lte());
        source.put(ServiceReferenceMetricTable.COLUMN_S5_GT, data.getS5Gt());
        source.put(ServiceReferenceMetricTable.COLUMN_SUMMARY, data.getSummary());
        source.put(ServiceReferenceMetricTable.COLUMN_ERROR, data.getError());
        source.put(ServiceReferenceMetricTable.COLUMN_COST_SUMMARY, data.getCostSummary());
        source.put(ServiceReferenceMetricTable.COLUMN_TIME_BUCKET, data.getTimeBucket());

        return getClient().prepareUpdate(ServiceReferenceMetricTable.TABLE, data.getId()).setDoc(source);
    }

    @Override public void deleteHistory(Long startTimestamp, Long endTimestamp) {
        long startTimeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(startTimestamp);
        long endTimeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(endTimestamp);
        BulkByScrollResponse response = getClient().prepareDelete()
            .filter(QueryBuilders.rangeQuery(ServiceReferenceMetricTable.COLUMN_TIME_BUCKET).gte(startTimeBucket).lte(endTimeBucket))
            .source(ServiceReferenceMetricTable.TABLE)
            .get();

        long deleted = response.getDeleted();
        logger.info("Delete {} rows history from {} index.", deleted, ServiceReferenceMetricTable.TABLE);
    }
}