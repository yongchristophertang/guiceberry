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

package com.google.guiceberry;

import com.google.common.testing.TearDown;
import com.google.common.testing.TearDownAccepter;
import com.google.common.testing.TearDownStack;
import com.google.guiceberry.GuiceBerryUniverse;
import com.google.guiceberry.TestScope;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * This Module provides the basic bindings required by GuiceBerry, namely
 * {@link TestId}, {@link TearDownAccepter} and the {@link TestScoped} scope.
 * Without these bindinds, GuiceBerry will fail to setup.

 * <p>Therefore, this module is required to be installed by all
 * GuiceBerry Envs.
 *
 * @author Luiz-Otavio Zorzella
 */
public class GuiceBerryModule extends AbstractModule {
    
  protected final GuiceBerryUniverse universe;
  protected final TestScope testScope;
  
  public GuiceBerryModule() {
    this(GuiceBerryUniverse.INSTANCE);
  }
  
  protected GuiceBerryModule(GuiceBerryUniverse universe) {
    this.universe = universe;
    this.testScope = new TestScope(universe);
  }
  
  @Override
  protected void configure() {
    bind(TestScope.class).toInstance(testScope);
    bindScope(TestScoped.class, testScope);
    bind(TearDownAccepter.class).to(ToTearDown.class);
  }

  @Provides
  @TestScoped
  ToTearDown getToTearDown() {
    return new ToTearDown() {
      TearDownStack delegate = new TearDownStack();
      
      public void addTearDown(TearDown tearDown) {
        delegate.addTearDown(tearDown);
      }
    
      public void runTearDown() {
        delegate.runTearDown();
      }
    };
  }
  
  @Provides
  @TestScoped
  TestId getTestId() {
    return universe.currentTestDescriptionThreadLocal.get().getTestId();
  }
  
  public interface ToTearDown extends TearDownAccepter {
    void runTearDown();
  }
}
