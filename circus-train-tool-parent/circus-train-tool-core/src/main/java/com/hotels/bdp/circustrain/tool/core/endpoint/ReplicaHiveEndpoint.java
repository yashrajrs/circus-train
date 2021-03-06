/**
 * Copyright (C) 2016-2017 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.bdp.circustrain.tool.core.endpoint;

import org.apache.hadoop.hive.conf.HiveConf;

import com.google.common.base.Supplier;

import com.hotels.bdp.circustrain.api.metastore.CloseableMetaStoreClient;
import com.hotels.bdp.circustrain.core.HiveEndpoint;
import com.hotels.bdp.circustrain.core.TableAndStatistics;
import com.hotels.bdp.circustrain.core.conf.TableReplication;

public class ReplicaHiveEndpoint extends HiveEndpoint {

  public ReplicaHiveEndpoint(
      String name,
      HiveConf hiveConf,
      Supplier<CloseableMetaStoreClient> metaStoreClientSupplier) {
    super(name, hiveConf, metaStoreClientSupplier);
  }

  @Override
  public TableAndStatistics getTableAndStatistics(TableReplication tableReplication) {
    return super.getTableAndStatistics(tableReplication.getReplicaDatabaseName(),
        tableReplication.getReplicaTableName());
  }
}
