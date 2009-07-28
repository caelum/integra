/***
 * 
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.integracao.server;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import br.com.caelum.integracao.server.queue.Job;

public class IntegracaoMatchers {

	public static TypeSafeMatcher<Job> jobFor(final Build build, final BuildCommand command) {
		return new TypeSafeMatcher<Job>() {

			@Override
			protected void describeMismatchSafely(Job item, Description mismatchDescription) {
				mismatchDescription.appendText("job for " + item.getBuild() + " and " + item.getCommand());
			}

			@Override
			protected boolean matchesSafely(Job item) {
				return build.equals(item.getBuild()) && command.equals(item.getCommand());
			}

			public void describeTo(Description description) {
				description.appendText("job for " + build + " and " + command);
			}
			
		};
	}

	public static <T> TypeSafeMatcher<T> naturalEquals(final T original) {
		return new TypeSafeMatcher<T>() {

			@Override
			protected void describeMismatchSafely(T item, Description mismatchDescription) {
				mismatchDescription.appendText("Type " + original);
			}

			@Override
			protected boolean matchesSafely(T item) {
				return item==original;
			}

			public void describeTo(Description description) {
				description.appendText("type " + original);
			}
			
		};
	}

}
