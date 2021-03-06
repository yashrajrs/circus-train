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
package com.hotels.bdp.circustrain.gcp;

import static com.hotels.bdp.circustrain.gcp.GCPConstants.GCP_KEYFILE_CACHED_LOCATION;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotels.bdp.circustrain.api.CircusTrainException;

class GCPCredentialCopier {
  private static final Logger LOG = LoggerFactory.getLogger(GCPCredentialCopier.class);
  private static final String WORKING_DIRECTORY = System.getProperty("user.dir");
  private static final String RANDOM_STRING = UUID.randomUUID().toString() + System.currentTimeMillis();
  private static final String CACHED_CREDENTIAL_NAME = "/ct-gcp-key-" + RANDOM_STRING + ".json";
  private static final String HDFS_GS_CREDENTIAL_DIRECTORY = "hdfs:///tmp/ct-gcp-" + RANDOM_STRING;
  private static final String HDFS_GS_CREDENTIAL_ABSOLUTE_PATH = HDFS_GS_CREDENTIAL_DIRECTORY + CACHED_CREDENTIAL_NAME;

  private final FileSystem fs;
  private final Configuration conf;
  private final String credentialProvider;

  GCPCredentialCopier(FileSystem fs, Configuration conf, String credentialProvider) {
    this.fs = fs;
    this.conf = conf;
    if (credentialProvider == null) {
      throw new IllegalArgumentException("credentialProvider must be set");
    }
    this.credentialProvider = credentialProvider;
  }

  void copyCredentials() {
    try {
      copyCredentialIntoWorkingDirectory();
      copyCredentialIntoHdfs();
      copyCredentialIntoDistributedCache();
    } catch (IOException | URISyntaxException e) {
      throw new CircusTrainException(e);
    }
  }

  private void copyCredentialIntoWorkingDirectory() throws IOException {
    File source = new File(credentialProvider);
    File destination = new File(WORKING_DIRECTORY + CACHED_CREDENTIAL_NAME);
    destination.deleteOnExit();
    LOG.debug("Copying credential into working directory {}", destination);
    FileUtils.copyFile(source, destination);
  }

  private void copyCredentialIntoHdfs() throws IOException {
    Path source = new Path(credentialProvider);
    Path destination = new Path(HDFS_GS_CREDENTIAL_ABSOLUTE_PATH);
    Path destinationFolder = new Path(HDFS_GS_CREDENTIAL_DIRECTORY);
    fs.deleteOnExit(destinationFolder);
    LOG.debug("Copying credential into HDFS {}", destination);
    fs.copyFromLocalFile(source, destination);
  }

  private void copyCredentialIntoDistributedCache() throws URISyntaxException {
    LOG.debug("{} added to distributed cache with symlink {}", HDFS_GS_CREDENTIAL_DIRECTORY,
        "." + CACHED_CREDENTIAL_NAME);
    DistributedCache.addCacheFile(new URI(HDFS_GS_CREDENTIAL_ABSOLUTE_PATH), conf);
    //The "." must be prepended for the symlink to be created correctly for reference in Map Reduce job
    conf.set(GCP_KEYFILE_CACHED_LOCATION, "." + CACHED_CREDENTIAL_NAME);
  }
}
