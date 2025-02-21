/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.segment.local.realtime.impl.invertedindex;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.SearcherManager;
import org.roaringbitmap.buffer.ImmutableRoaringBitmap;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


public class LuceneMutableTextIndexTest {
  private static final File INDEX_DIR = new File(FileUtils.getTempDirectory(), "LuceneMutableIndexTest");
  private static final String TEXT_COLUMN_NAME = "testColumnName";

  private RealtimeLuceneTextIndex _realtimeLuceneTextIndex;

  private String[][] getTextData() {
    return new String[][]{
        {"realtime stream processing"}, {"publish subscribe", "columnar processing for data warehouses", "concurrency"}
    };
  }

  @BeforeClass
  public void setUp()
      throws Exception {
    _realtimeLuceneTextIndex = new RealtimeLuceneTextIndex(TEXT_COLUMN_NAME, INDEX_DIR, "fooBar", null, null);
    String[][] documents = getTextData();
    for (String[] row : documents) {
      _realtimeLuceneTextIndex.add(row);
    }

    SearcherManager searcherManager = _realtimeLuceneTextIndex.getSearcherManager();
    try {
      searcherManager.maybeRefresh();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @AfterClass
  public void tearDown() {
    _realtimeLuceneTextIndex.close();
  }

  @Test
  public void testQueries() {
    assertEquals(_realtimeLuceneTextIndex.getDocIds("stream"), ImmutableRoaringBitmap.bitmapOf(0));
    assertEquals(_realtimeLuceneTextIndex.getDocIds("/.*house.*/"), ImmutableRoaringBitmap.bitmapOf(1));
    assertEquals(_realtimeLuceneTextIndex.getDocIds("invalid"), ImmutableRoaringBitmap.bitmapOf());
  }
}
