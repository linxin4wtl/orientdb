/*
 *
 *  *  Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://orientdb.com
 *
 */

package com.orientechnologies.orient.core.index.engine;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndexKeyUpdater;

/**
 * @author Andrey Lomakin (a.lomakin-at-orientdb.com)
 * @since 6/29/13
 */
public interface OIndexEngine extends OBaseIndexEngine {
  int VERSION = 0;

  boolean remove(Object key);

  Object get(Object key);

  void put(Object key, Object value);

  void update(Object key, OIndexKeyUpdater<Object> updater);

  /**
   * Puts the given value under the given key into this index engine. Validates the operation using the provided validator.
   *
   * @param key       the key to put the value under.
   * @param value     the value to put.
   * @param validator the operation validator.
   *
   * @return {@code true} if the validator allowed the put, {@code false} otherwise.
   *
   * @see Validator#validate(Object, Object, Object)
   */
  boolean validatedPut(Object key, OIdentifiable value, Validator<Object, OIdentifiable> validator);

  @Override
  default int getEngineVersion() {
    return VERSION;
  }
}
