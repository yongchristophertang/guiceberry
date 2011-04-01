/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.inject.testing.guiceberry.junit3;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.testing.guiceberry.TestScoped;

import junit.framework.TestCase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The JUnit-specific implementation of the {@link TestScoped} annotation. 
 * 
 * @see GuiceBerryJunit3
 * @see Scope 
 *
 * @author Luiz-Otavio Zorzella
 * @author Danka Karwanska
 */
@Singleton
class JunitTestScope implements Scope {

  private final ConcurrentMap<TestCase, Map<Key<?>, Object>> testMap =
      new ConcurrentHashMap<TestCase, Map <Key<?>, Object>>();

  JunitTestScope() {}

  void finishScope(TestCase testCase) {
    testMap.remove(testCase); 
  }
  
  @SuppressWarnings("unchecked")  
  public synchronized <T> Provider<T> scope(final Key<T> key, 
      final Provider<T> creator) {

    return new Provider<T>() {
      public T get() {

        TestCase actualTestCase = GuiceBerryJunit3.getActualTestCase();
        if (actualTestCase == null) {
          throw new IllegalStateException(
              "GuiceBerry can't find out what is the currently-running test. " +
              "There are a few reasons why this can happen, but a likely one " +
              "is that a GuiceBerry Injector is being asked to instantiate a " +
              "class in a thread not created by your test case.");
        }
        Map<Key<?>, Object> keyToInstanceProvider = testMap.get(actualTestCase);
        if (keyToInstanceProvider == null) {
          testMap.putIfAbsent(
              actualTestCase, new ConcurrentHashMap<Key<?>, Object>());
          keyToInstanceProvider = testMap.get(actualTestCase);
        }
        Object o = keyToInstanceProvider.get(key);
        if (o != null) {
          return (T) o;
        }
        // double checked locking -- handle with extreme care!
        synchronized(keyToInstanceProvider) {
          o = keyToInstanceProvider.get(key);
          if (o == null) {
            o = creator.get();
            keyToInstanceProvider.put(key, o);
          }
          return (T) o;
        }
      }
    };
  }
}