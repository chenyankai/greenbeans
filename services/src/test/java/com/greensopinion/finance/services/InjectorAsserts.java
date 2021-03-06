/*******************************************************************************
 * Copyright (c) 2015, 2016 David Green.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.greensopinion.finance.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

public class InjectorAsserts {

	public static <T> T assertSingletonBinding(Injector injector, Class<T> clazz) {
		Key<T> key = Key.get(clazz);
		Binding<T> binding = injector.getExistingBinding(key);
		assertNotNull(binding);
		T instance = injector.getInstance(key);
		assertSame(instance, injector.getInstance(key));
		return instance;
	}

	public static <T> T assertSingletonBinding(Injector injector, Class<T> binding, Class<? extends T> implementation) {
		T actual = assertSingletonBinding(injector, binding);
		assertTrue(implementation.isAssignableFrom(actual.getClass()));
		return actual;
	}

	private InjectorAsserts() {
		// prevent instantiation
	}
}
