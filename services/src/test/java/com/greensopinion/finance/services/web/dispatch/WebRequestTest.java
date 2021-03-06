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
package com.greensopinion.finance.services.web.dispatch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.greensopinion.finance.services.web.dispatch.WebRequest;

public class WebRequestTest {

	@Test
	public void create() {
		WebRequest webRequest = new WebRequest("GET", "/yo", "123");
		assertEquals("GET", webRequest.getHttpMethod());
		assertEquals("/yo", webRequest.getPath());
		assertEquals("123", webRequest.getEntity());
		assertEquals("WebRequest{httpMethod=GET, path=/yo}", webRequest.toString());
	}
}
