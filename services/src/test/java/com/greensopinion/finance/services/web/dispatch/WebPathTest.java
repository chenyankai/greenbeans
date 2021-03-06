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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.greensopinion.finance.services.web.dispatch.MatchResult;
import com.greensopinion.finance.services.web.dispatch.WebPath;

public class WebPathTest {

	@Test
	public void noVariables() {
		WebPath webPath = new WebPath("GET", "/one");
		assertEquals("\\Q/one\\E", webPath.getPattern().toString());
		assertEquals(ImmutableList.of(), webPath.getVariableNames());
	}

	@Test
	public void oneVariable() {
		WebPath webPath = new WebPath("GET", "/one/{two}");
		assertEquals("\\Q/one/\\E([^/]*)", webPath.getPattern().toString());
		assertEquals(ImmutableList.of("two"), webPath.getVariableNames());

		assertMatch(ImmutableMap.of("two", "abc"), "/one/abc", webPath);
		assertMatch(ImmutableMap.of("two", ""), "/one/", webPath);
		assertNoMatch("/one", webPath);
		assertNoMatch("/one/two/three", webPath);
	}

	@Test
	public void oneVariableWithTrailing() {
		WebPath webPath = new WebPath("GET", "/one/{two}/three");
		assertEquals("\\Q/one/\\E([^/]*)\\Q/three\\E", webPath.getPattern().toString());
		assertEquals(ImmutableList.of("two"), webPath.getVariableNames());

		assertMatch(ImmutableMap.of("two", "abc"), "/one/abc/three", webPath);
		assertNoMatch("/one/a/bc/three", webPath);
		assertNoMatch("/one/a/bc", webPath);
		assertNoMatch("/one/abc/", webPath);
	}

	@Test
	public void twoVariables() {
		WebPath webPath = new WebPath("GET", "/one/{two}/something/{three}");
		assertEquals("\\Q/one/\\E([^/]*)\\Q/something/\\E([^/]*)", webPath.getPattern().toString());
		assertEquals(ImmutableList.of("two", "three"), webPath.getVariableNames());

		assertMatch(ImmutableMap.of("two", "abc", "three", "def"), "/one/abc/something/def", webPath);
		assertMatch(ImmutableMap.of("two", "abc", "three", ""), "/one/abc/something/", webPath);
		assertNoMatch("/one/abc/something", webPath);
		assertNoMatch("/one/abc/something/def/", webPath);
	}

	@Test
	public void decodeVariables() {
		WebPath webPath = new WebPath("GET", "/one/{two}/{three}");
		MatchResult match = webPath.match("GET", "/one/a%20b/+c");
		assertEquals("a b", match.getVariables().get("two"));
		assertEquals(" c", match.getVariables().get("three"));
	}

	private void assertNoMatch(String path, WebPath webPath) {
		MatchResult match = webPath.match(webPath.getHttpMethod(), path);
		assertNoMatch(match);
	}

	private void assertNoMatch(MatchResult match) {
		assertNotNull(match);
		assertFalse(match.matches());
		assertEquals(ImmutableMap.of(), match.getVariables());
		assertSame(MatchResult.NO_MATCH, match);
	}

	private void assertMatch(Map<String, String> variables, String path, WebPath webPath) {
		MatchResult match = webPath.match(webPath.getHttpMethod(), path);
		assertNotNull(match);
		assertTrue(match.matches());
		assertEquals(variables, match.getVariables());

		assertNoMatch(webPath.match("NOT" + webPath.getHttpMethod(), path));
	}
}
